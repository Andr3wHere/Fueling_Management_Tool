package cz.ddt.fuelingmanagementtool.feature.camera

import kotlin.math.abs

data class OcrFuelResult(
    val liters: Double? = null,
    val totalPrice: Double? = null,
    val pricePerLiter: Double? = null
)

class PumpOcrParser {

    companion object {
        private val NOISE_PATTERNS = listOf(
            Regex("""\b(TATSUNO|TOKHEIM|WAYNE|GILBARCO|SCHEIDT|BACHMANN|ADAST|HEFA)\b""", RegexOption.IGNORE_CASE),
            Regex("""\bMMQ\b.*""", RegexOption.IGNORE_CASE),
            Regex("""\bMIN\.?\s*ODB[EĚ]R\b.*""", RegexOption.IGNORE_CASE)
        )

        private val CURRENCY_LINE_REGEX = Regex(
            """^(\s*[\dIl]+(?:[.,\s]+[\dIl]+)*\s*)((?:[Kk][čcČC](?:\s*(?:[/|\\1Il]\s*[lL]|[/|\\]\s*[1Il]|[lL]))?|[Cc][Zz][Kk])\s*)$"""
        )

        private val PRICE_PER_LITER_REGEX = Regex(
            """^\s*(\d+(?:[.,\s]+\d+)*)\s*(?:K[čcČC]\s*(?:[/|\\1Il]\s*[lL]|[/|\\]\s*[1Il]|[lL]))\s*$""",
            RegexOption.IGNORE_CASE
        )

        private val TOTAL_PRICE_REGEX = Regex(
            """^\s*(\d+(?:[.,\s]+\d+)*)\s*(?:K[čcČC]|CZK)\s*$""",
            RegexOption.IGNORE_CASE
        )

        private val LITERS_REGEX = Regex(
            """^\s*(\d+(?:[.,\s]+\d+)*)\s*[lL]\s*$""",
            RegexOption.IGNORE_CASE
        )
    }

    fun parse(rawText: String): OcrFuelResult {
        if (rawText.isBlank()) return OcrFuelResult()

        val cleanedLines = filterNoiseLines(rawText.lines())

        var rawTotalCandidates = mutableListOf<String>()
        var rawLitersCandidates = mutableListOf<String>()
        var rawUnitPriceCandidates = mutableListOf<String>()

        for (line in cleanedLines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            val normalizedLine = normalizePriceOcrTypos(trimmed)

            PRICE_PER_LITER_REGEX.matchEntire(normalizedLine)?.let {
                rawUnitPriceCandidates.add(it.groupValues[1])
                continue
            }

            TOTAL_PRICE_REGEX.matchEntire(normalizedLine)?.let {
                rawTotalCandidates.add(it.groupValues[1])
                continue
            }

            LITERS_REGEX.matchEntire(normalizedLine)?.let {
                rawLitersCandidates.add(it.groupValues[1])
                continue
            }
        }

        if (rawLitersCandidates.size > 1) {
            rawLitersCandidates = rawLitersCandidates.filterNot { it.trim() == "5" }.toMutableList()
        }

        val rawTotal = rawTotalCandidates.firstOrNull()
        val rawLiters = rawLitersCandidates.firstOrNull()
        val rawUnitPrice = rawUnitPriceCandidates.firstOrNull()

        val totalOptions = rawTotal?.let { generateTotalPriceCandidates(it) } ?: emptyList()
        val litersOptions = rawLiters?.let { generateLitersCandidates(it) } ?: emptyList()
        val unitPriceOptions = rawUnitPrice?.let { generateUnitPriceCandidates(it) } ?: emptyList()

        return resolveBestCombination(
            litersOptions = litersOptions,
            totalOptions = totalOptions,
            unitPriceOptions = unitPriceOptions
        )
    }

    private fun normalizePriceOcrTypos(line: String): String {
        val match = CURRENCY_LINE_REGEX.matchEntire(line) ?: return line
        val normalizedNumber = match.groupValues[1]
            .replace('I', '1')
            .replace('l', '1')
        return "$normalizedNumber${match.groupValues[2]}"
    }

    private fun filterNoiseLines(lines: List<String>): List<String> {
        val result = mutableListOf<String>()
        var skipNext5L = false

        for (rawLine in lines) {
            var line = rawLine.trim()
            if (line.isEmpty()) continue

            if (skipNext5L && line.matches(Regex("""^[25]\s*[lL]$""", RegexOption.IGNORE_CASE))) {
                skipNext5L = false
                continue
            }
            skipNext5L = false

            if (line.contains("MMQ", ignoreCase = true)) {
                skipNext5L = true
                continue
            }

            for (noise in NOISE_PATTERNS) {
                line = line.replace(noise, "").trim()
            }

            if (line.isNotEmpty()) {
                result.add(line)
            }
        }
        return result
    }

    private fun generateUnitPriceCandidates(raw: String): List<Double> {
        val explicit = parseExplicitDecimal(raw)
        if (raw.contains('.') || raw.contains(',')) {
            return listOfNotNull(explicit?.takeIf { it in 15.0..99.9 })
        }
        if (explicit != null && explicit in 15.0..99.9) {
            return listOf(explicit)
        }

        val digits = raw.filter { it.isDigit() }
        val candidates = linkedSetOf<Double>()

        if (digits.length == 4) {
            candidates.add(digits.toDouble() / 100.0)
        }
        if (digits.length == 3) {
            candidates.add(digits.toDouble() / 10.0)
        }
        candidates.addAll(allDecimalPlacements(digits).filter { it in 15.0..99.9 })

        return candidates.toList()
    }

    private fun generateLitersCandidates(raw: String): List<Double> {
        val explicit = parseExplicitDecimal(raw)
        if (explicit != null && explicit in 0.5..500.0) {
            return listOf(explicit)
        }

        val digits = raw.filter { it.isDigit() }
        if (digits.isEmpty()) return emptyList()

        val candidates = linkedSetOf<Double>()
        if (digits.length >= 3) {
            candidates.add(digits.toDouble() / 100.0)
        }
        if (digits.length >= 2) {
            candidates.add(digits.toDouble() / 10.0)
        }
        candidates.add(digits.toDouble())

        return candidates.filter { it in 0.5..500.0 }
    }

    private fun generateTotalPriceCandidates(raw: String): List<Double> {
        val explicit = parseExplicitDecimal(raw)
        if (explicit != null && explicit in 10.0..50000.0) {
            return listOf(explicit)
        }

        val trimmed = raw.trim()
        val digits = trimmed.filter { it.isDigit() }
        if (digits.isEmpty()) return emptyList()

        val isThousandsGrouped = !trimmed.contains('.') && !trimmed.contains(',') &&
            trimmed.any { it.isWhitespace() } &&
            trimmed.split(Regex("""\s+""")).last().length == 3

        val candidates = linkedSetOf<Double>()
        if (isThousandsGrouped) {
            candidates.add(digits.toDouble())
        }
        if (digits.length >= 2) candidates.add(digits.toDouble() / 10.0)
        if (digits.length >= 3) candidates.add(digits.toDouble() / 100.0)
        candidates.add(digits.toDouble())

        return candidates.filter { it in 10.0..50000.0 }
    }

    private fun parseExplicitDecimal(raw: String): Double? {
        val trimmed = raw.trim()
        if (trimmed.contains('.') || trimmed.contains(',')) {
            val compact = trimmed
                .replace(Regex("""\s+"""), "")
                .replace(',', '.')
            return compact.toDoubleOrNull()
        }
        if (trimmed.any { it.isWhitespace() }) {
            val parts = trimmed.split(Regex("""\s+"""))
            if (parts.size >= 2 && parts.last().length in 1..2) {
                val integerPart = parts.dropLast(1).joinToString("")
                val fractionalPart = parts.last()
                return "$integerPart.$fractionalPart".toDoubleOrNull()
            }
        }
        return null
    }

    private fun allDecimalPlacements(digits: String): List<Double> {
        if (digits.isEmpty()) return emptyList()
        val list = mutableListOf<Double>()
        list.add(digits.toDouble())
        for (i in 1 until digits.length) {
            val withDot = digits.substring(0, i) + "." + digits.substring(i)
            withDot.toDoubleOrNull()?.let { list.add(it) }
        }
        return list
    }

    private fun resolveBestCombination(
        litersOptions: List<Double>,
        totalOptions: List<Double>,
        unitPriceOptions: List<Double>
    ): OcrFuelResult {
        if (litersOptions.isNotEmpty() && totalOptions.isNotEmpty() && unitPriceOptions.isNotEmpty()) {
            var bestLiters: Double? = null
            var bestTotal: Double? = null
            var bestUnit: Double? = null
            var minRelativeError = Double.MAX_VALUE

            for (l in litersOptions) {
                for (u in unitPriceOptions) {
                    val expectedTotal = l * u
                    for (t in totalOptions) {
                        val absError = abs(expectedTotal - t)
                        val relError = absError / maxOf(t, 1.0)

                        if (relError < minRelativeError && relError <= 0.05) {
                            minRelativeError = relError
                            bestLiters = l
                            bestTotal = t
                            bestUnit = u
                        }
                    }
                }
            }

            if (bestLiters != null && bestTotal != null && bestUnit != null) {
                return OcrFuelResult(
                    liters = bestLiters,
                    totalPrice = bestTotal,
                    pricePerLiter = bestUnit
                )
            }
        }

        if (litersOptions.isNotEmpty() && unitPriceOptions.isNotEmpty() && totalOptions.isEmpty()) {
            val l = litersOptions.first()
            val u = unitPriceOptions.first()
            return OcrFuelResult(liters = l, totalPrice = roundToTwoDecimals(l * u), pricePerLiter = u)
        }

        if (totalOptions.isNotEmpty() && unitPriceOptions.isNotEmpty() && litersOptions.isEmpty()) {
            val t = totalOptions.first()
            val u = unitPriceOptions.first()
            return OcrFuelResult(liters = roundToTwoDecimals(t / u), totalPrice = t, pricePerLiter = u)
        }

        if (litersOptions.isNotEmpty() && totalOptions.isNotEmpty() && unitPriceOptions.isEmpty()) {
            for (l in litersOptions) {
                for (t in totalOptions) {
                    val computedUnit = roundToTwoDecimals(t / l)
                    if (computedUnit in 15.0..99.9) {
                        return OcrFuelResult(
                            liters = l,
                            totalPrice = t,
                            pricePerLiter = computedUnit
                        )
                    }
                }
            }
        }

        return OcrFuelResult(
            liters = litersOptions.firstOrNull(),
            totalPrice = totalOptions.firstOrNull(),
            pricePerLiter = unitPriceOptions.firstOrNull()
        )
    }

    private fun roundToTwoDecimals(value: Double): Double =
        kotlin.math.round(value * 100.0) / 100.0
}
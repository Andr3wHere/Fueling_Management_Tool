package cz.ddt.fuelingmanagementtool.feature.fuel_record

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.util.TimeZone

class FuelRecordDateUtilsTest {

    @Test
    fun formatFuelRecordDate_formatsWithLeadingZeros() {
        val date = LocalDate.of(2026, 3, 5)
        assertEquals("05.03.2026", formatFuelRecordDate(date))
    }

    @Test
    fun parseInitialFuelRecordDate_parsesCzechAndIsoFormatsAndFallsBack() {
        val fallback = LocalDate.of(2026, 11, 27)

        assertEquals(
            LocalDate.of(2026, 10, 18),
            parseInitialFuelRecordDate("18.10.2026", fallback)
        )
        assertEquals(
            LocalDate.of(2028, 2, 29),
            parseInitialFuelRecordDate("29.02.2028", fallback)
        )
        assertEquals(
            LocalDate.of(2026, 10, 18),
            parseInitialFuelRecordDate("2026-10-18", fallback)
        )
        assertEquals(
            fallback,
            parseInitialFuelRecordDate("   ", fallback)
        )
        assertEquals(
            fallback,
            parseInitialFuelRecordDate("31.02.2026", fallback)
        )
        assertEquals(
            fallback,
            parseInitialFuelRecordDate("not-a-date", fallback)
        )
    }

    @Test
    fun utcMillisRoundTrip_preservesDateAcrossTimeZonesAndDst() {
        val testDates = listOf(
            LocalDate.of(2026, 3, 29),
            LocalDate.of(2026, 10, 25),
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 12, 31)
        )
        val timeZones = listOf(
            "Europe/Prague",
            "America/Los_Angeles",
            "Pacific/Auckland"
        )

        val originalZone = TimeZone.getDefault()
        try {
            for (zoneId in timeZones) {
                TimeZone.setDefault(TimeZone.getTimeZone(zoneId))
                for (date in testDates) {
                    val millis = date.toDatePickerUtcMillis()
                    assertEquals(
                        "Round-trip failed for $date in $zoneId",
                        date,
                        utcMillisToLocalDate(millis)
                    )
                }
            }
        } finally {
            TimeZone.setDefault(originalZone)
        }
    }
}


package cz.ddt.fuelingmanagementtool.feature.fuel_record

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cz.ddt.fuelingmanagementtool.ui.theme.FuelingManagementToolTheme
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

private fun parseDecimal(value: String): BigDecimal? =
    try {
        BigDecimal(
            value.trim()
                .replace(" ", "")
                .replace("\u00A0", "")
                .replace(',', '.')
        )
    } catch (_: NumberFormatException) {
        null
    }

private fun formatDecimal(value: BigDecimal): String =
    value.setScale(2, RoundingMode.HALF_UP)
        .toPlainString()
        .replace('.', ',')

@Composable
fun EditFuelRecordScreen(
    onSave: (String, String, String, String) -> Unit =
        { _, _, _, _ -> },
    onBack: () -> Unit = {},
    onRetakePhoto: () -> Unit = {},
    onDiscard: () -> Unit = onBack,
    vehicleName: String = "Volkswagen Tayron",
    initialDate: String = LocalDate.now().format(
        DateTimeFormatter.ofPattern("dd.MM.uuuu")
    ),
    initialLiters: String = "",
    initialPrice: String = "",
    initialUnitPrice: String = "",
    recordTime: String = "16:01",
    gpsLocation: String = "ORLEN Benzina",
    isSaving: Boolean = false,
    saveError: String? = null,
    ocrWarning: String? = null,
    isDeleting: Boolean = false,
    onDelete: (() -> Unit)? = null
) {
    val isBusy = isSaving || isDeleting

    var date by remember(initialDate) {
        mutableStateOf(initialDate)
    }

    var liters by remember(initialLiters) {
        mutableStateOf(initialLiters)
    }

    var price by remember(initialPrice) {
        mutableStateOf(initialPrice)
    }

    var unitPrice by remember(
        initialUnitPrice, initialPrice, initialLiters
    ) {
        mutableStateOf(
            if (initialUnitPrice.isNotBlank()) {
                initialUnitPrice
            } else {
                val total = parseDecimal(initialPrice)
                val amount = parseDecimal(initialLiters)
                if (
                    total != null && amount != null &&
                    total > BigDecimal.ZERO &&
                    amount > BigDecimal.ZERO
                ) {
                    formatDecimal(
                        total.divide(amount, 2, RoundingMode.HALF_UP)
                    )
                } else {
                    ""
                }
            }
        )
    }

    var fullTank by remember {
        mutableStateOf(true)
    }

    var submitted by remember {
        mutableStateOf(false)
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    val formatter = remember {
        DateTimeFormatter.ofPattern("dd.MM.uuuu")
            .withResolverStyle(ResolverStyle.STRICT)
    }

    val validDate = date.matches(
        Regex("""\d{2}\.\d{2}\.\d{4}""")
    ) && runCatching {
        LocalDate.parse(date, formatter)
    }.isSuccess

    val validLiters =
        parseDecimal(liters)?.let {
            it > BigDecimal.ZERO
        } == true

    val validPrice =
        parseDecimal(price)?.let {
            it > BigDecimal.ZERO
        } == true

    val validUnitPrice =
        parseDecimal(unitPrice)?.let {
            it > BigDecimal.ZERO
        } == true

    fun updateUnitPrice() {
        val total = parseDecimal(price)
        val amount = parseDecimal(liters)

        unitPrice = if (
            total != null && amount != null &&
            total > BigDecimal.ZERO &&
            amount > BigDecimal.ZERO
        ) {
            formatDecimal(
                total.divide(amount, 2, RoundingMode.HALF_UP)
            )
        } else {
            ""
        }
    }

    fun updateTotalPrice(unit: BigDecimal?) {
        val amount = parseDecimal(liters)
        price = if (
            amount != null && unit != null &&
            amount > BigDecimal.ZERO && unit > BigDecimal.ZERO
        ) {
            formatDecimal(amount.multiply(unit))
        } else {
            ""
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 16.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Kontrola záznamu",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = vehicleName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (ocrWarning != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "⚠ $ocrWarning",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Karta 1: Údaje o tankování
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        "Množství paliva",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = liters,
                        onValueChange = {
                            liters = it
                            val unit = parseDecimal(unitPrice)
                            if (unit != null && unit > BigDecimal.ZERO) {
                                updateTotalPrice(unit)
                            } else {
                                updateUnitPrice()
                            }
                        },
                        placeholder = {
                            Text("Např. 42,5")
                        },
                        suffix = { Text("l") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        singleLine = true,
                        enabled = !isBusy,
                        isError = submitted && !validLiters,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (submitted && !validLiters) {
                        Text(
                            text = "Pole obsahuje povinný údaj",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Text(
                        "Celková cena",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = price,
                        onValueChange = {
                            price = it
                            updateUnitPrice()
                        },
                        placeholder = {
                            Text("Např. 1 520")
                        },
                        suffix = { Text("Kč") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        singleLine = true,
                        enabled = !isBusy,
                        isError = submitted && !validPrice,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (submitted && !validPrice) {
                        Text(
                            text = "Pole obsahuje povinný údaj",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Text(
                        "Jednotková cena",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = unitPrice,
                        onValueChange = {
                            unitPrice = it
                            updateTotalPrice(parseDecimal(it))
                        },
                        placeholder = {
                            Text("Např. 35,90")
                        },
                        suffix = { Text("Kč/l") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        singleLine = true,
                        enabled = !isBusy,
                        isError = submitted && !validUnitPrice,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (submitted && !validUnitPrice) {
                        Text(
                            text = "Pole obsahuje povinný údaj",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Row(
                        verticalAlignment =
                            androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = fullTank,
                            onCheckedChange = {
                                fullTank = it
                            },
                            enabled = !isBusy
                        )

                        Text(
                            text = "Plné tankování",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Karta 2: Informace o záznamu
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {

                    Text(
                        text = "Informace o záznamu",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = date,
                        onValueChange = {
                            date = it
                        },
                        label = {
                            Text("Datum")
                        },
                        placeholder = {
                            Text("dd.MM.rrrr")
                        },
                        singleLine = true,
                        enabled = !isBusy,
                        isError = submitted && !validDate,
                        supportingText = {
                            if (submitted && !validDate) {
                                Text(
                                    "Zadejte datum ve formátu dd.MM.rrrr"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    InfoRow("Čas", recordTime)
                    InfoRow("GPS poloha", gpsLocation)
                }
            }

            if (saveError != null) {
                Text(
                    text = saveError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    submitted = true

                    if (
                        validDate &&
                        validLiters &&
                        validPrice &&
                        validUnitPrice
                    ) {
                        onSave(
                            LocalDate.parse(date, formatter)
                                .toString(),
                            liters,
                            price,
                            unitPrice
                        )
                    }
                },
                enabled = !isBusy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Uložit tankování")
                }
            }

            OutlinedButton(
                onClick = onRetakePhoto,
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Vyfotit znovu")
            }

            TextButton(
                onClick = onDiscard,
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Zahodit záznam")
            }
            onDelete?.let { deleteRecord ->
                TextButton(
                    onClick = { showDeleteDialog = true },
                    enabled = !isBusy,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Smazat uložené tankování")
                }

                if (showDeleteDialog) {
                    DeleteFuelRecordDialog(
                        date = initialDate,
                        onConfirm = {
                            showDeleteDialog = false
                            deleteRecord()
                        },
                        onDismiss = { showDeleteDialog = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(
    name = "Kontrola OCR - světlý režim",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
private fun EditFuelRecordLightPreview() {
    FuelingManagementToolTheme(
        darkTheme = false,
        dynamicColor = false
    ) {
        EditFuelRecordScreen(
            initialDate = "18.10.2026",
            initialLiters = "42,5",
            initialPrice = "1520"
        )
    }
}

@Preview(
    name = "Kontrola OCR - tmavý režim",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun EditFuelRecordDarkPreview() {
    FuelingManagementToolTheme(
        darkTheme = true,
        dynamicColor = false
    ) {
        EditFuelRecordScreen(
            initialDate = "18.10.2026",
            initialLiters = "42,5",
            initialPrice = "1520"
        )
    }
}

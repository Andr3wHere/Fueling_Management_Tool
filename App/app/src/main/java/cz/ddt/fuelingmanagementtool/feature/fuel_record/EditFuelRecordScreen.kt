
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

@Composable
fun EditFuelRecordScreen(
    onSave: (String, String, String) -> Unit = { _, _, _ -> },
    onBack: () -> Unit = {},
    onRetakePhoto: () -> Unit = {},
    onDiscard: () -> Unit = onBack,
    vehicleName: String = "Volkswagen Tayron",
    initialDate: String = LocalDate.now().format(
        DateTimeFormatter.ofPattern("dd.MM.uuuu")
    ),
    initialLiters: String = "",
    initialPrice: String = "",
    recordTime: String = "16:01",
    gpsLocation: String = "ORLEN Benzina"
) {
    var date by remember(initialDate) {
        mutableStateOf(initialDate)
    }
    var liters by remember(initialLiters) {
        mutableStateOf(initialLiters)
    }
    var price by remember(initialPrice) {
        mutableStateOf(initialPrice)
    }

    var unitPrice by remember(initialPrice, initialLiters) {
        mutableStateOf(
            run {
                val total = initialPrice.replace(',', '.').toDoubleOrNull()
                val amount = initialLiters.replace(',', '.').toDoubleOrNull()

                if (total != null && amount != null &&
                    total.isFinite() && amount.isFinite() &&
                    total > 0 && amount > 0
                ) {
                    "%.2f".format(java.util.Locale.US, total / amount)
                        .replace('.', ',')
                } else {
                    ""
                }
            }
        )
    }
    var fullTank by remember { mutableStateOf(true) }
    var submitted by remember { mutableStateOf(false) }

    val formatter = remember {
        DateTimeFormatter.ofPattern("dd.MM.uuuu")
            .withResolverStyle(ResolverStyle.STRICT)
    }

    val validDate = date.matches(
        Regex("""\d{2}\.\d{2}\.\d{4}""")
    ) && runCatching {
        LocalDate.parse(date, formatter)
    }.isSuccess

    val validLiters = liters.replace(',', '.')
        .toDoubleOrNull()
        ?.let { it.isFinite() && it > 0 } == true

    val validPrice = price.replace(',', '.')
        .toDoubleOrNull()
        ?.let { it.isFinite() && it > 0 } == true

    val validUnitPrice = unitPrice.replace(',', '.')
        .toDoubleOrNull()
        ?.let { it.isFinite() && it > 0 } == true

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                TextButton(onClick = onBack) {
                    Text("← Kontrola tankování")
                }
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

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

            // Karta 1: Údaje o tankování
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Množství paliva",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = liters,
                        onValueChange = { liters = it },
                        placeholder = { Text("Např. 42,5") },
                        suffix = { Text("l") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        singleLine = true,
                        isError = submitted && !validLiters,
                        supportingText = null,
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
                        onValueChange = { price = it },
                        placeholder = { Text("Např. 1 520") },
                        suffix = { Text("Kč") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        singleLine = true,
                        isError = submitted && !validPrice,
                        supportingText = null,
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
                        text = "Jednotková cena",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = unitPrice,
                        onValueChange = { unitPrice = it },
                        placeholder = { Text("Např. 35,90") },
                        suffix = { Text("Kč/l") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        singleLine = true,
                        isError = submitted && !validUnitPrice,
                        supportingText = null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (submitted && !validUnitPrice) {
                        Text(
                            text = "Pole obsahuje povinný údaja",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = fullTank,
                            onCheckedChange = { fullTank = it }
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Informace o záznamu",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )


                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Datum") },
                        placeholder = { Text("dd.MM.rrrr") },
                        singleLine = true,
                        isError = submitted && !validDate,
                        supportingText = {
                            if (submitted && !validDate) {
                                Text("Zadejte datum ve formátu dd.MM.rrrr")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )


                    InfoRow("Čas", recordTime)
                    InfoRow("GPS poloha", gpsLocation)
                }
            }

            Button(
                onClick = {
                    submitted = true
                    if (validDate && validLiters && validPrice && validUnitPrice) {
                        onSave(
                            date,
                            liters.replace(',', '.'),
                            price.replace(',', '.')
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Uložit tankování")
            }

            OutlinedButton(
                onClick = onRetakePhoto,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Vyfotit znovu")
            }

            TextButton(
                onClick = onDiscard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Zahodit záznam")
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
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

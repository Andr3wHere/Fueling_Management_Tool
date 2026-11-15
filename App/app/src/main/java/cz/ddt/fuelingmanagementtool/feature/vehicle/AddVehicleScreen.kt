
package cz.ddt.fuelingmanagementtool.feature.vehicle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cz.ddt.fuelingmanagementtool.data.local.entity.FuelType

private val FormBackground = Color(0xFFDDE0E8)
private val FormPurple = Color(0xFF7053B5)
private val FormWhite = Color.White
private val FormText = Color(0xFF20202A)
private val FormBorder = Color(0xFFAAA6B8)

private fun fuelLabel(type: FuelType): String =
    when (type) {
        FuelType.PETROL -> "Benzín"
        FuelType.DIESEL -> "Nafta"
        FuelType.LPG -> "LPG"
        FuelType.CNG -> "CNG"
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleScreen(
    onSave: (
        String,
        FuelType,
        Double?,
        FuelType?,
        Double?
    ) -> Unit,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var primaryFuel by remember {
        mutableStateOf(FuelType.PETROL)
    }
    var primaryCapacity by remember {
        mutableStateOf("")
    }

    var hasSecondaryFuel by remember {
        mutableStateOf(false)
    }
    var secondaryFuel by remember {
        mutableStateOf(FuelType.LPG)
    }
    var secondaryCapacity by remember {
        mutableStateOf("")
    }

    val primaryValue = primaryCapacity
        .replace(',', '.')
        .toDoubleOrNull()

    val secondaryValue = secondaryCapacity
        .replace(',', '.')
        .toDoubleOrNull()

    val validPrimary =
        primaryCapacity.isBlank() ||
                (primaryValue != null &&
                        primaryValue.isFinite() &&
                        primaryValue > 0)

    val validSecondary =
        !hasSecondaryFuel ||
                (
                        secondaryFuel != primaryFuel &&
                                (
                                        secondaryCapacity.isBlank() ||
                                                (secondaryValue != null &&
                                                        secondaryValue.isFinite() &&
                                                        secondaryValue > 0)
                                        )
                        )

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = FormWhite,
        unfocusedContainerColor = FormWhite,
        focusedBorderColor = FormPurple,
        unfocusedBorderColor = FormBorder,
        focusedTextColor = FormText,
        unfocusedTextColor = FormText,
        focusedLabelColor = FormPurple,
        unfocusedLabelColor = FormText
    )

    Scaffold(
        containerColor = FormBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Nové vozidlo",
                        fontWeight = FontWeight.SemiBold,
                        color = FormText,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text(
                            "‹",
                            color = FormText,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FormWhite
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                "Informace o vozidle",
                fontWeight = FontWeight.Bold,
                color = FormText,
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Název vozidla") },
                placeholder = { Text("Např. Škoda Octavia") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(4.dp))

            Text(
                "Primární palivo",
                fontWeight = FontWeight.Bold,
                color = FormText,
                style = MaterialTheme.typography.bodyMedium
            )

            VehicleFuelDropdown(
                selected = primaryFuel,
                onSelected = { primaryFuel = it },
                label = "Typ paliva"
            )

            OutlinedTextField(
                value = primaryCapacity,
                onValueChange = { primaryCapacity = it },
                label = {
                    Text("Kapacita nádrže (volitelné)")
                },
                suffix = { Text("l") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                isError = !validPrimary,
                shape = RoundedCornerShape(8.dp),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        hasSecondaryFuel = !hasSecondaryFuel
                    }
            ) {
                Checkbox(
                    checked = hasSecondaryFuel,
                    onCheckedChange = {
                        hasSecondaryFuel = it
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = FormPurple
                    )
                )

                Text(
                    "Vozidlo má druhé palivo",
                    color = FormText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (hasSecondaryFuel) {
                HorizontalDivider(
                    color = FormBorder.copy(alpha = 0.4f)
                )

                Text(
                    "Sekundární palivo",
                    fontWeight = FontWeight.Bold,
                    color = FormText,
                    style = MaterialTheme.typography.bodyMedium
                )

                VehicleFuelDropdown(
                    selected = secondaryFuel,
                    onSelected = { secondaryFuel = it },
                    label = "Druhý typ paliva"
                )

                OutlinedTextField(
                    value = secondaryCapacity,
                    onValueChange = {
                        secondaryCapacity = it
                    },
                    label = {
                        Text("Kapacita druhé nádrže (volitelné)")
                    },
                    suffix = { Text("l") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    isError = !validSecondary,
                    shape = RoundedCornerShape(8.dp),
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                if (secondaryFuel == primaryFuel) {
                    Text(
                        "Druhé palivo musí být jiné než primární.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    onSave(
                        name.trim(),
                        primaryFuel,
                        primaryValue,
                        if (hasSecondaryFuel) secondaryFuel else null,
                        if (hasSecondaryFuel) secondaryValue else null
                    )
                },
                enabled = name.isNotBlank() &&
                        validPrimary &&
                        validSecondary,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FormPurple,
                    contentColor = FormWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    "Uložit vozidlo",
                    fontWeight = FontWeight.SemiBold
                )
            }

            OutlinedButton(
                onClick = onNavigateBack,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    FormPurple.copy(alpha = 0.5f)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = FormPurple
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Zrušit")
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VehicleFuelDropdown(
    selected: FuelType,
    onSelected: (FuelType) -> Unit,
    label: String
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = fuelLabel(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = FormWhite,
                unfocusedContainerColor = FormWhite,
                focusedBorderColor = FormPurple,
                unfocusedBorderColor = FormBorder,
                focusedTextColor = FormText,
                unfocusedTextColor = FormText
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
            containerColor = FormWhite
        ) {
            FuelType.entries.forEach { fuel ->
                DropdownMenuItem(
                    text = {
                        Text(fuelLabel(fuel))
                    },
                    onClick = {
                        onSelected(fuel)
                        expanded = false
                    }
                )
            }
        }
    }
}

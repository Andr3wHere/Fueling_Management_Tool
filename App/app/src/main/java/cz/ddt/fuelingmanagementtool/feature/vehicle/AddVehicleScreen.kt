
package cz.ddt.fuelingmanagementtool.feature.vehicle

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cz.ddt.fuelingmanagementtool.data.local.entity.FuelType
import androidx.activity.compose.BackHandler


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
    saveStatus: VehicleSaveStatus,
    onNavigateBack: () -> Unit
) {
    val saving = saveStatus == VehicleSaveStatus.SAVING ||
            saveStatus == VehicleSaveStatus.SAVED

    BackHandler(enabled = saving) {}
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
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Nové vozidlo",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack, enabled = !saving) {
                        Text(
                            "‹",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
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
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = name,
                supportingText = {
                    if (name.isBlank()) Text("Zadej název vozidla.")
                },
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
                color = MaterialTheme.colorScheme.onSurface,
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
                suffix = { Text(if (primaryFuel == FuelType.CNG) "kg" else "l") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                isError = !validPrimary,
                supportingText = {
                    if (!validPrimary) Text("Zadej kladný objem nebo pole nech prázdné.")
                },
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
                        checkedColor = MaterialTheme.colorScheme.primary
                    )
                )

                Text(
                    "Vozidlo má druhé palivo",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (hasSecondaryFuel) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )

                Text(
                    "Sekundární palivo",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
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
                    suffix = { Text(if (secondaryFuel == FuelType.CNG) "kg" else "l") },
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

            if (saveStatus == VehicleSaveStatus.FAILED) {
                Text(
                    text = "Vozidlo se nepodařilo uložit. Zkus to znovu.",
                    color = MaterialTheme.colorScheme.error
                )
            }

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
                enabled = !saving && name.isNotBlank() && validPrimary && validSecondary,
                shape = RoundedCornerShape(12.dp),

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
                enabled = !saving,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
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
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
            containerColor = MaterialTheme.colorScheme.surface
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

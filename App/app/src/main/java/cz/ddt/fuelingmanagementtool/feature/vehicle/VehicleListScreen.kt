package cz.ddt.fuelingmanagementtool.feature.vehicle

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cz.ddt.fuelingmanagementtool.data.local.entity.FuelType
import cz.ddt.fuelingmanagementtool.data.local.entity.VehicleEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val vehicleDateFormatter = DateTimeFormatter.ofPattern("d. M. yyyy")

@Composable
fun VehicleListScreen(
    vehicles: List<VehicleEntity>,
    onAddVehicle: () -> Unit,
    onVehicleClick: (Long) -> Unit,
    lastFuelingDates: Map<Long, LocalDate> = emptyMap()
) {
    val colors = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colors.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddVehicle,
                shape = RoundedCornerShape(16.dp),
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Přidat vozidlo"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(52.dp))

            Text(
                text = "Moje vozidla",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(56.dp))

            if (vehicles.isEmpty()) {
                Text(
                    text = "Zatím nemáš žádné vozidlo. Přidej ho tlačítkem +.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    itemsIndexed(vehicles, key = { _, vehicle -> vehicle.id }) { index, vehicle ->
                        VehicleCard(
                            vehicle = vehicle,
                            lastFuelingDate = lastFuelingDates[vehicle.id],
                            outlined = index == 0,
                            onClick = { onVehicleClick(vehicle.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleCard(
    vehicle: VehicleEntity,
    lastFuelingDate: LocalDate?,
    outlined: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = if (outlined) BorderStroke(1.dp, colors.onSurface) else null
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = colors.primaryContainer,
                    contentColor = colors.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = vehicle.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = listOfNotNull(
                            vehicle.primaryFuelType,
                            vehicle.secondaryFuelType
                        ).joinToString(" • ") { fuelTypeLabel(it) },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }

            HorizontalDivider(color = colors.outlineVariant)

            Text(
                text = "Datum: ${lastFuelingDate?.format(vehicleDateFormatter) ?: "—"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Celkem ujeté km: —",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Průměrná spotřeba: —",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun fuelTypeLabel(type: FuelType): String = when (type) {
    FuelType.PETROL -> "Benzín"
    FuelType.DIESEL -> "Nafta"
    FuelType.LPG -> "LPG"
    FuelType.CNG -> "CNG"
}
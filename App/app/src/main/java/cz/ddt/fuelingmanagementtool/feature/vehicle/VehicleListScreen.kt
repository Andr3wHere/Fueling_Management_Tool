
package cz.ddt.fuelingmanagementtool.feature.vehicle

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cz.ddt.fuelingmanagementtool.data.local.entity.VehicleEntity

@Composable
fun VehicleListScreen(
    vehicles: List<VehicleEntity>,
    onAddVehicle: () -> Unit,
    onVehicleClick: (Long) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddVehicle,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Moje vozidla",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (vehicles.isEmpty()) {

                Text(
                    text = "Zatím nemáš žádné vozidlo.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Klikni na + a přidej své první vozidlo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

            } else {

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {

                    items(
                        items = vehicles,
                        key = { it.id }
                    ) { vehicle ->

                        VehicleCard(
                            vehicle = vehicle,
                            onClick = {
                                onVehicleClick(vehicle.id)
                            }
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
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    text = "🚗",
                    style = MaterialTheme.typography.headlineMedium
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = vehicle.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = fuelTypeLabel(
                            vehicle.primaryFuelType.name
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "›",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            if (vehicle.primaryTankCapacity != null) {

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Kapacita nádrže: ${vehicle.primaryTankCapacity} l",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (vehicle.secondaryFuelType != null) {

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Druhé palivo: ${
                        fuelTypeLabel(
                            vehicle.secondaryFuelType.name
                        )
                    }",
                    style = MaterialTheme.typography.bodyMedium
                )

                vehicle.secondaryTankCapacity?.let { capacity ->
                    Text(
                        text = "Kapacita druhé nádrže: $capacity l",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

private fun fuelTypeLabel(type: String): String {
    return when (type) {
        "PETROL" -> "Benzín"
        "DIESEL" -> "Nafta"
        "LPG" -> "LPG"
        "CNG" -> "CNG"
        else -> type
    }
}

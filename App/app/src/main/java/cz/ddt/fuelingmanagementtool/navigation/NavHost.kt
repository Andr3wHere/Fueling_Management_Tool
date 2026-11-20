
package cz.ddt.fuelingmanagementtool.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import cz.ddt.fuelingmanagementtool.feature.vehicle.AddVehicleScreen
import cz.ddt.fuelingmanagementtool.feature.vehicle.VehicleListScreen
import cz.ddt.fuelingmanagementtool.feature.vehicle.VehicleViewModel

import cz.ddt.fuelingmanagementtool.feature.fuel_record.EditFuelRecordScreen
import cz.ddt.fuelingmanagementtool.feature.fuel_record.FuelRecordListScreen
import cz.ddt.fuelingmanagementtool.feature.fuel_record.FuelRecordUi
import cz.ddt.fuelingmanagementtool.feature.fuel_record.FuelRecordViewModel

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.format.DateTimeFormatter

import androidx.compose.material3.Text
import androidx.compose.runtime.produceState
import cz.ddt.fuelingmanagementtool.data.local.entity.FuelRecordEntity
import kotlinx.coroutines.CancellationException

sealed class Screen(val route: String) {
    data object VehicleList : Screen("vehicle_list")
    data object AddVehicle : Screen("add_vehicle")
    data object FuelRecordList : Screen("fuel_record_list/{vehicleId}")
    data object EditFuelRecord : Screen("edit_fuel_record/{vehicleId}")

    data object EditSavedFuelRecord : Screen("edit_saved_fuel_record/{recordId}")
}

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    val vehicleViewModel: VehicleViewModel = hiltViewModel()
    val vehicles by vehicleViewModel.vehicles.collectAsState()

    val fuelRecordViewModel: FuelRecordViewModel = hiltViewModel()
    val savedRecords by fuelRecordViewModel.records.collectAsState()

    val isSaving by fuelRecordViewModel.isSaving.collectAsState()
    val saveError by fuelRecordViewModel.saveError.collectAsState()
    val saveSuccessful by fuelRecordViewModel.saveSuccessful.collectAsState()

    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    val fuelRecords = savedRecords.map { record ->
        FuelRecordUi(
            id = record.id,
            date = record.date.format(dateFormatter),
            liters = formatStoredValue(
                record.volumeCentilitres,
                "l"
            ),
            price = formatStoredValue(
                record.totalPriceHalers,
                "Kč"
            )
        )
    }

    NavHost(
        navController = navController,
        startDestination = Screen.VehicleList.route
    ) {

        // 1. Seznam vozidel
        composable(Screen.VehicleList.route) {
            VehicleListScreen(
                vehicles = vehicles,
                onAddVehicle = {
                    navController.navigate(Screen.AddVehicle.route)
                },
                onVehicleClick = { vehicleId ->
                    navController.navigate("fuel_record_list/$vehicleId")
                }
            )
        }

        // 2. Přidání vozidla
        composable(Screen.AddVehicle.route) {
            AddVehicleScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSave = {
                        name,
                        primaryFuelType,
                        primaryTankCapacity,
                        secondaryFuelType,
                        secondaryTankCapacity ->

                    vehicleViewModel.addVehicle(
                        name = name,
                        primaryFuelType = primaryFuelType,
                        primaryTankCapacity = primaryTankCapacity,
                        secondaryFuelType = secondaryFuelType,
                        secondaryTankCapacity = secondaryTankCapacity
                    )

                    navController.popBackStack()
                }
            )
        }

        // 3. Nové tankování
        composable(Screen.EditFuelRecord.route) { backStackEntry ->
            val vehicleId = backStackEntry.arguments
                ?.getString("vehicleId")
                ?.toLongOrNull()

            LaunchedEffect(Unit) {
                fuelRecordViewModel.clearSaveStatus()
            }

            LaunchedEffect(saveSuccessful) {
                if (saveSuccessful) {
                    fuelRecordViewModel.clearSaveStatus()
                    navController.popBackStack()
                }
            }

            EditFuelRecordScreen(
                onSave = { date, liters, price, unitPrice ->
                    if (vehicleId != null) {
                        fuelRecordViewModel.saveRecord(
                            date = date,
                            liters = liters,
                            price = price,
                            unitPrice = unitPrice,
                            vehicleId = vehicleId
                        )
                    }
                },
                isSaving = isSaving,
                saveError = saveError,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // 4. Úprava uloženého tankování
        composable(Screen.EditSavedFuelRecord.route) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getString("recordId")?.toLongOrNull()
            if (recordId == null) {
                Text("Neplatné ID tankování.")
                return@composable
            }

            val editViewModel: FuelRecordViewModel = hiltViewModel(backStackEntry)
            val loadResult by produceState<Result<FuelRecordEntity?>?>(null, recordId) {
                value = try {
                    Result.success(editViewModel.getRecordById(recordId))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

            val result = loadResult
            val record = result?.getOrNull()
            val vehicleId = record?.vehicleId

            when {
                result == null -> Text("Načítám tankování…")
                result.isFailure -> Text("Tankování se nepodařilo načíst.")
                record == null -> Text("Tankování už neexistuje.")
                vehicleId == null -> Text("Tankování nemá přiřazené vozidlo.")
                else -> {
                    val saving by editViewModel.isSaving.collectAsState()
                    val error by editViewModel.saveError.collectAsState()
                    val saved by editViewModel.saveSuccessful.collectAsState()

                    LaunchedEffect(saved) {
                        if (saved) navController.popBackStack()
                    }

                    EditFuelRecordScreen(
                        vehicleName = vehicles.find { it.id == vehicleId }?.name ?: "Vozidlo",
                        initialDate = record.date.format(dateFormatter),
                        initialLiters = formatRecordInput(record.volumeCentilitres),
                        initialPrice = formatRecordInput(record.totalPriceHalers),
                        initialUnitPrice = formatRecordInput(record.unitPriceHalersPerLitre),
                        isSaving = saving,
                        saveError = error,
                        onBack = { navController.popBackStack() },
                        onSave = { date, liters, price, unitPrice ->
                            editViewModel.saveRecord(
                                date = date,
                                liters = liters,
                                price = price,
                                unitPrice = unitPrice,
                                vehicleId = vehicleId,
                                originalRecord = record
                            )
                        }
                    )
                }
            }
        }

        // 5. Historie tankování
        composable(Screen.FuelRecordList.route) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("vehicleId")?.toLongOrNull()
            FuelRecordListScreen(
                records = fuelRecords.filter { record ->
                    savedRecords.any { savedRecord ->
                        savedRecord.id == record.id &&
                                savedRecord.vehicleId == vehicleId
                    }
                },
                onAddRecord = {
                    if (vehicleId != null) {
                        navController.navigate(
                            "edit_fuel_record/$vehicleId"
                        )
                    }
                },
                onRecordClick = { recordId ->
                    val route = Screen.EditSavedFuelRecord.route
                        .replace("{recordId}", recordId.toString())
                    navController.navigate(route)
                },
                onDeleteRecord = { recordId ->
                    savedRecords.find { record ->
                        record.id == recordId
                    }?.let { record ->
                        fuelRecordViewModel.deleteRecord(record)
                    }
                }
            )
        }
    }
}

private fun formatStoredValue(
    value: Long,
    unit: String
): String {
    val formatted = BigDecimal.valueOf(value)
        .divide(
            BigDecimal.valueOf(100L),
            2,
            RoundingMode.HALF_UP
        )
        .toPlainString()
        .replace('.', ',')

    return "$formatted $unit"
}

private fun formatRecordInput(value: Long): String {
    return BigDecimal.valueOf(value, 2).toPlainString().replace('.', ',')
}

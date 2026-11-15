
package cz.ddt.fuelingmanagementtool.navigation

import androidx.compose.runtime.Composable
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

sealed class Screen(val route: String) {
    data object VehicleList : Screen("vehicle_list")
    data object AddVehicle : Screen("add_vehicle")
    data object FuelRecordList : Screen("fuel_record_list")
    data object EditFuelRecord : Screen("edit_fuel_record")
}

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    val vehicleViewModel: VehicleViewModel = hiltViewModel()
    val vehicles by vehicleViewModel.vehicles.collectAsState()

    val fuelRecordViewModel: FuelRecordViewModel = hiltViewModel()
    val savedRecords by fuelRecordViewModel.records.collectAsState()

    val fuelRecords = savedRecords.map { record ->
        FuelRecordUi(
            id = record.id,
            date = record.date,
            liters = record.liters.toString(),
            price = record.price.toString()
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

                onVehicleClick = {
                    navController.navigate(Screen.FuelRecordList.route)
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
        composable(Screen.EditFuelRecord.route) {

            EditFuelRecordScreen(
                onSave = { date, liters, price ->

                    val litersValue = liters
                        .replace(',', '.')
                        .toDoubleOrNull()

                    val priceValue = price
                        .replace(',', '.')
                        .toDoubleOrNull()

                    if (
                        date.isNotBlank() &&
                        litersValue != null &&
                        litersValue > 0.0 &&
                        priceValue != null &&
                        priceValue >= 0.0
                    ) {
                        fuelRecordViewModel.saveRecord(
                            date = date,
                            liters = litersValue,
                            price = priceValue
                        )

                        navController.navigate(
                            Screen.FuelRecordList.route
                        ) {
                            popUpTo(Screen.FuelRecordList.route) {
                                inclusive = true
                            }
                        }
                    }
                },

                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // 4. Historie tankování
        composable(Screen.FuelRecordList.route) {

            FuelRecordListScreen(
                records = fuelRecords,

                onAddRecord = {
                    navController.navigate(
                        Screen.EditFuelRecord.route
                    )
                },

                onDeleteRecord = { recordId ->

                    savedRecords.find {
                        it.id == recordId
                    }?.let { record ->

                        fuelRecordViewModel.deleteRecord(record)
                    }
                }
            )
        }
    }
}

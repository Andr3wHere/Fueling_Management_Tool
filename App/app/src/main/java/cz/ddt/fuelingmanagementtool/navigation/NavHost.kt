
package cz.ddt.fuelingmanagementtool.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cz.ddt.fuelingmanagementtool.feature.camera.CameraViewfinderScreen
import cz.ddt.fuelingmanagementtool.feature.camera.PumpOcrParser

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

import androidx.navigation.NavType
import androidx.navigation.navArgument
import java.util.Locale

sealed class Screen(val route: String) {
    data object VehicleList : Screen("vehicle_list")
    data object AddVehicle : Screen("add_vehicle")
    data object FuelRecordList : Screen("fuel_record_list/{vehicleId}")
    data object EditFuelRecord : Screen(
        "edit_fuel_record/{vehicleId}?liters={liters}&price={price}&unitPrice={unitPrice}&fromCamera={fromCamera}"
    )
    data object Camera : Screen("camera/{vehicleId}")
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
        // 0. Kamera – vyfocení stojanu
        composable(Screen.Camera.route) { backStackEntry ->
            val vehicleId = backStackEntry.arguments
                ?.getString("vehicleId")
                ?.toLongOrNull()

            CameraViewfinderScreen(
                onTextRecognized = { text, _ ->
                    val result = PumpOcrParser().parse(text)

                    if (vehicleId != null) {
                        navController.navigate(
                            "edit_fuel_record/$vehicleId" +
                                    "?liters=${result.liters.toFormValue()}" +
                                    "&price=${result.totalPrice.toFormValue()}" +
                                    "&unitPrice=${result.pricePerLiter.toFormValue()}" +
                                    "&fromCamera=true"
                        ) {
                            popUpTo(Screen.Camera.route) { inclusive = true }
                        }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

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
        composable(
            route = Screen.EditFuelRecord.route,
            arguments = listOf(
                navArgument("vehicleId") { type = NavType.StringType },
                navArgument("liters") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("price") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("unitPrice") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("fromCamera") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments
                ?.getString("vehicleId")
                ?.toLongOrNull()

            val ocrLiters = backStackEntry.arguments?.getString("liters").orEmpty()
            val ocrPrice = backStackEntry.arguments?.getString("price").orEmpty()
            val ocrUnitPrice = backStackEntry.arguments?.getString("unitPrice").orEmpty()
            val fromCamera = backStackEntry.arguments?.getBoolean("fromCamera") ?: false

            val missing = buildList {
                if (ocrLiters.isBlank()) add("množství paliva")
                if (ocrPrice.isBlank()) add("celková cena")
                if (ocrUnitPrice.isBlank()) add("cena za litr")
            }

            val ocrWarning =
                if (fromCamera && missing.isNotEmpty()) {
                    "Nepodařilo se rozpoznat: ${missing.joinToString()}. Doplň prosím ručně."
                } else {
                    null
                }

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
                initialLiters = ocrLiters,
                initialPrice = ocrPrice,
                initialUnitPrice = ocrUnitPrice,
                ocrWarning = ocrWarning,
                onRetakePhoto = {
                    if (vehicleId != null) {
                        navController.navigate("camera/$vehicleId") {
                            popUpTo(Screen.EditFuelRecord.route) { inclusive = true }
                        }
                    }
                },
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

        // 4. Historie tankování
        composable(Screen.FuelRecordList.route) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getString("vehicleId")?.toLongOrNull()
            FuelRecordListScreen(
                records = fuelRecords.filter { record ->
                    savedRecords.any { savedRecord ->
                        savedRecord.id == record.id &&
                                savedRecord.vehicleId == vehicleId
                    }
                },
                onTakePhoto = {
                    if (vehicleId != null) {
                        navController.navigate("camera/$vehicleId")
                    }
                },
                onManualEntry = {
                    if (vehicleId != null) {
                        navController.navigate("edit_fuel_record/$vehicleId")
                    }
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

private fun Double?.toFormValue(): String =
    this?.let {
        String.format(Locale.US, "%.2f", it).replace('.', ',')
    } ?: ""

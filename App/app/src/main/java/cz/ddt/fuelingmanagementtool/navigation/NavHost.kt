package cz.ddt.fuelingmanagementtool.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import cz.ddt.fuelingmanagementtool.ui.theme.FuelingManagementToolTheme

// 1. Definice cílů (tras)
sealed class Screen(val route: String) {
    data object VehicleList : Screen("vehicle_list")
    data object AddVehicle : Screen("add_vehicle")
}

// 2. Hlavní navigační uzel (NavHost)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.VehicleList.route
    ) {
        // Obrazovka 1: Přehled vozidel
        composable(route = Screen.VehicleList.route) {
            VehicleListScreen(
                onNavigateToAddVehicle = {
                    navController.navigate(Screen.AddVehicle.route)
                }
            )
        }

        // Obrazovka 2: Formulář nového vozidla
        composable(route = Screen.AddVehicle.route) {
            AddVehicleScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

// 3. Zástupná obrazovka přehledu
@Composable
fun VehicleListScreen(
    onNavigateToAddVehicle: () -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Přehled vozidel",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateToAddVehicle) {
                Text("Přidat nové vozidlo")
            }
        }
    }
}

// 4. Zástupná obrazovka formuláře
@Composable
fun AddVehicleScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Formulář nového vozidla",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateBack) {
                Text("Zpět na přehled")
            }
        }
    }
}

@Preview(
    name = "Prehled vozidel - svetly rezim",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO
)
@Composable
private fun VehicleListLightPreview() {
    FuelingManagementToolTheme(darkTheme = false) {
        VehicleListScreen(onNavigateToAddVehicle = {})
    }
}

@Preview(
    name = "Prehled vozidel - tmavy rezim",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun VehicleListDarkPreview() {
    FuelingManagementToolTheme(darkTheme = true) {
        VehicleListScreen(onNavigateToAddVehicle = {})
    }
}

@Preview(
    name = "Pridani vozidla - svetly rezim",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO
)
@Composable
private fun AddVehicleLightPreview() {
    FuelingManagementToolTheme(darkTheme = false) {
        AddVehicleScreen(onNavigateBack = {})
    }
}

@Preview(
    name = "Pridani vozidla - tmavy rezim",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun AddVehicleDarkPreview() {
    FuelingManagementToolTheme(darkTheme = true) {
        AddVehicleScreen(onNavigateBack = {})
    }
}


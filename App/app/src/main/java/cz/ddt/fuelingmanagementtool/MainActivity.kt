package cz.ddt.fuelingmanagementtool

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cz.ddt.fuelingmanagementtool.navigation.AppNavigation
import cz.ddt.fuelingmanagementtool.ui.theme.FuelingManagementToolTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint(ComponentActivity::class)
class MainActivity : Hilt_MainActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FuelingManagementToolTheme {
                AppNavigation()
            }
        }
    }
}

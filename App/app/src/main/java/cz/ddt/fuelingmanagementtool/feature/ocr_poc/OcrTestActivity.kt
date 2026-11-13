package cz.ddt.fuelingmanagementtool.feature.ocr_poc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cz.ddt.fuelingmanagementtool.feature.camera.CameraViewfinderScreen
import cz.ddt.fuelingmanagementtool.ui.theme.FuelingManagementToolTheme

class OcrTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FuelingManagementToolTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    //OcrPocScreen()
                    var capturedText by remember { mutableStateOf<String?>(null) }

                    CameraViewfinderScreen(
                        onTextRecognized = { text -> capturedText = text },
                        onNavigateBack = { finish() }
                    )

                    capturedText?.let { result ->
                        AlertDialog(
                            onDismissRequest = { capturedText = null },
                            title = { Text("Výstup z oříznuté oblasti") },
                            text = { Text(result.ifBlank { "Žádný text nerozpoznán." }) },
                            confirmButton = {
                                TextButton(onClick = { capturedText = null }) {
                                    Text("OK")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
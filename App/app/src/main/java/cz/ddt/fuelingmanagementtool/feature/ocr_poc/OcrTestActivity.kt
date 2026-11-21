package cz.ddt.fuelingmanagementtool.feature.ocr_poc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.ddt.fuelingmanagementtool.feature.camera.CameraViewfinderScreen
import cz.ddt.fuelingmanagementtool.feature.camera.OcrFuelResult
import cz.ddt.fuelingmanagementtool.ui.theme.FuelingManagementToolTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint(ComponentActivity::class)
class OcrTestActivity : Hilt_OcrTestActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FuelingManagementToolTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var ocrResult by remember { mutableStateOf<OcrFuelResult?>(null) }

                    CameraViewfinderScreen(
                        onOcrResult = { result ->
                            ocrResult = result
                        },
                        onNavigateBack = { finish() }
                    )

                    ocrResult?.let { result ->
                        AlertDialog(
                            onDismissRequest = { ocrResult = null },
                            title = { Text("Rozpoznané hodnoty") },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Objem: ${result.liters?.let { "$it l" } ?: "Nerozpoznáno"}")
                                    Text("Celková cena: ${result.totalPrice?.let { "$it Kč" } ?: "Nerozpoznáno"}")
                                    Text("Cena za litr: ${result.pricePerLiter?.let { "$it Kč/l" } ?: "Nerozpoznáno"}")
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { ocrResult = null }) {
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
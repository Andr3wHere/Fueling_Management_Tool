package cz.ddt.fuelingmanagementtool.feature.ocr_poc

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
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
                    var croppedPreview by remember { mutableStateOf<Bitmap?>(null) }

                    CameraViewfinderScreen(
                        onTextRecognized = { text, bitmapCopy ->
                            capturedText = text
                            croppedPreview = bitmapCopy
                        },
                        onNavigateBack = { finish() }
                    )

                    capturedText?.let { result ->
                        AlertDialog(
                            onDismissRequest = {
                                capturedText = null
                                croppedPreview = null
                            },
                            title = { Text("Výstup z oříznuté oblasti") },
                            text = {
                                Column {
                                    croppedPreview?.let { bmp ->
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = "Cropped ML Kit Input",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 12.dp)
                                        )
                                    }
                                    Text(result.ifBlank { "Žádný text nerozpoznán." })
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    capturedText = null
                                    croppedPreview = null
                                }) { Text("OK") }
                            }
                        )
                    }
                }
            }
        }
    }
}
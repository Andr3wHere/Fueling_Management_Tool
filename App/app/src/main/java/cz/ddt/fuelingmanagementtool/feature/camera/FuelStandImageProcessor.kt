package cz.ddt.fuelingmanagementtool.feature.camera

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "FuelStandImageProcessor"

class FuelStandImageProcessor(
    private val imageCropper: ImageCropper,
    private val textRecognizer: OcrTextRecognizer,
    private val pumpOcrParser: PumpOcrParser
) {
    suspend fun processImage(
        imageProxy: ImageProxy,
        reticleRect: Rect,
        previewViewSize: IntSize
    ): OcrFuelResult {
        val croppedBitmap = try {
            withContext(Dispatchers.Default) {
                val fullSensorSize = "${imageProxy.width}x${imageProxy.height}"
                imageProxy.use { proxy ->
                    imageCropper.cropToRect(
                        imageProxy = proxy,
                        rect = reticleRect,
                        previewViewSize = previewViewSize
                    )
                }.also { cropped ->
                    val uiRatio = if (reticleRect.height > 0f) reticleRect.width / reticleRect.height else 0f
                    val bmpRatio = if (cropped.height > 0) cropped.width.toFloat() / cropped.height else 0f
                    Log.d(TAG, "ImageProxy closed safely")
                    Log.d(TAG, "Full sensor frame: $fullSensorSize")
                    Log.d(
                        TAG,
                        "UI Reticle: ${reticleRect.width.toInt()}x${reticleRect.height.toInt()} (ratio=${"%.3f".format(uiRatio)})"
                    )
                    Log.d(
                        TAG,
                        "Cropped Bitmap: ${cropped.width}x${cropped.height} (ratio=${"%.3f".format(bmpRatio)})"
                    )
                }
            }
        } catch (e: Throwable) {
            imageProxy.close()
            throw e
        }

        return processBitmap(croppedBitmap)
    }

    suspend fun processBitmap(bitmap: Bitmap): OcrFuelResult {
        val rawText = textRecognizer.recognizeFromBitmap(bitmap)
        Log.d(TAG, "Raw OCR text:\n$rawText")
        return withContext(Dispatchers.Default) {
            pumpOcrParser.parse(rawText)
        }
    }

    fun close() {
        textRecognizer.close()
    }
}

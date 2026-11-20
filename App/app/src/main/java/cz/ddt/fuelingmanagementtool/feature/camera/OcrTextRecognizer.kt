package cz.ddt.fuelingmanagementtool.feature.camera

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class OcrTextRecognizer {
    private val recognizerLazy = lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }
    private val recognizer get() = recognizerLazy.value

    suspend fun recognizeFromBitmap(bitmap: Bitmap): String =
        suspendCancellableCoroutine { continuation ->
            recognizeFromBitmap(
                bitmap = bitmap,
                onSuccess = { text ->
                    if (continuation.isActive) {
                        continuation.resume(text)
                    }
                },
                onFailure = { e ->
                    if (continuation.isActive) {
                        continuation.resumeWithException(e)
                    }
                }
            )
        }

    // Kept temporarily while CameraViewfinderScreen still uses callbacks; remove once migrated to FuelStandImageProcessor.
    fun recognizeFromBitmap(
        bitmap: Bitmap,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    onSuccess(visionText.text)
                }
                .addOnFailureListener { e ->
                    onFailure(e)
                }
                .addOnCompleteListener {
                    if (!bitmap.isRecycled) {
                        bitmap.recycle()
                    }
                }
        } catch (e: Exception) {
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
            onFailure(e)
        }
    }

    fun close() {
        if (recognizerLazy.isInitialized()) {
            recognizerLazy.value.close()
        }
    }
}
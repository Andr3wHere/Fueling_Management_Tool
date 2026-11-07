package cz.ddt.fuelingmanagementtool.feature.ocr_poc

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class OcrProcessor {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    //@OptIn(ExperimentalGetImage::class)
    fun processImageProxy(
        imageProxy: ImageProxy,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    onSuccess(visionText.text)
                }
                .addOnFailureListener { e ->
                    onFailure(e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
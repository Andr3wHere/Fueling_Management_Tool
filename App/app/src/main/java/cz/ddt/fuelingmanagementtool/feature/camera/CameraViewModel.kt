package cz.ddt.fuelingmanagementtool.feature.camera

import androidx.camera.core.ImageProxy
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val fuelStandImageProcessor: FuelStandImageProcessor
) : ViewModel() {

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun onCaptureStarted() {
        _isProcessing.value = true
        _errorMessage.value = null
    }

    fun onError(message: String) {
        _isProcessing.value = false
        _errorMessage.value = message
    }

    fun processCapturedImage(
        imageProxy: ImageProxy,
        reticleRect: Rect,
        previewViewSize: IntSize,
        onResult: (OcrFuelResult) -> Unit
    ) {
        if (!viewModelScope.isActive) {
            imageProxy.close()
            return
        }

        viewModelScope.launch {
            try {
                val result = fuelStandImageProcessor.processImage(
                    imageProxy = imageProxy,
                    reticleRect = reticleRect,
                    previewViewSize = previewViewSize
                )
                _isProcessing.value = false
                onResult(result)
            } catch (e: CancellationException) {
                imageProxy.close()
                throw e
            } catch (e: Exception) {
                _isProcessing.value = false
                _errorMessage.value =
                    "Chyba zpracování snímku: ${e.localizedMessage ?: "Neznámá chyba"}"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        fuelStandImageProcessor.close()
    }
}

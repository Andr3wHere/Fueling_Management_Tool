package cz.ddt.fuelingmanagementtool.feature.camera

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import kotlin.math.max
import kotlin.math.roundToInt

class ImageCropper {
    fun cropToRect(imageProxy: ImageProxy, rect: Rect, previewViewSize: IntSize
    ): Bitmap {
        val rawBitmap = imageProxy.toBitmap()
        val rotatedBitmap = rotateBitmapIfNeeded(
            bitmap = rawBitmap,
            rotationDegrees = imageProxy.imageInfo.rotationDegrees
        )

        if (previewViewSize.width <= 0 || previewViewSize.height <= 0 || rect.isEmpty) {
            return rotatedBitmap
        }

        val viewWidth = previewViewSize.width.toFloat()
        val viewHeight = previewViewSize.height.toFloat()
        val bmpWidth = rotatedBitmap.width.toFloat()
        val bmpHeight = rotatedBitmap.height.toFloat()

        val scale = max(viewWidth / bmpWidth, viewHeight / bmpHeight)
        val scaledBmpWidth = scale * bmpWidth
        val scaledBmpHeight = scale * bmpHeight

        val offsetX = (scaledBmpWidth - viewWidth) / 2f
        val offsetY = (scaledBmpHeight - viewHeight) / 2f

        val cropLeft = ((rect.left + offsetX) / scale)
            .roundToInt()
            .coerceIn(0, rotatedBitmap.width -1)
        val cropTop = ((rect.top + offsetY) / scale)
            .roundToInt()
            .coerceIn(0, rotatedBitmap.height -1)
        val cropRight = ((rect.right + offsetX) / scale)
            .roundToInt()
            .coerceIn(cropLeft + 1, rotatedBitmap.width)
        val cropBottom = ((rect.bottom + offsetY) / scale)
            .roundToInt()
            .coerceIn(cropTop + 1, rotatedBitmap.height)

        val cropWidth = cropRight - cropLeft
        val cropHeight = cropBottom - cropTop

        val croppedBitmap = Bitmap.createBitmap(
            rotatedBitmap,
            cropLeft,
            cropTop,
            cropWidth,
            cropHeight
        )

        if (croppedBitmap != rotatedBitmap) {
            rotatedBitmap.recycle()
        }

        return croppedBitmap
    }

    private fun rotateBitmapIfNeeded(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        if (rotationDegrees == 0) return bitmap

        val matrix = Matrix().apply {
            postRotate(rotationDegrees.toFloat())
        }

        val rotated = Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
        if (rotated != bitmap) {
            bitmap.recycle()
        }
        return rotated
    }
}
package com.wade.ocr.util

import android.graphics.Bitmap
import android.graphics.Matrix

object BitmapUtils {
    fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    fun scaleBitmap(source: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val ratio = Math.min(
            maxWidth.toFloat() / source.width,
            maxHeight.toFloat() / source.height
        )
        val width = (source.width * ratio).toInt()
        val height = (source.height * ratio).toInt()
        return Bitmap.createScaledBitmap(source, width, height, true)
    }
}

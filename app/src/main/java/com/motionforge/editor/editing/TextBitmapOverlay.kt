package com.motionforge.editor.editing

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.media3.effect.BitmapOverlay
import com.motionforge.editor.data.OverlayPosition

/**
 * Renders [text] into a transparent bitmap the size of the video frame, so [OverlayEffect]
 * can composite it directly onto each output frame during export.
 */
class TextBitmapOverlay(
    private val text: String,
    private val position: OverlayPosition,
    private val frameWidth: Int,
    private val frameHeight: Int
) : BitmapOverlay() {

    private val bitmap: Bitmap by lazy { renderBitmap() }

    override fun getBitmap(presentationTimeUs: Long): Bitmap = bitmap

    private fun renderBitmap(): Bitmap {
        val width = frameWidth.coerceAtLeast(1)
        val height = frameHeight.coerceAtLeast(1)
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = height / 16f
            textAlign = Paint.Align.CENTER
            setShadowLayer(8f, 0f, 0f, Color.BLACK)
        }

        val x = width / 2f
        val y = when (position) {
            OverlayPosition.TOP -> height * 0.12f
            OverlayPosition.CENTER -> height * 0.5f
            OverlayPosition.BOTTOM -> height * 0.88f
        }
        canvas.drawText(text, x, y, paint)
        return bmp
    }
}

package com.motionforge.editor.editing

import androidx.media3.common.Effect
import androidx.media3.effect.Contrast
import androidx.media3.effect.HslAdjustment
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.RgbAdjustment
import androidx.media3.effect.TextureOverlay
import com.google.common.collect.ImmutableList
import com.motionforge.editor.data.Clip

/**
 * Maps a [Clip]'s color-adjustment and text-overlay settings onto Media3's GL video effects,
 * applied by the Transformer at export time.
 *
 * NOTE: built against the androidx.media3:media3-effect 1.4.1 API surface as documented at
 * developer.android.com/media/media3/transformer/effects. This module could not be compiled
 * against the real dependency in this sandbox (network access to Google's Maven repo is
 * blocked here), so double-check these exact class/method names against the Javadoc for the
 * Media3 version actually resolved when you first build in Android Studio.
 */
object ClipEffects {

    fun buildVideoEffects(clip: Clip, frameSize: FrameSize): List<Effect> {
        val effects = mutableListOf<Effect>()

        if (clip.brightness != 0f) {
            val scale = (1f + clip.brightness).coerceIn(0f, 2f)
            effects += RgbAdjustment.Builder()
                .setRedScale(scale)
                .setGreenScale(scale)
                .setBlueScale(scale)
                .build()
        }

        if (clip.contrast != 1f) {
            effects += Contrast((clip.contrast - 1f).coerceIn(-1f, 1f))
        }

        if (clip.saturation != 1f) {
            val percentChange = ((clip.saturation - 1f) * 100f).coerceIn(-100f, 100f)
            effects += HslAdjustment.Builder()
                .adjustSaturation(percentChange)
                .build()
        }

        if (clip.hasOverlayText) {
            val overlay: TextureOverlay = TextBitmapOverlay(
                text = clip.overlayText,
                position = clip.overlayPosition,
                frameWidth = frameSize.width,
                frameHeight = frameSize.height
            )
            effects += OverlayEffect(ImmutableList.of(overlay))
        }

        return effects
    }
}

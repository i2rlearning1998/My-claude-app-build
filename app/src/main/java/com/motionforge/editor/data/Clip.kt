package com.motionforge.editor.data

import kotlinx.serialization.Serializable
import java.util.UUID

enum class ClipType {
    VIDEO,
    IMAGE
}

enum class OverlayPosition {
    TOP,
    CENTER,
    BOTTOM
}

/**
 * A single piece of media placed on the timeline.
 *
 * Trim bounds are relative to the source media (0..sourceDurationMs).
 * Color adjustments follow Media3's effect ranges: brightness -1..1 (additive),
 * contrast and saturation 0..2 (1 = unchanged).
 */
@Serializable
data class Clip(
    val id: String = UUID.randomUUID().toString(),
    val uri: String,
    val type: ClipType,
    val sourceDurationMs: Long,
    val trimStartMs: Long = 0,
    val trimEndMs: Long = sourceDurationMs,
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val overlayText: String = "",
    val overlayPosition: OverlayPosition = OverlayPosition.BOTTOM
) {
    val trimmedDurationMs: Long
        get() = (trimEndMs - trimStartMs).coerceAtLeast(0)

    val hasOverlayText: Boolean
        get() = overlayText.isNotBlank()

    val hasColorAdjustments: Boolean
        get() = brightness != 0f || contrast != 1f || saturation != 1f
}

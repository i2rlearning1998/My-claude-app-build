package com.motionforge.editor.editing

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.motionforge.editor.data.Clip
import com.motionforge.editor.data.ClipType

data class FrameSize(val width: Int, val height: Int)

private val DEFAULT_FRAME_SIZE = FrameSize(1920, 1080)

/** Determines a clip's decoded (post-rotation) pixel dimensions, used to size text overlays. */
fun probeFrameSize(context: Context, clip: Clip): FrameSize = when (clip.type) {
    ClipType.VIDEO -> probeVideoFrameSize(context, Uri.parse(clip.uri))
    ClipType.IMAGE -> probeImageFrameSize(context, Uri.parse(clip.uri))
}

private fun probeVideoFrameSize(context: Context, uri: Uri): FrameSize {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, uri)
        val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            ?.toIntOrNull() ?: DEFAULT_FRAME_SIZE.width
        val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            ?.toIntOrNull() ?: DEFAULT_FRAME_SIZE.height
        val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
            ?.toIntOrNull() ?: 0
        if (rotation == 90 || rotation == 270) FrameSize(height, width) else FrameSize(width, height)
    } catch (e: Exception) {
        DEFAULT_FRAME_SIZE
    } finally {
        retriever.release()
    }
}

private fun probeImageFrameSize(context: Context, uri: Uri): FrameSize {
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    return try {
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }
        val width = options.outWidth.takeIf { it > 0 } ?: DEFAULT_FRAME_SIZE.height
        val height = options.outHeight.takeIf { it > 0 } ?: DEFAULT_FRAME_SIZE.width
        FrameSize(width, height)
    } catch (e: Exception) {
        FrameSize(DEFAULT_FRAME_SIZE.height, DEFAULT_FRAME_SIZE.width)
    }
}

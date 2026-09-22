package com.motionforge.editor.editing

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.motionforge.editor.data.Clip
import com.motionforge.editor.data.ClipType

/** Inspects a picked media Uri to build a starting [Clip] for the timeline. */
class MediaProbe(private val context: Context) {

    companion object {
        private const val DEFAULT_IMAGE_DURATION_MS = 3000L
    }

    fun probe(uri: Uri): Clip? {
        val mimeType = context.contentResolver.getType(uri) ?: return null
        return when {
            mimeType.startsWith("video/") -> probeVideo(uri)
            mimeType.startsWith("image/") -> probeImage(uri)
            else -> null
        }
    }

    private fun probeVideo(uri: Uri): Clip? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val durationMs = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()
                ?: return null
            if (durationMs <= 0) return null
            Clip(
                uri = uri.toString(),
                type = ClipType.VIDEO,
                sourceDurationMs = durationMs,
                trimEndMs = durationMs
            )
        } catch (e: Exception) {
            null
        } finally {
            retriever.release()
        }
    }

    private fun probeImage(uri: Uri): Clip = Clip(
        uri = uri.toString(),
        type = ClipType.IMAGE,
        sourceDurationMs = DEFAULT_IMAGE_DURATION_MS,
        trimEndMs = DEFAULT_IMAGE_DURATION_MS
    )
}

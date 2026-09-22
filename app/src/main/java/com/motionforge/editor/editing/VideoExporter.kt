package com.motionforge.editor.editing

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import com.motionforge.editor.data.Clip
import com.motionforge.editor.data.ClipType
import com.motionforge.editor.data.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Drives androidx.media3:media3-transformer to render a [Project]'s timeline (trim + color
 * effects + text overlay per clip) into a single MP4.
 */
class VideoExporter(private val context: Context) {

    sealed interface ExportState {
        data object Idle : ExportState
        data class InProgress(val percent: Int) : ExportState
        data class Success(val outputFile: File) : ExportState
        data class Failure(val message: String) : ExportState
    }

    private var transformer: Transformer? = null
    private var progressJob: Job? = null

    /** Heavy IO (probing clip dimensions, building effects) done off the main thread. */
    suspend fun buildComposition(project: Project): Composition = withContext(Dispatchers.IO) {
        val editedItems = project.clips.map { toEditedMediaItem(it) }
        val sequence = EditedMediaItemSequence(editedItems)
        Composition.Builder(sequence).build()
    }

    fun outputFileFor(project: Project): File {
        val dir = File(context.getExternalFilesDir(null), "MotionForge").apply { mkdirs() }
        val safeName = project.name.ifBlank { "MotionForge" }.replace(Regex("[^A-Za-z0-9_-]"), "_")
        return File(dir, "${safeName}_${System.currentTimeMillis()}.mp4")
    }

    /** Must be called from the main thread (Transformer requires a Looper). */
    fun start(
        composition: Composition,
        outputFile: File,
        scope: CoroutineScope,
        onState: (ExportState) -> Unit
    ) {
        val listener = object : Transformer.Listener {
            override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                progressJob?.cancel()
                onState(ExportState.Success(outputFile))
            }

            override fun onError(
                composition: Composition,
                exportResult: ExportResult,
                exportException: ExportException
            ) {
                progressJob?.cancel()
                onState(ExportState.Failure(exportException.message ?: "Export failed"))
            }
        }

        val newTransformer = Transformer.Builder(context)
            .addListener(listener)
            .build()
        transformer = newTransformer
        newTransformer.start(composition, outputFile.absolutePath)
        onState(ExportState.InProgress(0))

        val holder = ProgressHolder()
        progressJob = scope.launch {
            while (isActive) {
                val state = newTransformer.getProgress(holder)
                if (state == Transformer.PROGRESS_STATE_AVAILABLE) {
                    onState(ExportState.InProgress(holder.progress))
                }
                delay(250)
            }
        }
    }

    fun cancel() {
        progressJob?.cancel()
        progressJob = null
        transformer?.cancel()
        transformer = null
    }

    private fun toEditedMediaItem(clip: Clip): EditedMediaItem {
        val uri = Uri.parse(clip.uri)
        val mediaItemBuilder = MediaItem.Builder().setUri(uri)
        if (clip.type == ClipType.VIDEO) {
            mediaItemBuilder.setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs(clip.trimStartMs)
                    .setEndPositionMs(clip.trimEndMs.coerceAtLeast(clip.trimStartMs + 1))
                    .build()
            )
        }
        val mediaItem = mediaItemBuilder.build()

        val frameSize = probeFrameSize(context, clip)
        val videoEffects = ClipEffects.buildVideoEffects(clip, frameSize)

        val editedBuilder = EditedMediaItem.Builder(mediaItem)
            .setEffects(Effects(emptyList(), videoEffects))

        if (clip.type == ClipType.IMAGE) {
            editedBuilder
                .setDurationUs(clip.trimmedDurationMs * 1000)
                .setFrameRate(30)
        }

        return editedBuilder.build()
    }
}

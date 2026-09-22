package com.motionforge.editor.ui.editor

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motionforge.editor.data.Clip
import com.motionforge.editor.data.Project
import com.motionforge.editor.data.ProjectRepository
import com.motionforge.editor.editing.MediaProbe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditorViewModel(
    private val repository: ProjectRepository,
    private val projectId: String,
    private val mediaProbe: MediaProbe
) : ViewModel() {

    private val _project = MutableStateFlow<Project?>(null)
    val project: StateFlow<Project?> = _project.asStateFlow()

    private val _selectedClipId = MutableStateFlow<String?>(null)
    val selectedClipId: StateFlow<String?> = _selectedClipId.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeProject(projectId).collect { loaded ->
                _project.value = loaded
                if (_selectedClipId.value == null) {
                    _selectedClipId.value = loaded?.clips?.firstOrNull()?.id
                }
            }
        }
    }

    fun addMedia(uris: List<Uri>) {
        val current = _project.value ?: return
        val newClips = uris.mapNotNull { mediaProbe.probe(it) }
        if (newClips.isEmpty()) return
        val updated = current.copy(clips = current.clips + newClips)
        applyAndPersist(updated)
        _selectedClipId.value = newClips.first().id
    }

    fun selectClip(clipId: String) {
        _selectedClipId.value = clipId
    }

    /**
     * Updates a clip. [persist] can be set to false while a slider is actively being dragged,
     * so the UI/preview reflects each frame without hammering Room; call again with
     * [persist] = true (or rely on the next persisted change) once the drag settles.
     */
    fun updateClip(clipId: String, persist: Boolean = true, transform: (Clip) -> Clip) {
        val current = _project.value ?: return
        val updated = current.copy(
            clips = current.clips.map { if (it.id == clipId) transform(it) else it }
        )
        if (persist) applyAndPersist(updated) else _project.value = updated
    }

    fun deleteClip(clipId: String) {
        val current = _project.value ?: return
        val updated = current.copy(clips = current.clips.filterNot { it.id == clipId })
        applyAndPersist(updated)
        if (_selectedClipId.value == clipId) {
            _selectedClipId.value = updated.clips.firstOrNull()?.id
        }
    }

    fun moveClip(clipId: String, delta: Int) {
        val current = _project.value ?: return
        val index = current.clips.indexOfFirst { it.id == clipId }
        if (index < 0) return
        val newIndex = (index + delta).coerceIn(0, current.clips.lastIndex)
        if (newIndex == index) return
        val mutable = current.clips.toMutableList()
        val item = mutable.removeAt(index)
        mutable.add(newIndex, item)
        applyAndPersist(current.copy(clips = mutable))
    }

    fun renameProject(name: String) {
        val current = _project.value ?: return
        applyAndPersist(current.copy(name = name.ifBlank { current.name }))
    }

    private fun applyAndPersist(updated: Project) {
        _project.value = updated
        viewModelScope.launch { repository.saveProject(updated) }
    }
}

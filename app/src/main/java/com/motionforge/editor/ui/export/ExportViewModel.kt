package com.motionforge.editor.ui.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motionforge.editor.data.Project
import com.motionforge.editor.data.ProjectRepository
import com.motionforge.editor.editing.VideoExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExportViewModel(
    private val repository: ProjectRepository,
    private val projectId: String,
    private val exporter: VideoExporter
) : ViewModel() {

    private val _project = MutableStateFlow<Project?>(null)
    val project: StateFlow<Project?> = _project.asStateFlow()

    private val _state = MutableStateFlow<VideoExporter.ExportState>(VideoExporter.ExportState.Idle)
    val state: StateFlow<VideoExporter.ExportState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeProject(projectId).collect { _project.value = it }
        }
    }

    fun startExport() {
        val project = _project.value ?: return
        if (_state.value is VideoExporter.ExportState.InProgress) return
        viewModelScope.launch {
            _state.value = VideoExporter.ExportState.InProgress(0)
            val composition = exporter.buildComposition(project)
            val outputFile = exporter.outputFileFor(project)
            exporter.start(composition, outputFile, viewModelScope) { newState ->
                _state.value = newState
            }
        }
    }

    fun cancelExport() {
        exporter.cancel()
        _state.value = VideoExporter.ExportState.Idle
    }

    override fun onCleared() {
        exporter.cancel()
        super.onCleared()
    }
}

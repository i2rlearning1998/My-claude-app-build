package com.motionforge.editor.ui.editor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.motionforge.editor.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onBack: () -> Unit,
    onExport: () -> Unit
) {
    val project by viewModel.project.collectAsState()
    val selectedClipId by viewModel.selectedClipId.collectAsState()

    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        // The Photo Picker grants a read URI permission that already persists across
        // process death without needing an explicit takePersistableUriPermission call.
        if (uris.isNotEmpty()) {
            viewModel.addMedia(uris)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project?.name ?: stringResource(R.string.untitled_project)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = onExport) {
                        Icon(Icons.Filled.FileUpload, contentDescription = stringResource(R.string.export))
                    }
                }
            )
        }
    ) { padding ->
        val clips = project?.clips.orEmpty()
        val selectedClip = clips.firstOrNull { it.id == selectedClipId }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PreviewPlayer(
                clip = selectedClip,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            androidx.compose.material3.Button(
                onClick = {
                    pickMedia.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text(text = " " + stringResource(R.string.add_media))
            }

            TimelineView(
                clips = clips,
                selectedClipId = selectedClipId,
                onSelect = viewModel::selectClip,
                onMove = viewModel::moveClip,
                onDelete = viewModel::deleteClip,
                onTrimChange = { clipId, start, end, commit ->
                    viewModel.updateClip(clipId, persist = commit) {
                        it.copy(trimStartMs = start, trimEndMs = end)
                    }
                }
            )

            if (selectedClip != null) {
                ClipPropertiesPanel(
                    clip = selectedClip,
                    onUpdate = { persist, transform ->
                        viewModel.updateClip(selectedClip.id, persist, transform)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .verticalScroll(rememberScrollState())
                )
            }
        }
    }
}

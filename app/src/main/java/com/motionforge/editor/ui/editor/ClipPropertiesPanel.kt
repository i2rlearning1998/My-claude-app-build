package com.motionforge.editor.ui.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.motionforge.editor.R
import com.motionforge.editor.data.Clip
import com.motionforge.editor.data.OverlayPosition

/** Per-clip filter and text-overlay controls, shown beneath the timeline for the selected clip. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipPropertiesPanel(
    clip: Clip,
    onUpdate: (persist: Boolean, transform: (Clip) -> Clip) -> Unit,
    modifier: Modifier = Modifier
) {
    var tabIndex by remember(clip.id) { mutableIntStateOf(0) }

    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        TabRow(selectedTabIndex = tabIndex) {
            Tab(
                selected = tabIndex == 0,
                onClick = { tabIndex = 0 },
                text = { Text(stringResource(R.string.filters)) }
            )
            Tab(
                selected = tabIndex == 1,
                onClick = { tabIndex = 1 },
                text = { Text(stringResource(R.string.text_overlay)) }
            )
        }

        when (tabIndex) {
            0 -> FiltersTab(clip, onUpdate)
            else -> TextOverlayTab(clip, onUpdate)
        }
    }
}

@Composable
private fun FiltersTab(
    clip: Clip,
    onUpdate: (persist: Boolean, transform: (Clip) -> Clip) -> Unit
) {
    Column {
        LabeledSlider(
            label = stringResource(R.string.brightness),
            value = clip.brightness,
            valueRange = -1f..1f,
            onChange = { v, commit -> onUpdate(commit) { it.copy(brightness = v) } }
        )
        LabeledSlider(
            label = stringResource(R.string.contrast),
            value = clip.contrast,
            valueRange = 0f..2f,
            onChange = { v, commit -> onUpdate(commit) { it.copy(contrast = v) } }
        )
        LabeledSlider(
            label = stringResource(R.string.saturation),
            value = clip.saturation,
            valueRange = 0f..2f,
            onChange = { v, commit -> onUpdate(commit) { it.copy(saturation = v) } }
        )
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onChange: (Float, Boolean) -> Unit
) {
    var local by remember(label, value) { mutableStateOf(value) }
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = "$label: ${"%.2f".format(local)}", style = MaterialTheme.typography.labelMedium)
        Slider(
            value = local,
            valueRange = valueRange,
            onValueChange = {
                local = it
                onChange(it, false)
            },
            onValueChangeFinished = { onChange(local, true) }
        )
    }
}

@Composable
private fun TextOverlayTab(
    clip: Clip,
    onUpdate: (persist: Boolean, transform: (Clip) -> Clip) -> Unit
) {
    var text by remember(clip.id) { mutableStateOf(clip.overlayText) }

    Column {
        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it
                onUpdate(true) { c -> c.copy(overlayText = it) }
            },
            label = { Text(stringResource(R.string.text_overlay)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Row {
            OverlayPosition.entries.forEach { position ->
                FilterChip(
                    selected = clip.overlayPosition == position,
                    onClick = { onUpdate(true) { c -> c.copy(overlayPosition = position) } },
                    label = { Text(position.name) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
    }
}

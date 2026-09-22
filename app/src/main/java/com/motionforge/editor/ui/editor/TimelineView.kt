package com.motionforge.editor.ui.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.motionforge.editor.R
import com.motionforge.editor.data.Clip
import com.motionforge.editor.data.ClipType
import java.util.concurrent.TimeUnit

@Composable
fun TimelineView(
    clips: List<Clip>,
    selectedClipId: String?,
    onSelect: (String) -> Unit,
    onMove: (String, Int) -> Unit,
    onDelete: (String) -> Unit,
    onTrimChange: (String, Long, Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            items(clips, key = { it.id }) { clip ->
                ClipChip(
                    clip = clip,
                    selected = clip.id == selectedClipId,
                    onClick = { onSelect(clip.id) }
                )
            }
        }

        val selected = clips.firstOrNull { it.id == selectedClipId }
        if (selected != null) {
            SelectedClipControls(
                clip = selected,
                canMoveLeft = clips.indexOf(selected) > 0,
                canMoveRight = clips.indexOf(selected) < clips.lastIndex,
                onMoveLeft = { onMove(selected.id, -1) },
                onMoveRight = { onMove(selected.id, 1) },
                onDelete = { onDelete(selected.id) },
                onTrimChange = onTrimChange
            )
        }
    }
}

@Composable
private fun ClipChip(
    clip: Clip,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    Column(
        modifier = Modifier
            .width(96.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(BorderStroke(2.dp, borderColor), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (clip.type == ClipType.VIDEO) Icons.Filled.Videocam else Icons.Filled.Image,
                contentDescription = null
            )
        }
        Text(
            text = formatDuration(clip.trimmedDurationMs),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun SelectedClipControls(
    clip: Clip,
    canMoveLeft: Boolean,
    canMoveRight: Boolean,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onDelete: () -> Unit,
    onTrimChange: (String, Long, Long, Boolean) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onMoveLeft, enabled = canMoveLeft) {
                Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.move_left))
            }
            IconButton(onClick = onMoveRight, enabled = canMoveRight) {
                Icon(Icons.Filled.ArrowForward, contentDescription = stringResource(R.string.move_right))
            }
            Box(modifier = Modifier.size(1.dp))
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete_clip))
            }
        }

        Text(text = stringResource(R.string.trim), style = MaterialTheme.typography.labelLarge)
        var range by remember(clip.id) {
            mutableStateOf(clip.trimStartMs.toFloat()..clip.trimEndMs.toFloat())
        }
        RangeSlider(
            value = range,
            onValueChange = { newRange ->
                range = newRange
                onTrimChange(clip.id, newRange.start.toLong(), newRange.endInclusive.toLong(), false)
            },
            onValueChangeFinished = {
                onTrimChange(clip.id, range.start.toLong(), range.endInclusive.toLong(), true)
            },
            valueRange = 0f..clip.sourceDurationMs.toFloat().coerceAtLeast(1f)
        )
        Text(
            text = "${formatDuration(range.start.toLong())} – ${formatDuration(range.endInclusive.toLong())}",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(ms.coerceAtLeast(0))
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

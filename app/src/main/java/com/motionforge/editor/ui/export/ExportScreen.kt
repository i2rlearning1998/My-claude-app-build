package com.motionforge.editor.ui.export

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.motionforge.editor.R
import com.motionforge.editor.editing.VideoExporter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    viewModel: ExportViewModel,
    onBack: () -> Unit
) {
    val project by viewModel.project.collectAsState()
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.export)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (val current = state) {
                is VideoExporter.ExportState.Idle -> {
                    Text(project?.name.orEmpty(), style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${project?.clips?.size ?: 0} clips",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    Button(onClick = { viewModel.startExport() }) {
                        Text(stringResource(R.string.export))
                    }
                }

                is VideoExporter.ExportState.InProgress -> {
                    CircularProgressIndicator(progress = current.percent / 100f)
                    Text(
                        "${stringResource(R.string.exporting)} ${current.percent}%",
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    TextButton(
                        onClick = { viewModel.cancelExport() },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }

                is VideoExporter.ExportState.Success -> {
                    Text(stringResource(R.string.export_done), style = MaterialTheme.typography.titleMedium)
                    Row(modifier = Modifier.padding(top = 16.dp)) {
                        Button(onClick = {
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                current.outputFile
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "video/mp4"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)))
                        }) {
                            Text(stringResource(R.string.share))
                        }
                    }
                }

                is VideoExporter.ExportState.Failure -> {
                    Text(stringResource(R.string.export_failed), style = MaterialTheme.typography.titleMedium)
                    Text(current.message, style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { viewModel.startExport() }, modifier = Modifier.padding(top = 16.dp)) {
                        Text(stringResource(R.string.export))
                    }
                }
            }
        }
    }
}

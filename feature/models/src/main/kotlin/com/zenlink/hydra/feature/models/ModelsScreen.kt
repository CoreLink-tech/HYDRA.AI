package com.zenlink.hydra.feature.models

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zenlink.hydra.core.designsystem.HydraCard
import com.zenlink.hydra.core.designsystem.HydraKeyValue
import com.zenlink.hydra.core.designsystem.HydraProgress
import com.zenlink.hydra.core.designsystem.HydraSecondaryButton
import com.zenlink.hydra.core.designsystem.HydraSectionTitle
import com.zenlink.hydra.core.designsystem.HydraTag
import com.zenlink.hydra.core.download.StoragePolicy
import com.zenlink.hydra.core.model.ByteFormat
import com.zenlink.hydra.core.model.ModelState

/** Catalogue of models with download, progress, storage, delete and switch controls. */
@Composable
fun ModelsScreen(viewModel: ModelsViewModel) {
    val state by viewModel.state.collectAsState()
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Models", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Stored on this phone: ${ByteFormat.human(state.installedBytes)}" +
                    if (state.wifiOnly) ". Downloads wait for Wi-Fi." else ".",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            state.notice?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(Modifier.height(4.dp))
            HydraSectionTitle("Catalogue")
        }
        items(state.rows, key = { it.descriptor.id }) { row ->
            ModelRowCard(
                row = row,
                onDownload = { viewModel.download(row.descriptor.id) },
                onCancel = { viewModel.cancel(row.descriptor.id) },
                onDelete = { viewModel.delete(row.descriptor.id) },
                onSwitch = { viewModel.switchTo(row.descriptor.id) },
            )
        }
    }
}

@Composable
private fun ModelRowCard(
    row: ModelRow,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onSwitch: () -> Unit,
) {
    val d = row.descriptor
    HydraCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(d.displayName, style = MaterialTheme.typography.titleMedium)
            if (row.recommended) HydraTag("Recommended for this phone")
            if (row.isActive) HydraTag("In use")
        }
        Text(d.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        HydraKeyValue("Size", ByteFormat.human(d.sizeBytes))
        HydraKeyValue("Status", stateLabel(row))
        if (row.state == ModelState.INSTALLED && row.localPath != null) {
            HydraKeyValue("On this phone", ByteFormat.human(d.installedSizeBytes))
        }
        when (row.storage) {
            StoragePolicy.Verdict.BLOCKED_INSUFFICIENT ->
                Text("Not enough free storage for this model.", color = MaterialTheme.colorScheme.error)
            StoragePolicy.Verdict.WARN_LOW_SPACE ->
                Text("Storage will be low after download.", color = MaterialTheme.colorScheme.tertiary)
            StoragePolicy.Verdict.OK -> Unit
        }
        row.lastError?.takeIf { row.state == ModelState.FAILED }?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        row.progress?.let { p ->
            Spacer(Modifier.height(8.dp))
            HydraProgress(p)
        }
        Spacer(Modifier.height(8.dp))
        if (row.state == ModelState.DOWNLOADING) {
            HydraSecondaryButton("Cancel download", onCancel)
        } else if (row.canDownload) {
            HydraSecondaryButton(if (row.state == ModelState.PAUSED) "Resume download" else "Download", onDownload)
        }
        if (row.canSwitch) HydraSecondaryButton("Use this model", onSwitch)
        if (row.canDelete) HydraSecondaryButton("Delete file", onDelete)
    }
}

private fun stateLabel(row: ModelRow): String = when (row.state) {
    ModelState.NOT_DOWNLOADED -> "Not downloaded"
    ModelState.DOWNLOADING -> "Downloading"
    ModelState.PAUSED -> "Paused"
    ModelState.VERIFYING -> "Checking file"
    ModelState.INSTALLED -> if (row.isActive) "Ready, in use" else "Ready"
    ModelState.FAILED -> "Failed"
}

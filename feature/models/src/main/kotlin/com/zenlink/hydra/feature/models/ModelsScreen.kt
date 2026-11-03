package com.zenlink.hydra.feature.models

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zenlink.hydra.core.designsystem.HydraBanner
import com.zenlink.hydra.core.designsystem.HydraCard
import com.zenlink.hydra.core.designsystem.HydraCenteredContent
import com.zenlink.hydra.core.designsystem.HydraDangerButton
import com.zenlink.hydra.core.designsystem.HydraDialog
import com.zenlink.hydra.core.designsystem.HydraDivider
import com.zenlink.hydra.core.designsystem.HydraIcons
import com.zenlink.hydra.core.designsystem.HydraKeyValue
import com.zenlink.hydra.core.designsystem.HydraPrimaryButton
import com.zenlink.hydra.core.designsystem.HydraProgress
import com.zenlink.hydra.core.designsystem.HydraSecondaryButton
import com.zenlink.hydra.core.designsystem.HydraSectionTitle
import com.zenlink.hydra.core.designsystem.HydraTag
import com.zenlink.hydra.core.designsystem.HydraTextButton
import com.zenlink.hydra.core.designsystem.HydraTone
import com.zenlink.hydra.core.designsystem.HydraTopBar
import com.zenlink.hydra.core.designsystem.hydra
import com.zenlink.hydra.core.download.StoragePolicy
import com.zenlink.hydra.core.designsystem.HydraChoiceDialog
import com.zenlink.hydra.core.designsystem.HydraChoice
import com.zenlink.hydra.core.model.DownloadNetwork
import com.zenlink.hydra.core.model.ByteFormat
import com.zenlink.hydra.core.model.ModelState
import java.util.Locale

/**
 * Model catalogue. Rows are grouped by what the user can do with them: use what is installed,
 * watch what is downloading, pick something new, and see what this phone cannot run.
 */
@Composable
fun ModelsScreen(viewModel: ModelsViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var expandedId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<ModelRow?>(null) }
    var pendingDownload by remember { mutableStateOf<ModelRow?>(null) }

    val installed = state.rows.filter { it.state == ModelState.INSTALLED }
    val downloading = state.rows.filter {
        it.state == ModelState.DOWNLOADING || it.state == ModelState.PAUSED || it.state == ModelState.VERIFYING ||
            (it.progress != null && it.state != ModelState.INSTALLED)
    }
    val rest = state.rows - installed.toSet() - downloading.toSet()
    val unavailable = rest.filter { it.fitsDevice == false }
    val available = rest - unavailable.toSet()

    HydraCenteredContent {
        HydraTopBar(
            title = "Models",
            subtitle = buildString {
                append("${installed.size} installed · ${ByteFormat.human(state.installedBytes)} on this phone")
            },
        )
        HydraDivider()
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.notice?.let { note ->
                item(key = "notice") {
                    HydraBanner(
                        text = note,
                        tone = HydraTone.Danger,
                        icon = HydraIcons.Error,
                        onDismiss = viewModel::dismissNotice,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
            section("Installed", installed, expandedId, { expandedId = if (expandedId == it) null else it }, viewModel, { pendingDownload = it }) { pendingDelete = it }
            section("Downloading", downloading, expandedId, { expandedId = if (expandedId == it) null else it }, viewModel, { pendingDownload = it }) { pendingDelete = it }
            section("Available", available, expandedId, { expandedId = if (expandedId == it) null else it }, viewModel, { pendingDownload = it }) { pendingDelete = it }
            section("Not available on this phone", unavailable, expandedId, { expandedId = if (expandedId == it) null else it }, viewModel, { pendingDownload = it }) { pendingDelete = it }
        }
    }

    pendingDownload?.let { row ->
        HydraChoiceDialog(
            title = "Download over which connection?",
            message = "${row.descriptor.displayName} is ${ByteFormat.human(row.descriptor.sizeBytes)}.",
            choices = DownloadNetwork.entries.map { HydraChoice(it, it.label, it.detail) },
            initial = state.network,
            confirmLabel = "Download",
            onConfirm = {
                viewModel.download(row.descriptor.id, it)
                pendingDownload = null
            },
            onDismiss = { pendingDownload = null },
        )
    }

    pendingDelete?.let { row ->
        HydraDialog(
            title = "Delete ${row.descriptor.displayName}?",
            onDismiss = { pendingDelete = null },
            confirmLabel = "Delete",
            destructive = true,
            onConfirm = {
                viewModel.delete(row.descriptor.id)
                pendingDelete = null
            },
        ) {
            Text("The model file is removed from this phone and frees ${ByteFormat.human(row.descriptor.installedSizeBytes)}. You can download it again later.")
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.section(
    title: String,
    rows: List<ModelRow>,
    expandedId: String?,
    onToggle: (String) -> Unit,
    viewModel: ModelsViewModel,
    onAskDownload: (ModelRow) -> Unit,
    onAskDelete: (ModelRow) -> Unit,
) {
    if (rows.isEmpty()) return
    item(key = "title-$title") { HydraSectionTitle(title, Modifier.padding(top = 8.dp)) }
    items(rows, key = { it.descriptor.id }) { row ->
        ModelCard(
            row = row,
            expanded = expandedId == row.descriptor.id,
            onToggle = { onToggle(row.descriptor.id) },
            onDownload = { onAskDownload(row) },
            onCancel = { viewModel.cancel(row.descriptor.id) },
            onSwitch = { viewModel.switchTo(row.descriptor.id) },
            onDelete = { onAskDelete(row) },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ModelCard(
    row: ModelRow,
    expanded: Boolean,
    onToggle: () -> Unit,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onSwitch: () -> Unit,
    onDelete: () -> Unit,
) {
    val c = MaterialTheme.hydra
    val d = row.descriptor
    val tooBig = row.fitsDevice == false
    val quant = quantizationOf(d.fileName)

    HydraCard(highlighted = row.isActive) {
        Text(d.displayName, style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
        Spacer(Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (row.isActive) HydraTag("In use")
            if (row.recommended) HydraTag("Recommended for this phone")
            if (d.requiresPro) HydraTag("Pro", tone = HydraTone.Neutral)
            if (tooBig) HydraTag("Too large for this phone", tone = HydraTone.Danger)
        }
        Spacer(Modifier.height(10.dp))
        Text(d.description, style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        Spacer(Modifier.height(12.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            HydraTag("${ByteFormat.human(d.sizeBytes)} download", tone = HydraTone.Neutral)
            quant?.let { HydraTag(it, tone = HydraTone.Neutral) }
            HydraTag("${ramText(d.minRamMb)} RAM or more", tone = HydraTone.Neutral)
        }

        // Status line.
        Spacer(Modifier.height(12.dp))
        StatusLine(row)

        when (row.storage) {
            StoragePolicy.Verdict.BLOCKED_INSUFFICIENT ->
                Text("Not enough free storage for this model.", style = MaterialTheme.typography.bodySmall, color = c.danger, modifier = Modifier.padding(top = 6.dp))
            StoragePolicy.Verdict.WARN_LOW_SPACE ->
                Text("Storage will be low after download.", style = MaterialTheme.typography.bodySmall, color = c.warning, modifier = Modifier.padding(top = 6.dp))
            StoragePolicy.Verdict.OK -> Unit
        }
        row.lastError?.takeIf { row.state == ModelState.FAILED }?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = c.danger, modifier = Modifier.padding(top = 6.dp))
        }
        row.progress?.let { p ->
            Spacer(Modifier.height(10.dp))
            HydraProgress(p)
        }

        // Actions.
        val allowDownload = row.canDownload && !tooBig
        Spacer(Modifier.height(14.dp))
        if (row.state == ModelState.DOWNLOADING || (row.progress != null && row.state != ModelState.PAUSED && row.state != ModelState.INSTALLED)) {
            HydraSecondaryButton("Cancel download", onCancel)
        } else if (allowDownload) {
            HydraPrimaryButton(
                text = when (row.state) {
                    ModelState.PAUSED -> "Resume download"
                    ModelState.FAILED -> "Try again"
                    else -> "Download · ${ByteFormat.human(d.sizeBytes)}"
                },
                onClick = onDownload,
                icon = HydraIcons.Download,
                enabled = row.storage != StoragePolicy.Verdict.BLOCKED_INSUFFICIENT,
            )
        }
        if (row.canSwitch) HydraPrimaryButton("Use this model", onSwitch, icon = HydraIcons.Check)
        if (row.canDelete) {
            Spacer(Modifier.height(8.dp))
            HydraDangerButton("Delete from this phone", onDelete, icon = HydraIcons.Delete)
        } else if (row.state == ModelState.INSTALLED && row.isActive) {
            Text(
                "Switch to another model before deleting this one.",
                style = MaterialTheme.typography.bodySmall,
                color = c.textMuted,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.Center) {
            HydraTextButton(if (expanded) "Hide details" else "Details", onToggle)
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(tween(260)) + fadeIn(tween(200)),
            exit = shrinkVertically(tween(200)) + fadeOut(tween(120)),
        ) {
            Column {
                HydraDivider(Modifier.padding(bottom = 10.dp))
                HydraKeyValue("Publisher", d.publisher)
                HydraKeyValue("Download size", ByteFormat.human(d.sizeBytes))
                HydraKeyValue("Space once installed", ByteFormat.human(d.installedSizeBytes))
                quant?.let { HydraKeyValue("Quantization", it) }
                HydraKeyValue("Memory needed", "${ramText(d.minRamMb)} RAM")
                HydraKeyValue("Free storage needed", ByteFormat.human(d.minFreeStorageMb * 1024L * 1024L))
                HydraKeyValue("Context window", "${d.contextTokens} tokens")
                HydraKeyValue("Minimum Android", "API ${d.minAndroidSdk}")
                HydraKeyValue("Licence", d.license)
                BulletList("Strengths", d.strengths)
                BulletList("Limitations", d.limitations)
            }
        }
    }
}

@Composable
private fun StatusLine(row: ModelRow) {
    val c = MaterialTheme.hydra
    val d = row.descriptor
    val (text, color) = when (row.state) {
        ModelState.INSTALLED -> (if (row.isActive) "Ready · in use" else "Ready") + " · ${ByteFormat.human(d.installedSizeBytes)} on this phone" to c.success
        ModelState.DOWNLOADING -> {
            val pct = row.progress?.let { " · ${(it * 100).toInt()}%" }.orEmpty()
            "Downloading$pct" to c.accent
        }
        ModelState.PAUSED -> {
            val pct = row.progress?.let { " · ${(it * 100).toInt()}%" }.orEmpty()
            "Paused$pct" to c.warning
        }
        ModelState.VERIFYING -> "Checking the file" to c.accent
        ModelState.FAILED -> "Download failed" to c.danger
        ModelState.NOT_DOWNLOADED ->
            if (row.progress != null) "Downloading · ${((row.progress) * 100).toInt()}%" to c.accent
            else "Not downloaded" to c.textSecondary
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (row.state == ModelState.INSTALLED) {
            Icon(HydraIcons.CheckCircle, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}

@Composable
private fun BulletList(title: String, items: List<String>) {
    if (items.isEmpty()) return
    val c = MaterialTheme.hydra
    Text(title, style = MaterialTheme.typography.labelLarge, color = c.textPrimary, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
    items.forEach {
        Row(Modifier.padding(vertical = 2.dp)) {
            Text("•", style = MaterialTheme.typography.bodyMedium, color = c.textMuted, modifier = Modifier.width(16.dp))
            Text(it, style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        }
    }
}

/** 3072 becomes "3 GB", 6144 becomes "6 GB". */
private fun ramText(mb: Int): String = String.format(Locale.US, "%.0f GB", mb / 1024f)

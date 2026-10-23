package com.zenlink.hydra.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zenlink.hydra.core.designsystem.HydraCard
import com.zenlink.hydra.core.designsystem.HydraPrimaryButton
import com.zenlink.hydra.core.designsystem.HydraSecondaryButton

/** Settings: downloads, sharing, subscription, storage and delete-all. Rules live in [SettingsPlanner]. */
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, subscription: SettingsPlanner.SubscriptionStatus, shareCapacity: String) {
    val state by viewModel.state.collectAsState()
    var confirmText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)

        HydraCard {
            Text("Downloads", style = MaterialTheme.typography.titleMedium)
            ToggleRow(
                title = "Wi-Fi only downloads",
                checked = state.settings.wifiOnlyDownloads,
                onChange = viewModel::setWifiOnly,
            )
            SettingsPlanner.wifiOnlyWarning(state.settings.wifiOnlyDownloads)?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        }

        HydraCard {
            Text("Subscription", style = MaterialTheme.typography.titleMedium)
            Text(subscription.title, style = MaterialTheme.typography.bodyLarge)
            Text(subscription.detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        HydraCard {
            Text("Sharing", style = MaterialTheme.typography.titleMedium)
            Text(shareCapacity, style = MaterialTheme.typography.bodyMedium)
            ToggleRow(
                title = "Send crash reports",
                checked = state.settings.crashReportingOptIn,
                onChange = viewModel::setCrashReporting,
            )
        }

        HydraCard {
            Text("Delete all data", style = MaterialTheme.typography.titleMedium)
            Text(
                "Removes chat history, every downloaded model and your local settings. Live share links are revoked.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            HydraSecondaryButton("Delete all data", onClick = viewModel::openDeleteConfirm)
        }

        state.notice?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
    }

    if (state.deleteConfirmOpen) {
        AlertDialog(
            onDismissRequest = viewModel::closeDeleteConfirm,
            title = { Text("Delete all data?") },
            text = {
                Column {
                    SettingsPlanner.deletionSteps().forEach { Text("- $it") }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmText,
                        onValueChange = { confirmText = it },
                        label = { Text("Type DELETE to confirm") },
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = SettingsPlanner.deletionAllowed(confirmText),
                    onClick = { viewModel.deleteAll(onDone = { confirmText = "" }) },
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = viewModel::closeDeleteConfirm) { Text("Cancel") } },
        )
    }
}

@Composable
private fun ToggleRow(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

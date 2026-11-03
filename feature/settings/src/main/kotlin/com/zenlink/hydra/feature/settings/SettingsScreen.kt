package com.zenlink.hydra.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zenlink.hydra.core.designsystem.HydraBanner
import com.zenlink.hydra.core.designsystem.HydraCenteredContent
import com.zenlink.hydra.core.designsystem.HydraDangerButton
import com.zenlink.hydra.core.designsystem.HydraDialog
import com.zenlink.hydra.core.designsystem.HydraDivider
import com.zenlink.hydra.core.designsystem.HydraGroup
import com.zenlink.hydra.core.designsystem.HydraIcons
import com.zenlink.hydra.core.designsystem.HydraListRow
import com.zenlink.hydra.core.designsystem.HydraSectionTitle
import com.zenlink.hydra.core.designsystem.HydraSegmented
import com.zenlink.hydra.core.designsystem.HydraSwitchRow
import com.zenlink.hydra.core.designsystem.HydraTag
import com.zenlink.hydra.core.designsystem.HydraTextField
import com.zenlink.hydra.core.designsystem.HydraTone
import com.zenlink.hydra.core.designsystem.HydraTopBar
import com.zenlink.hydra.core.designsystem.ThemeMode
import com.zenlink.hydra.core.designsystem.hydra
import com.zenlink.hydra.core.model.ByteFormat
import com.zenlink.hydra.core.model.DownloadNetwork
import com.zenlink.hydra.core.model.PerformanceMode

/**
 * Settings. Every control here is wired to something that works today; features that are not
 * built yet (sharing, buying Pro) say so plainly instead of showing a button that does nothing.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    subscription: SettingsPlanner.SubscriptionStatus,
    shareCapacity: String,
    onOpenModels: () -> Unit,
    appVersion: String,
    privacyPolicyUrl: String?,
    onOpenUrl: (String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmText by remember { mutableStateOf("") }
    val c = MaterialTheme.hydra

    HydraCenteredContent {
        HydraTopBar(title = "Settings")
        HydraDivider()
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            state.notice?.let {
                HydraBanner(
                    text = it,
                    tone = HydraTone.Accent,
                    icon = HydraIcons.CheckCircle,
                    onDismiss = viewModel::dismissNotice,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }

            // ---- Appearance
            HydraSectionTitle("Appearance")
            HydraGroup {
                Column(Modifier.padding(16.dp)) {
                    Text("Theme", style = MaterialTheme.typography.bodyLarge, color = c.textPrimary)
                    Text(
                        "System follows your phone's dark mode setting.",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
                    )
                    HydraSegmented(
                        options = listOf(
                            ThemeMode.SYSTEM to "System",
                            ThemeMode.LIGHT to "Light",
                            ThemeMode.DARK to "Dark",
                        ),
                        selected = ThemeMode.fromKey(state.settings.themeMode),
                        onSelect = { viewModel.setThemeMode(it.key) },
                    )
                }
            }

            // ---- AI and models
            HydraSectionTitle("AI and models")
            HydraGroup {
                HydraListRow(
                    title = "Current model",
                    subtitle = state.activeModelName ?: "None selected. Choose one in Models.",
                    icon = HydraIcons.Models,
                    onClick = onOpenModels,
                )
                HydraDivider()
                Column(Modifier.padding(16.dp)) {
                    Text("Response style", style = MaterialTheme.typography.bodyLarge, color = c.textPrimary)
                    Text(
                        when (state.settings.performanceMode) {
                            PerformanceMode.COOL -> "Cool uses fewer processor threads and shorter replies, so the phone stays cooler and the battery lasts longer."
                            PerformanceMode.STANDARD -> "Standard uses more processor threads and allows longer replies."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
                    )
                    HydraSegmented(
                        options = listOf(
                            PerformanceMode.COOL to "Cool",
                            PerformanceMode.STANDARD to "Standard",
                        ),
                        selected = state.settings.performanceMode,
                        onSelect = viewModel::setPerformanceMode,
                    )
                }
            }

            // ---- Privacy
            HydraSectionTitle("Privacy")
            HydraGroup {
                HydraListRow(
                    title = "Conversations stay on this phone",
                    subtitle = "Chats are stored in an encrypted database on this device. Replies are generated here, not on a server.",
                    icon = HydraIcons.Lock,
                )
                HydraDivider()
                HydraSwitchRow(
                    title = "Send crash reports",
                    subtitle = "Technical details only. Your messages are never included.",
                    checked = state.settings.crashReportingOptIn,
                    onChange = viewModel::setCrashReporting,
                )
                HydraDivider()
                HydraListRow(
                    title = "Encrypted sharing",
                    subtitle = if (shareCapacity.isNotBlank()) shareCapacity else "Not available in this version yet.",
                    icon = HydraIcons.Share,
                    trailing = if (shareCapacity.isBlank()) ({ HydraTag("Coming soon", tone = HydraTone.Neutral) }) else null,
                )
                if (!privacyPolicyUrl.isNullOrBlank()) {
                    HydraDivider()
                    HydraListRow(
                        title = "Privacy policy",
                        icon = HydraIcons.Shield,
                        onClick = { onOpenUrl(privacyPolicyUrl) },
                    )
                }
            }

            // ---- Downloads
            HydraSectionTitle("Downloads")
            HydraGroup {
                Column(Modifier.padding(16.dp)) {
                    Text("Download connection", style = MaterialTheme.typography.bodyLarge, color = c.textPrimary)
                    Text(
                        "You are asked each time you download a model. This is the option that is preselected. ${state.settings.downloadNetwork.detail}",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
                    )
                    HydraSegmented(
                        options = listOf(
                            DownloadNetwork.WIFI_ONLY to "Wi-Fi",
                            DownloadNetwork.MOBILE_ONLY to "Mobile",
                            DownloadNetwork.ANY to "Both",
                        ),
                        selected = state.settings.downloadNetwork,
                        onSelect = viewModel::setDownloadNetwork,
                    )
                }
                SettingsPlanner.networkWarning(state.settings.downloadNetwork)?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = c.warning,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    )
                }
                HydraDivider()
                HydraListRow(
                    title = "Models on this phone",
                    subtitle = "${ByteFormat.human(state.installedBytes)} used · ${ByteFormat.human(state.freeBytes)} free",
                    icon = HydraIcons.Storage,
                    onClick = onOpenModels,
                )
            }

            // ---- Subscription
            HydraSectionTitle("Subscription")
            HydraGroup {
                HydraListRow(
                    title = "${subscription.title} plan",
                    subtitle = subscription.detail,
                    icon = HydraIcons.Bolt,
                    trailing = { HydraTag(if (subscription.proActive) "Active" else "Current", tone = if (subscription.proActive) HydraTone.Accent else HydraTone.Neutral) },
                )
                if (!subscription.proActive) {
                    HydraDivider()
                    HydraListRow(
                        title = "Upgrade to Pro",
                        subtitle = "Pro will be sold through Google Play. It is not available to buy in this version yet.",
                        icon = HydraIcons.Info,
                    )
                }
            }

            // ---- About
            HydraSectionTitle("About")
            HydraGroup {
                HydraListRow(
                    title = "HYDRA.AI",
                    subtitle = "Version $appVersion · by ZenLink Technologies",
                    icon = HydraIcons.Info,
                )
            }

            // ---- Delete
            HydraSectionTitle("Your data")
            HydraGroup {
                Column(Modifier.padding(16.dp)) {
                    Text("Delete all data", style = MaterialTheme.typography.bodyLarge, color = c.textPrimary)
                    Text(
                        "Removes chat history, every downloaded model and your local settings. Live share links are revoked.",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
                    )
                    HydraDangerButton("Delete all data", onClick = viewModel::openDeleteConfirm, icon = HydraIcons.Delete)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (state.deleteConfirmOpen) {
        HydraDialog(
            title = "Delete all data?",
            onDismiss = {
                confirmText = ""
                viewModel.closeDeleteConfirm()
            },
            confirmLabel = "Delete everything",
            destructive = true,
            confirmEnabled = SettingsPlanner.deletionAllowed(confirmText),
            onConfirm = { viewModel.deleteAll(onDone = { confirmText = "" }) },
        ) {
            SettingsPlanner.deletionSteps().forEach { Text("• $it") }
            HydraTextField(
                value = confirmText,
                onValueChange = { confirmText = it },
                label = "Type DELETE to confirm",
            )
        }
    }
}

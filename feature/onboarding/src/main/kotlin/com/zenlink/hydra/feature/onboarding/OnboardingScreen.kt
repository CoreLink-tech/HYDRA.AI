package com.zenlink.hydra.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zenlink.hydra.core.designsystem.HydraCard
import com.zenlink.hydra.core.designsystem.HydraPrimaryButton
import com.zenlink.hydra.core.designsystem.HydraProgress
import com.zenlink.hydra.core.designsystem.HydraSecondaryButton
import com.zenlink.hydra.core.designsystem.HydraTag
import com.zenlink.hydra.core.model.ByteFormat
import com.zenlink.hydra.core.model.ModelCatalog
import com.zenlink.hydra.core.model.ModelDescriptor

/** Welcome, device check, recommended model, download, first chat. Mirrors [OnboardingStep]. */
@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel) {
    val state by viewModel.state.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (state.step) {
            OnboardingStep.WELCOME -> {
                Text("Hydra runs an AI assistant on your phone.", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Chats stay on this device. Once a model is downloaded, you can use Hydra with no connection.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                HydraPrimaryButton("Check my phone", onClick = viewModel::begin)
            }
            OnboardingStep.DEVICE_CHECK -> {
                Text("Checking your phone", style = MaterialTheme.typography.headlineSmall)
                HydraProgress(null)
                // Runs once the step is shown; the view-model only changes step after the check.
                androidx.compose.runtime.LaunchedEffect(Unit) { viewModel.runDeviceCheck() }
            }
            OnboardingStep.RECOMMENDED -> RecommendedStep(state, viewModel)
            OnboardingStep.DOWNLOADING -> {
                val d = OnboardingFlow.selectedDescriptor(state)
                Text("Downloading ${d?.displayName.orEmpty()}", style = MaterialTheme.typography.headlineSmall)
                HydraProgress(state.downloadFraction)
                Text(
                    "${(state.downloadFraction * 100).toInt()}%. You can leave this screen; the download continues.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OnboardingStep.FIRST_CHAT -> {
                Text("Your assistant is ready.", style = MaterialTheme.typography.headlineSmall)
                Text("Ask it anything to start your first chat.", style = MaterialTheme.typography.bodyLarge)
                HydraPrimaryButton("Start first chat", onClick = viewModel::finish)
            }
            OnboardingStep.DONE -> Unit
        }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

@Composable
private fun RecommendedStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    val rec = state.recommendation
    Text("Pick a model", style = MaterialTheme.typography.headlineSmall)
    rec?.reasons?.forEach { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    ModelCatalog.all.forEach { d ->
        ModelChoice(
            descriptor = d,
            fits = rec?.fitting?.contains(d.tier) == true,
            recommended = rec?.recommended == d.tier,
            chosen = state.chosenModelId == d.id,
            onChoose = { viewModel.choose(d.id) },
        )
    }
    Spacer(Modifier.height(8.dp))
    HydraPrimaryButton("Download and continue", onClick = viewModel::download, enabled = state.chosenModelId != null)
}

@Composable
private fun ModelChoice(
    descriptor: ModelDescriptor,
    fits: Boolean,
    recommended: Boolean,
    chosen: Boolean,
    onChoose: () -> Unit,
) {
    HydraCard {
        Text(descriptor.displayName, style = MaterialTheme.typography.titleMedium)
        Text(
            "${ByteFormat.human(descriptor.sizeBytes)} download. ${descriptor.description}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        if (recommended) HydraTag("Recommended for this phone")
        if (!fits) Text("Too large for this phone.", color = MaterialTheme.colorScheme.error)
        if (fits) {
            if (chosen) HydraTag("Selected")
            else HydraSecondaryButton("Choose ${descriptor.displayName}", onChoose)
        }
    }
}

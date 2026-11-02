package com.zenlink.hydra.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zenlink.hydra.core.designsystem.HydraBanner
import com.zenlink.hydra.core.designsystem.HydraCard
import com.zenlink.hydra.core.designsystem.HydraCenteredContent
import com.zenlink.hydra.core.designsystem.HydraIcons
import com.zenlink.hydra.core.designsystem.HydraKeyValue
import com.zenlink.hydra.core.designsystem.HydraMark
import com.zenlink.hydra.core.designsystem.HydraMotion
import com.zenlink.hydra.core.designsystem.HydraPrimaryButton
import com.zenlink.hydra.core.designsystem.HydraProgressRing
import com.zenlink.hydra.core.designsystem.HydraShapes
import com.zenlink.hydra.core.designsystem.HydraSpinner
import com.zenlink.hydra.core.designsystem.HydraTag
import com.zenlink.hydra.core.designsystem.HydraTextButton
import com.zenlink.hydra.core.designsystem.HydraTone
import com.zenlink.hydra.core.designsystem.LocalReducedMotion
import com.zenlink.hydra.core.designsystem.hydra
import com.zenlink.hydra.core.model.ByteFormat
import com.zenlink.hydra.core.model.DeviceProfile
import com.zenlink.hydra.core.model.ModelCatalog
import com.zenlink.hydra.core.model.ModelDescriptor
import java.util.Locale

private const val TOTAL_STEPS = 5
private val stepNames = listOf("Welcome", "Your phone", "Choose a model", "Download", "Ready")

private fun stepNumber(step: OnboardingStep): Int = when (step) {
    OnboardingStep.WELCOME -> 1
    OnboardingStep.DEVICE_CHECK -> 2
    OnboardingStep.RECOMMENDED -> 3
    OnboardingStep.DOWNLOADING -> 4
    OnboardingStep.FIRST_CHAT, OnboardingStep.DONE -> 5
}

/**
 * Welcome and how it works, device check, model choice, download, first chat. The steps mirror
 * [OnboardingStep]; the device facts and model fit shown here come from the real device check.
 */
@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var introPage by rememberSaveable { mutableIntStateOf(0) }
    val reduced = LocalReducedMotion.current

    HydraCenteredContent(maxWidth = 560.dp) {
        StepHeader(current = stepNumber(state.step))
        AnimatedContent(
            targetState = state.step to (if (state.step == OnboardingStep.WELCOME) introPage else 0),
            modifier = Modifier.weight(1f).fillMaxWidth(),
            transitionSpec = {
                if (reduced) {
                    EnterTransition.None togetherWith ExitTransition.None
                } else {
                    (fadeIn(tween(HydraMotion.Medium, delayMillis = 60)) +
                        slideInHorizontally(tween(HydraMotion.Medium)) { it / 12 }) togetherWith
                        fadeOut(tween(HydraMotion.Short))
                }
            },
            label = "onboardingStep",
        ) { (step, page) ->
            when (step) {
                OnboardingStep.WELCOME ->
                    if (page == 0) WelcomePage(onContinue = { introPage = 1 })
                    else HowItWorksPage(onBack = { introPage = 0 }, onContinue = viewModel::begin)
                OnboardingStep.DEVICE_CHECK -> CheckingPage(onStart = viewModel::runDeviceCheck)
                OnboardingStep.RECOMMENDED -> ChooseModelPage(state, viewModel)
                OnboardingStep.DOWNLOADING -> DownloadingPage(state)
                OnboardingStep.FIRST_CHAT, OnboardingStep.DONE -> ReadyPage(state, onStart = viewModel::finish)
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Header and page frame
// ---------------------------------------------------------------------------------------------

@Composable
private fun StepHeader(current: Int) {
    val c = MaterialTheme.hydra
    Column(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 24.dp, vertical = 16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(TOTAL_STEPS) { index ->
                val color by animateColorAsState(
                    if (index < current) c.accent else c.border,
                    tween(HydraMotion.Medium),
                    label = "stepBar$index",
                )
                Box(Modifier.weight(1f).height(4.dp).clip(HydraShapes.tag).background(color))
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "Step $current of $TOTAL_STEPS · ${stepNames[current - 1]}",
            style = MaterialTheme.typography.labelMedium,
            color = c.textSecondary,
        )
    }
}

/** A scrolling body with a footer that stays pinned above the navigation bar. */
@Composable
private fun Page(
    footer: @Composable () -> Unit,
    body: @Composable () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        ) { body() }
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 24.dp, vertical = 16.dp)) { footer() }
    }
}

// ---------------------------------------------------------------------------------------------
// Steps
// ---------------------------------------------------------------------------------------------

@Composable
private fun WelcomePage(onContinue: () -> Unit) {
    val c = MaterialTheme.hydra
    Page(footer = { HydraPrimaryButton("Continue", onContinue) }) {
        Spacer(Modifier.height(32.dp))
        HydraMark(markSize = 72.dp)
        Spacer(Modifier.height(28.dp))
        Text("Welcome to HYDRA.AI", style = MaterialTheme.typography.displaySmall, color = c.textPrimary)
        Spacer(Modifier.height(12.dp))
        Text(
            "An AI assistant that runs on your phone. Chats stay on this device, and once a model is downloaded you can use it with no connection.",
            style = MaterialTheme.typography.bodyLarge,
            color = c.textSecondary,
        )
    }
}

@Composable
private fun HowItWorksPage(onBack: () -> Unit, onContinue: () -> Unit) {
    val c = MaterialTheme.hydra
    Page(
        footer = {
            HydraPrimaryButton("Check my phone", onContinue)
            HydraTextButton("Back", onBack, Modifier.fillMaxWidth())
        },
    ) {
        Spacer(Modifier.height(8.dp))
        Text("How private AI works", style = MaterialTheme.typography.headlineMedium, color = c.textPrimary)
        Spacer(Modifier.height(20.dp))
        Fact(HydraIcons.Phone, "The AI lives on your phone", "A language model is downloaded once and runs on this phone's own processor.")
        Fact(HydraIcons.Offline, "Works without internet", "After the download, you can chat with no connection at all.")
        Fact(HydraIcons.Lock, "Your chats stay here", "Conversations are kept in an encrypted database on this phone. They are not sent to a server to get a reply.")
        Spacer(Modifier.height(4.dp))
        HydraBanner(
            text = "HYDRA only needs network access, to download models. It does not ask for your contacts, photos or microphone.",
            icon = HydraIcons.Info,
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun Fact(icon: ImageVector, title: String, body: String) {
    val c = MaterialTheme.hydra
    Row(Modifier.padding(bottom = 20.dp)) {
        Box(
            Modifier.size(44.dp).clip(HydraShapes.button).background(c.accentSoft),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, contentDescription = null, tint = c.accent, modifier = Modifier.size(22.dp)) }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
            Spacer(Modifier.height(2.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
        }
    }
}

@Composable
private fun CheckingPage(onStart: () -> Unit) {
    val c = MaterialTheme.hydra
    // The view-model only leaves this step once the real device check has finished.
    LaunchedEffect(Unit) { onStart() }
    Column(
        Modifier.fillMaxSize().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        HydraSpinner(size = 40.dp)
        Spacer(Modifier.height(24.dp))
        Text("Checking your phone", style = MaterialTheme.typography.headlineSmall, color = c.textPrimary)
        Spacer(Modifier.height(8.dp))
        Text(
            "Looking at memory, free storage and your Android version.",
            style = MaterialTheme.typography.bodyMedium,
            color = c.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ChooseModelPage(state: OnboardingState, viewModel: OnboardingViewModel) {
    val c = MaterialTheme.hydra
    val rec = state.recommendation
    val noneFit = rec != null && rec.fitting.isEmpty()
    val chosen = OnboardingFlow.selectedDescriptor(state)
    val retrying = state.error != null && state.recommendation != null

    Page(
        footer = {
            HydraPrimaryButton(
                text = when {
                    chosen == null -> "Choose a model"
                    retrying -> "Try again · ${ByteFormat.human(chosen.sizeBytes)}"
                    else -> "Download · ${ByteFormat.human(chosen.sizeBytes)}"
                },
                onClick = viewModel::download,
                enabled = state.chosenModelId != null && !noneFit,
                icon = HydraIcons.Download,
            )
        },
    ) {
        Spacer(Modifier.height(8.dp))
        Text("Pick a model", style = MaterialTheme.typography.headlineMedium, color = c.textPrimary)
        Spacer(Modifier.height(8.dp))
        Text(
            "Bigger models write better but need more memory. We only enable the ones this phone can run.",
            style = MaterialTheme.typography.bodyMedium,
            color = c.textSecondary,
        )
        Spacer(Modifier.height(16.dp))

        state.profile?.let { PhoneSummary(it) }

        state.error?.let {
            Spacer(Modifier.height(12.dp))
            HydraBanner(text = it, tone = HydraTone.Danger, icon = HydraIcons.Error)
        }
        rec?.reasons?.forEach {
            Spacer(Modifier.height(8.dp))
            HydraBanner(text = it, tone = HydraTone.Neutral, icon = HydraIcons.Info)
        }
        Spacer(Modifier.height(16.dp))

        ModelCatalog.all.forEach { d ->
            ModelChoice(
                descriptor = d,
                fits = rec?.fitting?.contains(d.tier) == true,
                recommended = rec?.recommended == d.tier,
                chosen = state.chosenModelId == d.id,
                onChoose = { viewModel.choose(d.id) },
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PhoneSummary(profile: DeviceProfile) {
    HydraCard {
        HydraKeyValue("Memory", String.format(Locale.US, "%.1f GB", profile.totalRamMb / 1024f))
        HydraKeyValue("Free storage", ByteFormat.human(profile.freeStorageMb * 1024L * 1024L))
        HydraKeyValue("Android", "API ${profile.androidSdk}")
        HydraKeyValue("Processor", profile.primaryAbi)
        profile.batteryPercent?.let {
            HydraKeyValue("Battery", "$it%" + if (profile.charging) " (charging)" else "")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ModelChoice(
    descriptor: ModelDescriptor,
    fits: Boolean,
    recommended: Boolean,
    chosen: Boolean,
    onChoose: () -> Unit,
) {
    val c = MaterialTheme.hydra
    Box(Modifier.alpha(if (fits) 1f else 0.6f)) {
        HydraCard(onClick = if (fits) onChoose else null, highlighted = chosen && fits) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(descriptor.displayName, style = MaterialTheme.typography.titleMedium, color = c.textPrimary, modifier = Modifier.weight(1f))
                if (chosen && fits) Icon(HydraIcons.CheckCircle, contentDescription = "Selected", tint = c.accent, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (recommended) HydraTag("Recommended for this phone")
                if (!fits) HydraTag("Too large for this phone", tone = HydraTone.Danger)
            }
            Spacer(Modifier.height(8.dp))
            Text(descriptor.description, style = MaterialTheme.typography.bodyMedium, color = c.textSecondary)
            Spacer(Modifier.height(8.dp))
            Text(
                "${ByteFormat.human(descriptor.sizeBytes)} download · needs ${String.format(Locale.US, "%.0f", descriptor.minRamMb / 1024f)} GB memory",
                style = MaterialTheme.typography.bodySmall,
                color = c.textMuted,
            )
        }
    }
}

@Composable
private fun DownloadingPage(state: OnboardingState) {
    val c = MaterialTheme.hydra
    val d = OnboardingFlow.selectedDescriptor(state)
    val percent = (state.downloadFraction * 100).toInt()
    Column(
        Modifier.fillMaxSize().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        HydraProgressRing(fraction = state.downloadFraction) {
            Text("$percent%", style = MaterialTheme.typography.displaySmall, color = c.textPrimary)
        }
        Spacer(Modifier.height(32.dp))
        Text(
            "Downloading ${d?.displayName.orEmpty()}",
            style = MaterialTheme.typography.headlineSmall,
            color = c.textPrimary,
            textAlign = TextAlign.Center,
        )
        d?.let {
            Spacer(Modifier.height(6.dp))
            Text(
                "${ByteFormat.human((it.sizeBytes * state.downloadFraction).toLong())} of ${ByteFormat.human(it.sizeBytes)}",
                style = MaterialTheme.typography.bodyMedium,
                color = c.textSecondary,
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "You can leave this screen. The download continues in the background and is checked for corruption when it finishes.",
            style = MaterialTheme.typography.bodySmall,
            color = c.textMuted,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ReadyPage(state: OnboardingState, onStart: () -> Unit) {
    val c = MaterialTheme.hydra
    val d = OnboardingFlow.selectedDescriptor(state)
    Page(footer = { HydraPrimaryButton("Start first chat", onStart, icon = HydraIcons.Chat) }) {
        Spacer(Modifier.height(48.dp))
        Box(
            Modifier.size(72.dp).clip(HydraShapes.card).background(c.accentSoft),
            contentAlignment = Alignment.Center,
        ) { Icon(HydraIcons.CheckCircle, contentDescription = null, tint = c.accent, modifier = Modifier.size(40.dp)) }
        Spacer(Modifier.height(28.dp))
        Text("Your assistant is ready", style = MaterialTheme.typography.displaySmall, color = c.textPrimary)
        Spacer(Modifier.height(12.dp))
        Text(
            "${d?.displayName ?: "Your model"} is installed. Ask it anything. Replies are written on this phone, even with no connection.",
            style = MaterialTheme.typography.bodyLarge,
            color = c.textSecondary,
        )
        state.error?.let {
            Spacer(Modifier.height(16.dp))
            HydraBanner(text = it, tone = HydraTone.Danger, icon = HydraIcons.Error)
        }
    }
}

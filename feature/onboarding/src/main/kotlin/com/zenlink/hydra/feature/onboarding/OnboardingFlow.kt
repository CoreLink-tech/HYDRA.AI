package com.zenlink.hydra.feature.onboarding

import com.zenlink.hydra.core.device.TierRecommender
import com.zenlink.hydra.core.model.DeviceProfile
import com.zenlink.hydra.core.model.ModelCatalog
import com.zenlink.hydra.core.model.ModelDescriptor
import com.zenlink.hydra.core.model.TierRecommendation

/** Onboarding steps in order. The app shows onboarding first until [OnboardingStep.DONE]. */
enum class OnboardingStep { WELCOME, DEVICE_CHECK, RECOMMENDED, DOWNLOADING, FIRST_CHAT, DONE }

data class OnboardingState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val profile: DeviceProfile? = null,
    val recommendation: TierRecommendation? = null,
    /** The model the user chose (defaults to the recommended tier's free model). */
    val chosenModelId: String? = null,
    val downloadFraction: Float = 0f,
    val error: String? = null,
)

/** Pure step machine. Keeps the rules testable on the JVM with no Android types. */
object OnboardingFlow {

    fun onDeviceChecked(state: OnboardingState, profile: DeviceProfile): OnboardingState {
        val rec = TierRecommender.recommend(profile, ModelCatalog.all)
        val fallback = ModelCatalog.all.firstOrNull { it.tier == rec.recommended }?.id
            ?: ModelCatalog.FREE_MODEL_ID
        return state.copy(
            step = OnboardingStep.RECOMMENDED,
            profile = profile,
            recommendation = rec,
            chosenModelId = fallback,
            error = null,
        )
    }

    fun chooseModel(state: OnboardingState, modelId: String): OnboardingState {
        val d = ModelCatalog.byId(modelId) ?: return state.copy(error = "Unknown model.")
        val fits = state.recommendation?.fitting?.contains(d.tier) ?: false
        return if (!fits) state.copy(error = "This model is too large for this phone.")
        else state.copy(chosenModelId = modelId, error = null)
    }

    /** Starting a download moves to DOWNLOADING. Requires a chosen model that fits. */
    fun startDownload(state: OnboardingState): OnboardingState {
        if (state.step != OnboardingStep.RECOMMENDED) return state
        if (state.chosenModelId == null) return state.copy(error = "Pick a model first.")
        return state.copy(step = OnboardingStep.DOWNLOADING, downloadFraction = 0f, error = null)
    }

    fun onProgress(state: OnboardingState, fraction: Float): OnboardingState =
        if (state.step == OnboardingStep.DOWNLOADING) state.copy(downloadFraction = fraction.coerceIn(0f, 1f)) else state

    fun onDownloadFinished(state: OnboardingState): OnboardingState =
        if (state.step == OnboardingStep.DOWNLOADING) state.copy(step = OnboardingStep.FIRST_CHAT, downloadFraction = 1f) else state

    fun onDownloadFailed(state: OnboardingState, reason: String): OnboardingState =
        state.copy(step = OnboardingStep.RECOMMENDED, error = reason)

    fun finish(state: OnboardingState): OnboardingState =
        if (state.step == OnboardingStep.FIRST_CHAT) state.copy(step = OnboardingStep.DONE) else state

    fun selectedDescriptor(state: OnboardingState): ModelDescriptor? = state.chosenModelId?.let { ModelCatalog.byId(it) }
}

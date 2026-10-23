package com.zenlink.hydra.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenlink.hydra.core.database.repo.SettingsRepository
import com.zenlink.hydra.core.download.ModelDownloads
import com.zenlink.hydra.core.model.DeviceProfile
import com.zenlink.hydra.core.model.DownloadEvent
import com.zenlink.hydra.core.model.ModelCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Drives the onboarding steps. Completion is stored in the encrypted settings table. */
class OnboardingViewModel(
    private val settings: SettingsRepository,
    private val downloads: ModelDownloads,
    private val deviceProfile: () -> DeviceProfile,
    private val wifiOnly: suspend () -> Boolean,
    private val onFinished: () -> Unit,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            downloads.events.collect { event ->
                val chosen = _state.value.chosenModelId ?: return@collect
                if (_state.value.step != OnboardingStep.DOWNLOADING) return@collect
                when (event) {
                    is DownloadEvent.Progress -> if (event.modelId == chosen && event.total > 0) {
                        _state.update { OnboardingFlow.onProgress(it, event.downloaded.toFloat() / event.total) }
                    }
                    is DownloadEvent.Installed -> if (event.modelId == chosen) {
                        _state.update { OnboardingFlow.onDownloadFinished(it) }
                    }
                    is DownloadEvent.Failed -> if (event.modelId == chosen) {
                        _state.update { OnboardingFlow.onDownloadFailed(it, failureText(event)) }
                    }
                    is DownloadEvent.Verifying -> Unit
                }
            }
        }
    }

    /** Step 1: runs the device check and moves to the recommendation screen. */
    fun runDeviceCheck() {
        _state.update { OnboardingFlow.onDeviceChecked(it.copy(step = OnboardingStep.DEVICE_CHECK), deviceProfile()) }
    }

    fun begin() = _state.update { it.copy(step = OnboardingStep.DEVICE_CHECK) }

    fun choose(modelId: String) = _state.update { OnboardingFlow.chooseModel(it, modelId) }

    /** Step 3: starts the download of the chosen model, respecting the Wi-Fi setting. */
    fun download() {
        val next = OnboardingFlow.startDownload(_state.value)
        _state.value = next
        if (next.step != OnboardingStep.DOWNLOADING) return
        val d = OnboardingFlow.selectedDescriptor(next) ?: return
        viewModelScope.launch {
            downloads.start(d, wifiOnly())
        }
    }

    /** Step 4: marks onboarding done and hands over to the chat. */
    fun finish() {
        val chosen = _state.value.chosenModelId
        _state.update { OnboardingFlow.finish(it.copy(step = OnboardingStep.FIRST_CHAT)) }
        viewModelScope.launch {
            chosen?.let { settings.setActiveModel(it) }
            settings.setOnboardingComplete(true)
            onFinished()
        }
    }

    private fun failureText(event: DownloadEvent.Failed): String = when (event.reason) {
        com.zenlink.hydra.core.model.DownloadFailure.NO_NETWORK -> "No connection. Try again when you are online."
        com.zenlink.hydra.core.model.DownloadFailure.WAITING_FOR_WIFI -> "Waiting for Wi-Fi. You can allow mobile data in settings."
        com.zenlink.hydra.core.model.DownloadFailure.NOT_ENOUGH_STORAGE -> "Not enough free storage for this model."
        com.zenlink.hydra.core.model.DownloadFailure.CHECKSUM_MISMATCH -> "The file did not match its checksum. Try again."
        else -> "The download did not finish. Try again."
    }
}

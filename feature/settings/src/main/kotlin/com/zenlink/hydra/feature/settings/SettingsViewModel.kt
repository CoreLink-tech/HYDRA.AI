package com.zenlink.hydra.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenlink.hydra.core.database.repo.SettingsRepository
import com.zenlink.hydra.core.download.ModelDownloads
import com.zenlink.hydra.core.model.AppSettings
import com.zenlink.hydra.core.model.DownloadNetwork
import com.zenlink.hydra.core.model.ModelCatalog
import com.zenlink.hydra.core.model.PerformanceMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val notice: String? = null,
    val deleteConfirmOpen: Boolean = false,
    /** Bytes taken by installed model files, and free space left on the phone. */
    val installedBytes: Long = 0L,
    val freeBytes: Long = 0L,
    /** Display name of the model chosen for chat, or null when none is chosen. */
    val activeModelName: String? = null,
)

/** Holds the settings toggles and the delete-all flow. Rules come from [SettingsPlanner]. */
class SettingsViewModel(
    private val settings: SettingsRepository,
    private val downloads: ModelDownloads,
    private val wipeLocalData: suspend () -> Unit,
    observed: Flow<AppSettings>,
    installedBytes: Flow<Long> = flowOf(0L),
    private val freeStorageBytes: () -> Long = { 0L },
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(observed, installedBytes) { s, bytes -> s to bytes }.collect { (s, bytes) ->
                _state.update {
                    it.copy(
                        settings = s,
                        installedBytes = bytes,
                        freeBytes = freeStorageBytes(),
                        activeModelName = s.activeModelId?.let { id -> ModelCatalog.byId(id)?.displayName },
                    )
                }
            }
        }
    }

    fun setDownloadNetwork(value: DownloadNetwork) = viewModelScope.launch { settings.setDownloadNetwork(value) }

    fun setCrashReporting(value: Boolean) = viewModelScope.launch { settings.setCrashReporting(value) }

    /** [key] is "system", "light" or "dark". */
    fun setThemeMode(key: String) = viewModelScope.launch { settings.setThemeMode(key) }

    fun setPerformanceMode(mode: PerformanceMode) = viewModelScope.launch { settings.setPerformanceMode(mode) }

    fun openDeleteConfirm() = _state.update { it.copy(deleteConfirmOpen = true) }

    fun closeDeleteConfirm() = _state.update { it.copy(deleteConfirmOpen = false) }

    fun dismissNotice() = _state.update { it.copy(notice = null) }

    /** Deletes models and local data after the user types DELETE (checked in the screen). */
    fun deleteAll(onDone: () -> Unit) {
        viewModelScope.launch {
            ModelCatalog.all.forEach { downloads.delete(it.id) }
            wipeLocalData()
            _state.update { it.copy(deleteConfirmOpen = false, notice = "All local data was deleted.") }
            onDone()
        }
    }
}

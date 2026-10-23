package com.zenlink.hydra.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenlink.hydra.core.database.repo.SettingsRepository
import com.zenlink.hydra.core.download.ModelDownloads
import com.zenlink.hydra.core.model.AppSettings
import com.zenlink.hydra.core.model.ModelCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val notice: String? = null,
    val deleteConfirmOpen: Boolean = false,
)

/** Holds the settings toggles and the delete-all flow. Rules come from [SettingsPlanner]. */
class SettingsViewModel(
    private val settings: SettingsRepository,
    private val downloads: ModelDownloads,
    private val wipeLocalData: suspend () -> Unit,
    observed: kotlinx.coroutines.flow.Flow<AppSettings>,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observed.collect { s -> _state.update { it.copy(settings = s) } }
        }
    }

    fun setWifiOnly(value: Boolean) = viewModelScope.launch { settings.setWifiOnly(value) }

    fun setCrashReporting(value: Boolean) = viewModelScope.launch { settings.setCrashReporting(value) }

    fun openDeleteConfirm() = _state.update { it.copy(deleteConfirmOpen = true) }

    fun closeDeleteConfirm() = _state.update { it.copy(deleteConfirmOpen = false) }

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

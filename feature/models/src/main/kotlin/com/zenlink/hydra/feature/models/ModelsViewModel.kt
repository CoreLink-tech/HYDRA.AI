package com.zenlink.hydra.feature.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenlink.hydra.core.database.repo.ModelRepository
import com.zenlink.hydra.core.database.repo.SettingsRepository
import com.zenlink.hydra.core.download.ModelDownloads
import com.zenlink.hydra.core.model.DownloadEvent
import com.zenlink.hydra.core.model.DownloadNetwork
import com.zenlink.hydra.core.model.ModelDescriptor
import com.zenlink.hydra.core.model.ModelTier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ModelsUiState(
    val rows: List<ModelRow> = emptyList(),
    val installedBytes: Long = 0L,
    val network: DownloadNetwork = DownloadNetwork.ANY,
    val notice: String? = null,
)

/** Merges repository state, live progress and settings into the catalogue rows. */
class ModelsViewModel(
    private val catalogue: List<ModelDescriptor>,
    private val models: ModelRepository,
    private val settings: SettingsRepository,
    private val downloads: ModelDownloads,
    private val freeStorageBytes: () -> Long,
    private val recommendedTier: ModelTier?,
    private val fittingTiers: Set<ModelTier>? = null,
) : ViewModel() {

    private val progress = MutableStateFlow<Map<String, Float>>(emptyMap())
    private val notice = MutableStateFlow<String?>(null)
    private var statusShown = false

    private val _state = MutableStateFlow(ModelsUiState())
    val state: StateFlow<ModelsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            downloads.events.collect { event ->
                when (event) {
                    is DownloadEvent.Progress -> if (event.total > 0) {
                        if (statusShown) {
                            notice.value = null
                            statusShown = false
                        }
                        progress.value = progress.value + (event.modelId to event.downloaded.toFloat() / event.total)
                    }
                    is DownloadEvent.Verifying -> progress.value = progress.value - event.modelId
                    is DownloadEvent.Status -> {
                        notice.value = event.text
                        statusShown = true
                    }
                    is DownloadEvent.Installed -> progress.value = progress.value - event.modelId
                    is DownloadEvent.Failed -> {
                        progress.value = progress.value - event.modelId
                        notice.value = "Download failed: ${event.reason.name.lowercase().replace('_', ' ')}" +
                            (if (event.detail.isBlank()) "." else " (${event.detail}).")
                    }
                }
            }
        }
        viewModelScope.launch {
            combine(models.observeAll(), settings.observe(), progress, notice) { records, prefs, prog, note ->
                val byId = records.associateBy { it.modelId }
                val rows = ModelsPlanner.rows(
                    catalogue = catalogue,
                    records = byId,
                    activeModelId = prefs.activeModelId,
                    freeStorageBytes = freeStorageBytes(),
                    recommendedTier = recommendedTier,
                    fittingTiers = fittingTiers,
                ).map { row ->
                    row.copy(progress = prog[row.descriptor.id] ?: row.progress)
                }
                ModelsUiState(
                    rows = rows,
                    installedBytes = ModelsPlanner.installedBytes(byId),
                    network = prefs.downloadNetwork,
                    notice = note,
                )
            }.collect { _state.value = it }
        }
    }

    fun download(modelId: String, network: DownloadNetwork) {
        val d = catalogue.firstOrNull { it.id == modelId } ?: return
        val row = _state.value.rows.firstOrNull { it.descriptor.id == modelId } ?: return
        if (!ModelsPlanner.canStart(row.storage)) {
            notice.value = "Not enough free storage for ${d.displayName}."
            return
        }
        notice.value = null
        viewModelScope.launch { settings.setDownloadNetwork(network) }
        downloads.start(d, network)
    }

    fun cancel(modelId: String) {
        downloads.cancel(modelId)
        viewModelScope.launch { models.forget(modelId) }
    }

    fun delete(modelId: String) {
        viewModelScope.launch {
            downloads.delete(modelId)
            models.forget(modelId)
        }
    }

    fun switchTo(modelId: String) {
        viewModelScope.launch { settings.setActiveModel(modelId) }
    }

    fun dismissNotice() {
        notice.value = null
    }
}

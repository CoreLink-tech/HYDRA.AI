package com.zenlink.hydra.feature.models

import com.zenlink.hydra.core.download.StoragePolicy
import com.zenlink.hydra.core.model.InstalledModel
import com.zenlink.hydra.core.model.ModelDescriptor
import com.zenlink.hydra.core.model.ModelState
import com.zenlink.hydra.core.model.ModelTier

/** One row in the catalogue screen. Pure data so the rules can be unit tested on the JVM. */
data class ModelRow(
    val descriptor: ModelDescriptor,
    val state: ModelState,
    val progress: Float?,
    val localPath: String?,
    val lastError: String?,
    val storage: StoragePolicy.Verdict,
    val recommended: Boolean,
    val isActive: Boolean,
    val canDownload: Boolean,
    val canDelete: Boolean,
    val canSwitch: Boolean,
)

object ModelsPlanner {

    fun rows(
        catalogue: List<ModelDescriptor>,
        records: Map<String, InstalledModel>,
        activeModelId: String?,
        freeStorageBytes: Long,
        recommendedTier: ModelTier?,
    ): List<ModelRow> = catalogue.map { d ->
        val rec = records[d.id]
        val state = rec?.state ?: ModelState.NOT_DOWNLOADED
        val progress = if (state == ModelState.DOWNLOADING || state == ModelState.PAUSED) {
            rec?.let { if (it.totalBytes > 0) it.bytesDownloaded.toFloat() / it.totalBytes else null }
        } else null
        val installed = state == ModelState.INSTALLED
        ModelRow(
            descriptor = d,
            state = state,
            progress = progress,
            localPath = rec?.localPath,
            lastError = rec?.lastError,
            storage = StoragePolicy.check(
                freeBytes = freeStorageBytes,
                modelBytes = d.installedSizeBytes,
                alreadyHaveBytes = if (installed) d.installedSizeBytes else 0L,
            ),
            recommended = recommendedTier != null && d.tier == recommendedTier,
            isActive = d.id == activeModelId,
            canDownload = state == ModelState.NOT_DOWNLOADED || state == ModelState.FAILED || state == ModelState.PAUSED,
            canDelete = installed && d.id != activeModelId,
            canSwitch = installed && d.id != activeModelId,
        )
    }

    /** Total bytes used by installed models, for the storage line. */
    fun installedBytes(records: Map<String, InstalledModel>): Long =
        records.values.filter { it.state == ModelState.INSTALLED }.sumOf { it.bytesDownloaded }

    /** Whether a download may start now given the user's Wi-Fi preference and the storage verdict. */
    fun canStart(storage: StoragePolicy.Verdict): Boolean =
        storage != StoragePolicy.Verdict.BLOCKED_INSUFFICIENT
}

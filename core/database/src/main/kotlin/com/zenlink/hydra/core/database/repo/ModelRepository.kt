package com.zenlink.hydra.core.database.repo

import com.zenlink.hydra.core.database.dao.ModelDao
import com.zenlink.hydra.core.database.entities.ModelEntity
import com.zenlink.hydra.core.model.InstalledModel
import com.zenlink.hydra.core.model.ModelState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Local state of each downloaded model file. Files themselves live in app-private storage. */
class ModelRepository(
    private val dao: ModelDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    fun observeAll(): Flow<List<InstalledModel>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    suspend fun find(modelId: String): InstalledModel? = dao.find(modelId)?.toDomain()

    suspend fun save(model: InstalledModel) {
        dao.upsert(
            ModelEntity(
                modelId = model.modelId,
                version = model.version,
                state = model.state.name,
                bytesDownloaded = model.bytesDownloaded,
                totalBytes = model.totalBytes,
                localPath = model.localPath,
                lastError = model.lastError,
                updatedAtMs = clock(),
            ),
        )
    }

    /** Removes the record. The caller deletes the file on disk. */
    suspend fun forget(modelId: String) {
        dao.find(modelId)?.let { dao.delete(it) }
    }

    private fun ModelEntity.toDomain() = InstalledModel(
        modelId = modelId,
        version = version,
        state = runCatching { ModelState.valueOf(state) }.getOrDefault(ModelState.FAILED),
        bytesDownloaded = bytesDownloaded,
        totalBytes = totalBytes,
        localPath = localPath,
        lastError = lastError,
    )
}

package com.zenlink.hydra.core.download

import com.zenlink.hydra.core.model.DownloadEvent
import com.zenlink.hydra.core.model.DownloadNetwork
import com.zenlink.hydra.core.model.ModelDescriptor
import kotlinx.coroutines.flow.Flow

/**
 * What the UI and the app need from the download layer. The HTTP implementation lives behind
 * this interface so screens and view models never touch OkHttp or WorkManager directly.
 */
interface ModelDownloads {
    /** Progress and terminal events for every model. */
    val events: Flow<DownloadEvent>

    /** Queues a download and returns immediately. Progress arrives on [events]. */
    fun start(descriptor: ModelDescriptor, network: DownloadNetwork)

    /** Stops an in-flight download. The partial file is kept so it can resume later. */
    fun cancel(modelId: String)

    /** Deletes the model file and its record. The caller decides what happens to the active model. */
    suspend fun delete(modelId: String)
}

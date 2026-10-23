package com.zenlink.hydra.core.download

import com.zenlink.hydra.core.model.DownloadEvent
import com.zenlink.hydra.core.model.DownloadFailure
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Process-wide event stream for downloads. Workers publish, the UI and view-models collect. */
object DownloadBus {
    private val flow = MutableSharedFlow<DownloadEvent>(extraBufferCapacity = 64, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val events: SharedFlow<DownloadEvent> = flow.asSharedFlow()

    fun progress(modelId: String, downloaded: Long, total: Long) {
        flow.tryEmit(DownloadEvent.Progress(modelId, downloaded, total))
    }

    fun installed(modelId: String) {
        flow.tryEmit(DownloadEvent.Installed(modelId))
    }

    fun failed(modelId: String, reason: DownloadFailure) {
        flow.tryEmit(DownloadEvent.Failed(modelId, reason))
    }
}

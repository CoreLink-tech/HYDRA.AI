package com.zenlink.hydra.core.model

/** Lifecycle of a model file on this device. */
enum class ModelState { NOT_DOWNLOADED, DOWNLOADING, PAUSED, VERIFYING, INSTALLED, FAILED }

data class InstalledModel(
    val modelId: String,
    val version: Int,
    val state: ModelState,
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val localPath: String?,
    val lastError: String?,
)

/** Download progress surfaced to the UI. */
sealed interface DownloadEvent {
    data class Progress(val modelId: String, val downloaded: Long, val total: Long) : DownloadEvent
    data class Verifying(val modelId: String) : DownloadEvent
    data class Installed(val modelId: String) : DownloadEvent
    data class Failed(val modelId: String, val reason: DownloadFailure) : DownloadEvent
}

enum class DownloadFailure {
    NO_NETWORK,
    WAITING_FOR_WIFI,
    NOT_ENOUGH_STORAGE,
    HTTP_ERROR,
    CHECKSUM_MISMATCH,
    IO_ERROR,
}

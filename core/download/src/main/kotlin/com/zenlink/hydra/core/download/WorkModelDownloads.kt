package com.zenlink.hydra.core.download

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.zenlink.hydra.core.model.DownloadEvent
import com.zenlink.hydra.core.model.DownloadNetwork
import com.zenlink.hydra.core.model.ModelDescriptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import java.io.File

/** Production [ModelDownloads]: one unique WorkManager job per model, network-constrained. */
class WorkModelDownloads(private val context: Context) : ModelDownloads {

    override val events: Flow<DownloadEvent> get() = DownloadBus.events

    override fun start(descriptor: ModelDescriptor, network: DownloadNetwork) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(networkType(network))
            .build()
        val request = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
            .setConstraints(constraints)
            // Start right away instead of waiting for a maintenance window. Falls back to normal
            // scheduling when the system has no expedited quota left.
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setInputData(Data.Builder().putString(ModelDownloadWorker.KEY_MODEL_ID, descriptor.id).build())
            .build()
        WorkManager.getInstance(context)
            // REPLACE, not KEEP: a job queued earlier with a stricter connection (for example Wi-Fi only)
            // would otherwise sit at 0% forever and ignore the new choice. The partial file is kept,
            // so the download resumes where it stopped.
            .enqueueUniqueWork(workName(descriptor.id), ExistingWorkPolicy.REPLACE, request)
        watch(descriptor.id, network)
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val watchers = HashMap<String, Job>()

    /** Tells the UI why nothing is moving while the job is still queued (for example no Wi-Fi). */
    @Synchronized
    private fun watch(modelId: String, network: DownloadNetwork) {
        watchers.remove(modelId)?.cancel()
        watchers[modelId] = scope.launch {
            WorkManager.getInstance(context)
                .getWorkInfosForUniqueWorkFlow(workName(modelId))
                .takeWhile { infos -> infos.none { it.state.isFinished } }
                .collect { infos ->
                    if (infos.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.BLOCKED }) {
                        val wanted = when (network) {
                            DownloadNetwork.WIFI_ONLY -> "Waiting for Wi-Fi"
                            DownloadNetwork.MOBILE_ONLY -> "Waiting for mobile data"
                            DownloadNetwork.ANY -> "Waiting for a connection"
                        }
                        DownloadBus.status(modelId, "$wanted. It starts as soon as that is available.")
                    }
                }
        }
    }

    override fun cancel(modelId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(modelId))
    }

    override suspend fun delete(modelId: String) {
        cancel(modelId)
        val descriptor = com.zenlink.hydra.core.model.ModelCatalog.byId(modelId) ?: return
        val dir = File(context.filesDir, "models")
        File(dir, descriptor.fileName).delete()
        File(dir, descriptor.fileName + ".part").delete()
    }

    private fun networkType(network: DownloadNetwork): NetworkType = when (network) {
        DownloadNetwork.WIFI_ONLY -> NetworkType.UNMETERED
        DownloadNetwork.MOBILE_ONLY -> NetworkType.METERED
        DownloadNetwork.ANY -> NetworkType.CONNECTED
    }

    private fun workName(modelId: String) = "model-download-$modelId"
}

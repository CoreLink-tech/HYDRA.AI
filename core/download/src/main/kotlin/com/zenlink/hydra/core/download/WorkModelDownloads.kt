package com.zenlink.hydra.core.download

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.zenlink.hydra.core.model.DownloadEvent
import com.zenlink.hydra.core.model.DownloadNetwork
import com.zenlink.hydra.core.model.ModelDescriptor
import kotlinx.coroutines.flow.Flow
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
            .setInputData(Data.Builder().putString(ModelDownloadWorker.KEY_MODEL_ID, descriptor.id).build())
            .build()
        WorkManager.getInstance(context)
            // REPLACE, not KEEP: a job queued earlier with a stricter connection (for example Wi-Fi only)
            // would otherwise sit at 0% forever and ignore the new choice. The partial file is kept,
            // so the download resumes where it stopped.
            .enqueueUniqueWork(workName(descriptor.id), ExistingWorkPolicy.REPLACE, request)
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

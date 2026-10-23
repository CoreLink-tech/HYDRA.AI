package com.zenlink.hydra.core.download

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.zenlink.hydra.core.model.DownloadEvent
import com.zenlink.hydra.core.model.ModelDescriptor
import kotlinx.coroutines.flow.Flow
import java.io.File

/** Production [ModelDownloads]: one unique WorkManager job per model, network-constrained. */
class WorkModelDownloads(private val context: Context) : ModelDownloads {

    override val events: Flow<DownloadEvent> get() = DownloadBus.events

    override fun start(descriptor: ModelDescriptor, wifiOnly: Boolean) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
            .setConstraints(constraints)
            .setInputData(Data.Builder().putString(ModelDownloadWorker.KEY_MODEL_ID, descriptor.id).build())
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(workName(descriptor.id), ExistingWorkPolicy.KEEP, request)
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

    private fun workName(modelId: String) = "model-download-$modelId"
}

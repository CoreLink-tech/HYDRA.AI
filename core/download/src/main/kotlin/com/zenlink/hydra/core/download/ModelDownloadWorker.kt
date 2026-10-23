package com.zenlink.hydra.core.download

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.zenlink.hydra.core.model.ModelCatalog
import java.io.File

/**
 * WorkManager entry point. Runs [HttpModelFetcher] for one catalogue model, so downloads survive
 * the app being backgrounded. Progress and terminal events are reported through [DownloadBus].
 */
class ModelDownloadWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val modelId = inputData.getString(KEY_MODEL_ID) ?: return Result.failure()
        val descriptor = ModelCatalog.byId(modelId) ?: return Result.failure()
        val dir = File(applicationContext.filesDir, "models").apply { mkdirs() }
        val fetcher = HttpModelFetcher(HttpClients.default())
        val outcome = fetcher.fetch(
            descriptor = descriptor,
            partFile = File(dir, descriptor.fileName + ".part"),
            finalFile = File(dir, descriptor.fileName),
            onProgress = { done, total -> DownloadBus.progress(modelId, done, total) },
            isCancelled = { isStopped },
        )
        return when (outcome) {
            is HttpModelFetcher.Result.Installed -> {
                DownloadBus.installed(modelId)
                Result.success(Data.Builder().putString(KEY_MODEL_ID, modelId).build())
            }
            is HttpModelFetcher.Result.Failed -> {
                DownloadBus.failed(modelId, outcome.reason)
                // Partial files are kept for resume; only retry transient failures.
                if (outcome.reason == com.zenlink.hydra.core.model.DownloadFailure.NO_NETWORK) Result.retry()
                else Result.failure()
            }
        }
    }

    companion object {
        const val KEY_MODEL_ID = "model_id"
    }
}

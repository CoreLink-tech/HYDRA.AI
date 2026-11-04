package com.zenlink.hydra.core.download

import android.content.Context
import androidx.work.CoroutineWorker
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Data
import androidx.work.ForegroundInfo
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

    /** Needed for expedited work on Android 11 and older, and used to keep long downloads alive. */
    override suspend fun getForegroundInfo(): ForegroundInfo = foregroundInfo(inputData.getString(KEY_MODEL_ID))

    private fun foregroundInfo(modelId: String?): ForegroundInfo {
        val name = modelId?.let { ModelCatalog.byId(it)?.displayName } ?: "model"
        val nm = applicationContext.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm?.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Model downloads", NotificationManager.IMPORTANCE_LOW),
            )
        }
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Downloading $name")
            .setContentText("HYDRA.AI is downloading a model")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    override suspend fun doWork(): Result {
        val modelId = inputData.getString(KEY_MODEL_ID) ?: return Result.failure()
        val descriptor = ModelCatalog.byId(modelId) ?: return Result.failure()
        // A foreground notification keeps phones that stop background work from killing the download.
        // If the system refuses (for example notifications are blocked), the download still runs.
        runCatching { setForeground(foregroundInfo(modelId)) }
        DownloadBus.status(modelId, "Connecting\u2026")
        val dir = File(applicationContext.filesDir, "models").apply { mkdirs() }
        val fetcher = HttpModelFetcher(HttpClients.default())
        val outcome = try {
            fetcher.fetch(
                descriptor = descriptor,
                partFile = File(dir, descriptor.fileName + ".part"),
                finalFile = File(dir, descriptor.fileName),
                onProgress = { done, total -> DownloadBus.progress(modelId, done, total) },
                isCancelled = { isStopped },
            )
        } catch (e: Exception) {
            // Never die silently: the screen would sit at 0% with no explanation.
            HttpModelFetcher.Result.Failed(
                com.zenlink.hydra.core.model.DownloadFailure.IO_ERROR,
                "${e.javaClass.simpleName}: ${e.message.orEmpty()}",
            )
        }
        return when (outcome) {
            is HttpModelFetcher.Result.Installed -> {
                DownloadBus.installed(modelId)
                Result.success(Data.Builder().putString(KEY_MODEL_ID, modelId).build())
            }
            is HttpModelFetcher.Result.Failed -> {
                DownloadBus.failed(modelId, outcome.reason, outcome.message)
                // Partial files are kept for resume; only retry transient failures.
                if (outcome.reason == com.zenlink.hydra.core.model.DownloadFailure.NO_NETWORK) Result.retry()
                else Result.failure()
            }
        }
    }

    companion object {
        const val KEY_MODEL_ID = "model_id"
        private const val CHANNEL_ID = "model_downloads"
        private const val NOTIFICATION_ID = 4107
    }
}

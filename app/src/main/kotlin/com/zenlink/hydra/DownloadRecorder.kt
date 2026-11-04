package com.zenlink.hydra

import com.zenlink.hydra.core.model.DownloadEvent
import com.zenlink.hydra.core.model.InstalledModel
import com.zenlink.hydra.core.model.ModelCatalog
import com.zenlink.hydra.core.model.ModelState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

/**
 * Saves download results to the database, so a finished model shows as installed and chat can find
 * its file. It lives in the application, not in a screen, so it keeps recording while the user
 * is on another tab or the app is in the background.
 */
class DownloadRecorder(private val app: HydraApp) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lastSavedPercent = HashMap<String, Int>()

    fun start() {
        scope.launch {
            app.downloads.events.collect { event ->
                runCatching { handle(event) }
            }
        }
    }

    private suspend fun handle(event: DownloadEvent) {
        when (event) {
            is DownloadEvent.Progress -> {
                val d = ModelCatalog.byId(event.modelId) ?: return
                if (event.total <= 0) return
                val percent = (event.downloaded * 100 / event.total).toInt()
                if (lastSavedPercent[event.modelId] == percent) return
                lastSavedPercent[event.modelId] = percent
                app.models.save(
                    InstalledModel(d.id, d.version, ModelState.DOWNLOADING, event.downloaded, event.total, null, null),
                )
            }
            is DownloadEvent.Installed -> {
                val d = ModelCatalog.byId(event.modelId) ?: return
                lastSavedPercent.remove(event.modelId)
                val path = File(File(app.filesDir, "models"), d.fileName).absolutePath
                app.models.save(
                    InstalledModel(d.id, d.version, ModelState.INSTALLED, d.sizeBytes, d.sizeBytes, path, null),
                )
            }
            is DownloadEvent.Failed -> {
                val d = ModelCatalog.byId(event.modelId) ?: return
                lastSavedPercent.remove(event.modelId)
                val previous = app.models.find(d.id)
                app.models.save(
                    InstalledModel(
                        d.id, d.version, ModelState.FAILED,
                        previous?.bytesDownloaded ?: 0L, d.sizeBytes, null,
                        listOf(event.reason.name, event.detail).filter { it.isNotBlank() }.joinToString(": "),
                    ),
                )
            }
            is DownloadEvent.Verifying, is DownloadEvent.Status -> Unit
        }
    }
}

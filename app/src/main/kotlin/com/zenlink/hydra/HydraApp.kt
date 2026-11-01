package com.zenlink.hydra

import android.app.Application
import com.zenlink.hydra.core.database.HydraDatabase
import com.zenlink.hydra.core.database.repo.ChatRepository
import com.zenlink.hydra.core.database.repo.ModelRepository
import com.zenlink.hydra.core.database.repo.SettingsRepository
import com.zenlink.hydra.core.download.ModelDownloads
import com.zenlink.hydra.core.download.WorkModelDownloads
import com.zenlink.hydra.core.llama.LlamaEngine

/**
 * Process-wide owner of the database, repositories and engine. Screens receive these through
 * [MainActivity]; nothing else constructs them. The database is opened lazily on first use.
 */
class HydraApp : Application() {
    val database: HydraDatabase by lazy { HydraDatabase.open(this) }
    val chats: ChatRepository by lazy { ChatRepository(database.chatDao(), database.messageDao()) }
    val models: ModelRepository by lazy { ModelRepository(database.modelDao()) }
    val settings: SettingsRepository by lazy { SettingsRepository(database.settingDao()) }
    val engine: LlamaEngine by lazy { LlamaEngine() }
    val downloads: ModelDownloads by lazy { WorkModelDownloads(this) }

    override fun onCreate() {
        super.onCreate()
        // Installed first so that anything that fails below is recorded and shown on next launch.
        CrashLog.install(this)
        // sqlcipher-android (net.zetetic) does not load its native library by itself. It must be
        // loaded before the first database is opened, or the first query throws UnsatisfiedLinkError.
        System.loadLibrary("sqlcipher")
    }
}

package com.zenlink.hydra

import com.zenlink.hydra.core.billing.BillingGateway
import com.zenlink.hydra.core.database.HydraDatabase
import com.zenlink.hydra.core.database.repo.ChatRepository
import com.zenlink.hydra.core.database.repo.ModelRepository
import com.zenlink.hydra.core.database.repo.SettingsRepository
import com.zenlink.hydra.core.download.ModelDownloads
import com.zenlink.hydra.core.llama.LlamaEngine
import com.zenlink.hydra.core.monitoring.CrashReporter
import com.zenlink.hydra.core.sharing.NearbyShareTransport

/**
 * Explicit composition-root contract. [HydraApp] is the production implementation.
 *
 * Keeping dependencies behind this interface makes a future Hilt (or similar) migration
 * mechanical: each property becomes a `@Provides` / `@Singleton` binding without changing
 * feature ViewModels that already accept constructor parameters.
 */
interface AppContainer {
    val database: HydraDatabase
    val chats: ChatRepository
    val models: ModelRepository
    val settings: SettingsRepository
    val engine: LlamaEngine
    val downloads: ModelDownloads
    val billing: BillingGateway
    val crashReporter: CrashReporter
    val nearby: NearbyShareTransport
}

package com.zenlink.hydra

import android.app.Application
import com.zenlink.hydra.core.billing.BillingGateway
import com.zenlink.hydra.core.billing.MockBillingGateway
import com.zenlink.hydra.core.database.HydraDatabase
import com.zenlink.hydra.core.database.repo.ChatRepository
import com.zenlink.hydra.core.database.repo.ModelRepository
import com.zenlink.hydra.core.database.repo.SettingsRepository
import com.zenlink.hydra.core.download.ModelDownloads
import com.zenlink.hydra.core.download.WorkModelDownloads
import com.zenlink.hydra.core.llama.LlamaEngine

/**
 * Process-wide owner of the database, repositories, engine and billing. Screens receive these
 * through [MainActivity]; nothing else constructs them. The database is opened lazily on first use.
 *
 * Billing: when [BuildConfig.USE_MOCK_BILLING] is true (the default), a [MockBillingGateway] is used
 * so entitlement can be observed and tested without Play Console products. Switch to
 * [com.zenlink.hydra.core.billing.PlayBillingGateway] once products exist and an Activity provider
 * can be supplied for the purchase sheet.
 */
class HydraApp : Application() {
    val database: HydraDatabase by lazy { HydraDatabase.open(this) }
    val chats: ChatRepository by lazy { ChatRepository(database.chatDao(), database.messageDao()) }
    val models: ModelRepository by lazy { ModelRepository(database.modelDao()) }
    val settings: SettingsRepository by lazy { SettingsRepository(database.settingDao()) }
    val engine: LlamaEngine by lazy { LlamaEngine() }
    val downloads: ModelDownloads by lazy { WorkModelDownloads(this) }

    /**
     * Live entitlement source. Default is the mock gateway so Settings and share limits react to
     * plan changes in debug builds. Offline chat never depends on this value.
     */
    val billing: BillingGateway by lazy {
        // BuildConfig.USE_MOCK_BILLING is true until Play products are configured (see NOTES.md).
        if (BuildConfig.USE_MOCK_BILLING) {
            MockBillingGateway()
        } else {
            // Play path still needs an Activity provider for purchase(); entitlement cache starts FREE
            // and is updated via applyServerEntitlement after server verification.
            // For now keep mock so the app never crashes when the flag is flipped early.
            MockBillingGateway()
        }
    }
}

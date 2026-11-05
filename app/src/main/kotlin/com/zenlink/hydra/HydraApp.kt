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
import com.zenlink.hydra.core.monitoring.Consent
import com.zenlink.hydra.core.monitoring.ConsentGatedReporter
import com.zenlink.hydra.core.monitoring.CrashReporter
import com.zenlink.hydra.core.monitoring.NoopCrashReporter
import com.zenlink.hydra.core.sharing.NearbyShareTransport
import com.zenlink.hydra.core.sharing.NoOpNearbyShareTransport

/**
 * Process-wide owner of infrastructure. Implements [AppContainer] so feature code depends on
 * an interface, which keeps a future Hilt migration straightforward.
 *
 * Billing: [MockBillingGateway] while [BuildConfig.USE_MOCK_BILLING] is true.
 * Monitoring: consent-gated noop backend until a Sentry/Crashlytics DSN is configured.
 * Nearby: [NoOpNearbyShareTransport] until Play Nearby is verified on device.
 */
class HydraApp : Application(), AppContainer {
    override val database: HydraDatabase by lazy { HydraDatabase.open(this) }
    override val chats: ChatRepository by lazy { ChatRepository(database.chatDao(), database.messageDao()) }
    override val models: ModelRepository by lazy { ModelRepository(database.modelDao()) }
    override val settings: SettingsRepository by lazy { SettingsRepository(database.settingDao()) }
    override val engine: LlamaEngine by lazy { LlamaEngine() }
    override val downloads: ModelDownloads by lazy { WorkModelDownloads(this) }

    override val billing: BillingGateway by lazy {
        // Play path still needs an Activity provider for purchase(); keep mock until products exist.
        MockBillingGateway()
    }

    private val monitoringConsent = Consent(initial = false)

    override val crashReporter: CrashReporter by lazy {
        ConsentGatedReporter(
            consent = monitoringConsent,
            delegate = NoopCrashReporter(),
        )
    }

    override val nearby: NearbyShareTransport by lazy { NoOpNearbyShareTransport() }
}

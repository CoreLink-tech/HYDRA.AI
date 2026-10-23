package com.zenlink.hydra.core.billing

import com.zenlink.hydra.core.model.Entitlement
import kotlinx.coroutines.flow.Flow

/** Product ids must match the Play Console and the web allow-list (hydra_pro_monthly, hydra_pro_annual). */
object ProductIds {
    const val PRO_MONTHLY = "hydra_pro_monthly"
    const val PRO_ANNUAL = "hydra_pro_annual"
    val ALL = listOf(PRO_MONTHLY, PRO_ANNUAL)
}

sealed interface PurchaseResult {
    /** Purchase completed on Google's side. Token must be verified by the server before Pro is granted. */
    data class Purchased(val productId: String, val purchaseToken: String) : PurchaseResult
    data object Cancelled : PurchaseResult
    data class Failed(val message: String) : PurchaseResult
}

/**
 * Seam between the app and a billing backend. [PlayBillingGateway] talks to Google Play,
 * [MockBillingGateway] is used for dev builds and tests.
 */
interface BillingGateway {
    /** Entitlement as last known from the server or the local cache. */
    val entitlement: Flow<Entitlement>

    suspend fun refresh()

    suspend fun purchase(productId: String): PurchaseResult
}

package com.zenlink.hydra.core.billing

import android.app.Activity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.zenlink.hydra.core.model.Entitlement
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Google Play Billing wrapper. Launches the purchase sheet and reports the token. The server
 * (web /api/v1/billing/google-play/verify) is the only place that grants Pro. The local
 * [entitlement] is a cache for UI only and is refreshed from the server.
 *
 * NOT exercised in the sandbox: needs a Play Console app and products (see NOTES.md).
 */
class PlayBillingGateway(
    private val client: BillingClient,
    private val activityProvider: () -> Activity?,
) : BillingGateway, PurchasesUpdatedListener {

    private val state = MutableStateFlow(Entitlement.FREE)
    override val entitlement: StateFlow<Entitlement> get() = state

    private var pending: CompletableDeferred<PurchaseResult>? = null

    private suspend fun ensureConnected(): Boolean = suspendCancellableCoroutine { cont ->
        if (client.isReady) {
            cont.resume(true)
            return@suspendCancellableCoroutine
        }
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (cont.isActive) cont.resume(result.responseCode == BillingClient.BillingResponseCode.OK)
            }

            override fun onBillingServiceDisconnected() = Unit
        })
    }

    /**
     * Entitlement is owned by the server. The app only asks Play for owned subscriptions so the
     * caller can send any unverified token to the relay; it never sets Pro locally.
     */
    override suspend fun refresh() {
        if (!ensureConnected()) return
        val params = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        client.queryPurchasesAsync(params) { _, _ -> /* consumed by the server sync job, see NOTES.md */ }
    }

    override suspend fun purchase(productId: String): PurchaseResult {
        if (productId !in ProductIds.ALL) return PurchaseResult.Failed("unknown product")
        val activity = activityProvider() ?: return PurchaseResult.Failed("no foreground screen")
        if (!ensureConnected()) return PurchaseResult.Failed("play billing unavailable")

        val details = queryDetails(productId) ?: return PurchaseResult.Failed("product not found")
        val offer = details.subscriptionOfferDetails?.firstOrNull()
            ?: return PurchaseResult.Failed("no offer")
        val params = BillingFlowParams.newBuilder().setProductDetailsParamsList(
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(details)
                    .setOfferToken(offer.offerToken)
                    .build(),
            ),
        ).build()

        val deferred = CompletableDeferred<PurchaseResult>()
        pending = deferred
        val launch = client.launchBillingFlow(activity, params)
        if (launch.responseCode != BillingClient.BillingResponseCode.OK) {
            pending = null
            return PurchaseResult.Failed("could not open purchase sheet (${launch.responseCode})")
        }
        return deferred.await()
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        val waiting = pending ?: return
        pending = null
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val p = purchases?.firstOrNull()
                if (p == null) {
                    waiting.complete(PurchaseResult.Failed("no purchase returned"))
                } else {
                    val productId = p.products.firstOrNull() ?: ""
                    waiting.complete(PurchaseResult.Purchased(productId, p.purchaseToken))
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> waiting.complete(PurchaseResult.Cancelled)
            else -> waiting.complete(PurchaseResult.Failed("billing error ${result.responseCode}"))
        }
    }

    private suspend fun queryDetails(productId: String): ProductDetails? = suspendCancellableCoroutine { cont ->
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(productId)
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        val params = QueryProductDetailsParams.newBuilder().setProductList(listOf(product)).build()
        client.queryProductDetailsAsync(params) { _, result ->
            if (cont.isActive) cont.resume(result.firstOrNull())
        }
    }

    /** Replaces the UI cache with the last answer from the server. */
    fun applyServerEntitlement(entitlement: Entitlement) {
        state.value = entitlement
    }
}

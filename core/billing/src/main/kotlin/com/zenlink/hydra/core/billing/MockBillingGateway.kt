package com.zenlink.hydra.core.billing

import com.zenlink.hydra.core.model.Entitlement
import com.zenlink.hydra.core.model.Plan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Dev and test billing: no Play Console needed. Starts on the free plan. [purchase] grants
 * Pro for [mockDurationMs] so expiry paths can be exercised.
 */
class MockBillingGateway(
    private val nowMs: () -> Long = System::currentTimeMillis,
    private val mockDurationMs: Long = 30L * 24 * 3600 * 1000,
) : BillingGateway {
    private val state = MutableStateFlow(Entitlement.FREE)
    override val entitlement: StateFlow<Entitlement> get() = state

    override suspend fun refresh() = Unit

    override suspend fun purchase(productId: String): PurchaseResult {
        if (productId !in ProductIds.ALL) return PurchaseResult.Failed("unknown product $productId")
        state.value = Entitlement(Plan.PRO, nowMs() + mockDurationMs)
        return PurchaseResult.Purchased(productId, "mock-token-$productId")
    }

    /** Test hook: force the subscription to expire. */
    fun expireNow() {
        state.value = Entitlement(Plan.PRO, nowMs() - 1)
    }
}

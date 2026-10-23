package com.zenlink.hydra.core.billing

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import com.zenlink.hydra.core.model.Plan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MockBillingGatewayTest {
    @Test
    fun startsFreeAndPurchaseGrantsPro() = runBlocking {
        val gw = MockBillingGateway(nowMs = { 1_000L })
        assertEquals(Plan.FREE, gw.entitlement.first().plan)
        val result = gw.purchase(ProductIds.PRO_MONTHLY)
        assertTrue(result is PurchaseResult.Purchased)
        assertEquals(Plan.PRO, gw.entitlement.first().plan)
    }

    @Test
    fun unknownProductFails() = runBlocking {
        val gw = MockBillingGateway()
        assertTrue(gw.purchase("not_a_product") is PurchaseResult.Failed)
    }

    @Test
    fun expireNowMakesProInactive() = runBlocking {
        val gw = MockBillingGateway(nowMs = { 1_000L })
        gw.purchase(ProductIds.PRO_ANNUAL)
        gw.expireNow()
        assertTrue(!gw.entitlement.first().isProActive(1_000L))
    }
}

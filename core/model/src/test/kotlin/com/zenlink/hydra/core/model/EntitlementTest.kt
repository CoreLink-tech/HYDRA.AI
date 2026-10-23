package com.zenlink.hydra.core.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EntitlementTest {
    @Test
    fun proIsActiveUntilExpiry() {
        val e = Entitlement(Plan.PRO, expiresAtMs = 1_000L)
        assertTrue(e.isProActive(nowMs = 999L))
        assertFalse(e.isProActive(nowMs = 1_000L))
    }

    @Test
    fun freeIsNeverPro() {
        assertFalse(Entitlement.FREE.isProActive(nowMs = 0L))
    }

    @Test
    fun catalogueHasExactlyOneFreeModel() {
        val free = ModelCatalog.all.filter { !it.requiresPro }
        assertTrue(free.size == 1 && free.first().id == ModelCatalog.FREE_MODEL_ID)
    }
}

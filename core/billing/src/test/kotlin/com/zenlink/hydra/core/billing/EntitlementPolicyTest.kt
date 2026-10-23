package com.zenlink.hydra.core.billing

import com.zenlink.hydra.core.model.Entitlement
import com.zenlink.hydra.core.model.Plan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EntitlementPolicyTest {
    private val now = 1_000_000L
    private val activePro = Entitlement(Plan.PRO, now + 10_000)
    private val expiredPro = Entitlement(Plan.PRO, now - 1)

    @Test
    fun offlineChatNeverDependsOnSubscription() {
        assertTrue(EntitlementPolicy.isAllowed(Feature.OFFLINE_CHAT, Entitlement.FREE, now))
        assertTrue(EntitlementPolicy.isAllowed(Feature.OFFLINE_CHAT, expiredPro, now))
    }

    @Test
    fun proModelDownloadNeedsActivePro() {
        assertTrue(EntitlementPolicy.isAllowed(Feature.PRO_MODEL_DOWNLOAD, activePro, now))
        assertFalse(EntitlementPolicy.isAllowed(Feature.PRO_MODEL_DOWNLOAD, expiredPro, now))
        assertFalse(EntitlementPolicy.isAllowed(Feature.PRO_MODEL_DOWNLOAD, Entitlement.FREE, now))
    }

    @Test
    fun shareLimitFollowsPlan() {
        assertEquals(EntitlementPolicy.PRO_LIVE_SHARES, EntitlementPolicy.liveShareLimit(activePro, now))
        assertEquals(EntitlementPolicy.FREE_LIVE_SHARES, EntitlementPolicy.liveShareLimit(expiredPro, now))
        assertEquals(EntitlementPolicy.FREE_LIVE_SHARES, EntitlementPolicy.liveShareLimit(Entitlement.FREE, now))
    }
}

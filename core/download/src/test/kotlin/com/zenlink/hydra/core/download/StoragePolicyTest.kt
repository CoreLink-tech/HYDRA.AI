package com.zenlink.hydra.core.download

import org.junit.Assert.assertEquals
import org.junit.Test

class StoragePolicyTest {
    private val gib = 1L shl 30

    @Test
    fun plentyOfSpaceIsOk() {
        assertEquals(StoragePolicy.Verdict.OK, StoragePolicy.check(20 * gib, 2 * gib))
    }

    @Test
    fun tightSpaceWarns() {
        assertEquals(StoragePolicy.Verdict.WARN_LOW_SPACE, StoragePolicy.check(4 * gib, 1 * gib))
    }

    @Test
    fun notEnoughSpaceIsBlocked() {
        assertEquals(StoragePolicy.Verdict.BLOCKED_INSUFFICIENT, StoragePolicy.check(2 * gib, 2 * gib))
    }

    @Test
    fun partialBytesCountTowardTheDownload() {
        assertEquals(StoragePolicy.Verdict.OK, StoragePolicy.check(6 * gib, 4 * gib, alreadyHaveBytes = 3 * gib))
    }

    @Test
    fun wifiOnlyWaitsWhenOnCellular() {
        assertEquals(
            NetworkPolicy.Decision.WAIT_FOR_WIFI,
            NetworkPolicy.decide(onWifi = false, wifiOnly = true, userAllowsCellular = false),
        )
    }

    @Test
    fun allowedCellularProceeds() {
        assertEquals(
            NetworkPolicy.Decision.PROCEED,
            NetworkPolicy.decide(onWifi = false, wifiOnly = true, userAllowsCellular = true),
        )
    }
}

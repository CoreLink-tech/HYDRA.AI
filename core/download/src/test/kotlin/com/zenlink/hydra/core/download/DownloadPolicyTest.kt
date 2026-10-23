package com.zenlink.hydra.core.download

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class DownloadPolicyTest {

    @Test
    fun sha256OfKnownInput() {
        // sha256("abc") from FIPS 180-2
        val hex = Integrity.sha256(ByteArrayInputStream("abc".toByteArray()))
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", hex)
    }

    @Test
    fun matchIsCaseInsensitiveAndRejectsBlank() {
        assertTrue(Integrity.matches("ABCDEF", "abcdef"))
        assertFalse(Integrity.matches("abc", ""))
        assertFalse(Integrity.matches("abc", "abd"))
    }

    @Test
    fun resumeFromScratchWithoutRangeSupport() {
        assertEquals(ResumePlan.FromScratch, ResumePlan.decide(500, 1000, supportsRange = false))
    }

    @Test
    fun resumeContinuesFromPartialOffset() {
        assertEquals(ResumePlan.Continue(500), ResumePlan.decide(500, 1000, supportsRange = true))
    }

    @Test
    fun completePartialOnlyNeedsVerification() {
        assertEquals(ResumePlan.AlreadyComplete, ResumePlan.decide(1000, 1000, supportsRange = true))
    }

    @Test
    fun oversizedPartialRestarts() {
        assertEquals(ResumePlan.FromScratch, ResumePlan.decide(1200, 1000, supportsRange = true))
    }

    @Test
    fun only206WithMatchingOffsetAppends() {
        assertTrue(RangeResponse.canAppend(206, 500, 500))
        assertFalse(RangeResponse.canAppend(200, 500, null))
        assertFalse(RangeResponse.canAppend(206, 500, 0))
    }

    @Test
    fun storageBlocksWhenFreeSpaceTooSmall() {
        val gib = 1L shl 30
        // 2 GiB free, 2 GiB model: 0 left after download, below the 1 GiB margin.
        assertEquals(StoragePolicy.Verdict.BLOCKED_INSUFFICIENT, StoragePolicy.check(2 * gib, 2 * gib))
    }

    @Test
    fun storageWarnsWhenTightButAllowed() {
        val gib = 1L shl 30
        // 4 GiB free, 1.5 GiB model: 2.5 GiB left, under the 3 GiB warning line.
        assertEquals(StoragePolicy.Verdict.WARN_LOW_SPACE, StoragePolicy.check(4 * gib, gib + gib / 2))
    }

    @Test
    fun storageOkWithPlentyOfRoom() {
        val gib = 1L shl 30
        assertEquals(StoragePolicy.Verdict.OK, StoragePolicy.check(20 * gib, 2 * gib))
    }

    @Test
    fun partialDownloadReducesRequiredSpace() {
        val gib = 1L shl 30
        // Same 4 GiB free, but 3 GiB already downloaded: only 1 GiB still needed.
        assertEquals(StoragePolicy.Verdict.OK, StoragePolicy.check(4 * gib, 4 * gib, alreadyHaveBytes = 3 * gib))
    }

    @Test
    fun wifiOnlyWaitsOnCellularUnlessAllowed() {
        assertEquals(NetworkPolicy.Decision.WAIT_FOR_WIFI,
            NetworkPolicy.decide(onWifi = false, wifiOnly = true, userAllowsCellular = false))
        assertEquals(NetworkPolicy.Decision.PROCEED,
            NetworkPolicy.decide(onWifi = false, wifiOnly = true, userAllowsCellular = true))
        assertEquals(NetworkPolicy.Decision.PROCEED,
            NetworkPolicy.decide(onWifi = false, wifiOnly = false, userAllowsCellular = false))
        assertEquals(NetworkPolicy.Decision.PROCEED,
            NetworkPolicy.decide(onWifi = true, wifiOnly = true, userAllowsCellular = false))
    }
}

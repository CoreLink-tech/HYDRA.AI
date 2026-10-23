package com.zenlink.hydra.core.download

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ResumePlanTest {
    @Test
    fun noPartialFileStartsFromScratch() {
        assertEquals(ResumePlan.FromScratch, ResumePlan.decide(0, 1000, true))
    }

    @Test
    fun serverWithoutRangeStartsFromScratch() {
        assertEquals(ResumePlan.FromScratch, ResumePlan.decide(400, 1000, false))
    }

    @Test
    fun partialFileContinuesFromItsSize() {
        assertEquals(ResumePlan.Continue(400), ResumePlan.decide(400, 1000, true))
    }

    @Test
    fun fullPartialFileOnlyNeedsVerification() {
        assertEquals(ResumePlan.AlreadyComplete, ResumePlan.decide(1000, 1000, true))
    }

    @Test
    fun oversizedPartialStartsOver() {
        assertEquals(ResumePlan.FromScratch, ResumePlan.decide(1200, 1000, true))
    }

    @Test
    fun unknownTotalStillContinues() {
        assertEquals(ResumePlan.Continue(400), ResumePlan.decide(400, -1, true))
    }

    @Test
    fun only206WithMatchingOffsetAppends() {
        assertTrue(RangeResponse.canAppend(206, 400, 400))
        assertFalse(RangeResponse.canAppend(200, 400, null))
        assertFalse(RangeResponse.canAppend(206, 400, 0))
    }
}

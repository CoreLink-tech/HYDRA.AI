package com.zenlink.hydra.feature.chat

import com.zenlink.hydra.core.llama.GenerationEvent
import com.zenlink.hydra.core.llama.StopReason
import com.zenlink.hydra.core.model.MessageStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReplyAssemblerTest {

    private fun fold(vararg events: GenerationEvent): ReplyState =
        events.fold(ReplyState()) { s, e -> ReplyAssembler.apply(s, e) }

    @Test
    fun tokensConcatenateAndEndCompletes() {
        val s = fold(
            GenerationEvent.Token("Hel"),
            GenerationEvent.Token("lo"),
            GenerationEvent.Done(StopReason.END),
        )
        assertEquals("Hello", s.text)
        assertEquals(MessageStatus.COMPLETE, s.status)
        assertTrue(s.finished)
        assertNull(s.error)
    }

    @Test
    fun lengthStopStillCountsAsComplete() {
        val s = fold(GenerationEvent.Token("x"), GenerationEvent.Done(StopReason.LENGTH))
        assertEquals(MessageStatus.COMPLETE, s.status)
    }

    @Test
    fun cancelKeepsPartialTextAndMarksStopped() {
        val s = fold(GenerationEvent.Token("part"), GenerationEvent.Done(StopReason.CANCELLED))
        assertEquals("part", s.text)
        assertEquals(MessageStatus.STOPPED, s.status)
    }

    @Test
    fun failureRecordsMessageAndNoFurtherTokensApply() {
        val s = fold(
            GenerationEvent.Token("a"),
            GenerationEvent.Failed("model not loaded"),
            GenerationEvent.Token("late"),
        )
        assertEquals("a", s.text)
        assertEquals(MessageStatus.FAILED, s.status)
        assertEquals("model not loaded", s.error)
        assertTrue(s.finished)
    }
}

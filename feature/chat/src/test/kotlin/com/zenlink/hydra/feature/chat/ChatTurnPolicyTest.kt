package com.zenlink.hydra.feature.chat

import com.zenlink.hydra.core.model.ModelState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatTurnPolicyTest {
    @Test
    fun rejectsBlankAndBusy() {
        assertFalse(ChatTurnPolicy.canSend("", generating = false))
        assertFalse(ChatTurnPolicy.canSend("   ", generating = false))
        assertFalse(ChatTurnPolicy.canSend("hi", generating = true))
        assertTrue(ChatTurnPolicy.canSend("hi", generating = false))
    }

    @Test
    fun installedOnlyWhenStateInstalled() {
        assertTrue(ChatTurnPolicy.isInstalled(ModelState.INSTALLED))
        assertFalse(ChatTurnPolicy.isInstalled(ModelState.DOWNLOADING))
        assertFalse(ChatTurnPolicy.isInstalled(ModelState.NOT_DOWNLOADED))
    }

    @Test
    fun noticesAreNonBlank() {
        assertTrue(ChatTurnPolicy.noticeNoActiveModel().isNotBlank())
        assertTrue(ChatTurnPolicy.noticeModelNotInstalled("Fast").contains("Fast"))
        assertEquals("Could not load the model.", ChatTurnPolicy.noticeLoadFailed(null))
        assertEquals("oom", ChatTurnPolicy.noticeLoadFailed("oom"))
        assertEquals("Generation failed.", ChatTurnPolicy.noticeGenerationFailed("  "))
    }
}

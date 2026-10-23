package com.zenlink.hydra.core.download

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class IntegrityTest {
    @Test
    fun sha256OfEmptyInputMatchesKnownVector() {
        val hash = Integrity.sha256(ByteArrayInputStream(ByteArray(0)))
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", hash)
    }

    @Test
    fun sha256OfAbcMatchesKnownVector() {
        val hash = Integrity.sha256(ByteArrayInputStream("abc".toByteArray()))
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", hash)
    }

    @Test
    fun matchesIsCaseInsensitiveAndTrimmed() {
        assertTrue(Integrity.matches("ABCDEF", " abcdef "))
    }

    @Test
    fun blankExpectedNeverMatches() {
        assertFalse(Integrity.matches("abc", ""))
    }
}

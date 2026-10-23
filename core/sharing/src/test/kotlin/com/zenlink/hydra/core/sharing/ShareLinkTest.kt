package com.zenlink.hydra.core.sharing

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareLinkTest {
    private val id = "abcdefghijklmnop1234"
    private val key = ShareCrypto.newKey()

    @Test
    fun keyLivesOnlyInFragment() {
        val link = ShareLink.build("https://zenlink.tech/", id, key)
        assertTrue(link.startsWith("https://zenlink.tech/s/$id#k="))
        // The part before '#' is what a browser sends to the server; it must not contain the key.
        val beforeHash = link.substringBefore('#')
        assertEquals("https://zenlink.tech/s/$id", beforeHash)
    }

    @Test
    fun parseRoundTrips() {
        val parsed = ShareLink.parse(ShareLink.build("https://zenlink.tech", id, key))!!
        assertEquals(id, parsed.shareId)
        assertArrayEquals(key, parsed.key)
    }

    @Test
    fun rejectsMalformedLinks() {
        assertNull(ShareLink.parse("https://zenlink.tech/s/$id"))
        assertNull(ShareLink.parse("https://zenlink.tech/s/short#k=${ShareCrypto.encodeKey(key)}"))
        assertNull(ShareLink.parse("https://zenlink.tech/s/$id#k=not-a-key"))
        assertNull(ShareLink.parse("https://zenlink.tech/x/$id#k=${ShareCrypto.encodeKey(key)}"))
    }
}

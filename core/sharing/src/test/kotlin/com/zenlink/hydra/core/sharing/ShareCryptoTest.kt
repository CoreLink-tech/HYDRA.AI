package com.zenlink.hydra.core.sharing

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import javax.crypto.AEADBadTagException

class ShareCryptoTest {
    private val message = "hello private world".toByteArray()

    @Test
    fun roundTripsPlaintext() {
        val key = ShareCrypto.newKey()
        val sealed = ShareCrypto.seal(key, message)
        assertArrayEquals(message, ShareCrypto.open(key, sealed))
    }

    @Test
    fun ciphertextDoesNotContainPlaintext() {
        val key = ShareCrypto.newKey()
        val sealed = ShareCrypto.seal(key, message)
        assertFalse(String(sealed.ciphertext).contains("hello private"))
    }

    @Test
    fun freshIvEveryTime() {
        val key = ShareCrypto.newKey()
        val a = ShareCrypto.seal(key, message)
        val b = ShareCrypto.seal(key, message)
        assertNotEquals(a.iv.toList(), b.iv.toList())
        assertNotEquals(a.ciphertext.toList(), b.ciphertext.toList())
    }

    @Test
    fun wrongKeyFailsAuthentication() {
        val sealed = ShareCrypto.seal(ShareCrypto.newKey(), message)
        assertThrows(AEADBadTagException::class.java) { ShareCrypto.open(ShareCrypto.newKey(), sealed) }
    }

    @Test
    fun tamperedCiphertextFails() {
        val key = ShareCrypto.newKey()
        val sealed = ShareCrypto.seal(key, message)
        sealed.ciphertext[0] = (sealed.ciphertext[0].toInt() xor 1).toByte()
        assertThrows(AEADBadTagException::class.java) { ShareCrypto.open(key, sealed) }
    }

    @Test
    fun aadMismatchFails() {
        val key = ShareCrypto.newKey()
        val sealed = ShareCrypto.seal(key, message, aad = "share-1".toByteArray())
        assertThrows(AEADBadTagException::class.java) { ShareCrypto.open(key, sealed, aad = "share-2".toByteArray()) }
    }

    @Test
    fun keyEncodingRoundTrips() {
        val key = ShareCrypto.newKey()
        val encoded = ShareCrypto.encodeKey(key)
        assertFalse(encoded.contains('='))
        assertArrayEquals(key, ShareCrypto.decodeKey(encoded))
    }

    @Test
    fun rejectsWrongKeyLength() {
        assertThrows(IllegalArgumentException::class.java) { ShareCrypto.seal(ByteArray(16), message) }
    }

    @Test
    fun sha256IsStable() {
        assertEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824", ShareCrypto.sha256Hex("hello".toByteArray()))
    }
}

package com.zenlink.hydra.core.sharing

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.util.Base64
import java.security.MessageDigest

/**
 * End-to-end encryption for shared conversations. The key is generated on the device and only
 * leaves it inside the share link fragment (#k=...), which browsers never send to the server.
 * The relay stores { ciphertext, iv, sha256 } and cannot decrypt.
 */
object ShareCrypto {
    const val KEY_BYTES = 32
    private const val IV_BYTES = 12
    private const val TAG_BITS = 128
    private val random = SecureRandom()

    data class Sealed(val ciphertext: ByteArray, val iv: ByteArray)

    fun newKey(): ByteArray = ByteArray(KEY_BYTES).also { random.nextBytes(it) }

    fun seal(key: ByteArray, plaintext: ByteArray, aad: ByteArray = ByteArray(0)): Sealed {
        require(key.size == KEY_BYTES) { "key must be 256-bit" }
        val iv = ByteArray(IV_BYTES).also { random.nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(TAG_BITS, iv))
        if (aad.isNotEmpty()) cipher.updateAAD(aad)
        return Sealed(cipher.doFinal(plaintext), iv)
    }

    /** Throws [javax.crypto.AEADBadTagException] if the key, iv, aad or ciphertext is wrong. */
    fun open(key: ByteArray, sealed: Sealed, aad: ByteArray = ByteArray(0)): ByteArray {
        require(key.size == KEY_BYTES) { "key must be 256-bit" }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(TAG_BITS, sealed.iv))
        if (aad.isNotEmpty()) cipher.updateAAD(aad)
        return cipher.doFinal(sealed.ciphertext)
    }

    fun sha256Hex(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    fun encodeKey(key: ByteArray): String = Base64.getUrlEncoder().withoutPadding().encodeToString(key)

    fun decodeKey(encoded: String): ByteArray {
        val bytes = Base64.getUrlDecoder().decode(encoded)
        require(bytes.size == KEY_BYTES) { "bad key length" }
        return bytes
    }

    fun b64(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)
    fun unb64(s: String): ByteArray = Base64.getDecoder().decode(s)
}

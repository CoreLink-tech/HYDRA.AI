package com.zenlink.hydra.core.sharing

import kotlinx.serialization.Serializable

/** Modes match the relay contract: full, selected, read_only, continuable. */
enum class ShareMode(val wire: String) {
    FULL("full"),
    SELECTED("selected"),
    /** Recipients can read but not continue the conversation. */
    READ_ONLY("read_only"),
    /** Recipients get a copy they can continue on their own device. */
    CONTINUABLE("continuable");

    companion object {
        fun fromWire(value: String): ShareMode? = entries.firstOrNull { it.wire == value }
    }
}

/** Transport chosen by the user. Nearby is device-to-device, Online goes through the relay. */
enum class ShareTransport { NEARBY, ONLINE }

/** A conversation as it is serialised before encryption. Never sent to the server in plaintext. */
@Serializable
data class SharePayload(
    val title: String,
    val messages: List<PayloadMessage>,
) {
    @Serializable
    data class PayloadMessage(val role: String, val content: String)
}

data class CreatedShare(
    val id: String,
    val createdAtMs: Long,
    val expiresAtMs: Long?,
    val link: String,
)

data class ShareRecord(
    val id: String,
    val mode: ShareMode,
    val messageCount: Int,
    val createdAtMs: Long,
    val expiresAtMs: Long?,
    val revokedAtMs: Long?,
    val viewCount: Int,
)

sealed interface ShareError {
    val message: String

    data class Unauthorized(override val message: String = "Sign in again to share online.") : ShareError
    data class QuotaExceeded(val limit: Int, override val message: String) : ShareError
    data class TooLarge(override val message: String = "This conversation is larger than the 2 MB share limit.") : ShareError
    data class Network(override val message: String) : ShareError
    data class Server(val code: String, override val message: String) : ShareError
}

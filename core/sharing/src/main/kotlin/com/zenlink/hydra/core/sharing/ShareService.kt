package com.zenlink.hydra.core.sharing

import com.zenlink.hydra.core.model.Entitlement
import kotlinx.serialization.json.Json

/**
 * Orchestrates an online share: encrypt on device, upload ciphertext only, return a link whose
 * fragment holds the key. Enforces the live-share cap before the request is made.
 */
class ShareService(
    private val relay: ShareRelay,
    private val siteBase: String,
    private val liveShareLimit: (Entitlement) -> Int,
    private val keyFactory: () -> ByteArray = ShareCrypto::newKey,
    private val nowMs: () -> Long = System::currentTimeMillis,
) {
    suspend fun shareOnline(
        payload: SharePayload,
        mode: ShareMode,
        entitlement: Entitlement,
        expiresInHours: Int? = null,
    ): Result<CreatedShare> {
        val plaintext = Json.encodeToString(SharePayload.serializer(), payload).toByteArray(Charsets.UTF_8)
        if (plaintext.size > MAX_PLAINTEXT_BYTES) {
            return Result.failure(ShareException(ShareError.TooLarge()))
        }
        val live = relay.listMine().getOrElse { return Result.failure(it) }
            .count { it.revokedAtMs == null && (it.expiresAtMs == null || it.expiresAtMs > nowMs()) }
        val limit = liveShareLimit(entitlement)
        if (live >= limit) {
            return Result.failure(
                ShareException(ShareError.QuotaExceeded(limit, "Live share limit reached. Revoke an old share or upgrade to Pro.")),
            )
        }

        val key = keyFactory()
        val sealed = ShareCrypto.seal(key, plaintext)
        val created = relay.create(
            ShareRelay.CreateRequest(
                ciphertext = sealed.ciphertext,
                iv = sealed.iv,
                mode = mode,
                messageCount = payload.messages.size,
                expiresInHours = expiresInHours,
            ),
        ).getOrElse { return Result.failure(it) }

        return Result.success(created.copy(link = ShareLink.build(siteBase, created.id, key)))
    }

    suspend fun revoke(shareId: String): Result<Unit> = relay.revoke(shareId)

    companion object {
        /** Matches the relay's 2 MiB decoded ciphertext limit (plaintext is a little smaller). */
        const val MAX_PLAINTEXT_BYTES = 2 * 1024 * 1024
    }
}

class ShareException(val error: ShareError) : RuntimeException(error.message)

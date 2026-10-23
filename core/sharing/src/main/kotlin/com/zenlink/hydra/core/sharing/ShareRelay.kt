package com.zenlink.hydra.core.sharing

/**
 * Online share client contract. The implementation [HttpShareRelay] speaks the web API:
 *   POST   /api/v1/shares        create (Bearer)
 *   GET    /api/v1/shares        list own shares (Bearer)
 *   DELETE /api/v1/shares/:id    revoke (Bearer)
 * Plaintext never reaches this interface: callers pass ciphertext only.
 */
interface ShareRelay {
    suspend fun create(request: CreateRequest): Result<CreatedShare>
    suspend fun listMine(): Result<List<ShareRecord>>
    suspend fun revoke(shareId: String): Result<Unit>

    data class CreateRequest(
        val ciphertext: ByteArray,
        val iv: ByteArray,
        val mode: ShareMode,
        val messageCount: Int,
        val expiresInHours: Int?,
    )
}

package com.zenlink.hydra.core.sharing

import com.zenlink.hydra.core.model.Entitlement
import com.zenlink.hydra.core.model.Plan
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareServiceTest {

    private class FakeRelay(var live: List<ShareRecord> = emptyList()) : ShareRelay {
        var lastRequest: ShareRelay.CreateRequest? = null
        var revoked: String? = null
        override suspend fun create(request: ShareRelay.CreateRequest): Result<CreatedShare> {
            lastRequest = request
            return Result.success(CreatedShare("abcdefghijklmnop1234", 1L, null, ""))
        }
        override suspend fun listMine(): Result<List<ShareRecord>> = Result.success(live)
        override suspend fun revoke(shareId: String): Result<Unit> {
            revoked = shareId
            return Result.success(Unit)
        }
    }

    private fun record(id: String, revoked: Long? = null) = ShareRecord(
        id = id, mode = ShareMode.READ_ONLY, messageCount = 1, createdAtMs = 0L,
        expiresAtMs = null, revokedAtMs = revoked, viewCount = 0,
    )

    private val payload = SharePayload("t", listOf(SharePayload.PayloadMessage("user", "secret prompt")))
    private val limits: (Entitlement) -> Int = { e -> if (e.plan == Plan.PRO) 50 else 3 }

    @Test
    fun uploadsCiphertextOnlyAndLinkCarriesKey() = runBlocking {
        val relay = FakeRelay()
        val key = ShareCrypto.newKey()
        val svc = ShareService(relay, "https://zenlink.tech", limits, keyFactory = { key })
        val created = svc.shareOnline(payload, ShareMode.READ_ONLY, Entitlement.FREE).getOrThrow()

        val req = relay.lastRequest!!
        assertFalse(String(req.ciphertext).contains("secret prompt"))
        assertEquals(1, req.messageCount)
        assertTrue(created.link.endsWith("#k=" + ShareCrypto.encodeKey(key)))
    }

    @Test
    fun freeAccountHitsLiveShareCap() = runBlocking {
        val relay = FakeRelay(live = listOf(record("a"), record("b"), record("c")))
        val svc = ShareService(relay, "https://zenlink.tech", limits)
        val result = svc.shareOnline(payload, ShareMode.FULL, Entitlement.FREE)
        assertTrue(result.isFailure)
        val err = (result.exceptionOrNull() as ShareException).error
        assertTrue(err is ShareError.QuotaExceeded)
        assertEquals(3, (err as ShareError.QuotaExceeded).limit)
        assertEquals(null, relay.lastRequest)
    }

    @Test
    fun revokedSharesDoNotCountAgainstCap() = runBlocking {
        val relay = FakeRelay(live = listOf(record("a", revoked = 5L), record("b"), record("c")))
        val svc = ShareService(relay, "https://zenlink.tech", limits)
        assertTrue(svc.shareOnline(payload, ShareMode.FULL, Entitlement.FREE).isSuccess)
    }

    @Test
    fun proPlanHasHigherCap() = runBlocking {
        val relay = FakeRelay(live = List(5) { record("x$it") })
        val svc = ShareService(relay, "https://zenlink.tech", limits)
        val pro = Entitlement(Plan.PRO, Long.MAX_VALUE)
        assertTrue(svc.shareOnline(payload, ShareMode.CONTINUABLE, pro).isSuccess)
    }

    @Test
    fun oversizedPayloadRejectedBeforeUpload() = runBlocking {
        val relay = FakeRelay()
        val big = SharePayload("t", listOf(SharePayload.PayloadMessage("user", "x".repeat(ShareService.MAX_PLAINTEXT_BYTES + 10))))
        val svc = ShareService(relay, "https://zenlink.tech", limits)
        val result = svc.shareOnline(big, ShareMode.FULL, Entitlement.FREE)
        assertTrue((result.exceptionOrNull() as ShareException).error is ShareError.TooLarge)
        assertEquals(null, relay.lastRequest)
    }

    @Test
    fun revokeDelegatesToRelay() = runBlocking {
        val relay = FakeRelay()
        ShareService(relay, "https://zenlink.tech", limits).revoke("abc").getOrThrow()
        assertEquals("abc", relay.revoked)
    }
}

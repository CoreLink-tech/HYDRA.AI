package com.zenlink.hydra.core.download

import com.zenlink.hydra.core.model.DownloadFailure
import com.zenlink.hydra.core.model.ModelDescriptor
import com.zenlink.hydra.core.model.ModelTier
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.security.MessageDigest

class HttpModelFetcherTest {

    @get:Rule val tmp = TemporaryFolder()
    private lateinit var server: MockWebServer
    private val body = ByteArray(10_000) { (it % 251).toByte() }
    private val sha = MessageDigest.getInstance("SHA-256").digest(body).joinToString("") { "%02x".format(it) }

    @Before fun setUp() { server = MockWebServer().apply { start() } }
    @After fun tearDown() { server.shutdown() }

    private fun descriptor() = ModelDescriptor(
        id = "m", displayName = "m", publisher = "p", version = 1, description = "d",
        fileName = "m.gguf", downloadUrl = server.url("/m.gguf").toString(), sizeBytes = body.size.toLong(),
        sha256 = sha, tier = ModelTier.FAST, contextTokens = 4096, minRamMb = 1, minFreeStorageMb = 1,
        minAndroidSdk = 26, license = "x", licenseUrl = "x", strengths = emptyList(), limitations = emptyList(),
        requiresPro = false,
    )

    private fun fetcher() = HttpModelFetcher(OkHttpClient())

    @Test fun downloadsAndVerifies() {
        server.enqueue(MockResponse().setBody(okio.Buffer().write(body)))
        val dir = tmp.newFolder()
        val result = fetcher().fetch(descriptor(), File(dir, "m.part"), File(dir, "m.gguf"), { _, _ -> }, { false })
        assertTrue(result is HttpModelFetcher.Result.Installed)
        assertEquals(body.size.toLong(), File(dir, "m.gguf").length())
        assertTrue(!File(dir, "m.part").exists())
    }

    @Test fun resumesFromPartialWithRange() {
        val dir = tmp.newFolder()
        val part = File(dir, "m.part").apply { writeBytes(body.copyOfRange(0, 4_000)) }
        server.enqueue(
            MockResponse().setResponseCode(206)
                .setHeader("Content-Range", "bytes 4000-9999/10000")
                .setBody(okio.Buffer().write(body.copyOfRange(4_000, body.size)))
        )
        val result = fetcher().fetch(descriptor(), part, File(dir, "m.gguf"), { _, _ -> }, { false })
        assertTrue(result is HttpModelFetcher.Result.Installed)
        assertEquals("bytes=4000-", server.takeRequest().getHeader("Range"))
    }

    @Test fun checksumMismatchFailsAndRemovesPart() {
        server.enqueue(MockResponse().setBody(okio.Buffer().write(ByteArray(body.size))))
        val dir = tmp.newFolder()
        val part = File(dir, "m.part")
        val result = fetcher().fetch(descriptor(), part, File(dir, "m.gguf"), { _, _ -> }, { false })
        assertEquals(DownloadFailure.CHECKSUM_MISMATCH, (result as HttpModelFetcher.Result.Failed).reason)
        assertTrue(!part.exists())
    }

    @Test fun httpErrorReported() {
        server.enqueue(MockResponse().setResponseCode(500))
        val dir = tmp.newFolder()
        val result = fetcher().fetch(descriptor(), File(dir, "m.part"), File(dir, "m.gguf"), { _, _ -> }, { false })
        assertEquals(DownloadFailure.HTTP_ERROR, (result as HttpModelFetcher.Result.Failed).reason)
    }
}

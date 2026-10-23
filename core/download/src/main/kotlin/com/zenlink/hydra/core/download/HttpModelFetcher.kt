package com.zenlink.hydra.core.download

import com.zenlink.hydra.core.model.DownloadFailure
import com.zenlink.hydra.core.model.ModelDescriptor
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

/**
 * Resumable HTTP download of one model file into [partFile], then sha256 verification and an
 * atomic rename to [finalFile]. Pure JVM (OkHttp only), so it can be tested with MockWebServer.
 */
class HttpModelFetcher(
    private val client: OkHttpClient,
) {
    sealed interface Result {
        data class Installed(val bytes: Long) : Result
        data class Failed(val reason: DownloadFailure, val message: String) : Result
    }

    /**
     * @param onProgress called with (downloaded, total) as bytes arrive.
     * @param isCancelled polled between chunks so a cancel stops quickly and keeps the partial file.
     */
    fun fetch(
        descriptor: ModelDescriptor,
        partFile: File,
        finalFile: File,
        onProgress: (Long, Long) -> Unit,
        isCancelled: () -> Boolean,
    ): Result {
        val total = descriptor.sizeBytes
        val have = if (partFile.exists()) partFile.length() else 0L
        val plan = ResumePlan.decide(partFile.length(), total, supportsRange = true)
        var offset = when (plan) {
            ResumePlan.FromScratch -> 0L
            is ResumePlan.Continue -> plan.offset
            ResumePlan.AlreadyComplete -> return verifyAndInstall(descriptor, partFile, finalFile)
        }
        if (offset == 0L && partFile.exists()) partFile.delete()

        val builder = Request.Builder().url(descriptor.downloadUrl)
        if (offset > 0L) builder.header("Range", "bytes=$offset-")
        val response = try {
            client.newCall(builder.build()).execute()
        } catch (e: IOException) {
            return Result.Failed(DownloadFailure.NO_NETWORK, e.message ?: "network error")
        }

        response.use { resp ->
            if (!resp.isSuccessful) {
                return Result.Failed(DownloadFailure.HTTP_ERROR, "HTTP ${resp.code}")
            }
            val contentRangeStart = resp.header("Content-Range")
                ?.substringAfter("bytes ", "")?.substringBefore("-")?.toLongOrNull()
            if (offset > 0L && !RangeResponse.canAppend(resp.code, offset, contentRangeStart)) {
                // Server ignored the Range header: start over instead of corrupting the file.
                offset = 0L
                partFile.delete()
            }
            val body = resp.body ?: return Result.Failed(DownloadFailure.IO_ERROR, "empty body")
            val append = offset > 0L
            try {
                // Open in append mode when resuming. Opening with outputStream() first would truncate the partial file.
                val out = java.io.FileOutputStream(partFile, append)
                out.use { sink ->
                        body.byteStream().use { input ->
                            val buffer = ByteArray(1 shl 16)
                            var written = offset
                            while (true) {
                                if (isCancelled()) return Result.Failed(DownloadFailure.IO_ERROR, "cancelled")
                                val read = input.read(buffer)
                                if (read < 0) break
                                sink.write(buffer, 0, read)
                                written += read
                                onProgress(written, total)
                            }
                        }
                }
            } catch (e: IOException) {
                return Result.Failed(DownloadFailure.IO_ERROR, e.message ?: "write error")
            }
        }
        return verifyAndInstall(descriptor, partFile, finalFile)
    }

    private fun verifyAndInstall(descriptor: ModelDescriptor, partFile: File, finalFile: File): Result {
        if (!partFile.exists() || partFile.length() != descriptor.sizeBytes) {
            return Result.Failed(DownloadFailure.IO_ERROR, "incomplete download")
        }
        val actual = Integrity.sha256(partFile)
        if (!Integrity.matches(actual, descriptor.sha256)) {
            partFile.delete()
            return Result.Failed(DownloadFailure.CHECKSUM_MISMATCH, "checksum mismatch")
        }
        finalFile.parentFile?.mkdirs()
        if (finalFile.exists()) finalFile.delete()
        if (!partFile.renameTo(finalFile)) {
            return Result.Failed(DownloadFailure.IO_ERROR, "could not move file into place")
        }
        return Result.Installed(finalFile.length())
    }
}

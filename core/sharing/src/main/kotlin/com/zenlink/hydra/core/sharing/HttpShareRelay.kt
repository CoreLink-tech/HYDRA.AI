package com.zenlink.hydra.core.sharing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * HTTP client for the web relay. [baseUrl] is the site origin, e.g. https://zenlink.tech.
 * Auth is a bearer token from the account session, never stored in the share link.
 */
class HttpShareRelay(
    private val baseUrl: String,
    private val bearer: () -> String?,
    private val client: OkHttpClient = OkHttpClient(),
) : ShareRelay {

    private val api = baseUrl.trimEnd('/') + "/api/v1/shares"
    private val jsonType = "application/json".toMediaType()

    override suspend fun create(request: ShareRelay.CreateRequest): Result<CreatedShare> = withContext(Dispatchers.IO) {
        runCatching {
            val body = buildJsonObject {
                put("ciphertext", ShareCrypto.b64(request.ciphertext))
                put("iv", ShareCrypto.b64(request.iv))
                put("mode", request.mode.wire)
                put("messageCount", request.messageCount)
                request.expiresInHours?.let { put("expiresInHours", it) }
            }.toString()
            val json = execute(Request.Builder().url(api).post(body.toRequestBody(jsonType)))
            CreatedShare(
                id = json.str("id"),
                createdAtMs = json.getValue("createdAt").jsonPrimitive.long,
                expiresAtMs = json["expiresAt"]?.jsonPrimitive?.longOrNull,
                link = "",
            )
        }.mapError()
    }

    override suspend fun listMine(): Result<List<ShareRecord>> = withContext(Dispatchers.IO) {
        runCatching {
            val json = execute(Request.Builder().url(api).get())
            json.getValue("shares").jsonArray.map { el ->
                val o = el.jsonObject
                ShareRecord(
                    id = o.str("id"),
                    mode = ShareMode.fromWire(o.str("mode")) ?: ShareMode.FULL,
                    messageCount = o.getValue("messageCount").jsonPrimitive.int,
                    createdAtMs = o.getValue("createdAt").jsonPrimitive.long,
                    expiresAtMs = o["expiresAt"]?.jsonPrimitive?.longOrNull,
                    revokedAtMs = o["revokedAt"]?.jsonPrimitive?.longOrNull,
                    viewCount = o["viewCount"]?.jsonPrimitive?.int ?: 0,
                )
            }
        }.mapError()
    }

    override suspend fun revoke(shareId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            execute(Request.Builder().url("$api/$shareId").delete())
            Unit
        }.mapError()
    }

    private fun execute(builder: Request.Builder): JsonObject {
        val token = bearer() ?: throw ShareException(ShareError.Unauthorized())
        val req = builder.header("Authorization", "Bearer $token").build()
        client.newCall(req).execute().use { resp ->
            val text = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                val err = runCatching {
                    Json.parseToJsonElement(text).jsonObject["error"]?.jsonObject
                }.getOrNull()
                val code = err?.get("code")?.jsonPrimitive?.contentOrNull ?: "http_${resp.code}"
                val msg = err?.get("message")?.jsonPrimitive?.contentOrNull ?: "Share request failed."
                throw ShareException(
                    when {
                        resp.code == 401 -> ShareError.Unauthorized()
                        resp.code == 413 -> ShareError.TooLarge()
                        resp.code == 429 && code == "quota_exceeded" -> ShareError.QuotaExceeded(
                            err?.get("limit")?.jsonPrimitive?.int ?: 0,
                            msg,
                        )
                        else -> ShareError.Server(code, msg)
                    },
                )
            }
            return Json.parseToJsonElement(text).jsonObject
        }
    }

    private fun JsonObject.str(key: String): String = getValue(key).jsonPrimitive.content

    private fun <T> Result<T>.mapError(): Result<T> = recoverCatching { e ->
        when (e) {
            is ShareException -> throw e
            else -> throw ShareException(ShareError.Network(e.message ?: "Network error"))
        }
    }
}

package com.zenlink.hydra.core.sharing

import kotlinx.coroutines.flow.Flow

/**
 * Device-to-device share over Nearby Connections (Play services). Both phones must be on the
 * same screen, and the user confirms a short code on each side before any bytes move.
 * The payload is encrypted with a session key established by the handshake, so nothing
 * leaves the pair of devices in plaintext.
 *
 * Interface only in this module. The concrete Nearby implementation lives behind
 * play-services-nearby and is compiled but not exercised in the sandbox.
 */
interface NearbyShareTransport {
    /** Start advertising so a nearby device can discover this one. Emits states until stopped. */
    fun advertise(displayName: String): Flow<NearbyState>

    /** Start discovering nearby advertisers. */
    fun discover(): Flow<NearbyState>

    /** Send an already-encrypted payload to a connected endpoint. */
    suspend fun send(endpointId: String, ciphertext: ByteArray): Result<Unit>

    suspend fun stop()
}

sealed interface NearbyState {
    data class Found(val endpointId: String, val name: String) : NearbyState
    data class ConfirmCode(val endpointId: String, val code: String) : NearbyState
    data class Connected(val endpointId: String) : NearbyState
    data class Received(val endpointId: String, val ciphertext: ByteArray) : NearbyState
    data class Error(val message: String) : NearbyState
    data object Stopped : NearbyState
}

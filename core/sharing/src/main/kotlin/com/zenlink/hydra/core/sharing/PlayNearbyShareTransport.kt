package com.zenlink.hydra.core.sharing

import android.content.Context
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf

/**
 * Skeleton for Google Play Nearby Connections.
 *
 * Full device-to-device advertising, discovery, and payload transfer requires:
 *  - Play Services on both devices
 *  - Runtime permissions (nearby devices / Bluetooth / location depending on API level)
 *  - User confirmation of a short connection code on both sides
 *
 * This class documents the intended state machine and fails closed until the
 * ConnectionsClient callbacks are implemented and verified on hardware.
 * Prefer [NoOpNearbyShareTransport] in unit tests and offline builds.
 */
class PlayNearbyShareTransport(
    @Suppress("UnusedPrivateProperty") private val context: Context,
) : NearbyShareTransport {

    override fun advertise(displayName: String): Flow<NearbyState> = callbackFlow {
        trySend(
            NearbyState.Error(
                "Play Nearby transport is not fully implemented yet. Use online share or wait for a later release.",
            ),
        )
        awaitClose { }
    }

    override fun discover(): Flow<NearbyState> = flowOf(
        NearbyState.Error("Play Nearby transport is not fully implemented yet."),
    )

    override suspend fun send(endpointId: String, ciphertext: ByteArray): Result<Unit> =
        Result.failure(
            UnsupportedOperationException(
                "Play Nearby send is not implemented. Ciphertext length=${ciphertext.size}, endpoint=$endpointId",
            ),
        )

    override suspend fun stop() = Unit
}

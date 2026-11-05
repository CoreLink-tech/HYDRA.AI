package com.zenlink.hydra.core.sharing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Default Nearby transport used until Play Services Nearby is wired and tested on device.
 * Emits [NearbyState.Stopped] immediately so callers can branch on "not available" without
 * crashing. Unit tests inject this; production can swap in [PlayNearbyShareTransport].
 */
class NoOpNearbyShareTransport : NearbyShareTransport {
    override fun advertise(displayName: String): Flow<NearbyState> =
        flowOf(NearbyState.Error("Nearby sharing is not available in this build."))

    override fun discover(): Flow<NearbyState> =
        flowOf(NearbyState.Error("Nearby sharing is not available in this build."))

    override suspend fun send(endpointId: String, ciphertext: ByteArray): Result<Unit> =
        Result.failure(IllegalStateException("Nearby sharing is not available in this build."))

    override suspend fun stop() = Unit
}

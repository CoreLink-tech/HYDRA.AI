package com.zenlink.hydra.core.sharing

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class NoOpNearbyShareTransportTest {
    private val transport = NoOpNearbyShareTransport()

    @Test
    fun advertiseReportsUnavailable() = runBlocking {
        val state = transport.advertise("Hydra").first()
        assertTrue(state is NearbyState.Error)
    }

    @Test
    fun sendFailsClosed() = runBlocking {
        val result = transport.send("ep1", byteArrayOf(1, 2, 3))
        assertTrue(result.isFailure)
    }
}

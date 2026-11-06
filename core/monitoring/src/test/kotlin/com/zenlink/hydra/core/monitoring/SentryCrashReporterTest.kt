package com.zenlink.hydra.core.monitoring

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SentryCrashReporterTest {
    @Test
    fun consentGateBlocksSentrySink() {
        val sent = mutableListOf<Throwable>()
        val sentry = SentryCrashReporter(dsnPresent = true) { t, _ -> sent += t }
        val gated = ConsentGatedReporter(Consent(false), sentry) { t, c -> sentry.recordError(t, c) }
        gated.recordError(RuntimeException("x"), mapOf("screen" to "chat"))
        assertTrue(sent.isEmpty())
        assertEquals(0, sentry.sendAttempts)
    }

    @Test
    fun consentGrantedAllowsSentrySinkWhenDsnPresent() {
        val sent = mutableListOf<Pair<Throwable, Map<String, String>>>()
        val sentry = SentryCrashReporter(dsnPresent = true) { t, c -> sent += t to c }
        val gated = ConsentGatedReporter(Consent(true), sentry) { t, c -> sentry.recordError(t, c) }
        gated.recordError(RuntimeException("boom"), mapOf("screen" to "chat", "prompt" to "secret"))
        assertEquals(1, sent.size)
        assertEquals(1, sentry.sendAttempts)
        // Scrubber in ConsentGatedReporter drops prompt
        assertEquals(mapOf("screen" to "chat"), sent[0].second)
    }

    @Test
    fun noDsnDoesNotCallSink() {
        val sent = mutableListOf<Throwable>()
        val sentry = SentryCrashReporter(dsnPresent = false) { t, _ -> sent += t }
        sentry.recordError(RuntimeException("x"), emptyMap())
        assertTrue(sent.isEmpty())
        assertEquals(0, sentry.sendAttempts)
    }
}

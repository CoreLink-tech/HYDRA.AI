package com.zenlink.hydra.core.monitoring

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MonitoringTest {

    private val sent = mutableListOf<Pair<Throwable, Map<String, String>>>()
    private val noop = object : CrashReporter {
        override fun recordError(throwable: Throwable, context: Map<String, String>) = Unit
        override fun setEnabled(enabled: Boolean) = Unit
    }

    @Test
    fun consentDefaultsToOff() {
        assertFalse(Consent().granted)
    }

    @Test
    fun nothingSentWithoutConsent() {
        val reporter = ConsentGatedReporter(Consent(false), noop) { t, c -> sent += t to c }
        reporter.recordError(RuntimeException("boom"))
        assertTrue(sent.isEmpty())
    }

    @Test
    fun sentOnceConsentGranted() {
        val reporter = ConsentGatedReporter(Consent(false), noop) { t, c -> sent += t to c }
        reporter.setEnabled(true)
        reporter.recordError(RuntimeException("boom"), mapOf("screen" to "chat"))
        assertEquals(1, sent.size)
        assertEquals(mapOf("screen" to "chat"), sent[0].second)
    }

    @Test
    fun revokingConsentStopsSends() {
        val reporter = ConsentGatedReporter(Consent(true), noop) { t, c -> sent += t to c }
        reporter.setEnabled(false)
        reporter.recordError(RuntimeException("after opt out"))
        assertTrue(sent.isEmpty())
    }

    @Test
    fun promptAndResponseContentIsDropped() {
        val scrubbed = Scrubber.context(
            mapOf("prompt" to "my secret", "Response" to "answer", "title" to "x", "screen" to "chat"),
        )
        assertEquals(mapOf("screen" to "chat"), scrubbed)
    }

    @Test
    fun longValuesAreTruncated() {
        val scrubbed = Scrubber.context(mapOf("code" to "a".repeat(500)))
        assertEquals(Scrubber.MAX_VALUE_LENGTH, scrubbed["code"]!!.length)
    }

    @Test
    fun noopReporterRecordsNothing() {
        NoopCrashReporter().recordError(RuntimeException("x"), mapOf("prompt" to "secret"))
    }
}

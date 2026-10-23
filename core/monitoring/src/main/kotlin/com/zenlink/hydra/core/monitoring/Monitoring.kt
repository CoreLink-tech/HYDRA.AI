package com.zenlink.hydra.core.monitoring

/**
 * Crash and error reporting, off by default. The user opts in from settings.
 *
 * Privacy rules enforced here, not left to callers:
 *  - nothing is sent unless [Consent.granted] is true
 *  - breadcrumbs and error messages are passed through [Scrubber] and free text is dropped
 *  - prompt, response and chat-title content is never a valid attribute value
 */
interface CrashReporter {
    fun recordError(throwable: Throwable, context: Map<String, String> = emptyMap())
    fun setEnabled(enabled: Boolean)
}

/** Holds the user's consent. Reporters must check it before every send. */
class Consent(initial: Boolean = false) {
    @Volatile var granted: Boolean = initial
        private set

    fun update(value: Boolean) {
        granted = value
    }
}

/**
 * Stub used until a real backend (Sentry or Crashlytics) is configured.
 * Records nothing, so the default build never sends a report.
 */
class NoopCrashReporter : CrashReporter {
    override fun recordError(throwable: Throwable, context: Map<String, String>) = Unit
    override fun setEnabled(enabled: Boolean) = Unit
}

/**
 * Wraps a real reporter. Drops the event if consent is missing, and removes any
 * context key or value that could carry user content.
 */
class ConsentGatedReporter(
    private val consent: Consent,
    private val delegate: CrashReporter,
    private val sink: (Throwable, Map<String, String>) -> Unit = { t, c -> delegate.recordError(t, c) },
) : CrashReporter {
    override fun recordError(throwable: Throwable, context: Map<String, String>) {
        if (!consent.granted) return
        sink(throwable, Scrubber.context(context))
    }

    override fun setEnabled(enabled: Boolean) {
        consent.update(enabled)
        delegate.setEnabled(enabled)
    }
}

object Scrubber {
    /** Keys whose values are never sent, regardless of content. */
    private val blockedKeys = setOf("prompt", "response", "message", "content", "title", "chat", "text", "body")

    fun context(raw: Map<String, String>): Map<String, String> =
        raw.filterKeys { it.lowercase() !in blockedKeys }
            .mapValues { (_, v) -> v.take(MAX_VALUE_LENGTH) }

    const val MAX_VALUE_LENGTH = 120
}

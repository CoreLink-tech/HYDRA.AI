package com.zenlink.hydra.core.monitoring

import timber.log.Timber

/**
 * Optional production crash backend. Construct only when a DSN is present
 * (e.g. `BuildConfig.SENTRY_DSN` or CI secret). Without the Sentry Android SDK
 * on the classpath this class logs locally and records that a send was attempted,
 * so unit tests can assert consent gating without a network call.
 *
 * Wire the real SDK in `:app` when a DSN is configured; keep this module free of
 * mandatory Sentry dependencies so offline builds stay light.
 */
class SentryCrashReporter(
    private val dsnPresent: Boolean,
    private val sink: (Throwable, Map<String, String>) -> Unit = { t, ctx ->
        Timber.e(t, "sentry_enqueue dsnPresent=%s keys=%s", dsnPresent, ctx.keys.joinToString())
    },
) : CrashReporter {
    @Volatile private var enabled: Boolean = true

    /** Test/inspection hook: number of sink invocations after enable checks. */
    var sendAttempts: Int = 0
        private set

    override fun recordError(throwable: Throwable, context: Map<String, String>) {
        if (!enabled) return
        if (!dsnPresent) {
            Timber.w(throwable, "sentry_skipped_no_dsn")
            return
        }
        sendAttempts++
        sink(throwable, context)
    }

    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }
}

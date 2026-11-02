package com.zenlink.hydra.core.model

enum class PerformanceMode(val threads: Int, val maxNewTokens: Int) {
    /** Fewer threads and shorter replies so the phone stays cool and the battery lasts. */
    COOL(threads = 2, maxNewTokens = 384),
    STANDARD(threads = 4, maxNewTokens = 768),
}

data class AppSettings(
    val wifiOnlyDownloads: Boolean = true,
    /** Crash reporting is off until the user opts in. Prompts are never reported. */
    val crashReportingOptIn: Boolean = false,
    val activeModelId: String? = null,
    val performanceMode: PerformanceMode = PerformanceMode.STANDARD,
    val onboardingComplete: Boolean = false,
    /** "system", "light" or "dark". Stored as text so core:model needs no UI types. */
    val themeMode: String = "system",
)

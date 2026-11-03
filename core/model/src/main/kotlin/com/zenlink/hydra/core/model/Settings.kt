package com.zenlink.hydra.core.model

enum class PerformanceMode(val threads: Int, val maxNewTokens: Int) {
    /** Fewer threads and shorter replies so the phone stays cool and the battery lasts. */
    COOL(threads = 2, maxNewTokens = 384),
    STANDARD(threads = 4, maxNewTokens = 768),
}

/** Which connection a model download may use. The user picks one every time they start a download. */
enum class DownloadNetwork(val key: String, val label: String, val detail: String) {
    WIFI_ONLY("wifi", "Wi-Fi only", "Waits for Wi-Fi. Uses none of your mobile data."),
    MOBILE_ONLY("mobile", "Mobile data only", "Uses your data plan, even when Wi-Fi is available."),
    ANY("any", "Wi-Fi and mobile data", "Starts on whichever connection is available."),
    ;

    companion object {
        fun fromKey(key: String?): DownloadNetwork = entries.firstOrNull { it.key == key } ?: ANY
    }
}

data class AppSettings(
    /** The last connection choice. It is the preselected option the next time a download starts. */
    val downloadNetwork: DownloadNetwork = DownloadNetwork.ANY,
    /** Crash reporting is off until the user opts in. Prompts are never reported. */
    val crashReportingOptIn: Boolean = false,
    val activeModelId: String? = null,
    val performanceMode: PerformanceMode = PerformanceMode.STANDARD,
    val onboardingComplete: Boolean = false,
    /** "system", "light" or "dark". Stored as text so core:model needs no UI types. */
    val themeMode: String = "system",
)

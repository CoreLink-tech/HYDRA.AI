package com.zenlink.hydra.core.database.repo

import com.zenlink.hydra.core.database.dao.SettingDao
import com.zenlink.hydra.core.database.entities.SettingEntity
import com.zenlink.hydra.core.model.AppSettings
import com.zenlink.hydra.core.model.DownloadNetwork
import com.zenlink.hydra.core.model.PerformanceMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val dao: SettingDao) {

    fun observe(): Flow<AppSettings> = dao.observeAll().map { rows ->
        val map = rows.associate { it.key to it.value }
        AppSettings(
            downloadNetwork = map[KEY_DOWNLOAD_NETWORK]?.let { DownloadNetwork.fromKey(it) }
                // Older versions stored a Wi-Fi-only switch. Keep the choice if the user made one.
                ?: if (map[KEY_WIFI_ONLY]?.toBoolean() == true) DownloadNetwork.WIFI_ONLY else DownloadNetwork.ANY,
            crashReportingOptIn = map[KEY_CRASH_OPT_IN]?.toBoolean() ?: false,
            activeModelId = map[KEY_ACTIVE_MODEL]?.takeIf { it.isNotBlank() },
            performanceMode = map[KEY_PERF]?.let { runCatching { PerformanceMode.valueOf(it) }.getOrNull() }
                ?: PerformanceMode.STANDARD,
            onboardingComplete = map[KEY_ONBOARDED]?.toBoolean() ?: false,
            themeMode = map[KEY_THEME]?.takeIf { it in THEME_MODES } ?: "system",
        )
    }

    suspend fun setDownloadNetwork(value: DownloadNetwork) = put(KEY_DOWNLOAD_NETWORK, value.key)
    suspend fun setCrashReporting(value: Boolean) = put(KEY_CRASH_OPT_IN, value.toString())
    suspend fun setActiveModel(modelId: String?) = put(KEY_ACTIVE_MODEL, modelId.orEmpty())
    suspend fun setPerformanceMode(mode: PerformanceMode) = put(KEY_PERF, mode.name)
    suspend fun setOnboardingComplete(value: Boolean) = put(KEY_ONBOARDED, value.toString())

    suspend fun setThemeMode(value: String) = put(KEY_THEME, if (value in THEME_MODES) value else "system")

    private suspend fun put(key: String, value: String) = dao.put(SettingEntity(key, value))

    private companion object {
        const val KEY_WIFI_ONLY = "wifi_only_downloads"
        const val KEY_DOWNLOAD_NETWORK = "download_network"
        const val KEY_CRASH_OPT_IN = "crash_reporting_opt_in"
        const val KEY_ACTIVE_MODEL = "active_model_id"
        const val KEY_PERF = "performance_mode"
        const val KEY_ONBOARDED = "onboarding_complete"
        const val KEY_THEME = "theme_mode"
        val THEME_MODES = setOf("system", "light", "dark")
    }
}

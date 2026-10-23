package com.zenlink.hydra.core.database.repo

import com.zenlink.hydra.core.database.dao.SettingDao
import com.zenlink.hydra.core.database.entities.SettingEntity
import com.zenlink.hydra.core.model.AppSettings
import com.zenlink.hydra.core.model.PerformanceMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val dao: SettingDao) {

    fun observe(): Flow<AppSettings> = dao.observeAll().map { rows ->
        val map = rows.associate { it.key to it.value }
        AppSettings(
            wifiOnlyDownloads = map[KEY_WIFI_ONLY]?.toBoolean() ?: true,
            crashReportingOptIn = map[KEY_CRASH_OPT_IN]?.toBoolean() ?: false,
            activeModelId = map[KEY_ACTIVE_MODEL]?.takeIf { it.isNotBlank() },
            performanceMode = map[KEY_PERF]?.let { runCatching { PerformanceMode.valueOf(it) }.getOrNull() }
                ?: PerformanceMode.STANDARD,
            onboardingComplete = map[KEY_ONBOARDED]?.toBoolean() ?: false,
        )
    }

    suspend fun setWifiOnly(value: Boolean) = put(KEY_WIFI_ONLY, value.toString())
    suspend fun setCrashReporting(value: Boolean) = put(KEY_CRASH_OPT_IN, value.toString())
    suspend fun setActiveModel(modelId: String?) = put(KEY_ACTIVE_MODEL, modelId.orEmpty())
    suspend fun setPerformanceMode(mode: PerformanceMode) = put(KEY_PERF, mode.name)
    suspend fun setOnboardingComplete(value: Boolean) = put(KEY_ONBOARDED, value.toString())

    private suspend fun put(key: String, value: String) = dao.put(SettingEntity(key, value))

    private companion object {
        const val KEY_WIFI_ONLY = "wifi_only_downloads"
        const val KEY_CRASH_OPT_IN = "crash_reporting_opt_in"
        const val KEY_ACTIVE_MODEL = "active_model_id"
        const val KEY_PERF = "performance_mode"
        const val KEY_ONBOARDED = "onboarding_complete"
    }
}

package com.zenlink.hydra.core.device

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import com.zenlink.hydra.core.model.DeviceProfile
import com.zenlink.hydra.core.model.ModelDescriptor
import com.zenlink.hydra.core.model.ModelTier
import com.zenlink.hydra.core.model.ThermalState
import com.zenlink.hydra.core.model.TierRecommendation

/** Reads the hardware facts the recommender needs. No network, no permissions beyond defaults. */
class DeviceChecker(private val context: Context) {

    fun snapshot(): DeviceProfile {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
        val totalRamMb = (memInfo.totalMem / (1024 * 1024)).toInt()

        val battery = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = battery?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = battery?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val status = battery?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val percent = if (level >= 0 && scale > 0) level * 100 / scale else null
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        return DeviceProfile(
            totalRamMb = totalRamMb,
            freeStorageMb = freeStorageMb(),
            androidSdk = Build.VERSION.SDK_INT,
            primaryAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown",
            supportedAbis = Build.SUPPORTED_ABIS.toList(),
            thermal = thermalState(),
            batteryPercent = percent,
            charging = charging,
            isLowRamDevice = am.isLowRamDevice,
        )
    }

    private fun freeStorageMb(): Long {
        val stat = StatFs(context.filesDir.absolutePath)
        return stat.availableBytes / (1024 * 1024)
    }

    private fun thermalState(): ThermalState {
        // PowerManager.getCurrentThermalStatus exists from API 29. Hydra's minSdk is 26.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return ThermalState.UNKNOWN
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return when (pm.currentThermalStatus) {
            PowerManager.THERMAL_STATUS_NONE -> ThermalState.NONE
            PowerManager.THERMAL_STATUS_LIGHT -> ThermalState.LIGHT
            PowerManager.THERMAL_STATUS_MODERATE -> ThermalState.MODERATE
            PowerManager.THERMAL_STATUS_SEVERE -> ThermalState.SEVERE
            PowerManager.THERMAL_STATUS_CRITICAL,
            PowerManager.THERMAL_STATUS_EMERGENCY,
            PowerManager.THERMAL_STATUS_SHUTDOWN -> ThermalState.CRITICAL
            else -> ThermalState.UNKNOWN
        }
    }
}

/**
 * Pure recommendation logic, kept free of Android types so it is unit-testable on the JVM.
 * Conservative by design: a model is only recommended when it fits with headroom.
 */
object TierRecommender {

    fun recommend(profile: DeviceProfile, catalogue: List<ModelDescriptor>): TierRecommendation {
        val reasons = mutableListOf<String>()
        val fitting = catalogue
            .filter { fits(profile, it, reasons) }
            .map { it.tier }
            .distinct()
            .sortedByDescending { it.ordinal }

        // Hot or nearly empty battery: push one tier down, and say why.
        var recommended = fitting.firstOrNull()
        if (recommended != null && profile.thermal in setOf(ThermalState.SEVERE, ThermalState.CRITICAL)) {
            recommended = fitting.firstOrNull { it.ordinal < recommended!!.ordinal } ?: recommended
            reasons += "Your phone is warm right now, so we suggest a lighter model."
        }
        if (recommended != null && (profile.batteryPercent ?: 100) < 20 && !profile.charging) {
            recommended = fitting.firstOrNull { it.ordinal < recommended!!.ordinal } ?: recommended
            reasons += "Battery is low, so we suggest a lighter model until you charge."
        }
        if (recommended == null) {
            reasons += "This phone does not have enough memory or storage for a local model yet."
        }
        return TierRecommendation(recommended, fitting, reasons.distinct())
    }

    private fun fits(profile: DeviceProfile, model: ModelDescriptor, reasons: MutableList<String>): Boolean {
        val abiOk = profile.supportedAbis.any { it == "arm64-v8a" || it == "armeabi-v7a" || it == "x86_64" }
        if (!abiOk) {
            reasons += "This processor type is not supported yet."
            return false
        }
        if (profile.androidSdk < model.minAndroidSdk) return false
        if (profile.totalRamMb < model.minRamMb) return false
        if (profile.freeStorageMb < model.minFreeStorageMb) return false
        return true
    }
}

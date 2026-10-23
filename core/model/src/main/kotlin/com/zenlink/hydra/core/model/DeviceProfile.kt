package com.zenlink.hydra.core.model

enum class ThermalState { NONE, LIGHT, MODERATE, SEVERE, CRITICAL, UNKNOWN }

/** Snapshot of what the phone can afford right now. Built by core:device. */
data class DeviceProfile(
    val totalRamMb: Int,
    val freeStorageMb: Long,
    val androidSdk: Int,
    val primaryAbi: String,
    val supportedAbis: List<String>,
    val thermal: ThermalState,
    val batteryPercent: Int?,
    val charging: Boolean,
    val isLowRamDevice: Boolean,
)

data class TierRecommendation(
    val recommended: ModelTier?,
    /** Tiers whose model fits this device at all, highest first. */
    val fitting: List<ModelTier>,
    /** Plain-language reasons shown in onboarding. */
    val reasons: List<String>,
)

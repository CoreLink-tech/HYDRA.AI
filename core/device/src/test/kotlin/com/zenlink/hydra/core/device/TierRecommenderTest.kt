package com.zenlink.hydra.core.device

import com.zenlink.hydra.core.model.DeviceProfile
import com.zenlink.hydra.core.model.ModelCatalog
import com.zenlink.hydra.core.model.ModelTier
import com.zenlink.hydra.core.model.ThermalState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TierRecommenderTest {

    private fun profile(
        ramMb: Int = 8192,
        storageMb: Long = 10_000,
        sdk: Int = 34,
        thermal: ThermalState = ThermalState.NONE,
        battery: Int? = 80,
        charging: Boolean = false,
    ) = DeviceProfile(
        totalRamMb = ramMb,
        freeStorageMb = storageMb,
        androidSdk = sdk,
        primaryAbi = "arm64-v8a",
        supportedAbis = listOf("arm64-v8a"),
        thermal = thermal,
        batteryPercent = battery,
        charging = charging,
        isLowRamDevice = false,
    )

    @Test
    fun highEndPhoneGetsAdvanced() {
        val r = TierRecommender.recommend(profile(ramMb = 12_000, sdk = 34), ModelCatalog.all)
        assertEquals(ModelTier.ADVANCED, r.recommended)
    }

    @Test
    fun midRangePhoneGetsBalanced() {
        val r = TierRecommender.recommend(profile(ramMb = 6_500, sdk = 34), ModelCatalog.all)
        assertEquals(ModelTier.BALANCED, r.recommended)
    }

    @Test
    fun lowRamPhoneGetsFast() {
        val r = TierRecommender.recommend(profile(ramMb = 3_500), ModelCatalog.all)
        assertEquals(ModelTier.FAST, r.recommended)
    }

    @Test
    fun heatStepsDownOneTier() {
        val r = TierRecommender.recommend(
            profile(ramMb = 12_000, thermal = ThermalState.SEVERE), ModelCatalog.all,
        )
        assertEquals(ModelTier.BALANCED, r.recommended)
    }

    @Test
    fun lowBatteryStepsDownUnlessCharging() {
        val low = TierRecommender.recommend(profile(ramMb = 12_000, battery = 10), ModelCatalog.all)
        assertEquals(ModelTier.BALANCED, low.recommended)
        val charging = TierRecommender.recommend(
            profile(ramMb = 12_000, battery = 10, charging = true), ModelCatalog.all,
        )
        assertEquals(ModelTier.ADVANCED, charging.recommended)
    }

    @Test
    fun tinyPhoneGetsNoRecommendation() {
        val r = TierRecommender.recommend(profile(ramMb = 1_000, storageMb = 100), ModelCatalog.all)
        assertNull(r.recommended)
    }
}

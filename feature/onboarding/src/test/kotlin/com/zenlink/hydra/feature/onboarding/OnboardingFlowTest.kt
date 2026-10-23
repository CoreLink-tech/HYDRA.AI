package com.zenlink.hydra.feature.onboarding

import com.zenlink.hydra.core.model.DeviceProfile
import com.zenlink.hydra.core.model.ModelCatalog
import com.zenlink.hydra.core.model.ThermalState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class OnboardingFlowTest {

    private val capable = DeviceProfile(
        totalRamMb = 12000, freeStorageMb = 20000, androidSdk = 34, primaryAbi = "arm64-v8a",
        supportedAbis = listOf("arm64-v8a"), thermal = ThermalState.NONE, batteryPercent = 80,
        charging = false, isLowRamDevice = false,
    )

    @Test
    fun happyPathWalksEveryStepInOrder() {
        var s = OnboardingState()
        s = OnboardingFlow.onDeviceChecked(s, capable)
        assertEquals(OnboardingStep.RECOMMENDED, s.step)
        assertNotNull(s.chosenModelId)
        s = OnboardingFlow.startDownload(s)
        assertEquals(OnboardingStep.DOWNLOADING, s.step)
        s = OnboardingFlow.onProgress(s, 0.5f)
        assertEquals(0.5f, s.downloadFraction, 0.0001f)
        s = OnboardingFlow.onDownloadFinished(s)
        assertEquals(OnboardingStep.FIRST_CHAT, s.step)
        s = OnboardingFlow.finish(s)
        assertEquals(OnboardingStep.DONE, s.step)
    }

    @Test
    fun downloadCannotStartBeforeRecommendations() {
        val s = OnboardingFlow.startDownload(OnboardingState())
        assertEquals(OnboardingStep.WELCOME, s.step)
    }

    @Test
    fun failedDownloadReturnsToRecommendedWithReason() {
        val s0 = OnboardingFlow.startDownload(OnboardingFlow.onDeviceChecked(OnboardingState(), capable))
        val s = OnboardingFlow.onDownloadFailed(s0, "No network")
        assertEquals(OnboardingStep.RECOMMENDED, s.step)
        assertEquals("No network", s.error)
    }

    @Test
    fun unknownModelIsRejected() {
        val s = OnboardingFlow.onDeviceChecked(OnboardingState(), capable)
        assertEquals("Unknown model.", OnboardingFlow.chooseModel(s, "nope").error)
    }

    @Test
    fun finishOnlyFromFirstChat() {
        val s = OnboardingFlow.finish(OnboardingState(step = OnboardingStep.RECOMMENDED))
        assertEquals(OnboardingStep.RECOMMENDED, s.step)
    }

    @Test
    fun selectedDescriptorResolvesFromCatalogue() {
        val s = OnboardingFlow.onDeviceChecked(OnboardingState(), capable)
        assertEquals(ModelCatalog.byId(s.chosenModelId!!), OnboardingFlow.selectedDescriptor(s))
        assertNull(OnboardingFlow.selectedDescriptor(OnboardingState()))
    }
}

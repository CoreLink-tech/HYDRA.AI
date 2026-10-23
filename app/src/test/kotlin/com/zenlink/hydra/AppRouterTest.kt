package com.zenlink.hydra

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppRouterTest {

    @Test
    fun onboardingIsFirstUntilCompleted() {
        assertEquals(Route.Onboarding, AppRouter.startRoute(onboardingComplete = false))
        assertEquals(Route.Chat, AppRouter.startRoute(onboardingComplete = true))
    }

    @Test
    fun bottomBarHiddenDuringOnboarding() {
        assertFalse(AppRouter.showsBottomBar(Route.Onboarding))
        assertTrue(AppRouter.showsBottomBar(Route.Chat))
        assertTrue(AppRouter.showsBottomBar(Route.Models))
        assertTrue(AppRouter.showsBottomBar(Route.Settings))
    }

    @Test
    fun onlineShowsNoOfflineBanner() {
        assertNull(AppRouter.offlineIndicator(online = true, hasDownloadedModel = false))
    }

    @Test
    fun offlineWithModelSaysItStillWorks() {
        val msg = AppRouter.offlineIndicator(online = false, hasDownloadedModel = true)!!
        assertTrue(msg.contains("downloaded model"))
    }

    @Test
    fun offlineWithoutModelAsksToDownload() {
        val msg = AppRouter.offlineIndicator(online = false, hasDownloadedModel = false)!!
        assertTrue(msg.contains("Download a model"))
    }

    @Test
    fun onboardingIsNotATab() {
        assertNull(AppRouter.tabFor(Route.Onboarding.path))
        assertEquals(Route.Models, AppRouter.tabFor(Route.Models.path))
    }
}

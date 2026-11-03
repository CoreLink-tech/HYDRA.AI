package com.zenlink.hydra.feature.settings

import com.zenlink.hydra.core.model.Entitlement
import com.zenlink.hydra.core.model.Plan
import com.zenlink.hydra.core.model.DownloadNetwork
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsPlannerTest {

    private val now = 1_000_000L

    @Test
    fun freePlanStatusSaysOfflineStillWorks() {
        val s = SettingsPlanner.subscriptionStatus(Entitlement.FREE, now)
        assertEquals("Free", s.title)
        assertFalse(s.proActive)
    }

    @Test
    fun activeProShowsProStatus() {
        val pro = Entitlement(Plan.PRO, now + 10_000)
        val s = SettingsPlanner.subscriptionStatus(pro, now)
        assertEquals("Pro", s.title)
        assertTrue(s.proActive)
    }

    @Test
    fun lapsedProSaysDownloadsStillWork() {
        val lapsed = Entitlement(Plan.PRO, now - 1)
        val s = SettingsPlanner.subscriptionStatus(lapsed, now)
        assertFalse(s.proActive)
        assertTrue(s.detail.contains("still work offline"))
    }

    @Test
    fun shareCapacityUsesFreeLimitWhenNotPro() {
        assertEquals("1 of 3 live shares used", SettingsPlanner.shareCapacityLine(1, Entitlement.FREE, now))
    }

    @Test
    fun shareCapacityUsesProLimitWhenPro() {
        val pro = Entitlement(Plan.PRO, now + 1)
        assertEquals("4 of 50 live shares used", SettingsPlanner.shareCapacityLine(4, pro, now))
    }

    @Test
    fun wifiOnlyOnHasNoWarning() {
        assertNull(SettingsPlanner.networkWarning(DownloadNetwork.WIFI_ONLY))
    }

    @Test
    fun wifiOnlyOffWarnsAboutData() {
        assertTrue(SettingsPlanner.networkWarning(DownloadNetwork.MOBILE_ONLY)!!.contains("mobile data"))
    }

    @Test
    fun deletionRequiresExactConfirmWord() {
        assertTrue(SettingsPlanner.deletionAllowed("DELETE"))
        assertTrue(SettingsPlanner.deletionAllowed("  DELETE "))
        assertFalse(SettingsPlanner.deletionAllowed("delete"))
        assertFalse(SettingsPlanner.deletionAllowed(""))
    }

    @Test
    fun deletionStepsCoverModelsChatsAndShares() {
        val steps = SettingsPlanner.deletionSteps().joinToString(" ")
        assertTrue(steps.contains("chat history"))
        assertTrue(steps.contains("models"))
        assertTrue(steps.contains("share"))
    }
}

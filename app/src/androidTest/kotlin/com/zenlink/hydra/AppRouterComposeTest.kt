package com.zenlink.hydra

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Lightweight Compose UI test. Validates that offline banner copy from [AppRouter]
 * can be rendered in a Compose hierarchy on device/emulator.
 */
@RunWith(AndroidJUnit4::class)
class AppRouterComposeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun offlineBannerWithModelIsVisible() {
        val message = AppRouter.offlineIndicator(online = false, hasDownloadedModel = true)!!
        composeRule.setContent { Text(message) }
        composeRule.onNodeWithText(message, substring = true).assertIsDisplayed()
    }

    @Test
    fun offlineBannerWithoutModelAsksToDownload() {
        val message = AppRouter.offlineIndicator(online = false, hasDownloadedModel = false)!!
        composeRule.setContent { Text(message) }
        composeRule.onNodeWithText("Download a model", substring = true).assertIsDisplayed()
    }
}

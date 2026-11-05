package com.zenlink.hydra

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Minimal instrumented smoke test. Confirms the app process can start on a device/emulator.
 * Full Compose UI journeys belong in later phases once the native llama binary is available
 * in CI.
 */
@RunWith(AndroidJUnit4::class)
class SmokeInstrumentedTest {
    @Test
    fun appContextPackageNameIsHydra() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        // debug builds append ".debug" via applicationIdSuffix
        assertTrue(
            "unexpected package: ${ctx.packageName}",
            ctx.packageName == "com.zenlink.hydra" || ctx.packageName == "com.zenlink.hydra.debug",
        )
    }
}

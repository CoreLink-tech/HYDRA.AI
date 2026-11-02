package com.zenlink.hydra

import com.zenlink.hydra.core.designsystem.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {
    @Test
    fun storedKeysMapToModes() {
        assertEquals(ThemeMode.DARK, ThemeMode.fromKey("dark"))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromKey("light"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromKey("system"))
    }

    @Test
    fun unknownOrMissingKeyFollowsTheSystem() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromKey(null))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromKey("sepia"))
    }

    @Test
    fun keysRoundTrip() {
        ThemeMode.entries.forEach { assertEquals(it, ThemeMode.fromKey(it.key)) }
    }
}

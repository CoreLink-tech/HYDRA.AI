package com.zenlink.hydra.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic colour roles for HYDRA.AI. Screens read these through `MaterialTheme.hydra` instead of
 * hard-coding hex values, so the dark and light themes stay in step with each other and with the
 * website (web/src/app/globals.css defines the same tokens).
 */
@Immutable
class HydraColors(
    val isDark: Boolean,
    // Surfaces, from the page background up to the highest card.
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val card: Color,
    val border: Color,
    val borderStrong: Color,
    // Brand accent.
    val accent: Color,
    val accentStrong: Color,
    val onAccent: Color,
    val accentSoft: Color,
    // Text.
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    // Status.
    val success: Color,
    val warning: Color,
    val warningSoft: Color,
    val danger: Color,
    val dangerSoft: Color,
    val scrim: Color,
)

val DarkHydraColors = HydraColors(
    isDark = true,
    background = Color(0xFF0B0E0D),
    surface = Color(0xFF111614),
    surfaceElevated = Color(0xFF171E1A),
    card = Color(0xFF1B231F),
    border = Color(0xFF26302A),
    borderStrong = Color(0xFF34413A),
    accent = Color(0xFF66C995),
    accentStrong = Color(0xFF42A982),
    onAccent = Color(0xFF06150D),
    accentSoft = Color(0xFF1C2F25),
    textPrimary = Color(0xFFF3F6F3),
    textSecondary = Color(0xFFA0ABA4),
    textMuted = Color(0xFF78857D),
    success = Color(0xFF66C995),
    warning = Color(0xFFE5B567),
    warningSoft = Color(0xFF2E2615),
    danger = Color(0xFFF2877D),
    dangerSoft = Color(0xFF31191A),
    scrim = Color(0xCC000000),
)

val LightHydraColors = HydraColors(
    isDark = false,
    background = Color(0xFFFBFAF6),
    surface = Color(0xFFF3F5F0),
    surfaceElevated = Color(0xFFFFFFFF),
    card = Color(0xFFFFFFFF),
    border = Color(0xFFDDE3DB),
    borderStrong = Color(0xFFC3CCC2),
    accent = Color(0xFF1F7A4F),
    accentStrong = Color(0xFF17613F),
    onAccent = Color(0xFFFFFFFF),
    accentSoft = Color(0xFFE2F0E7),
    textPrimary = Color(0xFF111A15),
    textSecondary = Color(0xFF45524A),
    textMuted = Color(0xFF66726A),
    success = Color(0xFF1F7A4F),
    warning = Color(0xFF8F5F00),
    warningSoft = Color(0xFFFBF0D6),
    danger = Color(0xFFB3261E),
    dangerSoft = Color(0xFFFBE3E1),
    scrim = Color(0x99000000),
)

val LocalHydraColors = staticCompositionLocalOf { DarkHydraColors }

package com.zenlink.hydra.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HydraDark = darkColorScheme(
    primary = Accent,
    onPrimary = AccentOn,
    primaryContainer = Ink3,
    onPrimaryContainer = TextHi,
    secondary = TextMid,
    onSecondary = Ink0,
    background = Ink0,
    onBackground = TextHi,
    surface = Ink1,
    onSurface = TextHi,
    surfaceVariant = Ink2,
    onSurfaceVariant = TextMid,
    outline = Line,
    outlineVariant = Ink3,
    error = Danger,
    onError = Ink0,
    errorContainer = Color(0xFF3A1B19),
    onErrorContainer = Danger,
)

/** The app is dark-only by design. There is no light theme to fall back to. */
@Composable
fun HydraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HydraDark,
        typography = HydraTypography,
        shapes = HydraShapes,
        content = content,
    )
}

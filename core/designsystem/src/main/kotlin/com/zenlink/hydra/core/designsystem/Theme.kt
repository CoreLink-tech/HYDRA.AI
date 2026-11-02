package com.zenlink.hydra.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier

/** How the app picks light or dark. SYSTEM follows the phone. */
enum class ThemeMode(val key: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        /** Maps the stored setting text back to a mode. Unknown or missing values mean SYSTEM. */
        fun fromKey(key: String?): ThemeMode = entries.firstOrNull { it.key == key } ?: SYSTEM
    }
}

/** Whether this mode renders dark right now (SYSTEM follows the phone). */
@Composable
fun ThemeMode.resolveDark(): Boolean = when (this) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

/** The HYDRA.AI colour roles for the current theme, e.g. `MaterialTheme.hydra.accent`. */
val MaterialTheme.hydra: HydraColors
    @Composable
    @ReadOnlyComposable
    get() = LocalHydraColors.current

private fun HydraColors.toColorScheme(): ColorScheme =
    if (isDark) {
        darkColorScheme(
            primary = accent,
            onPrimary = onAccent,
            primaryContainer = accentSoft,
            onPrimaryContainer = accent,
            secondary = accentStrong,
            onSecondary = onAccent,
            secondaryContainer = surfaceElevated,
            onSecondaryContainer = textPrimary,
            tertiary = warning,
            background = background,
            onBackground = textPrimary,
            surface = surface,
            onSurface = textPrimary,
            surfaceVariant = card,
            onSurfaceVariant = textSecondary,
            surfaceTint = accent,
            surfaceContainerLowest = background,
            surfaceContainerLow = surface,
            surfaceContainer = surfaceElevated,
            surfaceContainerHigh = surfaceElevated,
            surfaceContainerHighest = card,
            outline = borderStrong,
            outlineVariant = border,
            error = danger,
            onError = background,
            errorContainer = dangerSoft,
            onErrorContainer = danger,
            scrim = scrim,
        )
    } else {
        lightColorScheme(
            primary = accent,
            onPrimary = onAccent,
            primaryContainer = accentSoft,
            onPrimaryContainer = accentStrong,
            secondary = accentStrong,
            onSecondary = onAccent,
            secondaryContainer = surface,
            onSecondaryContainer = textPrimary,
            tertiary = warning,
            background = background,
            onBackground = textPrimary,
            surface = surface,
            onSurface = textPrimary,
            surfaceVariant = card,
            onSurfaceVariant = textSecondary,
            surfaceTint = accent,
            surfaceContainerLowest = background,
            surfaceContainerLow = surface,
            surfaceContainer = surfaceElevated,
            surfaceContainerHigh = surfaceElevated,
            surfaceContainerHighest = card,
            outline = borderStrong,
            outlineVariant = border,
            error = danger,
            onError = onAccent,
            errorContainer = dangerSoft,
            onErrorContainer = danger,
            scrim = scrim,
        )
    }

/**
 * App-wide theme. Material 3 supplies behaviour (ripples, text fields, dialogs) while the colours,
 * type and shapes come from HYDRA.AI tokens, so nothing falls back to the default Material look.
 */
@Composable
fun HydraTheme(
    mode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = mode.resolveDark()
    val colors = if (dark) DarkHydraColors else LightHydraColors
    val reducedMotion = rememberSystemReducedMotion()
    CompositionLocalProvider(
        LocalHydraColors provides colors,
        LocalReducedMotion provides reducedMotion,
    ) {
        MaterialTheme(
            colorScheme = colors.toColorScheme(),
            typography = HydraTypography,
            shapes = HydraMaterialShapes,
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = colors.background,
                contentColor = colors.textPrimary,
                content = content,
            )
        }
    }
}

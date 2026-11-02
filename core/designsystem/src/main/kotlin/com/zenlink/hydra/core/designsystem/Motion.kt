package com.zenlink.hydra.core.designsystem

import android.provider.Settings
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext

/** True when the user turned system animations off. Components fall back to instant changes. */
val LocalReducedMotion = staticCompositionLocalOf { false }

object HydraMotion {
    const val Short = 150
    const val Medium = 260
    const val Long = 420
}

@Composable
fun rememberSystemReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        }.getOrDefault(false)
    }
}

/** A tween that becomes an instant change when reduced motion is on. */
fun <T> hydraTween(
    reduced: Boolean,
    durationMillis: Int = HydraMotion.Medium,
    easing: Easing = FastOutSlowInEasing,
): FiniteAnimationSpec<T> = if (reduced) snap() else tween(durationMillis, easing = easing)

/** Slight press-down feedback for tappable surfaces. */
@Composable
fun Modifier.pressScale(interactionSource: InteractionSource, pressedScale: Float = 0.97f): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val reduced = LocalReducedMotion.current
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduced) pressedScale else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 600f),
        label = "pressScale",
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

package com.zenlink.hydra.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// The system sans keeps the APK small and renders every script a phone user might type.
// Monospace is used for code blocks and technical values.
val HydraMono = FontFamily.Monospace

val HydraTypography = Typography(
    displaySmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 30.sp, lineHeight = 36.sp, letterSpacing = (-0.6).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = (-0.4).sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.3).sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp, letterSpacing = (-0.2).sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 25.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
)

/** Spacing scale in dp. Use these instead of one-off numbers so rhythm stays consistent. */
object HydraSpacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 48.dp
}

/** Named shapes. Corners are rounded but never fully pill-shaped, apart from tiny status dots. */
object HydraShapes {
    val tag = RoundedCornerShape(6.dp)
    val small = RoundedCornerShape(8.dp)
    val button = RoundedCornerShape(12.dp)
    val card = RoundedCornerShape(16.dp)
    val input = RoundedCornerShape(16.dp)
    val sheet = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    val bubble = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 6.dp)
}

val HydraMaterialShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

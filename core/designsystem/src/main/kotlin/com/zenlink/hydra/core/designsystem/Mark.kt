package com.zenlink.hydra.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The HYDRA.AI mark: one stem that splits into three heads. The same geometry is used by the launcher
 * icon and the splash screen (res/drawable/ic_hydra_mark.xml), so keep the three in step.
 */
@Composable
fun HydraMark(
    modifier: Modifier = Modifier,
    markSize: Dp = 32.dp,
    color: Color = MaterialTheme.hydra.accent,
) {
    Canvas(modifier.size(markSize)) {
        val s = this.size.minDimension / 24f
        val path = Path().apply {
            moveTo(12f * s, 20.5f * s)
            lineTo(12f * s, 13.5f * s)
            moveTo(12f * s, 13.5f * s)
            cubicTo(12f * s, 10f * s, 8.5f * s, 9.5f * s, 6f * s, 6.5f * s)
            moveTo(12f * s, 13.5f * s)
            lineTo(12f * s, 5f * s)
            moveTo(12f * s, 13.5f * s)
            cubicTo(12f * s, 10f * s, 15.5f * s, 9.5f * s, 18f * s, 6.5f * s)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 1.9f * s, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        listOf(Offset(6f * s, 5.8f * s), Offset(12f * s, 4.2f * s), Offset(18f * s, 5.8f * s)).forEach {
            drawCircle(color = color, radius = 1.7f * s, center = it)
        }
    }
}

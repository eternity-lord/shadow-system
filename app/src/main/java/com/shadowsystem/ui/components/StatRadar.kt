package com.shadowsystem.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.shadowsystem.ui.theme.ShadowColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Stat radar drawn with Canvas. [values] are 0..1 (one per axis, starting at the top). */
@Composable
fun StatRadar(values: List<Float>, modifier: Modifier = Modifier) {
    val n = values.size
    if (n < 3) return
    Canvas(modifier = modifier.size(200.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = min(cx, cy) * 0.8f
        fun point(i: Int, f: Float): Offset {
            val angle = -PI / 2 + 2 * PI * i / n
            return Offset(cx + (cos(angle) * r * f).toFloat(), cy + (sin(angle) * r * f).toFloat())
        }
        fun polygon(scale: (Int) -> Float): Path {
            val path = Path()
            for (i in 0 until n) {
                val p = point(i, scale(i))
                if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
            }
            path.close()
            return path
        }
        for (ring in 1..4) {
            drawPath(polygon { ring / 4f }, color = ShadowColors.TextMuted.copy(alpha = 0.35f), style = Stroke(width = 1.dp.toPx()))
        }
        for (i in 0 until n) {
            drawLine(
                color = ShadowColors.TextMuted.copy(alpha = 0.35f),
                start = Offset(cx, cy),
                end = point(i, 1f),
                strokeWidth = 1.dp.toPx()
            )
        }
        val data = polygon { values[it].coerceIn(0.05f, 1f) }
        drawPath(data, color = ShadowColors.Primary.copy(alpha = 0.35f))
        drawPath(data, color = ShadowColors.Primary, style = Stroke(width = 2.dp.toPx()))
    }
}

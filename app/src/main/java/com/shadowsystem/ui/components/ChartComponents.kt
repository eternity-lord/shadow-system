package com.shadowsystem.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.shadowsystem.ui.theme.ShadowColors

private val palette = listOf(
    Color(0xFF7C4DFF), Color(0xFF00E5FF), Color(0xFFFFD54F), Color(0xFFFF5252),
    Color(0xFF69F0AE), Color(0xFFFF80AB), Color(0xFF82B1FF), Color(0xFFFFAB40)
)

fun chartColor(index: Int): Color = palette[index % palette.size]

/** Donut chart of labelled values. Empty input draws an empty ring. */
@Composable
fun DonutChart(values: List<Pair<String, Long>>, modifier: Modifier = Modifier) {
    val total = values.sumOf { it.second }.toFloat()
    Canvas(modifier = modifier.size(170.dp)) {
        val stroke = 26.dp.toPx()
        val inset = stroke / 2
        val arcSize = Size(size.width - stroke, size.height - stroke)
        drawArc(
            color = ShadowColors.SurfaceHigh,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(width = stroke)
        )
        var start = -90f
        if (total > 0f) {
            values.forEachIndexed { i, (_, v) ->
                val sweep = 360f * v / total
                drawArc(
                    color = chartColor(i),
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = stroke)
                )
                start += sweep
            }
        }
    }
}

/** Vertical bars with labels underneath. */
@Composable
fun BarChart(labels: List<String>, values: List<Long>, modifier: Modifier = Modifier) {
    val max = (values.maxOrNull() ?: 0L).coerceAtLeast(1L)
    Row(
        modifier = modifier.fillMaxWidth().height(160.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        values.forEachIndexed { i, v ->
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                val fraction = (v.toFloat() / max).coerceIn(0.03f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp * fraction)
                        .clip(RoundedCornerShape(6.dp))
                        .background(chartColor(0))
                )
                Text(labels.getOrElse(i) { "" }, style = MaterialTheme.typography.bodyMedium, color = ShadowColors.TextMuted)
            }
        }
    }
}

/** Polyline over the values, scaled to the largest one. */
@Composable
fun LineChart(values: List<Long>, modifier: Modifier = Modifier) {
    val max = (values.maxOrNull() ?: 0L).coerceAtLeast(1L).toFloat()
    Canvas(modifier = modifier.fillMaxWidth().height(140.dp)) {
        if (values.size < 2) return@Canvas
        val path = Path()
        val dx = size.width / (values.size - 1)
        values.forEachIndexed { i, v ->
            val x = i * dx
            val y = size.height - (v / max) * size.height
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = ShadowColors.Secondary, style = Stroke(width = 4.dp.toPx()))
    }
}

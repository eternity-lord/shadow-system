package com.shadowsystem.core

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Resolves art assets by name (spec section 14).
 * If a drawable with that name exists in res/drawable-nodpi, it is used;
 * otherwise a procedural placeholder is drawn with Compose Canvas
 * (soldier-specific color, stage-dependent size/details).
 * Real images can replace placeholders later with no code change.
 */
object AssetRegistry {

    fun drawableId(context: Context, name: String): Int =
        context.resources.getIdentifier(name, "drawable", context.packageName)

    fun hasAsset(context: Context, name: String): Boolean = drawableId(context, name) != 0

    fun soldierAssetName(key: String, stage: Int): String = "shadow_${key}_stage$stage"

    /** The highest stage art that exists at or below [stage]; falls back to stage 1. */
    fun soldierBestAsset(context: Context, key: String, stage: Int): String {
        for (s in stage.coerceIn(1, 5) downTo 1) {
            val name = soldierAssetName(key, s)
            if (hasAsset(context, name)) return name
        }
        return soldierAssetName(key, 1)
    }
    fun wisdomAssetName(key: String, tier: Int): String = "wisdom_${key}_$tier"

    /** Overlay for the highest wisdom tier (1..3) that has art, at or below [tier]; null when none exists. */
    fun wisdomBestAsset(context: Context, key: String, tier: Int): String? {
        for (t in tier.coerceIn(0, 3) downTo 1) {
            val name = wisdomAssetName(key, t)
            if (hasAsset(context, name)) return name
        }
        return null
    }
    fun monsterAssetName(rank: String, n: Int): String = "monster_${rank.lowercase()}_$n"
    fun gateAssetName(rank: String): String = "gate_${rank.lowercase()}"
    fun rankAssetName(rank: String): String = "rank_${rank.lowercase()}"
    fun statAssetName(stat: String): String = "stat_${stat.lowercase()}"
    fun categoryAssetName(name: String): String = "icon_cat_$name"
    fun backgroundAssetName(name: String): String = "bg_$name"
}

/** Shows the real drawable if it exists, otherwise a procedural placeholder. */
@Composable
fun AssetImage(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    placeholderColor: Color = Color(0xFF7C4DFF),
    stage: Int = 1,
    contentDescription: String? = null,
    tint: Color? = null
) {
    val context = LocalContext.current
    val resId = AssetRegistry.drawableId(context, name)
    if (resId != 0) {
        Image(
            painter = painterResource(resId),
            contentDescription = contentDescription,
            modifier = modifier.size(size),
            contentScale = ContentScale.Fit,
            colorFilter = tint?.let { androidx.compose.ui.graphics.ColorFilter.tint(it, androidx.compose.ui.graphics.BlendMode.Modulate) }
        )
    } else {
        ProceduralPlaceholder(
            modifier = modifier.size(size),
            color = placeholderColor,
            stage = stage,
            label = name
        )
    }
}

/**
 * Procedural placeholder: a shadow silhouette whose size and detail grow
 * with [stage] (1..5), drawn in the soldier's color.
 */
@Composable
fun ProceduralPlaceholder(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF7C4DFF),
    stage: Int = 1,
    label: String = ""
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        // Ground shadow
        drawOval(
            color = color.copy(alpha = 0.25f),
            topLeft = androidx.compose.ui.geometry.Offset(cx - w * 0.32f, h * 0.82f),
            size = androidx.compose.ui.geometry.Size(w * 0.64f, h * 0.1f)
        )
        // Body silhouette grows with stage
        val bodyScale = 0.45f + stage * 0.09f
        val bodyW = w * bodyScale
        val bodyH = h * (0.42f + stage * 0.07f)
        val bodyTop = h * 0.88f - bodyH
        val bodyPath = Path().apply {
            moveTo(cx, bodyTop)
            quadraticBezierTo(cx + bodyW * 0.55f, bodyTop + bodyH * 0.25f, cx + bodyW * 0.5f, bodyTop + bodyH)
            lineTo(cx - bodyW * 0.5f, bodyTop + bodyH)
            quadraticBezierTo(cx - bodyW * 0.55f, bodyTop + bodyH * 0.25f, cx, bodyTop)
            close()
        }
        drawPath(bodyPath, color = color.copy(alpha = 0.85f))
        // Head
        val headR = w * (0.09f + stage * 0.012f)
        drawCircle(
            color = color,
            radius = headR,
            center = androidx.compose.ui.geometry.Offset(cx, bodyTop - headR * 0.6f)
        )
        // Eyes glow more with stage
        val eyeAlpha = 0.5f + stage * 0.1f
        drawCircle(
            color = Color.White.copy(alpha = eyeAlpha),
            radius = headR * 0.18f,
            center = androidx.compose.ui.geometry.Offset(cx - headR * 0.35f, bodyTop - headR * 0.6f)
        )
        drawCircle(
            color = Color.White.copy(alpha = eyeAlpha),
            radius = headR * 0.18f,
            center = androidx.compose.ui.geometry.Offset(cx + headR * 0.35f, bodyTop - headR * 0.6f)
        )
        // Stage details: weapon line / aura ring / crown
        if (stage >= 2) {
            drawLine(
                color = color.copy(alpha = 0.9f),
                start = androidx.compose.ui.geometry.Offset(cx + bodyW * 0.45f, bodyTop + bodyH * 0.3f),
                end = androidx.compose.ui.geometry.Offset(cx + bodyW * 0.75f, bodyTop - bodyH * 0.15f),
                strokeWidth = w * 0.03f
            )
        }
        if (stage >= 3) {
            drawCircle(
                color = color.copy(alpha = 0.35f),
                radius = w * 0.42f,
                center = androidx.compose.ui.geometry.Offset(cx, h * 0.5f),
                style = Stroke(width = w * 0.02f)
            )
        }
        if (stage >= 5) {
            // Crown spikes
            val crownY = bodyTop - headR * 1.6f
            for (i in -1..1) {
                drawLine(
                    color = Color(0xFFFFD54F),
                    start = androidx.compose.ui.geometry.Offset(cx + i * headR * 0.5f, crownY),
                    end = androidx.compose.ui.geometry.Offset(cx + i * headR * 0.5f, crownY - headR * 0.5f),
                    strokeWidth = w * 0.02f
                )
            }
        }
        // Label initial for identification in placeholder mode
        if (label.isNotEmpty()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.15f),
                radius = w * 0.06f,
                center = androidx.compose.ui.geometry.Offset(w * 0.85f, h * 0.15f)
            )
        }
    }
}

/** Box wrapper keeping a fixed size for asset slots. */
@Composable
fun AssetSlot(name: String, size: Dp = 96.dp, color: Color = Color(0xFF7C4DFF), stage: Int = 1) {
    Box(modifier = Modifier.size(size)) {
        AssetImage(name = name, size = size, placeholderColor = color, stage = stage)
    }
}

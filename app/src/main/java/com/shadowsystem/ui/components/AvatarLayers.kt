package com.shadowsystem.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.shadowsystem.core.AssetImage
import com.shadowsystem.core.AssetRegistry
import com.shadowsystem.core.AvatarLayerRules

/**
 * Draws the equipped layers over the avatar:
 * - outfit: its look image replaces the picture (handled by the caller), otherwise a vector torso band;
 * - weapon: a sprite at the lower right when its image exists, otherwise a vector blade;
 * - frame: a vector border.
 * The modifier sets the size; drawing scales with it.
 */
@Composable
fun AvatarLayers(equipped: Set<String>, modifier: Modifier = Modifier.size(96.dp), drawOutfit: Boolean = true) {
    val context = LocalContext.current
    val outfit = if (drawOutfit) AvatarLayerRules.equippedIn(equipped, "outfit_")?.let { AvatarLayerRules.outfitArgb(it) } else null
    val hairKey = AvatarLayerRules.equippedIn(equipped, "hair_")
    val headgearKey = AvatarLayerRules.equippedIn(equipped, "headgear_")
    val weaponKey = AvatarLayerRules.equippedIn(equipped, "weapon_")
    val weaponSprite = weaponKey
        ?.let { "avatar_weapon_" + it.removePrefix("weapon_") }
        ?.takeIf { AssetRegistry.hasAsset(context, it) }
    val frameKey = AvatarLayerRules.equippedIn(equipped, "frame_")
    val frameSprite = frameKey?.let { "avatar_$it" }?.takeIf { AssetRegistry.hasAsset(context, it) }
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            outfit?.let { c ->
                drawRoundRect(
                    color = Color(c),
                    topLeft = Offset(w * 0.28f, h * 0.52f),
                    size = Size(w * 0.44f, h * 0.34f),
                    cornerRadius = CornerRadius(w * 0.08f),
                    alpha = 0.85f
                )
            }
            // Hair and headgear sit on the head area of the picture. Vector shapes, so they align by construction.
            hairKey?.let { k -> AvatarLayerRules.hairArgb(k)?.let { c -> drawHair(k, Color(c), w, h) } }
            headgearKey?.let { k -> AvatarLayerRules.headgearArgb(k)?.let { c -> drawHeadgear(k, Color(c), w, h) } }
            // Vector blade only when there is no weapon sprite.
            if (weaponSprite == null) {
                weaponKey?.let { k ->
                    AvatarLayerRules.weaponArgb(k)?.let { c ->
                        val len = h * AvatarLayerRules.weaponLength(k)
                        drawLine(
                            color = Color(c),
                            start = Offset(w * 0.84f, h * 0.9f),
                            end = Offset(w * 0.84f + len * 0.35f, h * 0.9f - len),
                            strokeWidth = w * 0.04f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
            // Vector border only when there is no frame sprite.
            if (frameSprite == null) frameKey?.let { k ->
                AvatarLayerRules.frameArgb(k)?.let { c ->
                    drawRect(
                        color = Color(c),
                        style = Stroke(width = AvatarLayerRules.frameWidthDp(k).dp.toPx())
                    )
                }
            }
        }
        frameSprite?.let { name ->
            AssetImage(name = name, size = 104.dp, modifier = Modifier.align(Alignment.Center))
        }
        weaponSprite?.let { name ->
            AssetImage(
                name = name,
                size = 48.dp,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

/** Short hair: a dome over the top of the head. Spiky hair: a row of points on the dome. */
private fun DrawScope.drawHair(key: String, color: Color, w: Float, h: Float) {
    val path = Path()
    if (key == "hair_spiky") {
        path.moveTo(w * 0.31f, h * 0.22f)
        val peaks = listOf(0.36f to 0.02f, 0.42f to 0.12f, 0.49f to 0.0f, 0.56f to 0.12f, 0.63f to 0.01f, 0.69f to 0.22f)
        peaks.forEach { (x, y) -> path.lineTo(w * x, h * y) }
        path.close()
    } else {
        path.addArc(Rect(w * 0.31f, h * 0.04f, w * 0.69f, h * 0.30f), 180f, 180f)
        path.close()
    }
    drawPath(path, color = color, alpha = 0.9f)
}

/** Hood: a dark cowl with a gold rim. Crown: gold-tipped points on a dark band. */
private fun DrawScope.drawHeadgear(key: String, color: Color, w: Float, h: Float) {
    if (key == "headgear_hood") {
        val hood = Path().apply {
            addArc(Rect(w * 0.27f, h * 0.02f, w * 0.73f, h * 0.34f), 180f, 180f)
            close()
        }
        drawPath(hood, color = color, alpha = 0.92f)
        drawPath(hood, color = Color(0xFFC9A227), style = Stroke(width = w * 0.02f))
    } else {
        val crown = Path().apply {
            moveTo(w * 0.33f, h * 0.15f)
            lineTo(w * 0.36f, h * 0.02f)
            lineTo(w * 0.42f, h * 0.10f)
            lineTo(w * 0.50f, h * -0.01f)
            lineTo(w * 0.58f, h * 0.10f)
            lineTo(w * 0.64f, h * 0.02f)
            lineTo(w * 0.67f, h * 0.15f)
            close()
        }
        drawPath(crown, color = color)
    }
}


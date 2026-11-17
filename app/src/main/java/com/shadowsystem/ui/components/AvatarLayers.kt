package com.shadowsystem.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.shadowsystem.core.AvatarLayerRules

/**
 * Draws the equipped outfit (torso band), weapon (blade) and frame (border) over the avatar.
 * Expects the modifier to set the size; the drawing scales with it.
 */
@Composable
fun AvatarLayers(equipped: Set<String>, modifier: Modifier = Modifier.size(96.dp), drawOutfit: Boolean = true) {
    val outfit = if (drawOutfit) AvatarLayerRules.equippedIn(equipped, "outfit_")?.let { AvatarLayerRules.outfitArgb(it) } else null
    val weaponKey = AvatarLayerRules.equippedIn(equipped, "weapon_")
    val frameKey = AvatarLayerRules.equippedIn(equipped, "frame_")
    Canvas(modifier = modifier) {
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
        frameKey?.let { k ->
            AvatarLayerRules.frameArgb(k)?.let { c ->
                drawRect(
                    color = Color(c),
                    style = Stroke(width = AvatarLayerRules.frameWidthDp(k).dp.toPx())
                )
            }
        }
    }
}

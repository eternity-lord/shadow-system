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
    val weaponKey = AvatarLayerRules.equippedIn(equipped, "weapon_")
    val weaponSprite = weaponKey
        ?.let { "avatar_weapon_" + it.removePrefix("weapon_") }
        ?.takeIf { AssetRegistry.hasAsset(context, it) }
    val frameKey = AvatarLayerRules.equippedIn(equipped, "frame_")
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
            frameKey?.let { k ->
                AvatarLayerRules.frameArgb(k)?.let { c ->
                    drawRect(
                        color = Color(c),
                        style = Stroke(width = AvatarLayerRules.frameWidthDp(k).dp.toPx())
                    )
                }
            }
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

package com.shadowsystem.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shadowsystem.R
import com.shadowsystem.core.AssetImage
import com.shadowsystem.core.AssetRegistry
import com.shadowsystem.core.AvatarData
import com.shadowsystem.core.AvatarLayerRules
import com.shadowsystem.core.GameMath
import com.shadowsystem.core.PersianText
import com.shadowsystem.ui.theme.ShadowColors

/**
 * Hunter avatar with layers on top of the base image: an aura ring when an aura is equipped,
 * the stage badge, and the two strongest stats as small overlays. Art for the layers is a placeholder until it exists.
 */
@Composable
fun AvatarStack(
    rank: GameMath.Rank,
    topStats: List<Pair<String, String>>,
    aura: Boolean,
    tint: androidx.compose.ui.graphics.Color? = null,
    equipped: Set<String> = emptySet(),
    pose: Int = 0
) {
    val stage = (GameMath.Rank.values().indexOf(rank) + 1).coerceAtMost(5)
    val context = androidx.compose.ui.platform.LocalContext.current
    Box(modifier = Modifier.size(112.dp), contentAlignment = Alignment.Center) {
        if (aura) {
            // The aura art when it exists, otherwise a coloured square border.
            val auraKey = AvatarLayerRules.equippedIn(equipped, "aura_")
            val auraSprite = auraKey?.let { "avatar_$it" }?.takeIf { AssetRegistry.hasAsset(context, it) }
            if (auraSprite != null) {
                AssetImage(name = auraSprite, size = 112.dp)
            } else {
                val auraColor = auraKey
                    ?.let { AvatarLayerRules.auraArgb(it) }
                    ?.let { Color(it) }
                    ?: ShadowColors.Gold
                Box(modifier = Modifier.size(108.dp).border(3.dp, auraColor, RectangleShape))
            }
        }
        // An equipped outfit with its own look image replaces the rank picture and skips the tint and vector outfit.
        // Priority: outfit look, then the mood pose, then the rank picture. The tint only applies to the rank picture.
        val look = equippedLook(context, equipped)
        val pic = look ?: poseAssetName(context, pose)
        AssetImage(
            name = pic ?: AvatarData.rankAssetName(rank),
            size = 96.dp,
            placeholderColor = ShadowColors.Primary,
            stage = stage,
            contentDescription = stringResource(R.string.content_desc_avatar),
            tint = if (pic == null) tint else null
        )
        AvatarLayers(equipped, drawOutfit = look == null)
        Text(
            stringResource(R.string.stage_badge, PersianText.toFa(stage)),
            style = MaterialTheme.typography.labelSmall,
            color = ShadowColors.Gold,
            modifier = Modifier.align(Alignment.BottomCenter).background(Color.Black.copy(alpha = 0.6f)).padding(horizontal = 6.dp)
        )
        topStats.getOrNull(0)?.let { (stat, label) ->
            StatBadge(stat, label, Modifier.align(Alignment.TopEnd))
        }
        topStats.getOrNull(1)?.let { (stat, label) ->
            StatBadge(stat, label, Modifier.align(Alignment.TopStart))
        }
    }
}

/** Stat emblem when its art exists, otherwise the stat label as text. */
@Composable
private fun StatBadge(stat: String, label: String, modifier: Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val name = AssetRegistry.statAssetName(stat)
    if (AssetRegistry.hasAsset(context, name)) {
        AssetImage(name = name, size = 28.dp, contentDescription = label, modifier = modifier)
    } else {
        Text(label, style = MaterialTheme.typography.labelSmall, modifier = modifier.background(Color.Black.copy(alpha = 0.6f)).padding(2.dp))
    }
}

/** Drawable name of the look image for the equipped outfit, or null when none exists. */
private fun equippedLook(context: android.content.Context, equipped: Set<String>): String? {
    val key = AvatarLayerRules.equippedIn(equipped, "outfit_") ?: return null
    val name = "avatar_look_$key"
    return if (AssetRegistry.hasAsset(context, name)) name else null
}

/**
 * Mood pose picture: 1 standing, 2 determined, 3 tired but satisfied, 4 low energy. No score (0) has no
 * picture and falls back to the rank picture.
 */
private fun poseAssetName(context: android.content.Context, pose: Int): String? {
    val name = when (pose) {
        1 -> "avatar_pose_standing"
        2 -> "avatar_pose_determined"
        3 -> "avatar_pose_tired"
        4 -> "avatar_pose_low"
        else -> return null
    }
    return if (AssetRegistry.hasAsset(context, name)) name else null
}


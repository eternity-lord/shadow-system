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
import com.shadowsystem.core.AvatarData
import com.shadowsystem.core.GameMath
import com.shadowsystem.core.PersianText
import com.shadowsystem.ui.theme.ShadowColors

/**
 * Hunter avatar with layers on top of the base image: an aura ring when an aura is equipped,
 * the stage badge, and the two strongest stats as small overlays. Art for the layers is a placeholder until it exists.
 */
@Composable
fun AvatarStack(rank: GameMath.Rank, topBadges: List<String>, aura: Boolean) {
    val stage = (GameMath.Rank.values().indexOf(rank) + 1).coerceAtMost(5)
    Box(modifier = Modifier.size(112.dp), contentAlignment = Alignment.Center) {
        if (aura) {
            Box(modifier = Modifier.size(108.dp).border(3.dp, ShadowColors.Gold, RectangleShape))
        }
        AssetImage(
            name = AvatarData.rankAssetName(rank),
            size = 96.dp,
            placeholderColor = ShadowColors.Primary,
            stage = stage,
            contentDescription = stringResource(R.string.content_desc_avatar)
        )
        Text(
            stringResource(R.string.stage_badge, PersianText.toFa(stage)),
            style = MaterialTheme.typography.labelSmall,
            color = ShadowColors.Gold,
            modifier = Modifier.align(Alignment.BottomCenter).background(Color.Black.copy(alpha = 0.6f)).padding(horizontal = 6.dp)
        )
        topBadges.getOrNull(0)?.let {
            Text(it, style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.TopEnd).background(Color.Black.copy(alpha = 0.6f)).padding(2.dp))
        }
        topBadges.getOrNull(1)?.let {
            Text(it, style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.TopStart).background(Color.Black.copy(alpha = 0.6f)).padding(2.dp))
        }
    }
}

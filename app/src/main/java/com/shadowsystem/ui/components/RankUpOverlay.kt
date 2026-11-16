package com.shadowsystem.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shadowsystem.R
import com.shadowsystem.core.AssetImage
import com.shadowsystem.core.AvatarData
import com.shadowsystem.core.GameMath
import com.shadowsystem.ui.theme.ShadowColors

/** Full-screen rank-up moment: the new rank's avatar grows in. Tap anywhere to continue. */
@Composable
fun RankUpOverlay(rank: GameMath.Rank, onDismiss: () -> Unit) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    // The rank-up moment lasts 5 seconds; tapping skips it.
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(5_000)
        onDismiss()
    }
    val scale by animateFloatAsState(
        targetValue = if (shown) 1f else 0.5f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "rankUpScale"
    )
    val fade by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "rankUpFade"
    )
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f * fade))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(24.dp).graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    alpha = fade
                }
            ) {
                AssetImage(
                    name = AvatarData.rankAssetName(rank),
                    size = 180.dp,
                    placeholderColor = ShadowColors.Gold,
                    stage = (GameMath.Rank.values().indexOf(rank) + 1).coerceAtMost(5),
                    contentDescription = stringResource(R.string.rank_label, rank.name)
                )
                Text(stringResource(R.string.rank_up_title), style = MaterialTheme.typography.headlineMedium, color = ShadowColors.Gold)
                Text(stringResource(R.string.rank_up_text, rank.name), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.rank_up_tap), style = MaterialTheme.typography.bodyMedium, color = ShadowColors.TextMuted)
                androidx.compose.material3.TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.heightIn(min = 48.dp)
                ) { Text(stringResource(R.string.rank_up_skip)) }
            }
        }
    }
}

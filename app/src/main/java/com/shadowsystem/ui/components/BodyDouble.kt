package com.shadowsystem.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shadowsystem.R
import com.shadowsystem.ShadowApp
import com.shadowsystem.core.AssetImage
import com.shadowsystem.core.AssetRegistry
import com.shadowsystem.core.SoldierData
import com.shadowsystem.ui.theme.ShadowColors

/** Display name for a soldier key: the template name, or the key itself when unknown. */
fun soldierDisplayName(key: String): String = SoldierData.ALL.firstOrNull { it.key == key }?.name ?: key

/**
 * Body doubling: a summoned soldier keeps pace beside the user during a run.
 * The linked soldier is shown when the run has one; otherwise the first summoned soldier.
 * The figure bobs gently while the timer runs; it stops when paused, and with reduce-motion on.
 */
@Composable
fun SoldierBodyDouble(active: Boolean, soldierKey: String? = null) {
    val context = LocalContext.current
    val app = context.applicationContext as ShadowApp
    val soldiers by app.container.soldiers.observeSaved().collectAsStateWithLifecycle(initialValue = emptyList())
    val pool = soldiers.filter { it.summonedAt != null }
    val soldier = pool.firstOrNull { it.key == soldierKey } ?: pool.minByOrNull { it.summonedAt ?: Long.MAX_VALUE }
    val reduce = rememberReduceMotion()
    val bob by rememberInfiniteTransition(label = "bodyDouble").animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 900), repeatMode = RepeatMode.Reverse),
        label = "bodyDoubleBob"
    )
    if (soldier != null) {
        val offsetY = if (active && !reduce) bob else 0f
        val name = soldier.customName ?: soldierDisplayName(soldier.key)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AssetImage(
                name = AssetRegistry.soldierBestAsset(context, soldier.key, 1),
                size = 72.dp,
                placeholderColor = ShadowColors.Secondary,
                contentDescription = name,
                modifier = Modifier.graphicsLayer { translationY = offsetY }
            )
            Text(
                stringResource(R.string.body_double_text, name),
                color = ShadowColors.TextMuted,
                modifier = Modifier.padding(end = 4.dp)
            )
        }
    }
}

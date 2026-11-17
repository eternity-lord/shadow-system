package com.shadowsystem.ui.components

import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shadowsystem.R
import com.shadowsystem.core.AssetRegistry
import com.shadowsystem.core.AssetImage
import com.shadowsystem.core.PersianText
import com.shadowsystem.core.SoldierProgress
import com.shadowsystem.core.SoldierRow
import com.shadowsystem.data.repo.SoldierRepository
import com.shadowsystem.ui.theme.ShadowColors
import kotlinx.coroutines.delay

/** Voice line for the soldier's current stage, rotating with active days. */
fun voiceLineFor(row: SoldierRow): String {
    val lines = row.def.voiceLines[row.displayStage.coerceAtLeast(1)].orEmpty()
    if (lines.isEmpty()) return ""
    return lines[row.progress.activeDays % lines.size]
}

/** Detail sheet: stats, state, a wake hint when sleeping, rename, and summon when eligible. */
@Composable
fun SoldierDetailDialog(
    row: SoldierRow,
    onRename: (String) -> Unit,
    onSummon: () -> Unit,
    onClose: () -> Unit
) {
    var name by remember(row.def.key) { mutableStateOf(row.customName ?: "") }
    val p = row.progress
    AlertDialog(
        onDismissRequest = onClose,
        containerColor = ShadowColors.Surface,
        title = { Text(row.name) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(stringResource(R.string.soldier_habit, row.def.habit), color = ShadowColors.TextMuted)
                Text(row.def.personality, color = ShadowColors.TextMuted)
                if (row.summoned) {
                    Text(stringResource(R.string.soldier_stage, PersianText.toFa(p.stage)))
                    Text(stringResource(stateLabelRes(p.state)), color = ShadowColors.Secondary)
                    Text(stringResource(R.string.soldier_level, PersianText.toFa(p.level)))
                    XpBar(
                        progress = if (p.levelNeed > 0) p.levelXp.toFloat() / p.levelNeed else 0f,
                        label = stringResource(R.string.soldier_xp, PersianText.toFa(p.levelXp), PersianText.toFa(p.levelNeed))
                    )
                    Text(stringResource(R.string.soldier_power, PersianText.toFa(p.activeDays)))
                    Text(
                        stringResource(
                            when (p.wisdomTier) {
                                3 -> R.string.soldier_wisdom_full
                                2 -> R.string.soldier_wisdom_medium
                                1 -> R.string.soldier_wisdom_simple
                                else -> R.string.soldier_wisdom_none
                            }
                        )
                    )
                    if (p.state == SoldierProgress.State.SLEEPING) {
                        Text(stringResource(R.string.soldier_wake_task, row.def.habit), color = ShadowColors.Gold)
                    }
                    Text(voiceLineFor(row), style = MaterialTheme.typography.bodyLarge)
                } else if (row.locked) {
                    Text(stringResource(R.string.soldier_rule_bellion), color = ShadowColors.TextMuted)
                } else {
                    Text(
                        stringResource(R.string.soldier_egg_progress, PersianText.toFa(p.activeDays), PersianText.toFa(SoldierProgress.SUMMON_ACTIVE_DAYS)),
                        color = ShadowColors.TextMuted
                    )
                }
                Text(stringResource(R.string.soldier_linked_done, PersianText.toFa(p.linkedDone)))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(SoldierRepository.MAX_NAME) },
                    label = { Text(stringResource(R.string.soldier_rename_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(onClick = { onRename(name) }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.soldier_rename_save))
                }
                if (row.canSummon) {
                    Button(
                        onClick = onSummon,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = ShadowColors.Gold)
                    ) { Text(stringResource(R.string.soldier_summon), color = ShadowColors.Background) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onClose, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.soldier_close))
            }
        }
    )
}

/** Five-second summoning ritual. Skippable. */
@Composable
fun SummonRitualDialog(row: SoldierRow, onFinish: () -> Unit) {
    LaunchedEffect(row.def.key) {
        delay(5_000)
        onFinish()
    }
    FullscreenScene {
        Text(stringResource(R.string.ritual_title), style = MaterialTheme.typography.titleLarge)
        SoldierImage(row, stage = 1, size = 160.dp)
        Text(stringResource(R.string.ritual_text, row.name), style = MaterialTheme.typography.bodyLarge)
        TextButton(onClick = onFinish, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(R.string.ritual_skip))
        }
    }
}

/** Three-second evolution cutscene with the new voice line. Skippable. */
@Composable
fun EvolutionDialog(row: SoldierRow, onFinish: () -> Unit) {
    LaunchedEffect(row.def.key, row.progress.stage) {
        delay(3_000)
        onFinish()
    }
    FullscreenScene {
        Text(stringResource(R.string.evolution_title), style = MaterialTheme.typography.titleLarge)
        val reduce = rememberReduceMotion()
        val oldAlpha = remember { androidx.compose.animation.core.Animatable(1f) }
        val newAlpha = remember { androidx.compose.animation.core.Animatable(0f) }
        val newScale = remember { androidx.compose.animation.core.Animatable(0.7f) }
        LaunchedEffect(row.def.key, row.progress.stage) {
            if (reduce) {
                oldAlpha.snapTo(0f)
                newAlpha.snapTo(1f)
                newScale.snapTo(1f)
            } else {
                delay(500)
                launch { oldAlpha.animateTo(0f, androidx.compose.animation.core.tween(1000)) }
                launch { newAlpha.animateTo(1f, androidx.compose.animation.core.tween(1000)) }
                newScale.animateTo(1f, androidx.compose.animation.core.tween(1000))
            }
        }
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            val prev = (row.displayStage - 1).coerceAtLeast(1)
            if (prev != row.displayStage) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.graphicsLayer { alpha = oldAlpha.value }
                ) { SoldierImage(row, stage = prev, size = 160.dp, silhouette = true) }
            }
            // The new form starts as its silhouette and gains its colours as the silhouette fades out.
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.graphicsLayer {
                    alpha = 1f - newAlpha.value
                    scaleX = newScale.value
                    scaleY = newScale.value
                }
            ) { SoldierImage(row, stage = row.displayStage, size = 160.dp, silhouette = true) }
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.graphicsLayer {
                    alpha = newAlpha.value
                    scaleX = newScale.value
                    scaleY = newScale.value
                }
            ) { SoldierImage(row, stage = row.displayStage, size = 160.dp) }
        }
        Text(stringResource(R.string.soldier_stage, PersianText.toFa(row.displayStage)), color = ShadowColors.Gold)
        Text(voiceLineFor(row), style = MaterialTheme.typography.bodyLarge)
        TextButton(onClick = onFinish, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(R.string.evolution_skip))
        }
    }
}

@Composable
private fun FullscreenScene(content: @Composable () -> Unit) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(color = ShadowColors.Background, modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
            }
        }
    }
}

@Composable
fun SoldierImage(row: SoldierRow, stage: Int, size: androidx.compose.ui.unit.Dp, silhouette: Boolean = false) {
    val context = LocalContext.current
    val s = stage.coerceAtLeast(1)
    // A silhouette is the same picture tinted solid black: the alpha shape is kept, the detail is not.
    val wisdom = if (silhouette) null else AssetRegistry.wisdomBestAsset(context, row.def.key, row.progress.wisdomTier)
    androidx.compose.foundation.layout.Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size)
    ) {
        AssetImage(
            name = AssetRegistry.soldierBestAsset(context, row.def.key, s),
            size = size,
            placeholderColor = if (silhouette) Color.Black else Color(row.def.colorHex),
            stage = s,
            contentDescription = row.name,
            tint = if (silhouette) Color.Black else null
        )
        if (wisdom != null) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(AssetRegistry.drawableId(context, wisdom)),
                contentDescription = null,
                modifier = Modifier.size(size).graphicsLayer { alpha = 0.6f },
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )
        }
    }
}

fun stateLabelRes(state: SoldierProgress.State): Int = when (state) {
    SoldierProgress.State.AWAKE -> R.string.soldier_state_awake
    SoldierProgress.State.TIRED -> R.string.soldier_state_tired
    SoldierProgress.State.SLEEPING -> R.string.soldier_state_sleeping
}

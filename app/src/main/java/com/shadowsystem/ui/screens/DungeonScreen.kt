package com.shadowsystem.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shadowsystem.R
import com.shadowsystem.core.AssetImage
import com.shadowsystem.core.AssetRegistry
import com.shadowsystem.core.GameMath
import com.shadowsystem.core.GameMath.Rank
import com.shadowsystem.core.PersianText
import com.shadowsystem.ui.components.ConfirmDialog
import com.shadowsystem.ui.components.MessageBanner
import com.shadowsystem.ui.components.ShadowCard
import com.shadowsystem.ui.components.XpBar
import com.shadowsystem.ui.theme.ShadowColors
import com.shadowsystem.ui.vm.DungeonRun
import com.shadowsystem.ui.vm.GameViewModel

/** Dungeon gates: timed focus sessions. Time spent counts toward the daily score. */
@Composable
fun DungeonScreen(vm: GameViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    var confirmExit by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.dungeon_title), style = MaterialTheme.typography.headlineMedium)
        state.message?.let { MessageBanner(it) { vm.dismissMessage() } }

        val run = state.run
        if (run == null) {
            Text(stringResource(R.string.dungeon_idle), color = ShadowColors.TextMuted)
            ShadowCard {
                Text(
                    stringResource(R.string.dungeon_today, PersianText.durationMinutes(state.dungeonSeconds / 60L)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ShadowColors.TextMuted
                )
            }
            Rank.values().filter { it.ordinal <= state.maxRank.ordinal }.reversed().forEach { rank ->
                GateCard(rank) { vm.startDungeon(rank) }
            }
        } else {
            ActiveRun(
                run = run,
                onFinish = { vm.endDungeon(completed = true) },
                onExitRequest = { confirmExit = true }
            )
        }
    }

    if (confirmExit) {
        ConfirmDialog(
            text = stringResource(R.string.dungeon_cancel_confirm),
            confirmText = stringResource(R.string.dungeon_cancel),
            onConfirm = {
                confirmExit = false
                vm.endDungeon(completed = false)
            },
            onDismiss = { confirmExit = false }
        )
    }
}

@Composable
private fun GateCard(rank: Rank, onEnter: () -> Unit) {
    ShadowCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AssetImage(
                AssetRegistry.gateAssetName(rank.name),
                size = 64.dp,
                placeholderColor = ShadowColors.Primary,
                contentDescription = stringResource(R.string.gate_rank, rank.name)
            )
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(stringResource(R.string.gate_rank, rank.name), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.gate_minutes, PersianText.toFa(GameMath.dungeonMinutes(rank))),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ShadowColors.TextMuted
                )
            }
            Button(
                onClick = onEnter,
                modifier = Modifier.heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ShadowColors.Primary)
            ) { Text(stringResource(R.string.dungeon_start)) }
        }
    }
}

@Composable
private fun ActiveRun(run: DungeonRun, onFinish: () -> Unit, onExitRequest: () -> Unit) {
    val remaining = (run.plannedSeconds - run.elapsedSeconds).coerceAtLeast(0)
    val mm = PersianText.toFa(String.format("%02d", remaining / 60))
    val ss = PersianText.toFa(String.format("%02d", remaining % 60))
    ShadowCard {
        Text(stringResource(R.string.dungeon_inside), style = MaterialTheme.typography.titleMedium)
        Text(
            "$mm:$ss",
            style = MaterialTheme.typography.headlineMedium,
            color = ShadowColors.Secondary,
            modifier = Modifier.fillMaxWidth()
        )
        XpBar(
            progress = if (run.plannedSeconds > 0) run.elapsedSeconds.toFloat() / run.plannedSeconds else 0f,
            label = stringResource(R.string.gate_rank, run.rank.name)
        )
        Button(
            onClick = onFinish,
            enabled = run.finished,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ShadowColors.Gold)
        ) { Text(stringResource(R.string.dungeon_finish), color = ShadowColors.Background) }
        TextButton(onClick = onExitRequest, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text(stringResource(R.string.dungeon_cancel), color = ShadowColors.Danger)
        }
    }
}

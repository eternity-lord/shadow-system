package com.shadowsystem.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shadowsystem.R
import com.shadowsystem.core.AssetRegistry
import androidx.compose.ui.graphics.Color
import com.shadowsystem.core.AvatarData
import com.shadowsystem.core.GameMath
import com.shadowsystem.core.GameMath.Stat
import com.shadowsystem.core.PersianText
import com.shadowsystem.core.SoldierData
import com.shadowsystem.core.SoldierDef
import com.shadowsystem.core.AssetImage
import com.shadowsystem.ui.components.SectionTitle
import com.shadowsystem.ui.components.ShadowCard
import com.shadowsystem.ui.components.XpBar
import com.shadowsystem.ui.theme.ShadowColors
import com.shadowsystem.ui.vm.GameViewModel

/** Hunter profile: level, rank, the five stats, class and the shadow soldiers. */
@Composable
fun HunterScreen(vm: GameViewModel, onSettings: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val stats by vm.stats.collectAsStateWithLifecycle()
    val level = GameMath.levelForXp(state.profile.totalXp)
    val rank = GameMath.rankForLevel(level)
    val statLevels = Stat.values().associateWith { GameMath.statLevelForXp(stats[it] ?: 0L) }
    val top = statLevels.entries.sortedByDescending { it.value }.take(2).map { it.key }.toSet()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.hunter_title), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            androidx.compose.material3.TextButton(onClick = onSettings, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.settings_title))
            }
        }
        ShadowCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AssetImage(
                    name = AvatarData.rankAssetName(rank),
                    size = 96.dp,
                    placeholderColor = ShadowColors.Primary,
                    stage = (GameMath.Rank.values().indexOf(rank) + 1).coerceAtMost(5),
                    contentDescription = stringResource(R.string.content_desc_avatar)
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(stringResource(R.string.level_label, PersianText.toFa(level)), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.rank_label, rank.name), color = ShadowColors.Gold)
                    Text(
                        stringResource(R.string.class_label, GameMath.className(top)),
                        color = ShadowColors.TextMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Text(stringResource(R.string.gold_label, PersianText.toFa(state.profile.gold)), color = ShadowColors.Gold)
            Text(
                stringResource(R.string.active_days, PersianText.toFa(state.profile.activeDays)),
                color = ShadowColors.TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        ShadowCard {
            Stat.values().forEach { s ->
                val xp = stats[s] ?: 0L
                val lvl = statLevels[s] ?: 1
                val levelStart = (1 until lvl).sumOf { GameMath.statXpForLevel(it).toLong() }
                val size = GameMath.statXpForLevel(lvl).toFloat()
                XpBar(
                    progress = ((xp - levelStart).toFloat() / size),
                    label = "${statLabel(s)} · ${PersianText.toFa(lvl)}"
                )
            }
        }

        ShadowCard {
            SectionTitle(stringResource(R.string.shop_title))
            Text(stringResource(R.string.shop_coming_soon), color = ShadowColors.TextMuted, style = MaterialTheme.typography.bodyMedium)
        }
        Text(stringResource(R.string.soldiers_title), style = MaterialTheme.typography.titleMedium, color = ShadowColors.TextMuted)
        SoldierData.ALL.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { soldier ->
                    val unlocked = isUnlocked(soldier, statLevels, rank)
                    SoldierTile(soldier, unlocked, Modifier.weight(1f))
                }
            }
        }
    }
}

private fun isUnlocked(soldier: SoldierDef, statLevels: Map<Stat, Int>, rank: GameMath.Rank): Boolean {
    if (soldier.stat == null) return rank == GameMath.Rank.S
    return (statLevels[soldier.stat] ?: 1) >= 5
}

@Composable
private fun SoldierTile(soldier: SoldierDef, unlocked: Boolean, modifier: Modifier) {
    ShadowCard(modifier = modifier) {
        AssetImage(
            name = AssetRegistry.soldierAssetName(soldier.key, 1),
            size = 72.dp,
            placeholderColor = Color(soldier.colorHex),
            stage = 1,
            contentDescription = soldier.name
        )
        Text(soldier.name, style = MaterialTheme.typography.bodyMedium)
        Text(
            if (unlocked) stringResource(R.string.soldier_unlocked) else stringResource(R.string.soldier_locked),
            color = if (unlocked) ShadowColors.Secondary else ShadowColors.TextMuted,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun statLabel(stat: Stat): String = when (stat) {
    Stat.STR -> stringResource(R.string.stat_str)
    Stat.INT -> stringResource(R.string.stat_int)
    Stat.AGI -> stringResource(R.string.stat_agi)
    Stat.VIT -> stringResource(R.string.stat_vit)
    Stat.SENSE -> stringResource(R.string.stat_sense)
}

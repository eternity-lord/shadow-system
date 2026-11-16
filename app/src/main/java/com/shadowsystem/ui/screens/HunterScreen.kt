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
import androidx.compose.ui.platform.LocalContext
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
import com.shadowsystem.ui.vm.SoldierViewModel
import com.shadowsystem.ui.vm.ProfileViewModel
import com.shadowsystem.ui.components.AvatarShop
import com.shadowsystem.ui.components.StatRadar
import com.shadowsystem.core.TitleRules
import androidx.compose.runtime.LaunchedEffect
import com.shadowsystem.core.SoldierRow
import com.shadowsystem.ui.components.EvolutionDialog
import com.shadowsystem.ui.components.SoldierDetailDialog
import com.shadowsystem.ui.components.SoldierImage
import com.shadowsystem.ui.components.SummonRitualDialog
import com.shadowsystem.ui.components.stateLabelRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

/** Hunter profile: level, rank, the five stats, class and the shadow soldiers. */
@Composable
fun HunterScreen(vm: GameViewModel, soldiers: SoldierViewModel, profile: ProfileViewModel, onSettings: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val extras by profile.state.collectAsStateWithLifecycle()
    val roster by soldiers.roster.collectAsStateWithLifecycle()
    val ritual by soldiers.ritual.collectAsStateWithLifecycle()
    val evolution by soldiers.evolution.collectAsStateWithLifecycle()
    var selectedKey by remember { mutableStateOf<String?>(null) }
    val stats by vm.stats.collectAsStateWithLifecycle()
    val level = GameMath.levelForXp(state.profile.totalXp)
    val rank = GameMath.rankForLevel(level)
    val statLevels = Stat.values().associateWith { GameMath.statLevelForXp(stats[it] ?: 0L) }
    val top = statLevels.entries.sortedByDescending { it.value }.take(2).map { it.key }.toSet()
    val weekly = extras.weeklyTop.takeIf { it.size == 2 } ?: top
    LaunchedEffect(top) { profile.refresh(top) }

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
                        stringResource(R.string.class_label, GameMath.className(weekly)),
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

        GoldShop(state.profile.gold, state.boosts) { item, onResult -> vm.buyBoost(item, onResult) }

        ShadowCard {
            SectionTitle(stringResource(R.string.radar_title))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                StatRadar(values = Stat.values().map { ((statLevels[it] ?: 1) / 10f).coerceIn(0.05f, 1f) })
            }
            Text(
                Stat.values().joinToString(" · ") { "${statLabel(it)} ${PersianText.toFa(statLevels[it] ?: 1)}" },
                style = MaterialTheme.typography.bodyMedium,
                color = ShadowColors.TextMuted
            )
            Text(stringResource(R.string.pose_label, stringResource(poseRes(state.score?.score))), style = MaterialTheme.typography.bodyMedium)
        }

        ShadowCard {
            SectionTitle(stringResource(R.string.titles_title))
            AvatarData.TITLES.forEach { t ->
                val ok = t.key in extras.titles
                Text(
                    if (ok) t.name else "${stringResource(R.string.locked_label)}: ${t.hint}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (ok) ShadowColors.Gold else ShadowColors.TextMuted
                )
            }
        }

        ShadowCard {
            SectionTitle(stringResource(R.string.achievements_title))
            AvatarData.ACHIEVEMENTS.forEach { a ->
                val ok = a.key in extras.achievements
                Text(
                    if (ok) "✓ ${a.name}" else "○ ${a.name}: ${a.hint}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (ok) ShadowColors.Secondary else ShadowColors.TextMuted
                )
            }
        }

        AvatarShop(
            gold = state.profile.gold,
            owned = extras.owned,
            equipped = extras.equipped,
            onBuy = { item -> profile.buy(item, top) { } },
            onEquip = { item -> profile.toggleEquip(item) }
        )
        Text(stringResource(R.string.soldiers_title), style = MaterialTheme.typography.titleMedium, color = ShadowColors.TextMuted)
        roster.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { soldier ->
                    SoldierTile(soldier, Modifier.weight(1f)) { selectedKey = soldier.def.key }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }

    roster.firstOrNull { it.def.key == selectedKey }?.let { selected ->
        SoldierDetailDialog(
            row = selected,
            onRename = { soldiers.rename(selected.def.key, it) },
            onSummon = {
                selectedKey = null
                soldiers.startRitual(selected.def.key)
            },
            onClose = { selectedKey = null }
        )
    }
    ritual?.let { row -> SummonRitualDialog(row) { soldiers.finishRitual() } }
    evolution?.let { row -> EvolutionDialog(row) { soldiers.finishEvolution(row) } }
}

@Composable
private fun SoldierTile(row: SoldierRow, modifier: Modifier, onClick: () -> Unit) {
    val stage = row.displayStage
    ShadowCard(modifier = modifier.clickable(onClick = onClick)) {
        if (row.summoned) {
            SoldierImage(row, stage, 72.dp)
        } else {
            AssetImage(
                name = "egg_placeholder",
                size = 72.dp,
                placeholderColor = Color(row.def.colorHex),
                stage = 1,
                contentDescription = row.name
            )
        }
        Text(row.name, style = MaterialTheme.typography.bodyMedium)
        Text(
            when {
                row.summoned -> stringResource(R.string.soldier_stage, PersianText.toFa(stage))
                row.locked -> stringResource(R.string.soldier_locked)
                else -> stringResource(R.string.soldier_egg)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = ShadowColors.TextMuted
        )
        if (row.summoned) {
            Text(stringResource(stateLabelRes(row.progress.state)), style = MaterialTheme.typography.bodyMedium, color = ShadowColors.Secondary)
        }
        if (row.canSummon) {
            Text(stringResource(R.string.soldier_summon_ready), style = MaterialTheme.typography.bodyMedium, color = ShadowColors.Gold)
        }
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

private fun poseRes(score: Int?): Int = when (TitleRules.pose(score)) {
    1 -> R.string.pose_standing
    2 -> R.string.pose_determined
    3 -> R.string.pose_tired_satisfied
    4 -> R.string.pose_low_energy
    else -> R.string.pose_none
}

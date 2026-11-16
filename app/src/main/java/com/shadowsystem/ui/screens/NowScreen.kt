package com.shadowsystem.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shadowsystem.R
import com.shadowsystem.core.GameMath
import com.shadowsystem.core.Jalali
import com.shadowsystem.core.PersianText
import com.shadowsystem.data.db.QuestEntity
import com.shadowsystem.ui.components.MessageBanner
import com.shadowsystem.ui.components.MoodPicker
import com.shadowsystem.ui.components.QuestCard
import com.shadowsystem.ui.components.ShadowCard
import com.shadowsystem.ui.components.XpBar
import com.shadowsystem.ui.theme.ShadowColors
import com.shadowsystem.ui.vm.GameViewModel

/** Today: score, level, the System's briefing and the three mandatory quests. */
@Composable
fun NowScreen(vm: GameViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val yesterday by vm.yesterdayScore.collectAsStateWithLifecycle()
    val level = GameMath.levelForXp(state.profile.totalXp)
    val rank = GameMath.rankForLevel(level)
    val levelStart = GameMath.totalXpForLevel(level)
    val levelSize = GameMath.xpForLevel(level).toLong()
    val inLevel = state.profile.totalXp - levelStart

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        Text(Jalali.format(Jalali.fromMillis(System.currentTimeMillis())), style = MaterialTheme.typography.bodyMedium, color = ShadowColors.TextMuted)
        Text(stringResource(R.string.now_title), style = MaterialTheme.typography.headlineMedium)

        state.message?.let { MessageBanner(it) { vm.dismissMessage() } }

        ShadowCard {
            Text(stringResource(R.string.score_label), style = MaterialTheme.typography.titleMedium, color = ShadowColors.TextMuted)
            Text(
                PersianText.toFa(state.score?.score ?: 0),
                style = MaterialTheme.typography.headlineMedium,
                color = ShadowColors.Gold
            )
            yesterday?.let {
                Text(stringResource(R.string.shadow_yesterday, PersianText.toFa(it)), style = MaterialTheme.typography.bodyMedium, color = ShadowColors.TextMuted)
            }
        }

        ShadowCard {
            Text(
                stringResource(R.string.level_label, PersianText.toFa(level)) + "  ·  " +
                    stringResource(R.string.rank_label, rank.name),
                style = MaterialTheme.typography.titleMedium
            )
            XpBar(
                progress = if (levelSize > 0) inLevel.toFloat() / levelSize else 0f,
                label = stringResource(R.string.xp_progress, PersianText.toFa(inLevel), PersianText.toFa(levelSize))
            )
        }

        if (state.quests.isEmpty()) {
            MoodPicker { vm.generate(it) }
        } else {
            SectionHeader(stringResource(R.string.mandatory_title))
            state.quests.filter { it.mandatory }.forEach { quest ->
                QuestCard(quest, state.steps.filter { it.questId == quest.id }) { vm.tickStep(it) }
            }
            val optionalLeft = state.quests.count { !it.mandatory && it.status == QuestEntity.STATUS_OPEN }
            if (optionalLeft > 0) {
                Text(
                    stringResource(R.string.optional_title) + ": " + PersianText.toFa(optionalLeft),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ShadowColors.TextMuted
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = ShadowColors.TextMuted, modifier = Modifier.fillMaxWidth())
}

package com.shadowsystem.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shadowsystem.R
import com.shadowsystem.core.PersianText
import com.shadowsystem.ui.components.ShadowCard
import com.shadowsystem.ui.components.trendText
import com.shadowsystem.ui.theme.ShadowColors
import com.shadowsystem.ui.vm.DailyReportViewModel

/** Full report for today: score and trend, quests, XP, dungeon time, rare loot and spending. */
@Composable
fun DailyReportScreen(vm: DailyReportViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.report_full_title), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.back)) }
        }
        val r = state.report
        if (!state.loaded || r == null) {
            Text(stringResource(R.string.report_loading), color = ShadowColors.TextMuted)
            return@Column
        }
        ShadowCard {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.report_section_score), style = MaterialTheme.typography.titleMedium)
                if (r.score == null) {
                    Text(stringResource(R.string.report_no_score), color = ShadowColors.TextMuted)
                } else {
                    Text(stringResource(R.string.report_today_score, PersianText.toFa(r.score)))
                    Text(trendText(r.score, r.yesterdayScore), color = ShadowColors.TextMuted)
                }
                Text(
                    r.yesterdayScore?.let { stringResource(R.string.report_yesterday_score, PersianText.toFa(it)) }
                        ?: stringResource(R.string.report_no_yesterday),
                    color = ShadowColors.TextMuted
                )
            }
        }
        ShadowCard {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.report_section_work), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.report_quests, PersianText.toFa(r.questsDone), PersianText.toFa(r.questsTotal)))
                Text(stringResource(R.string.report_xp, PersianText.toFa(r.xpToday)))
                Text(stringResource(R.string.report_dungeons_done, PersianText.toFa(r.dungeonsCompleted)))
                Text(stringResource(R.string.report_dungeon_minutes, PersianText.toFa(r.dungeonMinutes)))
                Text(stringResource(R.string.report_rare, PersianText.toFa(r.rareLoot)), color = ShadowColors.Gold)
            }
        }
        ShadowCard {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.report_section_money), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.report_spent, PersianText.formatMoney(state.spentRial), PersianText.toFa(state.spentCount)))
                Text(stringResource(R.string.report_money_note), style = MaterialTheme.typography.bodyMedium, color = ShadowColors.TextMuted)
            }
        }
    }
}

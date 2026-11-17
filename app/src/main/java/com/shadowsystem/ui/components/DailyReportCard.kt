package com.shadowsystem.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shadowsystem.R
import com.shadowsystem.core.DailyReport
import com.shadowsystem.core.PersianText
import com.shadowsystem.core.PhraseBank
import com.shadowsystem.ui.theme.ShadowColors

/** Morning briefing (phrase and yesterday's score) or night report (today's score, trend, dungeon time). */
@Composable
fun DailyReportCard(morning: Boolean, score: Int?, yesterday: Int?, dungeonMinutes: Int, onMore: () -> Unit) {
    val phrase = remember(morning) { if (morning) PhraseBank.morning() else "" }
    ShadowCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                stringResource(if (morning) R.string.report_morning_title else R.string.report_night_title),
                style = MaterialTheme.typography.titleMedium
            )
            if (morning) {
                Text(phrase, style = MaterialTheme.typography.bodyLarge)
                Text(
                    yesterday?.let { stringResource(R.string.report_yesterday_score, PersianText.toFa(it)) }
                        ?: stringResource(R.string.report_no_yesterday),
                    color = ShadowColors.TextMuted
                )
            } else {
                if (score == null) {
                    Text(stringResource(R.string.report_no_score), color = ShadowColors.TextMuted)
                } else {
                    Text(stringResource(R.string.report_today_score, PersianText.toFa(score)))
                    Text(trendText(score, yesterday), color = ShadowColors.TextMuted)
                }
                Text(
                    stringResource(R.string.report_dungeon_minutes, PersianText.toFa(dungeonMinutes)),
                    color = ShadowColors.TextMuted
                )
            }
            TextButton(onClick = onMore, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.report_full_button))
            }
        }
    }
}

/** Trend sentence comparing today's score with yesterday's. Shared with the full report. */
@Composable
fun trendText(score: Int, yesterday: Int?): String = when (DailyReport.trend(score, yesterday)) {
    DailyReport.Trend.UP -> stringResource(R.string.report_trend_up, PersianText.toFa(DailyReport.delta(score, yesterday) ?: 0))
    DailyReport.Trend.SAME -> stringResource(R.string.report_trend_same)
    DailyReport.Trend.DOWN -> stringResource(R.string.report_trend_down, PersianText.toFa(-(DailyReport.delta(score, yesterday) ?: 0)))
    DailyReport.Trend.NONE -> stringResource(R.string.report_no_yesterday)
}

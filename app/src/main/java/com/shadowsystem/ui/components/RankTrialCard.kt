package com.shadowsystem.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.shadowsystem.R
import com.shadowsystem.core.PersianText
import com.shadowsystem.core.RankTrial
import com.shadowsystem.core.RankTrialState
import com.shadowsystem.ui.theme.ShadowColors

/** The weekly boss that stands between the player and the next rank. */
@Composable
fun RankTrialCard(trial: RankTrialState) {
    ShadowCard {
        Text(
            stringResource(R.string.rank_trial_title, trial.target.name),
            style = MaterialTheme.typography.titleMedium,
            color = ShadowColors.Gold
        )
        Text(
            stringResource(R.string.rank_trial_hint, PersianText.toFa(RankTrial.TARGET_QUESTS), PersianText.toFa(RankTrial.WINDOW_DAYS.toInt())),
            style = MaterialTheme.typography.bodyMedium,
            color = ShadowColors.TextMuted
        )
        XpBar(
            progress = (trial.done.toFloat() / RankTrial.TARGET_QUESTS).coerceIn(0f, 1f),
            label = stringResource(R.string.rank_trial_progress, PersianText.toFa(trial.done), PersianText.toFa(RankTrial.TARGET_QUESTS))
        )
    }
}

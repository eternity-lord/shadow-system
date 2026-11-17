package com.shadowsystem.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.shadowsystem.R
import com.shadowsystem.core.PersianText
import com.shadowsystem.core.WeeklyBoss
import com.shadowsystem.ui.theme.ShadowColors
import com.shadowsystem.ui.vm.BossState

/** Weekly boss: HP bar, minutes and quests toward it, and the one-time reward. */
@Composable
fun BossCard(state: BossState, onClaim: () -> Unit) {
    val st = state.status ?: return
    ShadowCard {
        SectionTitle(stringResource(R.string.boss_title))
        XpBar(
            progress = st.hpPercent / 100f,
            label = stringResource(R.string.boss_hp, PersianText.toFa(st.hpPercent))
        )
        Text(
            stringResource(R.string.boss_minutes, PersianText.toFa(st.minutes), PersianText.toFa(WeeklyBoss.TARGET_MINUTES)),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            stringResource(R.string.boss_quests, PersianText.toFa(st.quests), PersianText.toFa(WeeklyBoss.TARGET_QUESTS)),
            style = MaterialTheme.typography.bodyMedium
        )
        when {
            state.claimed -> Text(stringResource(R.string.boss_claimed), color = ShadowColors.Secondary)
            st.defeated -> PrimaryButton(stringResource(R.string.boss_claim), onClick = onClaim)
            else -> Text(
                stringResource(R.string.boss_reward, PersianText.toFa(WeeklyBoss.REWARD_XP), PersianText.toFa(WeeklyBoss.REWARD_GOLD)),
                style = MaterialTheme.typography.bodyMedium,
                color = ShadowColors.TextMuted
            )
        }
    }
}

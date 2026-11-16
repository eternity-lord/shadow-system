package com.shadowsystem.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shadowsystem.R
import com.shadowsystem.core.GameMath
import com.shadowsystem.core.PersianText
import com.shadowsystem.data.db.QuestEntity
import com.shadowsystem.data.db.QuestStepEntity
import com.shadowsystem.ui.theme.ShadowColors

/** One quest with its steps. Steps are tapped one at a time (48dp rows). */
@Composable
fun QuestCard(
    quest: QuestEntity,
    steps: List<QuestStepEntity>,
    onStep: (QuestStepEntity) -> Unit
) {
    val done = quest.status == QuestEntity.STATUS_DONE
    ShadowCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.padding(end = 8.dp)) {
                Text(
                    text = quest.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (done) ShadowColors.TextMuted else ShadowColors.TextPrimary
                )
                Text(
                    text = "${PersianText.toFa(quest.minutes)} ${stringResource(R.string.unit_minutes)} · ${quest.rank}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ShadowColors.TextMuted
                )
            }
        }
        if (quest.comebackApplied && !done) {
            Text(stringResource(R.string.comeback_active), color = ShadowColors.Gold, style = MaterialTheme.typography.bodyMedium)
        }
        steps.sortedBy { it.position }.forEach { step ->
            StepRow(step = step, enabled = !done && !step.done, onClick = { onStep(step) })
        }
        if (done) {
            Text(
                stringResource(R.string.reward_claimed),
                color = ShadowColors.Secondary,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            val reward = GameMath.questReward(GameMath.Rank.valueOf(quest.rank))
            Text(
                stringResource(R.string.quest_reward, PersianText.toFa(reward.xp), PersianText.toFa(reward.gold)),
                color = ShadowColors.TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun StepRow(step: QuestStepEntity, enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(enabled = enabled, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Checkbox(checked = step.done, onCheckedChange = null, enabled = enabled)
        Text(
            text = step.text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (step.done) ShadowColors.TextMuted else ShadowColors.TextPrimary
        )
    }
}

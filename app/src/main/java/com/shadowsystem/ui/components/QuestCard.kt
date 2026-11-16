package com.shadowsystem.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.mutableFloatStateOf
import kotlin.math.abs
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shadowsystem.R
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.shadowsystem.core.GameMath
import com.shadowsystem.core.PersianText
import com.shadowsystem.data.db.QuestEntity
import com.shadowsystem.data.db.QuestStepEntity
import com.shadowsystem.ui.theme.ShadowColors

/** One quest with its steps. Each step is a 48dp row. "I'm stuck" offers a smaller version. */
@Composable
fun QuestCard(
    quest: QuestEntity,
    steps: List<QuestStepEntity>,
    onStep: (QuestStepEntity) -> Unit,
    onSmaller: () -> Unit
) {
    val done = quest.status == QuestEntity.STATUS_DONE
    var stuckOpen by remember(quest.id) { mutableStateOf(false) }
    val remainingSteps = steps.count { !it.done }
    ShadowCard {
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
        if (quest.comebackApplied && !done) {
            Text(stringResource(R.string.comeback_active), color = ShadowColors.Gold, style = MaterialTheme.typography.bodyMedium)
        }
        steps.sortedBy { it.position }.forEach { step ->
            StepRow(step = step, enabled = !done && !step.done, onClick = { onStep(step) })
        }
        if (done) {
            Text(stringResource(R.string.reward_claimed), color = ShadowColors.Secondary, style = MaterialTheme.typography.bodyMedium)
        } else {
            val reward = GameMath.questReward(GameMath.Rank.valueOf(quest.rank))
            Text(
                stringResource(R.string.quest_reward, PersianText.toFa(reward.xp), PersianText.toFa(reward.gold)),
                color = ShadowColors.TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
            if (stuckOpen) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    if (remainingSteps > 1) {
                        OutlinedButton(
                            onClick = {
                                stuckOpen = false
                                onSmaller()
                            },
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                        ) { Text(stringResource(R.string.make_smaller)) }
                    }
                    OutlinedButton(
                        onClick = { stuckOpen = false },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                    ) { Text(stringResource(R.string.later)) }
                }
            } else {
                TextButton(onClick = { stuckOpen = true }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.im_stuck), color = ShadowColors.TextMuted)
                }
            }
        }
    }
}

@Composable
private fun StepRow(step: QuestStepEntity, enabled: Boolean, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    var drag by remember(step.id) { mutableFloatStateOf(0f) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .pointerInput(enabled, step.id) {
                // A horizontal swipe past SWIPE_THRESHOLD_PX completes the step, like a tap.
                detectHorizontalDragGestures(
                    onDragStart = { drag = 0f },
                    onDragEnd = {
                        if (enabled && abs(drag) > SWIPE_THRESHOLD_PX) onClick()
                        drag = 0f
                    },
                    onDragCancel = { drag = 0f },
                    onHorizontalDrag = { _, amount -> drag += amount }
                )
            }
            .clickable(enabled = enabled) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Checkbox(checked = step.done, onCheckedChange = null, enabled = enabled)
        Text(
            text = step.text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (step.done) ShadowColors.TextMuted else ShadowColors.TextPrimary,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

private const val SWIPE_THRESHOLD_PX = 120f

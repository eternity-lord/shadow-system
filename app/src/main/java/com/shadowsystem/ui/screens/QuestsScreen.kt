package com.shadowsystem.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shadowsystem.R
import com.shadowsystem.data.db.QuestEntity
import com.shadowsystem.ui.components.MessageBanner
import com.shadowsystem.ui.components.MoodPicker
import com.shadowsystem.ui.components.QuestCard
import com.shadowsystem.ui.theme.ShadowColors
import com.shadowsystem.ui.vm.GameViewModel

/** All of today's quests, mandatory first, with every step. */
@Composable
fun QuestsScreen(vm: GameViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.quests_title), style = MaterialTheme.typography.headlineMedium)
        state.message?.let { MessageBanner(it) { vm.dismissMessage() } }
        if (state.quests.isEmpty()) {
            Text(stringResource(R.string.empty_quests_list), color = ShadowColors.TextMuted)
            MoodPicker { vm.generate(it) }
        } else {
            state.quests.forEach { quest: QuestEntity ->
                QuestCard(quest, state.steps.filter { it.questId == quest.id }) { vm.tickStep(it) }
            }
        }
    }
}

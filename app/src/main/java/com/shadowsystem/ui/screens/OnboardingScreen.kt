package com.shadowsystem.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shadowsystem.R
import com.shadowsystem.core.PersianText
import com.shadowsystem.data.prefs.AppPrefs
import com.shadowsystem.ui.components.PrimaryButton
import com.shadowsystem.ui.theme.ShadowColors

/**
 * Answers from the question screens. Index values match the chip order on each screen.
 * [wakeHour] is the hour chosen; the morning reminder fires at it.
 */
data class OnboardingAnswers(
    val name: String,
    val base: Int,
    val wakeHour: Int,
    val focus: Int,
    val struggle: Int,
    val minutes: Int,
    val reminders: Boolean
)

/** Wake hours offered on the wake-time screen. */
val WAKE_HOURS = listOf(5, 6, 7, 8, 9, 10)

private val introPages = listOf(
    R.string.onb_title_1 to R.string.onb_body_1,
    R.string.onb_title_2 to R.string.onb_body_2,
    R.string.onb_title_3 to R.string.onb_body_3,
    R.string.onb_title_4 to R.string.onb_body_4
)

/** Four intro pages, then name, avatar base, wake time and four questions. The last question starts the app. */
@Composable
fun OnboardingScreen(onDone: (OnboardingAnswers) -> Unit) {
    val context = LocalContext.current
    var index by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var base by remember { mutableIntStateOf(-1) }
    var wake by remember { mutableIntStateOf(-1) }
    var focus by remember { mutableIntStateOf(-1) }
    var struggle by remember { mutableIntStateOf(-1) }
    var minutes by remember { mutableIntStateOf(-1) }
    var reminders by remember { mutableStateOf<Boolean?>(null) }
    val total = introPages.size + 7
    val last = index == total - 1
    val answered = when (index - introPages.size) {
        0 -> name.trim().isNotEmpty()
        1 -> base >= 0
        2 -> wake >= 0
        3 -> focus >= 0
        4 -> struggle >= 0
        5 -> minutes >= 0
        6 -> reminders != null
        else -> true
    }
    val notifPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.onb_progress, index + 1, total),
            style = MaterialTheme.typography.bodyMedium,
            color = ShadowColors.TextMuted
        )
        Spacer(Modifier.height(16.dp))
        when (index) {
            in introPages.indices -> {
                val (title, body) = introPages[index]
                Text(stringResource(title), style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(12.dp))
                Text(stringResource(body), style = MaterialTheme.typography.bodyLarge, color = ShadowColors.TextMuted)
            }
            introPages.size -> {
                Text(stringResource(R.string.onb_q_name_title), style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(AppPrefs.MAX_NAME) },
                    singleLine = true,
                    label = { Text(stringResource(R.string.onb_name_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            introPages.size + 1 -> QuestionBlock(
                title = stringResource(R.string.onb_q_base_title),
                options = listOf(R.string.onb_base_1, R.string.onb_base_2, R.string.onb_base_3).map { stringResource(it) },
                selected = base,
                onSelect = { base = it }
            )
            introPages.size + 2 -> QuestionBlock(
                title = stringResource(R.string.onb_q_wake_title),
                options = WAKE_HOURS.map { PersianText.toFa(it) + ":" + PersianText.toFa(0) + PersianText.toFa(0) },
                selected = wake,
                onSelect = { wake = it }
            )
            introPages.size + 3 -> QuestionBlock(
                title = stringResource(R.string.onb_title_5),
                options = listOf(R.string.onb_focus_1, R.string.onb_focus_2, R.string.onb_focus_3).map { stringResource(it) },
                selected = focus,
                onSelect = { focus = it }
            )
            introPages.size + 4 -> QuestionBlock(
                title = stringResource(R.string.onb_q_struggle_title),
                options = listOf(R.string.onb_struggle_1, R.string.onb_struggle_2, R.string.onb_struggle_3, R.string.onb_struggle_4).map { stringResource(it) },
                selected = struggle,
                onSelect = { struggle = it }
            )
            introPages.size + 5 -> QuestionBlock(
                title = stringResource(R.string.onb_q_time_title),
                options = listOf(R.string.onb_time_1, R.string.onb_time_2, R.string.onb_time_3).map { stringResource(it) },
                selected = minutes,
                onSelect = { minutes = it }
            )
            else -> {
                QuestionBlock(
                    title = stringResource(R.string.onb_q_reminder_title),
                    options = listOf(stringResource(R.string.onb_reminder_on), stringResource(R.string.onb_reminder_off)),
                    selected = when (reminders) { true -> 0; false -> 1; null -> -1 },
                    onSelect = { reminders = it == 0 }
                )
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.onb_reminder_note), style = MaterialTheme.typography.bodyMedium, color = ShadowColors.TextMuted)
            }
        }
        if (index >= introPages.size && !answered) {
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.onb_pick_one), color = ShadowColors.TextMuted)
        }
        Spacer(Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (index > 0) {
                OutlinedButton(onClick = { index-- }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.onb_back))
                }
            }
            PrimaryButton(
                text = stringResource(if (last) R.string.onb_start else R.string.onb_next),
                enabled = answered,
                onClick = {
                    if (!last) {
                        index++
                    } else {
                        if (reminders == true && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        onDone(
                            OnboardingAnswers(
                                name = name.trim(),
                                base = base,
                                wakeHour = WAKE_HOURS[wake],
                                focus = focus,
                                struggle = struggle,
                                minutes = minutes,
                                reminders = reminders == true
                            )
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun QuestionBlock(title: String, options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Text(title, style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(12.dp))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { i, label ->
            FilterChip(
                selected = selected == i,
                onClick = { onSelect(i) },
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            )
        }
    }
}

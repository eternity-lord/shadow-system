package com.shadowsystem.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shadowsystem.R
import com.shadowsystem.ui.components.PrimaryButton
import com.shadowsystem.ui.theme.ShadowColors

private val pages = listOf(
    R.string.onb_title_1 to R.string.onb_body_1,
    R.string.onb_title_2 to R.string.onb_body_2,
    R.string.onb_title_3 to R.string.onb_body_3,
    R.string.onb_title_4 to R.string.onb_body_4
)

/** Five short screens shown once on first launch: four intro pages and one question. */
@Composable
fun OnboardingScreen(onDone: (Int) -> Unit) {
    var index by remember { mutableIntStateOf(0) }
    var focus by remember { mutableIntStateOf(-1) }
    val total = pages.size + 1
    val last = index == total - 1
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.onb_progress, index + 1, total),
            style = MaterialTheme.typography.bodyMedium,
            color = ShadowColors.TextMuted
        )
        Spacer(Modifier.height(16.dp))
        if (index < pages.size) {
            val (title, body) = pages[index]
            Text(stringResource(title), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(body), style = MaterialTheme.typography.bodyLarge, color = ShadowColors.TextMuted)
        } else {
            Text(stringResource(R.string.onb_title_5), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(R.string.onb_focus_1, R.string.onb_focus_2, R.string.onb_focus_3).forEachIndexed { i, label ->
                    FilterChip(
                        selected = focus == i,
                        onClick = { focus = i },
                        label = { Text(stringResource(label)) },
                        modifier = Modifier.heightIn(min = 48.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.onb_soldier_hint), style = MaterialTheme.typography.bodyLarge, color = ShadowColors.TextMuted)
            if (focus < 0) {
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.onb_pick_one), color = ShadowColors.TextMuted)
            }
        }
        Spacer(Modifier.height(32.dp))
        PrimaryButton(
            text = stringResource(if (last) R.string.onb_start else R.string.onb_next),
            onClick = {
                if (!last) index++
                else if (focus >= 0) onDone(focus)
            }
        )
    }
}

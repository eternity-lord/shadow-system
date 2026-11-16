package com.shadowsystem.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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

/** Four short screens shown once on first launch. */
@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    var index by remember { mutableIntStateOf(0) }
    val (title, body) = pages[index]
    val last = index == pages.lastIndex
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.onb_progress, index + 1, pages.size),
            style = MaterialTheme.typography.bodyMedium,
            color = ShadowColors.TextMuted
        )
        Spacer(Modifier.height(16.dp))
        Text(stringResource(title), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Text(stringResource(body), style = MaterialTheme.typography.bodyLarge, color = ShadowColors.TextMuted)
        Spacer(Modifier.height(32.dp))
        PrimaryButton(
            text = stringResource(if (last) R.string.onb_start else R.string.onb_next),
            onClick = { if (last) onDone() else index++ }
        )
    }
}

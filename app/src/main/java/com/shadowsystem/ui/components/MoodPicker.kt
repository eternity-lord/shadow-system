package com.shadowsystem.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shadowsystem.R
import com.shadowsystem.core.Mood
import com.shadowsystem.ui.theme.ShadowColors

/** Asks how the user feels; the choice decides how many quests are generated. */
@Composable
fun MoodPicker(onPick: (Mood) -> Unit) {
    ShadowCard {
        Text(stringResource(R.string.mood_question), style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { onPick(Mood.GREAT) },
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ShadowColors.Gold)
            ) { Text(stringResource(R.string.mood_great), color = ShadowColors.Background) }
            Button(
                onClick = { onPick(Mood.NORMAL) },
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ShadowColors.Primary)
            ) { Text(stringResource(R.string.mood_normal), color = ShadowColors.TextPrimary) }
            Button(
                onClick = { onPick(Mood.HARD) },
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ShadowColors.Secondary)
            ) { Text(stringResource(R.string.mood_hard), color = ShadowColors.Background) }
        }
    }
}

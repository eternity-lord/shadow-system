package com.shadowsystem.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shadowsystem.R
import com.shadowsystem.ui.theme.ShadowColors

/** Top bar for sub-screens. The arrow points right because the layout is right-to-left. */
@Composable
fun BackHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) {
            Icon(Icons.Filled.ArrowForward, contentDescription = stringResource(R.string.back))
        }
        Text(title, style = MaterialTheme.typography.headlineMedium)
    }
}

/** Shows a 5-second undo for the last ticked step. */
@Composable
fun UndoBanner(text: String, onUndo: () -> Unit) {
    ShadowCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f).padding(end = 8.dp))
            TextButton(onClick = onUndo, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.quest_undo), color = ShadowColors.Secondary)
            }
        }
    }
}

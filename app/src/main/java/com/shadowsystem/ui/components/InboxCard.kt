package com.shadowsystem.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.shadowsystem.core.PersianText
import com.shadowsystem.data.db.InboxEntity
import com.shadowsystem.ui.theme.ShadowColors

/** "Park a thought": quick note with a triage list. Deleting asks for confirmation first. */
@Composable
fun InboxCard(
    items: List<InboxEntity>,
    pendingDelete: Long?,
    onAdd: (String) -> Unit,
    onAskDelete: (Long) -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    if (pendingDelete != null) {
        ConfirmDialog(
            text = stringResource(R.string.inbox_delete_confirm),
            confirmText = stringResource(R.string.delete),
            onConfirm = onConfirmDelete,
            onDismiss = onCancelDelete
        )
    }
    ShadowCard {
        SectionTitle(stringResource(R.string.inbox_title))
        Text(stringResource(R.string.inbox_hint), style = MaterialTheme.typography.bodyMedium, color = ShadowColors.TextMuted)
        OutlinedTextField(
            value = text,
            onValueChange = { text = it.take(200) },
            label = { Text(stringResource(R.string.inbox_field)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        PrimaryButton(
            stringResource(R.string.inbox_save),
            onClick = {
                onAdd(text)
                text = ""
            },
            enabled = text.isNotBlank()
        )
        if (items.isEmpty()) {
            Text(stringResource(R.string.inbox_empty), style = MaterialTheme.typography.bodyMedium, color = ShadowColors.TextMuted)
        }
        items.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                TextButton(onClick = { onAskDelete(item.id) }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.delete), color = ShadowColors.Danger)
                }
            }
        }
    }
}

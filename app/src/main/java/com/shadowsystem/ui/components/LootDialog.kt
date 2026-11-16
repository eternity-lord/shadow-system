package com.shadowsystem.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shadowsystem.R
import com.shadowsystem.core.PersianText
import com.shadowsystem.ui.theme.ShadowColors
import com.shadowsystem.ui.vm.LootInfo

/** Loot after a dungeon run: XP, gold, a rare-item chance, and an optional journal line. */
@Composable
fun LootDialog(loot: LootInfo, onClose: (String) -> Unit) {
    var note by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { onClose(note) },
        containerColor = ShadowColors.Surface,
        title = { Text(stringResource(R.string.loot_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.loot_xp, PersianText.toFa(loot.xp)))
                if (loot.gold > 0) Text(stringResource(R.string.loot_gold, PersianText.toFa(loot.gold)))
                if (loot.rare) Text(stringResource(R.string.loot_rare), color = ShadowColors.Gold)
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(140) },
                    label = { Text(stringResource(R.string.loot_note_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onClose(note) }, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.loot_save))
            }
        }
    )
}

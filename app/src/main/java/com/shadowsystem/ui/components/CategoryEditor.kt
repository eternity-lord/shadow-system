package com.shadowsystem.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shadowsystem.R
import com.shadowsystem.ShadowApp
import com.shadowsystem.core.CategoryRules
import com.shadowsystem.data.db.CategoryEntity
import com.shadowsystem.ui.theme.ShadowColors
import com.shadowsystem.ui.vm.CategoryViewModel

/** Names of the categories the user can pick now, for one kind (expense or income). */
@Composable
fun rememberCategoryNames(isIncome: Boolean): List<String> {
    val app = LocalContext.current.applicationContext as ShadowApp
    val flow = remember(isIncome) { app.container.categories.observeActive(isIncome) }
    val names by flow.collectAsStateWithLifecycle(initialValue = emptyList())
    return names
}

/** Add, rename and archive money categories. Archived ones stay on old transactions. */
@Composable
fun CategoryEditorDialog(vm: CategoryViewModel, onDismiss: () -> Unit) {
    val all by vm.all.collectAsStateWithLifecycle()
    val notice by vm.notice.collectAsStateWithLifecycle()
    var newName by remember { mutableStateOf("") }
    var newIncome by remember { mutableStateOf(false) }
    var renameFrom by remember { mutableStateOf<String?>(null) }
    var renameTo by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.category_title)) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                notice?.let { n ->
                    Text(stringResource(noticeRes(n)), color = ShadowColors.Secondary)
                }
                all.forEach { c ->
                    CategoryRow(
                        category = c,
                        onRename = {
                            renameFrom = c.name
                            renameTo = c.name
                        },
                        onArchive = { vm.setArchived(c.name, !c.archived) }
                    )
                }
                Text(stringResource(R.string.category_archive_help), color = ShadowColors.TextMuted, style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.category_add_label), style = MaterialTheme.typography.titleSmall)
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.category_add_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !newIncome,
                        onClick = { newIncome = false },
                        label = { Text(stringResource(R.string.category_kind_expense)) },
                        modifier = Modifier.heightIn(min = 48.dp)
                    )
                    FilterChip(
                        selected = newIncome,
                        onClick = { newIncome = true },
                        label = { Text(stringResource(R.string.category_kind_income)) },
                        modifier = Modifier.heightIn(min = 48.dp)
                    )
                }
                Button(
                    onClick = {
                        vm.clearNotice()
                        vm.add(newName, newIncome)
                        newName = ""
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) { Text(stringResource(R.string.category_add_button)) }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.category_close))
            }
        }
    )

    renameFrom?.let { from ->
        AlertDialog(
            onDismissRequest = { renameFrom = null },
            title = { Text(stringResource(R.string.category_rename_title)) },
            text = {
                OutlinedTextField(
                    value = renameTo,
                    onValueChange = { renameTo = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.clearNotice()
                        vm.rename(from, renameTo)
                        renameFrom = null
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) { Text(stringResource(R.string.category_save)) }
            },
            dismissButton = {
                TextButton(onClick = { renameFrom = null }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun CategoryRow(category: CategoryEntity, onRename: () -> Unit, onArchive: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                category.name,
                color = if (category.archived) ShadowColors.TextMuted else MaterialTheme.colorScheme.onSurface
            )
            val kind = stringResource(if (category.isIncome) R.string.category_kind_income else R.string.category_kind_expense)
            val state = if (category.archived) " · " + stringResource(R.string.category_archived) else ""
            Text(kind + state, color = ShadowColors.TextMuted, style = MaterialTheme.typography.bodySmall)
        }
        TextButton(onClick = onRename, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(R.string.category_rename))
        }
        TextButton(onClick = onArchive, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(if (category.archived) R.string.category_restore else R.string.category_archive))
        }
    }
}

private fun noticeRes(c: CategoryRules.Check): Int = when (c) {
    CategoryRules.Check.OK -> R.string.category_done
    CategoryRules.Check.EMPTY -> R.string.category_err_empty
    CategoryRules.Check.TOO_LONG -> R.string.category_err_long
    CategoryRules.Check.EXISTS -> R.string.category_err_exists
}

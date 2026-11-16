package com.shadowsystem.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shadowsystem.R
import com.shadowsystem.ui.components.ConfirmDialog
import com.shadowsystem.ui.components.EntryCard
import com.shadowsystem.ui.components.ShadowCard
import com.shadowsystem.ui.components.SummaryCard
import com.shadowsystem.ui.components.TransactionRow
import com.shadowsystem.ui.theme.ShadowColors
import com.shadowsystem.ui.vm.MoneyViewModel
import com.shadowsystem.ui.vm.TypeFilter

/** Money tab: quick entry, month navigation, search and filters. */
@Composable
fun MoneyScreen(vm: MoneyViewModel, onPlan: () -> Unit, onReports: () -> Unit, onWallets: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    var receiptTarget by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<Long?>(null) }
    val receiptPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri ->
        val id = receiptTarget
        receiptTarget = null
        if (uri != null && id != null) {
            // Keep read access across restarts; some providers refuse, which is fine for this session.
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            vm.setReceipt(id, uri.toString())
        }
    }
    state.pendingDelete?.let {
        ConfirmDialog(
            text = stringResource(R.string.money_delete_confirm),
            confirmText = stringResource(R.string.money_yes_delete),
            onConfirm = { vm.confirmDelete() },
            onDismiss = { vm.cancelDelete() }
        )
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.money_title), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = onPlan, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.money_plan)) }
                TextButton(onClick = onReports, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.money_reports)) }
                TextButton(onClick = onWallets, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.money_wallets)) }
            }
        }
        item { MonthBar(state.monthLabel, state.canGoForward, onPrev = { vm.previousMonth() }, onNext = { vm.nextMonth() }) }
        if (state.wallets.size > 1) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.wallet_pick), modifier = Modifier.padding(top = 12.dp))
                    state.wallets.forEach { w ->
                        FilterChip(
                            selected = state.form.walletId == w.id,
                            onClick = { vm.onWallet(w.id) },
                            label = { Text(w.name) },
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                }
            }
        }
        item {
            EntryCard(
                form = state.form,
                onIncome = vm::onIncomeToggle,
                onAmount = vm::onAmountChange,
                onCategory = vm::onCategory,
                onNote = vm::onNoteChange,
                onSave = vm::save
            )
        }
        item {
            SummaryCard(state.monthSpentRial, state.monthIncomeRial, state.todaySpentRial, state.hidden) { vm.toggleHidden() }
        }
        item {
            OutlinedTextField(
                value = state.query,
                onValueChange = vm::onQuery,
                label = { Text(stringResource(R.string.search_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.filter == TypeFilter.ALL,
                    onClick = { vm.onFilter(TypeFilter.ALL) },
                    label = { Text(stringResource(R.string.filter_all)) },
                    modifier = Modifier.heightIn(min = 48.dp)
                )
                FilterChip(
                    selected = state.filter == TypeFilter.EXPENSE,
                    onClick = { vm.onFilter(TypeFilter.EXPENSE) },
                    label = { Text(stringResource(R.string.money_expense)) },
                    modifier = Modifier.heightIn(min = 48.dp)
                )
                FilterChip(
                    selected = state.filter == TypeFilter.INCOME,
                    onClick = { vm.onFilter(TypeFilter.INCOME) },
                    label = { Text(stringResource(R.string.money_income)) },
                    modifier = Modifier.heightIn(min = 48.dp)
                )
            }
        }
        if (state.entries.isEmpty()) {
            item { Text(stringResource(R.string.money_empty), color = ShadowColors.TextMuted) }
        }
        items(state.entries, key = { it.id }) { tx ->
            TransactionRow(
                tx,
                state.hidden,
                onReceipt = {
                    receiptTarget = tx.id
                    receiptPicker.launch(arrayOf("image/*"))
                }
            ) { vm.askDelete(tx) }
        }
        item { Text(stringResource(R.string.money_privacy), style = MaterialTheme.typography.bodyMedium, color = ShadowColors.TextMuted) }
    }
}

@Composable
private fun MonthBar(label: String, canGoForward: Boolean, onPrev: () -> Unit, onNext: () -> Unit) {
    ShadowCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onPrev, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.month_prev)) }
            Text(label, style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onNext, enabled = canGoForward, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.month_next)) }
        }
    }
}

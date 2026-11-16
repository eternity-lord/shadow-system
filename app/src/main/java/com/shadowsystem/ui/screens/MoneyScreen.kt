package com.shadowsystem.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shadowsystem.R
import com.shadowsystem.core.Jalali
import com.shadowsystem.core.MoneyRules
import com.shadowsystem.core.PersianText
import com.shadowsystem.data.db.TransactionEntity
import com.shadowsystem.ui.components.ConfirmDialog
import com.shadowsystem.ui.components.PrimaryButton
import com.shadowsystem.ui.components.ShadowCard
import com.shadowsystem.ui.theme.ShadowColors
import com.shadowsystem.ui.vm.MoneyForm
import com.shadowsystem.ui.vm.MoneyViewModel

/** Money: quick expense/income logging, monthly summary and history. */
@Composable
fun MoneyScreen(vm: MoneyViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    state.pendingDelete?.let { tx ->
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
        item { Text(stringResource(R.string.money_title), style = MaterialTheme.typography.headlineMedium) }
        item { Entry(state.form, vm) }
        item { Summary(state.monthSpentRial, state.monthIncomeRial, state.todaySpentRial) }
        item { Text(stringResource(R.string.money_privacy), style = MaterialTheme.typography.bodyMedium, color = ShadowColors.TextMuted) }
        if (state.entries.isEmpty()) {
            item { Text(stringResource(R.string.money_empty), color = ShadowColors.TextMuted) }
        }
        items(state.entries, key = { it.id }) { tx -> TransactionRow(tx) { vm.askDelete(tx) } }
    }
}

@Composable
private fun Entry(form: MoneyForm, vm: MoneyViewModel) {
    ShadowCard {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = !form.isIncome,
                onClick = { vm.onIncomeToggle(false) },
                label = { Text(stringResource(R.string.money_expense)) },
                modifier = Modifier.heightIn(min = 48.dp)
            )
            FilterChip(
                selected = form.isIncome,
                onClick = { vm.onIncomeToggle(true) },
                label = { Text(stringResource(R.string.money_income)) },
                modifier = Modifier.heightIn(min = 48.dp)
            )
        }
        OutlinedTextField(
            value = form.amountText,
            onValueChange = vm::onAmountChange,
            label = { Text(stringResource(R.string.money_amount_hint)) },
            isError = form.error,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (form.error) {
            Text(stringResource(R.string.money_amount_invalid), color = ShadowColors.Danger, style = MaterialTheme.typography.bodyMedium)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(MoneyRules.CATEGORIES) { cat ->
                FilterChip(
                    selected = form.category == cat,
                    onClick = { vm.onCategory(cat) },
                    label = { Text(cat) },
                    modifier = Modifier.heightIn(min = 48.dp)
                )
            }
        }
        OutlinedTextField(
            value = form.note,
            onValueChange = vm::onNoteChange,
            label = { Text(stringResource(R.string.money_note_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        PrimaryButton(stringResource(R.string.money_save), onClick = { vm.save() })
    }
}

@Composable
private fun Summary(spent: Long, income: Long, today: Long) {
    ShadowCard {
        Text(stringResource(R.string.money_month_spent) + ": " + PersianText.formatMoney(spent), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.money_month_income) + ": " + PersianText.formatMoney(income), style = MaterialTheme.typography.bodyMedium, color = ShadowColors.TextMuted)
        Text(stringResource(R.string.money_today) + ": " + PersianText.formatMoney(today), style = MaterialTheme.typography.bodyMedium, color = ShadowColors.TextMuted)
    }
}

@Composable
private fun TransactionRow(tx: TransactionEntity, onDelete: () -> Unit) {
    ShadowCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    (if (tx.isIncome) "+ " else "− ") + PersianText.formatMoney(tx.amountRial),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (tx.isIncome) ShadowColors.Secondary else ShadowColors.TextPrimary
                )
                Text(
                    listOf(tx.category, tx.note).filter { it.isNotBlank() }.joinToString(" · ") +
                        "  ·  " + Jalali.format(Jalali.fromGregorian(java.time.LocalDate.ofEpochDay(tx.epochDay))),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ShadowColors.TextMuted
                )
            }
            TextButton(onClick = onDelete, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.delete), color = ShadowColors.Danger)
            }
        }
    }
}

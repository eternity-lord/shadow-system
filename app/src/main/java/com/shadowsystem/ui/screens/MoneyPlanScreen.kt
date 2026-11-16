package com.shadowsystem.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shadowsystem.R
import com.shadowsystem.ui.components.BackHeader
import com.shadowsystem.ui.components.ConfirmDialog
import com.shadowsystem.ui.components.MessageBanner
import com.shadowsystem.ui.vm.PlanNotice
import com.shadowsystem.ui.vm.PlanViewModel

/** Budgets, recurring rules, savings goals and debts on one screen. */
@Composable
fun MoneyPlanScreen(vm: PlanViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    state.pending?.let { p ->
        ConfirmDialog(
            text = stringResource(R.string.plan_delete_confirm),
            confirmText = stringResource(R.string.money_yes_delete),
            onConfirm = { vm.confirmDelete() },
            onDismiss = { vm.cancelDelete() }
        )
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { BackHeader(stringResource(R.string.plan_title), onBack) }
        state.notice?.let { n ->
            item { MessageBanner(noticeText(n)) { vm.clearNotice() } }
        }
        item { BudgetSection(state.budgets, onSave = vm::saveBudget, onRemove = { vm.ask("budget", category = it) }) }
        item {
            RecurringSection(
                state.recurring,
                onAdd = vm::addRecurring,
                onDelete = { vm.ask("recurring", id = it) }
            )
        }
        item { GoalSection(state.goals, onAdd = vm::addGoal, onDeposit = vm::depositGoal, onDelete = { vm.ask("goal", id = it) }) }
        item { DebtSection(state.debts, onAdd = vm::addDebt, onPay = vm::payDebt, onDelete = { vm.ask("debt", id = it) }) }
        item { Text(stringResource(R.string.plan_note), style = MaterialTheme.typography.bodyMedium) }
    }
}

@Composable
private fun noticeText(n: PlanNotice): String = stringResource(
    when (n) {
        PlanNotice.INVALID_AMOUNT -> R.string.money_amount_invalid
        PlanNotice.EMPTY_TITLE -> R.string.plan_title_required
        PlanNotice.PAYMENT_TOO_HIGH -> R.string.plan_payment_too_high
        PlanNotice.SAVED -> R.string.plan_saved
    }
)

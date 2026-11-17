package com.shadowsystem.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shadowsystem.R
import com.shadowsystem.core.FinanceRules
import com.shadowsystem.core.MoneyRules
import com.shadowsystem.core.PersianText
import com.shadowsystem.core.PhraseBank
import com.shadowsystem.data.db.DebtEntity
import com.shadowsystem.data.db.GoalEntity
import com.shadowsystem.data.db.RecurringEntity
import com.shadowsystem.ui.components.AmountField
import com.shadowsystem.ui.components.CategoryChips
import com.shadowsystem.ui.components.rememberCategoryNames
import com.shadowsystem.ui.components.PrimaryButton
import com.shadowsystem.ui.components.SectionTitle
import com.shadowsystem.ui.components.ShadowCard
import com.shadowsystem.ui.components.XpBar
import com.shadowsystem.ui.theme.ShadowColors
import com.shadowsystem.ui.vm.BudgetRow

@Composable
fun BudgetSection(rows: List<BudgetRow>, onSave: (String, String) -> Unit, onRemove: (String) -> Unit) {
    var category by remember { mutableStateOf(MoneyRules.CATEGORIES.first()) }
    var amount by remember { mutableStateOf("") }
    val categories = rememberCategoryNames(false)
    val alert = remember(rows) { rows.any { it.level != FinanceRules.BudgetState.OK } }
    val phrase = remember(alert) { PhraseBank.budget() }
    ShadowCard {
        SectionTitle(stringResource(R.string.budget_section))
        if (alert) Text(phrase, color = ShadowColors.Gold, style = MaterialTheme.typography.bodyMedium)
        if (rows.isEmpty()) Text(stringResource(R.string.budget_empty), color = ShadowColors.TextMuted)
        rows.forEach { row ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                XpBar(
                    progress = if (row.limitRial > 0) row.spentRial.toFloat() / row.limitRial else 0f,
                    label = "${row.category}: ${PersianText.formatMoney(row.spentRial)} / ${PersianText.formatMoney(row.limitRial)}"
                )
                if (row.carriedRial > 0L) {
                    Text(
                        stringResource(R.string.budget_carried, PersianText.formatMoney(row.carriedRial)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ShadowColors.TextMuted
                    )
                }
                Row {
                    Text(
                        stringResource(
                            when (row.level) {
                                FinanceRules.BudgetState.OK -> R.string.budget_ok
                                FinanceRules.BudgetState.WARN -> R.string.budget_warn
                                FinanceRules.BudgetState.OVER -> R.string.budget_over
                            }
                        ),
                        color = when (row.level) {
                            FinanceRules.BudgetState.OK -> ShadowColors.TextMuted
                            FinanceRules.BudgetState.WARN -> ShadowColors.Gold
                            FinanceRules.BudgetState.OVER -> ShadowColors.Danger
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { onRemove(row.category) }, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text(stringResource(R.string.delete), color = ShadowColors.Danger)
                    }
                }
            }
        }
        CategoryChips(category, categories) { category = it }
        AmountField(amount, { amount = PersianText.parseDigits(it).filter { c -> c.isDigit() } }, stringResource(R.string.money_amount_hint))
        PrimaryButton(stringResource(R.string.budget_save), onClick = { onSave(category, amount) })
    }
}

@Composable
fun RecurringSection(rules: List<RecurringEntity>, onAdd: (String, String, String, Boolean, Boolean) -> Unit, onDelete: (Long) -> Unit) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(MoneyRules.CATEGORIES.first()) }
    var income by remember { mutableStateOf(false) }
    var monthly by remember { mutableStateOf(true) }
    val categories = rememberCategoryNames(income)
    ShadowCard {
        SectionTitle(stringResource(R.string.recurring_section))
        if (rules.isEmpty()) Text(stringResource(R.string.recurring_empty), color = ShadowColors.TextMuted)
        rules.forEach { r ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${r.title} · ${PersianText.formatMoney(r.amountRial)} · " +
                        stringResource(if (r.monthly) R.string.every_month else R.string.every_week),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { onDelete(r.id) }, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.delete), color = ShadowColors.Danger)
                }
            }
        }
        OutlinedTextField(title, { title = it.take(40) }, label = { Text(stringResource(R.string.plan_title_hint)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        AmountField(amount, { amount = PersianText.parseDigits(it).filter { c -> c.isDigit() } }, stringResource(R.string.money_amount_hint))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !income, onClick = { income = false }, label = { Text(stringResource(R.string.money_expense)) }, modifier = Modifier.heightIn(min = 48.dp))
            FilterChip(selected = income, onClick = { income = true }, label = { Text(stringResource(R.string.money_income)) }, modifier = Modifier.heightIn(min = 48.dp))
            FilterChip(selected = monthly, onClick = { monthly = true }, label = { Text(stringResource(R.string.every_month)) }, modifier = Modifier.heightIn(min = 48.dp))
            FilterChip(selected = !monthly, onClick = { monthly = false }, label = { Text(stringResource(R.string.every_week)) }, modifier = Modifier.heightIn(min = 48.dp))
        }
        CategoryChips(category, categories) { category = it }
        PrimaryButton(stringResource(R.string.recurring_save), onClick = { onAdd(title, amount, category, income, monthly) })
    }
}

@Composable
fun GoalSection(goals: List<GoalEntity>, onAdd: (String, String) -> Unit, onDeposit: (GoalEntity, String) -> Unit, onDelete: (Long) -> Unit) {
    var title by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    ShadowCard {
        SectionTitle(stringResource(R.string.goal_section))
        if (goals.isEmpty()) Text(stringResource(R.string.goal_empty), color = ShadowColors.TextMuted)
        goals.forEach { g -> GoalRow(g, onDeposit, onDelete) }
        OutlinedTextField(title, { title = it.take(40) }, label = { Text(stringResource(R.string.plan_title_hint)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        AmountField(target, { target = PersianText.parseDigits(it).filter { c -> c.isDigit() } }, stringResource(R.string.goal_target_hint))
        PrimaryButton(stringResource(R.string.goal_save), onClick = { onAdd(title, target) })
    }
}

@Composable
private fun GoalRow(g: GoalEntity, onDeposit: (GoalEntity, String) -> Unit, onDelete: (Long) -> Unit) {
    var deposit by remember(g.id) { mutableStateOf("") }
    val done = g.savedRial >= g.targetRial
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        XpBar(
            progress = g.savedRial.toFloat() / g.targetRial.coerceAtLeast(1L),
            label = "${g.title}: ${PersianText.toFa(FinanceRules.goalPercent(g.savedRial, g.targetRial))}٪ · " +
                PersianText.formatMoney(g.savedRial) + " / " + PersianText.formatMoney(g.targetRial)
        )
        if (done) Text(stringResource(R.string.goal_done), color = ShadowColors.Secondary, style = MaterialTheme.typography.bodyMedium)
        goalEstimate(g)?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = ShadowColors.TextMuted) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            AmountField(deposit, { deposit = PersianText.parseDigits(it).filter { c -> c.isDigit() } }, stringResource(R.string.goal_deposit_hint), modifier = Modifier.weight(1f))
            Button(onClick = { onDeposit(g, deposit); deposit = "" }, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.goal_deposit)) }
            TextButton(onClick = { onDelete(g.id) }, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.delete), color = ShadowColors.Danger) }
        }
    }
}

@Composable
fun DebtSection(
    debts: List<DebtEntity>,
    onAdd: (String, String, Boolean, String, com.shadowsystem.core.Jalali.Date) -> Unit,
    onPay: (DebtEntity, String) -> Unit,
    onDelete: (Long) -> Unit,
    onInstall: (DebtEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var installments by remember { mutableStateOf("") }
    var iOwe by remember { mutableStateOf(true) }
    var due by remember { mutableStateOf(com.shadowsystem.core.Jalali.fromMillis(System.currentTimeMillis() + 30L * 86_400_000L)) }
    ShadowCard {
        SectionTitle(stringResource(R.string.debt_section))
        if (debts.isEmpty()) Text(stringResource(R.string.debt_empty), color = ShadowColors.TextMuted)
        debts.forEach { d -> DebtRow(d, onPay, onDelete, onInstall) }
        OutlinedTextField(title, { title = it.take(40) }, label = { Text(stringResource(R.string.plan_title_hint)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        AmountField(amount, { amount = PersianText.parseDigits(it).filter { c -> c.isDigit() } }, stringResource(R.string.money_amount_hint))
        AmountField(installments, { installments = PersianText.parseDigits(it).filter { c -> c.isDigit() }.take(3) }, stringResource(R.string.debt_installments_hint))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = iOwe, onClick = { iOwe = true }, label = { Text(stringResource(R.string.debt_i_owe)) }, modifier = Modifier.heightIn(min = 48.dp))
            FilterChip(selected = !iOwe, onClick = { iOwe = false }, label = { Text(stringResource(R.string.debt_owed_to_me)) }, modifier = Modifier.heightIn(min = 48.dp))
        }
        Text(stringResource(R.string.debt_due), color = ShadowColors.TextMuted)
        com.shadowsystem.ui.components.JalaliDatePicker(due) { due = it }
        PrimaryButton(stringResource(R.string.debt_save), onClick = { onAdd(title, amount, iOwe, installments, due) })
    }
}

@Composable
private fun DebtRow(d: DebtEntity, onPay: (DebtEntity, String) -> Unit, onDelete: (Long) -> Unit, onInstall: (DebtEntity) -> Unit) {
    var pay by remember(d.id) { mutableStateOf("") }
    val remaining = FinanceRules.debtRemaining(d.totalRial, d.paidRial)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "${d.title} · " + stringResource(if (d.iOwe) R.string.debt_i_owe else R.string.debt_owed_to_me) +
                " · " + stringResource(R.string.debt_remaining, PersianText.formatMoney(remaining)),
            style = MaterialTheme.typography.bodyMedium
        )
        if (d.installmentCount > 0) {
            Text(
                stringResource(
                    R.string.debt_installment_progress,
                    PersianText.toFa(d.installmentsPaid),
                    PersianText.toFa(d.installmentCount)
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = ShadowColors.TextMuted
            )
            if (d.installmentsPaid < d.installmentCount) {
                Button(onClick = { onInstall(d) }, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.debt_installment_pay)) }
            }
        }
        if (remaining > 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AmountField(pay, { pay = PersianText.parseDigits(it).filter { c -> c.isDigit() } }, stringResource(R.string.debt_pay_hint), modifier = Modifier.weight(1f))
                Button(onClick = { onPay(d, pay); pay = "" }, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.debt_pay)) }
            }
        }
        TextButton(onClick = { onDelete(d.id) }, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.delete), color = ShadowColors.Danger) }
    }
}

/** Estimate from the average daily saving since the goal was created. Null when the goal is already met. */
@Composable
private fun goalEstimate(g: GoalEntity): String? {
    val today = com.shadowsystem.core.DayRules.gameDay(System.currentTimeMillis())
    val days = (today - g.createdDay + 1).coerceAtLeast(1L)
    val estimate = FinanceRules.savingsEstimateDays(g.targetRial - g.savedRial, g.savedRial.toDouble() / days)
    return when {
        estimate == null -> stringResource(R.string.goal_estimate_none)
        estimate == 0L -> null
        else -> stringResource(R.string.goal_estimate, PersianText.toFa(estimate))
    }
}

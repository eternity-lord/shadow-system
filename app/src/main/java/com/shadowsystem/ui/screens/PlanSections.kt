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
import androidx.compose.runtime.saveable.rememberSaveable
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
    var category by rememberSaveable { mutableStateOf(MoneyRules.CATEGORIES.first()) }
    var amount by rememberSaveable { mutableStateOf("") }
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
fun RecurringSection(rules: List<RecurringEntity>, onAdd: (String, String, String, Boolean, com.shadowsystem.core.RecurrencePeriod) -> Unit, onDelete: (Long) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(MoneyRules.CATEGORIES.first()) }
    var income by rememberSaveable { mutableStateOf(false) }
    var period by remember { mutableStateOf(com.shadowsystem.core.RecurrencePeriod.MONTHLY) }
    val categories = rememberCategoryNames(income)
    ShadowCard {
        SectionTitle(stringResource(R.string.recurring_section))
        if (rules.isEmpty()) Text(stringResource(R.string.recurring_empty), color = ShadowColors.TextMuted)
        rules.forEach { r ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${r.title} · ${PersianText.formatMoney(r.amountRial)} · " +
                        stringResource(periodLabel(com.shadowsystem.core.RecurrencePeriod.parse(r.period))),
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
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            com.shadowsystem.core.RecurrencePeriod.values().forEach { p ->
                FilterChip(
                    selected = period == p,
                    onClick = { period = p },
                    label = { Text(stringResource(periodLabel(p))) },
                    modifier = Modifier.heightIn(min = 48.dp)
                )
            }
        }
        CategoryChips(category, categories) { category = it }
        PrimaryButton(stringResource(R.string.recurring_save), onClick = { onAdd(title, amount, category, income, period) })
    }
}

@Composable
fun GoalSection(
    goals: List<GoalEntity>,
    wallets: List<com.shadowsystem.data.db.WalletEntity>,
    balances: Map<Long, Long>,
    onAdd: (String, String, Long?, Long?) -> Unit,
    onDeposit: (GoalEntity, String) -> Unit,
    onDelete: (Long) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var target by rememberSaveable { mutableStateOf("") }
    var hasDeadline by rememberSaveable { mutableStateOf(false) }
    // The deadline is kept as an epoch day so it survives process death; the picker shows it in Jalali.
    var deadlineDay by rememberSaveable { mutableStateOf(java.time.LocalDate.now().toEpochDay() + 90L) }
    var walletId by rememberSaveable { mutableStateOf<Long?>(null) }
    val walletNames = wallets.associate { it.id to it.name }
    ShadowCard {
        SectionTitle(stringResource(R.string.goal_section))
        if (goals.isEmpty()) Text(stringResource(R.string.goal_empty), color = ShadowColors.TextMuted)
        goals.forEach { g -> GoalRow(g, g.walletId?.let { walletNames[it] }, g.walletId?.let { balances[it] }, onDeposit, onDelete) }
        OutlinedTextField(title, { title = it.take(40) }, label = { Text(stringResource(R.string.plan_title_hint)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        AmountField(target, { target = PersianText.parseDigits(it).filter { c -> c.isDigit() } }, stringResource(R.string.goal_target_hint))
        FilterChip(
            selected = hasDeadline,
            onClick = { hasDeadline = !hasDeadline },
            label = { Text(stringResource(R.string.goal_deadline_toggle)) },
            modifier = Modifier.heightIn(min = 48.dp)
        )
        if (hasDeadline) {
            com.shadowsystem.ui.components.JalaliDatePicker(
                com.shadowsystem.core.Jalali.fromGregorian(java.time.LocalDate.ofEpochDay(deadlineDay))
            ) { picked ->
                deadlineDay = com.shadowsystem.core.Jalali.toGregorian(picked.year, picked.month, picked.day).toEpochDay()
            }
        }
        Text(stringResource(R.string.goal_wallet_pick), color = ShadowColors.TextMuted)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = walletId == null,
                onClick = { walletId = null },
                label = { Text(stringResource(R.string.goal_no_wallet)) },
                modifier = Modifier.heightIn(min = 48.dp)
            )
            wallets.forEach { w ->
                FilterChip(
                    selected = walletId == w.id,
                    onClick = { walletId = w.id },
                    label = { Text(w.name) },
                    modifier = Modifier.heightIn(min = 48.dp)
                )
            }
        }
        PrimaryButton(stringResource(R.string.goal_save), onClick = {
            val due = if (hasDeadline) deadlineDay else null
            onAdd(title, target, due, walletId)
        })
    }
}

@Composable
private fun GoalRow(g: GoalEntity, walletName: String?, walletBalance: Long?, onDeposit: (GoalEntity, String) -> Unit, onDelete: (Long) -> Unit) {
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
        g.deadlineEpochDay?.let { due ->
            val left = due - java.time.LocalDate.now().toEpochDay()
            val dateText = com.shadowsystem.core.Jalali.format(
                com.shadowsystem.core.Jalali.fromGregorian(java.time.LocalDate.ofEpochDay(due))
            )
            Text(
                if (left >= 0) stringResource(R.string.goal_deadline_left, dateText, PersianText.toFa(left.toInt()))
                else stringResource(R.string.goal_deadline_passed, dateText),
                style = MaterialTheme.typography.bodyMedium,
                color = ShadowColors.TextMuted
            )
        }
        walletName?.let { Text(stringResource(R.string.goal_wallet_line, it), style = MaterialTheme.typography.bodyMedium, color = ShadowColors.TextMuted) }
        if (walletBalance != null && !done && walletBalance < g.targetRial - g.savedRial) {
            Text(stringResource(R.string.goal_wallet_short), style = MaterialTheme.typography.bodyMedium, color = ShadowColors.Danger)
        }
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

private fun periodLabel(p: com.shadowsystem.core.RecurrencePeriod): Int = when (p) {
    com.shadowsystem.core.RecurrencePeriod.DAILY -> R.string.every_day
    com.shadowsystem.core.RecurrencePeriod.WEEKLY -> R.string.every_week
    com.shadowsystem.core.RecurrencePeriod.MONTHLY -> R.string.every_month
    com.shadowsystem.core.RecurrencePeriod.YEARLY -> R.string.every_year
}

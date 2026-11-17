package com.shadowsystem.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shadowsystem.R
import com.shadowsystem.core.Jalali
import com.shadowsystem.core.MoneyRules
import com.shadowsystem.core.PersianText
import com.shadowsystem.data.db.TransactionEntity
import com.shadowsystem.ui.theme.ShadowColors
import com.shadowsystem.ui.vm.MoneyForm

/** Amount text, or a mask when balances are hidden. */
fun moneyText(rial: Long, hidden: Boolean): String =
    if (hidden) "••••" else PersianText.formatMoney(rial)

@Composable
fun CategoryChips(
    selected: String,
    categories: List<String>,
    onSelect: (String) -> Unit,
    colours: Map<String, Long> = emptyMap()
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories, key = { it }) { cat ->
            FilterChip(
                selected = selected == cat,
                onClick = { onSelect(cat) },
                label = { Text(cat) },
                leadingIcon = { CategoryDot(cat, colours[cat]) },
                modifier = Modifier.heightIn(min = 48.dp)
            )
        }
    }
}

/** Category icon: a round colour mark with the first letter of the name. */
@Composable
fun CategoryDot(name: String, colourArgb: Long?) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(colourArgb?.let { Color(it) } ?: ShadowColors.TextMuted)
    ) {
        Text(name.take(1), style = MaterialTheme.typography.labelSmall, color = Color.Black)
    }
}

@Composable
fun AmountField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    isError: Boolean = false,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier
    )
}

@Composable
fun EntryCard(
    form: MoneyForm,
    categories: List<String>,
    onIncome: (Boolean) -> Unit,
    onAmount: (String) -> Unit,
    onCategory: (String) -> Unit,
    onNote: (String) -> Unit,
    onSave: () -> Unit,
    onSubcategory: (String) -> Unit = {},
    onLocation: (String) -> Unit = {},
    onDate: (Long?) -> Unit = {}
) {
    ShadowCard {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = !form.isIncome,
                onClick = { onIncome(false) },
                label = { Text(stringResource(R.string.money_expense)) },
                modifier = Modifier.heightIn(min = 48.dp)
            )
            FilterChip(
                selected = form.isIncome,
                onClick = { onIncome(true) },
                label = { Text(stringResource(R.string.money_income)) },
                modifier = Modifier.heightIn(min = 48.dp)
            )
        }
        AmountField(form.amountText, onAmount, stringResource(R.string.money_amount_hint), form.error)
        NumPad(
            onDigit = { if (form.amountText.length < 12) onAmount(form.amountText + it) },
            onErase = { onAmount(form.amountText.dropLast(1)) }
        )
        if (form.error) {
            Text(stringResource(R.string.money_amount_invalid), color = ShadowColors.Danger, style = MaterialTheme.typography.bodyMedium)
        }
        CategoryChips(form.category, categories, onCategory, rememberCategoryColours())
        OutlinedTextField(
            value = form.note,
            onValueChange = onNote,
            label = { Text(stringResource(R.string.money_note_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        val todayDay = com.shadowsystem.core.DayRules.gameDay(System.currentTimeMillis())
        if (form.epochDay == null) {
            FilterChip(
                selected = false,
                onClick = { onDate(todayDay) },
                label = { Text(stringResource(R.string.money_other_day)) },
                modifier = Modifier.heightIn(min = 48.dp)
            )
        } else {
            FilterChip(
                selected = true,
                onClick = { onDate(null) },
                label = { Text(stringResource(R.string.money_today)) },
                modifier = Modifier.heightIn(min = 48.dp)
            )
            JalaliDatePicker(
                com.shadowsystem.core.Jalali.fromGregorian(java.time.LocalDate.ofEpochDay(form.epochDay))
            ) { picked ->
                onDate(com.shadowsystem.core.Jalali.toGregorian(picked.year, picked.month, picked.day).toEpochDay())
            }
        }
        OutlinedTextField(
            value = form.subcategory,
            onValueChange = onSubcategory,
            label = { Text(stringResource(R.string.money_subcategory_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = form.location,
            onValueChange = onLocation,
            label = { Text(stringResource(R.string.money_location_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        PrimaryButton(stringResource(R.string.money_save), onClick = onSave)
    }
}

@Composable
fun SummaryCard(spent: Long, income: Long, today: Long, hidden: Boolean, onToggleHidden: () -> Unit) {
    ShadowCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.money_month_spent) + ": " + moneyText(spent, hidden),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onToggleHidden, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(
                    stringResource(if (hidden) R.string.show_balances else R.string.hide_balances),
                    color = ShadowColors.TextMuted
                )
            }
        }
        Text(stringResource(R.string.money_month_income) + ": " + moneyText(income, hidden), style = MaterialTheme.typography.bodyMedium, color = ShadowColors.TextMuted)
        Text(stringResource(R.string.money_today) + ": " + moneyText(today, hidden), style = MaterialTheme.typography.bodyMedium, color = ShadowColors.TextMuted)
    }
}

@Composable
fun TransactionRow(tx: TransactionEntity, hidden: Boolean, onReceipt: () -> Unit = {}, onDelete: () -> Unit) {
    ShadowCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    (if (tx.isIncome) "+ " else "− ") + moneyText(tx.amountRial, hidden),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (tx.isIncome) ShadowColors.Secondary else ShadowColors.TextPrimary
                )
                Text(
                    listOf(tx.category, tx.subcategory ?: "", tx.note, tx.location ?: "").filter { it.isNotBlank() }.joinToString(" · ") +
                        "  ·  " + Jalali.format(Jalali.fromGregorian(java.time.LocalDate.ofEpochDay(tx.epochDay))),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ShadowColors.TextMuted
                )
                tx.receiptUri?.let { ReceiptThumb(it) }
                TextButton(onClick = onReceipt, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(if (tx.receiptUri == null) R.string.receipt_attach else R.string.receipt_change))
                }
            }
            TextButton(onClick = onDelete, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.delete), color = ShadowColors.Danger)
            }
        }
    }
}

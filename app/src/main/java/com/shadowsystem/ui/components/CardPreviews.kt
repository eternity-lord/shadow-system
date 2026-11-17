package com.shadowsystem.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shadowsystem.data.db.TransactionEntity
import com.shadowsystem.ui.theme.ShadowTheme
import com.shadowsystem.ui.vm.MoneyForm

@Preview(name = "Daily report card (morning)", showBackground = true)
@Composable
private fun DailyReportMorningPreview() = ShadowTheme {
    Column(modifier = Modifier.padding(16.dp)) {
        DailyReportCard(morning = true, score = 72, yesterday = 60, dungeonMinutes = 25, onMore = {})
    }
}

@Preview(name = "Daily report card (night, no data)", showBackground = true)
@Composable
private fun DailyReportNightPreview() = ShadowTheme {
    Column(modifier = Modifier.padding(16.dp)) {
        DailyReportCard(morning = false, score = null, yesterday = null, dungeonMinutes = 0, onMore = {})
    }
}

@Preview(name = "Money summary", showBackground = true)
@Composable
private fun SummaryCardPreview() = ShadowTheme {
    Column(modifier = Modifier.padding(16.dp)) {
        SummaryCard(spent = 1_500_000, income = 2_000_000, today = 200_000, hidden = false, onToggleHidden = {})
    }
}

@Preview(name = "Category chips", showBackground = true)
@Composable
private fun CategoryChipsPreview() = ShadowTheme {
    Column(modifier = Modifier.padding(16.dp)) {
        CategoryChips(selected = "خوراک", categories = listOf("خوراک", "حمل‌ونقل", "خانه"), onSelect = {})
    }
}

@Preview(name = "Entry card", showBackground = true)
@Composable
private fun EntryCardPreview() = ShadowTheme {
    Column(modifier = Modifier.padding(16.dp)) {
        EntryCard(
            form = MoneyForm(amountText = "۱۲۰۰۰", category = "خوراک", note = "قهوه #کار"),
            categories = listOf("خوراک", "حمل‌ونقل", "خانه"),
            onIncome = {}, onAmount = {}, onCategory = {}, onNote = {}, onSave = {}
        )
    }
}

@Preview(name = "Transaction row", showBackground = true)
@Composable
private fun TransactionRowPreview() = ShadowTheme {
    Column(modifier = Modifier.padding(16.dp)) {
        TransactionRow(
            tx = TransactionEntity(
                amountRial = 1_200_000, category = "خوراک", note = "قهوه #کار",
                isIncome = false, epochDay = 100, createdAt = 0
            ),
            hidden = false,
            onDelete = {}
        )
    }
}

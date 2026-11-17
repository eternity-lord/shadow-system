package com.shadowsystem.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shadowsystem.core.SoldierData
import com.shadowsystem.core.SoldierProgress
import com.shadowsystem.core.SoldierRow
import com.shadowsystem.data.db.InboxEntity
import com.shadowsystem.data.db.QuestEntity
import com.shadowsystem.data.db.QuestStepEntity
import com.shadowsystem.data.db.TransactionEntity
import com.shadowsystem.ui.vm.LootInfo
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

@Preview(name = "Quest card", showBackground = true)
@Composable
private fun QuestCardPreview() = ShadowTheme {
    Column(modifier = Modifier.padding(16.dp)) {
        QuestCard(
            quest = QuestEntity(
                id = 1, title = "ورزش ۲۰ دقیقه‌ای", description = "", stat = "STR", rank = "E",
                minutes = 20, mandatory = true, lowEnergy = false, gameDay = 100
            ),
            steps = listOf(
                QuestStepEntity(id = 1, questId = 1, position = 0, text = "گرم کردن", done = true),
                QuestStepEntity(id = 2, questId = 1, position = 1, text = "ده اسکات", done = false)
            ),
            onStep = {}, onSmaller = {}
        )
    }
}

@Preview(name = "Inbox card", showBackground = true)
@Composable
private fun InboxCardPreview() = ShadowTheme {
    Column(modifier = Modifier.padding(16.dp)) {
        InboxCard(
            items = listOf(InboxEntity(id = 1, text = "ایده برای پروژه بعدی", createdAt = 0)),
            pendingDelete = null,
            onAdd = {}, onAskDelete = {}, onConfirmDelete = {}, onCancelDelete = {}
        )
    }
}

@Preview(name = "Loot dialog", showBackground = true)
@Composable
private fun LootDialogPreview() = ShadowTheme {
    LootDialog(loot = LootInfo(sessionId = 1, xp = 30, gold = 12, rare = true), onClose = {})
}

@Preview(name = "Soldier image", showBackground = true)
@Composable
private fun SoldierImagePreview() = ShadowTheme {
    val def = SoldierData.byKey.getValue("igris")
    val row = SoldierRow(
        def = def, name = def.name, customName = null, summoned = true, locked = false,
        progress = SoldierProgress.Progress(
            linkedDone = 12, activeDays = 20, xp = 240, level = 2, levelXp = 40, levelNeed = 60,
            stage = 2, wisdomScore = 45, wisdomTier = 1, lastActiveDay = 100,
            state = SoldierProgress.State.AWAKE
        ),
        canSummon = false
    )
    SoldierImage(row, stage = 2, size = 160.dp)
}


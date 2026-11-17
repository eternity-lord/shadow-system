package com.shadowsystem.data.backup

import com.shadowsystem.data.db.BudgetEntity
import com.shadowsystem.data.db.ChequeEntity
import com.shadowsystem.data.db.TransferEntity
import com.shadowsystem.data.db.WalletEntity
import com.shadowsystem.data.db.DebtEntity
import com.shadowsystem.data.db.GoalEntity
import com.shadowsystem.data.db.ProfileEntity
import com.shadowsystem.data.db.RecurringEntity
import com.shadowsystem.data.db.StatProgressEntity
import com.shadowsystem.data.db.TransactionEntity

/** Money and progress data included in a backup. */
data class BackupSnapshot(
    val profile: ProfileEntity?,
    val stats: List<StatProgressEntity>,
    val transactions: List<TransactionEntity>,
    val budgets: List<BudgetEntity>,
    val recurring: List<RecurringEntity>,
    val goals: List<GoalEntity>,
    val debts: List<DebtEntity>,
    val wallets: List<WalletEntity> = emptyList(),
    val transfers: List<TransferEntity> = emptyList(),
    val cheques: List<ChequeEntity> = emptyList()
)

/**
 * Line-based text format: one record per line, tab-separated, with escaping.
 * Unknown record types are skipped so newer backups stay readable.
 */
object BackupCodec {
    private const val SEP = '\t'

    fun encode(s: BackupSnapshot): String {
        val out = StringBuilder()
        fun line(vararg fields: Any?) {
            out.append(fields.joinToString(SEP.toString()) { escape(it.toString()) }).append('\n')
        }
        s.profile?.let { line("profile", it.totalXp, it.gold, it.activeDays, it.lastActiveDay, it.onboarded) }
        s.stats.forEach { line("stat", it.stat, it.xp) }
        s.transactions.forEach {
            line("tx", it.id, it.amountRial, it.category, it.note, it.isIncome, it.epochDay, it.createdAt, it.walletId ?: "", it.receiptUri ?: "", it.subcategory ?: "", it.location ?: "")
        }
        s.budgets.forEach { line("budget", it.category, it.limitRial) }
        s.recurring.forEach {
            line("rec", it.id, it.title, it.amountRial, it.category, it.isIncome, it.period, it.anchorEpochDay, it.posted, it.active)
        }
        s.goals.forEach { line("goal", it.id, it.title, it.targetRial, it.savedRial, it.createdDay, it.deadlineEpochDay ?: "", it.walletId ?: "") }
        s.debts.forEach { line("debt", it.id, it.title, it.totalRial, it.paidRial, it.iOwe, it.installmentCount, it.installmentsPaid) }
        s.wallets.forEach { line("wallet", it.id, it.name, it.openingRial) }
        s.transfers.forEach { line("transfer", it.id, it.fromWalletId, it.toWalletId, it.amountRial, it.epochDay, it.createdAt) }
        s.cheques.forEach { line("cheque", it.id, it.title, it.amountRial, it.dueEpochDay, it.issuedByMe, it.cleared) }
        return out.toString()
    }

    fun decode(text: String): BackupSnapshot {
        var profile: ProfileEntity? = null
        val stats = mutableListOf<StatProgressEntity>()
        val txs = mutableListOf<TransactionEntity>()
        val budgets = mutableListOf<BudgetEntity>()
        val recs = mutableListOf<RecurringEntity>()
        val goals = mutableListOf<GoalEntity>()
        val debts = mutableListOf<DebtEntity>()
        val wallets = mutableListOf<WalletEntity>()
        val transfers = mutableListOf<TransferEntity>()
        val cheques = mutableListOf<ChequeEntity>()
        for (raw in text.lineSequence()) {
            if (raw.isBlank()) continue
            val f = raw.split(SEP).map { unescape(it) }
            when (f[0]) {
                "profile" -> profile = ProfileEntity(
                    totalXp = f[1].toLong(), gold = f[2].toInt(), activeDays = f[3].toInt(),
                    lastActiveDay = f[4].toLong(), onboarded = f[5].toBoolean()
                )
                "stat" -> stats += StatProgressEntity(stat = f[1], xp = f[2].toLong())
                "tx" -> txs += TransactionEntity(
                    id = f[1].toLong(), amountRial = f[2].toLong(), category = f[3], note = f[4],
                    isIncome = f[5].toBoolean(), epochDay = f[6].toLong(), createdAt = f[7].toLong(),
                    walletId = f.getOrNull(8)?.toLongOrNull(),
                    receiptUri = f.getOrNull(9)?.ifBlank { null },
                    subcategory = f.getOrNull(10)?.ifBlank { null },
                    location = f.getOrNull(11)?.ifBlank { null }
                )
                "budget" -> budgets += BudgetEntity(category = f[1], limitRial = f[2].toLong())
                "rec" -> recs += RecurringEntity(
                    id = f[1].toLong(), title = f[2], amountRial = f[3].toLong(), category = f[4],
                    isIncome = f[5].toBoolean(), period = parseRecurrence(f[6]), anchorEpochDay = f[7].toLong(),
                    posted = f[8].toInt(), active = f[9].toBoolean()
                )
                "goal" -> goals += GoalEntity(
                    id = f[1].toLong(), title = f[2], targetRial = f[3].toLong(),
                    savedRial = f[4].toLong(), createdDay = f[5].toLong(),
                    deadlineEpochDay = f.getOrNull(6)?.toLongOrNull(), walletId = f.getOrNull(7)?.toLongOrNull()
                )
                "debt" -> debts += DebtEntity(
                    id = f[1].toLong(), title = f[2], totalRial = f[3].toLong(),
                    paidRial = f[4].toLong(), iOwe = f[5].toBoolean(),
                    installmentCount = f.getOrNull(6)?.toIntOrNull() ?: 0,
                    installmentsPaid = f.getOrNull(7)?.toIntOrNull() ?: 0
                )
                "wallet" -> wallets += WalletEntity(id = f[1].toLong(), name = f[2], openingRial = f[3].toLong())
                "transfer" -> transfers += TransferEntity(
                    id = f[1].toLong(), fromWalletId = f[2].toLong(), toWalletId = f[3].toLong(),
                    amountRial = f[4].toLong(), epochDay = f[5].toLong(), createdAt = f[6].toLong()
                )
                "cheque" -> cheques += ChequeEntity(
                    id = f[1].toLong(), title = f[2], amountRial = f[3].toLong(), dueEpochDay = f[4].toLong(),
                    issuedByMe = f[5].toBoolean(), cleared = f[6].toBoolean()
                )
            }
        }
        return BackupSnapshot(profile, stats, txs, budgets, recs, goals, debts, wallets, transfers, cheques)
    }

    private fun escape(s: String): String =
        s.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n").replace("\r", "\\r")

    private fun unescape(s: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            if (c == '\\' && i + 1 < s.length) {
                when (s[i + 1]) {
                    't' -> sb.append('\t')
                    'n' -> sb.append('\n')
                    'r' -> sb.append('\r')
                    else -> sb.append(s[i + 1])
                }
                i += 2
            } else {
                sb.append(c)
                i++
            }
        }
        return sb.toString()
    }
}

/** Old backups stored the monthly flag as true/false. New ones store the period name. */
private fun parseRecurrence(v: String): String = when (v) {
    "true" -> "MONTHLY"
    "false" -> "WEEKLY"
    else -> com.shadowsystem.core.RecurrencePeriod.parse(v).name
}

package com.shadowsystem.data.backup

import com.shadowsystem.data.db.BudgetEntity
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
    val debts: List<DebtEntity>
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
            line("tx", it.id, it.amountRial, it.category, it.note, it.isIncome, it.epochDay, it.createdAt)
        }
        s.budgets.forEach { line("budget", it.category, it.limitRial) }
        s.recurring.forEach {
            line("rec", it.id, it.title, it.amountRial, it.category, it.isIncome, it.monthly, it.anchorEpochDay, it.posted, it.active)
        }
        s.goals.forEach { line("goal", it.id, it.title, it.targetRial, it.savedRial, it.createdDay) }
        s.debts.forEach { line("debt", it.id, it.title, it.totalRial, it.paidRial, it.iOwe) }
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
                    isIncome = f[5].toBoolean(), epochDay = f[6].toLong(), createdAt = f[7].toLong()
                )
                "budget" -> budgets += BudgetEntity(category = f[1], limitRial = f[2].toLong())
                "rec" -> recs += RecurringEntity(
                    id = f[1].toLong(), title = f[2], amountRial = f[3].toLong(), category = f[4],
                    isIncome = f[5].toBoolean(), monthly = f[6].toBoolean(), anchorEpochDay = f[7].toLong(),
                    posted = f[8].toInt(), active = f[9].toBoolean()
                )
                "goal" -> goals += GoalEntity(
                    id = f[1].toLong(), title = f[2], targetRial = f[3].toLong(),
                    savedRial = f[4].toLong(), createdDay = f[5].toLong()
                )
                "debt" -> debts += DebtEntity(
                    id = f[1].toLong(), title = f[2], totalRial = f[3].toLong(),
                    paidRial = f[4].toLong(), iOwe = f[5].toBoolean()
                )
            }
        }
        return BackupSnapshot(profile, stats, txs, budgets, recs, goals, debts)
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

package com.shadowsystem.data.repo

import com.shadowsystem.core.DueRules
import com.shadowsystem.core.FinanceRules
import com.shadowsystem.core.MoneyRules
import com.shadowsystem.core.RecurrencePeriod
import com.shadowsystem.core.RecurrenceRules
import com.shadowsystem.data.db.AppDatabase
import com.shadowsystem.data.db.BudgetEntity
import com.shadowsystem.data.db.DebtEntity
import com.shadowsystem.data.db.GoalEntity
import com.shadowsystem.data.db.RecurringEntity
import com.shadowsystem.data.db.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Budgets, recurring rules, savings goals and debts. */
class PlanRepository(
    private val db: AppDatabase,
    private val game: GameRepository,
    private val clock: () -> Long = System::currentTimeMillis
) {
    private val budgets = db.budgetDao()
    private val recurring = db.recurringDao()
    private val goals = db.goalDao()
    private val debts = db.debtDao()
    private val tx = db.transactionDao()

    fun observeBudgets(): Flow<List<BudgetEntity>> = budgets.observeAll()
    fun observeRecurring(): Flow<List<RecurringEntity>> = recurring.observeAll()
    fun observeGoals(): Flow<List<GoalEntity>> = goals.observeAll()
    fun observeDebts(): Flow<List<DebtEntity>> = debts.observeAll()

    fun observeMonthSpent(from: Long, to: Long): Flow<Map<String, Long>> =
        tx.observeRange(from, to).map { spentByCategory(it) }

    fun spentByCategory(list: List<TransactionEntity>): Map<String, Long> =
        list.filter { !it.isIncome }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amountRial } }

    suspend fun setBudget(category: String, toman: Long) {
        budgets.upsert(BudgetEntity(category, MoneyRules.tomanToRial(toman)))
    }

    suspend fun removeBudget(category: String) {
        budgets.delete(category)
    }

    suspend fun addRecurring(title: String, toman: Long, category: String, isIncome: Boolean, period: RecurrencePeriod) {
        recurring.insert(
            RecurringEntity(
                title = title.trim(),
                amountRial = MoneyRules.tomanToRial(toman),
                category = category,
                isIncome = isIncome,
                period = period.name,
                anchorEpochDay = game.today(),
                posted = 0,
                active = true
            )
        )
        processRecurring()
    }

    suspend fun deleteRecurring(id: Long) {
        recurring.deleteById(id)
    }

    /**
     * Posts every due occurrence of active rules (catch-up after the app was closed).
     * Automatic postings never give XP or Gold. Returns how many were posted.
     */
    suspend fun processRecurring(): Int {
        val today = game.today()
        var total = 0
        for (rule in recurring.activeRules()) {
            val due = RecurrenceRules.dueCount(rule.anchorEpochDay, rule.posted, today, RecurrencePeriod.parse(rule.period))
            if (due == 0) continue
            for (i in 0 until due) {
                tx.insert(
                    TransactionEntity(
                        amountRial = rule.amountRial,
                        category = rule.category,
                        note = "${rule.title} (خودکار)",
                        isIncome = rule.isIncome,
                        epochDay = RecurrenceRules.occurrence(rule.anchorEpochDay, rule.posted + i, RecurrencePeriod.parse(rule.period)),
                        createdAt = clock()
                    )
                )
            }
            recurring.update(rule.copy(posted = rule.posted + due))
            total += due
        }
        return total
    }

    suspend fun addGoal(title: String, toman: Long) {
        goals.insert(
            GoalEntity(
                title = title.trim(),
                targetRial = MoneyRules.tomanToRial(toman),
                savedRial = 0L,
                createdDay = game.today()
            )
        )
    }

    /** Adds money to a goal. Reaching the target grants the heavy frame once (spec: savings goal as boss). */
    suspend fun depositGoal(goal: GoalEntity, toman: Long) {
        val after = goal.savedRial + MoneyRules.tomanToRial(toman)
        goals.update(goal.copy(savedRial = after))
        if (FinanceRules.goalJustCompleted(goal.targetRial, goal.savedRial, after)) {
            db.keyValueDao().upsert(com.shadowsystem.data.db.KeyValueEntity("owned_$GOAL_REWARD", 1L))
        }
    }

    suspend fun deleteGoal(id: Long) {
        goals.deleteById(id)
    }

    suspend fun addDebt(title: String, toman: Long, iOwe: Boolean, installments: Int = 0, dueEpochDay: Long? = null) {
        debts.insert(
            DebtEntity(
                title = title.trim(),
                totalRial = MoneyRules.tomanToRial(toman),
                paidRial = 0L,
                iOwe = iOwe,
                installmentCount = installments.coerceIn(0, 120),
                dueEpochDay = dueEpochDay
            )
        )
    }

    /** Unpaid cheques and debts due within three days or already overdue. Shown in the reminders. */
    suspend fun dueAttentionCount(today: Long): Int {
        val cheques = db.chequeDao().all().count { !it.cleared && DueRules.needsAttention(it.dueEpochDay, today, false) }
        val debtItems = debts.all().count { d ->
            val due = DueRules.debtDueDay(d.dueEpochDay, d.installmentCount, d.installmentsPaid) ?: return@count false
            val done = d.paidRial >= d.totalRial || (d.installmentCount > 0 && d.installmentsPaid >= d.installmentCount)
            DueRules.needsAttention(due, today, done)
        }
        return cheques + debtItems
    }

    suspend fun payDebt(debt: DebtEntity, toman: Long) {
        val paid = (debt.paidRial + MoneyRules.tomanToRial(toman)).coerceAtMost(debt.totalRial)
        debts.update(debt.copy(paidRial = paid))
    }

    /** Pays the next installment; the amount comes from [FinanceRules.installmentPart]. */
    suspend fun payInstallment(debt: DebtEntity) {
        if (debt.installmentCount <= 0 || debt.installmentsPaid >= debt.installmentCount) return
        val part = FinanceRules.installmentPart(debt.totalRial, debt.installmentCount, debt.installmentsPaid)
        debts.update(
            debt.copy(
                paidRial = (debt.paidRial + part).coerceAtMost(debt.totalRial),
                installmentsPaid = debt.installmentsPaid + 1
            )
        )
    }

    suspend fun deleteDebt(id: Long) {
        debts.deleteById(id)
    }

    companion object {
        /** Heavy cosmetic given for completing a savings goal. */
        const val GOAL_REWARD = "frame_sovereign"
    }
}

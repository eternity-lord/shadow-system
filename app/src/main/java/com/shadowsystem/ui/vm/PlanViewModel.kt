package com.shadowsystem.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.shadowsystem.ShadowApp
import com.shadowsystem.core.FinanceRules
import com.shadowsystem.core.PersianText
import com.shadowsystem.data.db.BudgetEntity
import com.shadowsystem.data.db.DebtEntity
import com.shadowsystem.data.db.GoalEntity
import com.shadowsystem.data.db.RecurringEntity
import com.shadowsystem.data.repo.PlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Short user-facing notices; the screen maps them to Persian strings. */
enum class PlanNotice { INVALID_AMOUNT, EMPTY_TITLE, PAYMENT_TOO_HIGH, SAVED }

data class BudgetRow(val category: String, val limitRial: Long, val spentRial: Long, val level: FinanceRules.BudgetState)

/** Pending destructive action; [kind] is one of budget, recurring, goal, debt. */
data class PendingDelete(val kind: String, val id: Long = 0, val category: String = "")

data class PlanState(
    val budgets: List<BudgetRow> = emptyList(),
    val recurring: List<RecurringEntity> = emptyList(),
    val goals: List<GoalEntity> = emptyList(),
    val debts: List<DebtEntity> = emptyList(),
    val monthLabel: String = "",
    val notice: PlanNotice? = null,
    val pending: PendingDelete? = null
)

private data class PlanData(
    val budgets: List<BudgetEntity>,
    val spent: Map<String, Long>,
    val recurring: List<RecurringEntity>,
    val goals: List<GoalEntity>,
    val debts: List<DebtEntity>
)

class PlanViewModel(private val plan: PlanRepository) : ViewModel() {

    private val now = System.currentTimeMillis()
    private val pending = MutableStateFlow<PendingDelete?>(null)
    private val notice = MutableStateFlow<PlanNotice?>(null)

    private val data = combine(
        plan.observeBudgets(),
        plan.observeMonthSpent(
            FinanceRules.monthRange(0, now).first,
            FinanceRules.monthRange(0, now).second
        ),
        plan.observeRecurring(),
        plan.observeGoals(),
        plan.observeDebts()
    ) { b, s, r, g, d -> PlanData(b, s, r, g, d) }

    val state: StateFlow<PlanState> = combine(data, pending, notice) { d, p, n ->
        PlanState(
            budgets = d.budgets.map {
                BudgetRow(
                    it.category, it.limitRial, d.spent[it.category] ?: 0L,
                    FinanceRules.budgetState(d.spent[it.category] ?: 0L, it.limitRial)
                )
            },
            recurring = d.recurring,
            goals = d.goals,
            debts = d.debts,
            monthLabel = FinanceRules.monthLabel(0, now),
            notice = n,
            pending = p
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, PlanState())

    fun saveBudget(category: String, amountText: String) {
        val toman = PersianText.parseLong(amountText)
        if (toman == null || toman <= 0) return notify(PlanNotice.INVALID_AMOUNT)
        viewModelScope.launch {
            plan.setBudget(category, toman)
            notify(PlanNotice.SAVED)
        }
    }

    fun addRecurring(title: String, amountText: String, category: String, isIncome: Boolean, monthly: Boolean) {
        val toman = PersianText.parseLong(amountText)
        if (title.isBlank()) return notify(PlanNotice.EMPTY_TITLE)
        if (toman == null || toman <= 0) return notify(PlanNotice.INVALID_AMOUNT)
        viewModelScope.launch {
            plan.addRecurring(title, toman, category, isIncome, monthly)
            notify(PlanNotice.SAVED)
        }
    }

    fun addGoal(title: String, amountText: String) {
        val toman = PersianText.parseLong(amountText)
        if (title.isBlank()) return notify(PlanNotice.EMPTY_TITLE)
        if (toman == null || toman <= 0) return notify(PlanNotice.INVALID_AMOUNT)
        viewModelScope.launch {
            plan.addGoal(title, toman)
            notify(PlanNotice.SAVED)
        }
    }

    fun depositGoal(goal: GoalEntity, amountText: String) {
        val toman = PersianText.parseLong(amountText)
        if (toman == null || toman <= 0) return notify(PlanNotice.INVALID_AMOUNT)
        viewModelScope.launch { plan.depositGoal(goal, toman) }
    }

    fun addDebt(title: String, amountText: String, iOwe: Boolean) {
        val toman = PersianText.parseLong(amountText)
        if (title.isBlank()) return notify(PlanNotice.EMPTY_TITLE)
        if (toman == null || toman <= 0) return notify(PlanNotice.INVALID_AMOUNT)
        viewModelScope.launch {
            plan.addDebt(title, toman, iOwe)
            notify(PlanNotice.SAVED)
        }
    }

    fun payDebt(debt: DebtEntity, amountText: String) {
        val toman = PersianText.parseLong(amountText)
        if (toman == null || toman <= 0) return notify(PlanNotice.INVALID_AMOUNT)
        val remainingToman = FinanceRules.debtRemaining(debt.totalRial, debt.paidRial) / 10
        if (toman > remainingToman) return notify(PlanNotice.PAYMENT_TOO_HIGH)
        viewModelScope.launch { plan.payDebt(debt, toman) }
    }

    fun ask(kind: String, id: Long = 0, category: String = "") {
        pending.value = PendingDelete(kind, id, category)
    }

    fun cancelDelete() {
        pending.value = null
    }

    fun confirmDelete() {
        val p = pending.value ?: return
        pending.value = null
        viewModelScope.launch {
            when (p.kind) {
                "budget" -> plan.removeBudget(p.category)
                "recurring" -> plan.deleteRecurring(p.id)
                "goal" -> plan.deleteGoal(p.id)
                "debt" -> plan.deleteDebt(p.id)
            }
        }
    }

    fun clearNotice() {
        notice.value = null
    }

    private fun notify(n: PlanNotice) {
        notice.value = n
    }

    companion object {
        fun factory(app: ShadowApp): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                PlanViewModel(app.container.plan) as T
        }
    }
}

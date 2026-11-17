package com.shadowsystem.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.shadowsystem.ShadowApp
import com.shadowsystem.core.FinanceRules
import com.shadowsystem.data.repo.GameRepository
import com.shadowsystem.data.repo.MoneyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReportsState(
    val monthLabel: String = "",
    val categories: List<Pair<String, Long>> = emptyList(),
    val monthTotals: List<Pair<String, Long>> = emptyList(),
    val daily: List<Long> = emptyList(),
    val changePercent: Int? = null,
    val dailyAverage: Long = 0,
    val yearTotal: Long = 0,
    val topExpenses: List<Pair<String, Long>> = emptyList()
)

/** Expense reports: this month by category, last six months, and the last 30 days. */
class ReportsViewModel(private val money: MoneyRepository, private val game: GameRepository) : ViewModel() {

    private val _state = MutableStateFlow(ReportsState())
    val state: StateFlow<ReportsState> = _state.asStateFlow()

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val now = System.currentTimeMillis()
        val (from, to) = FinanceRules.monthRange(0, now)
        val categories = money.range(from, to).filter { !it.isIncome }
            .groupBy { it.category }
            .map { it.key to it.value.sumOf { t -> t.amountRial } }
            .sortedByDescending { it.second }
        val totals = (-5..0).map { o ->
            val (a, b) = FinanceRules.monthRange(o, now)
            FinanceRules.monthLabel(o, now) to money.range(a, b).filter { !it.isIncome }.sumOf { it.amountRial }
        }
        val today = game.today()
        val byDay = money.range(today - 29, today).filter { !it.isIncome }.groupBy { it.epochDay }
        val daily = (29 downTo 0).map { back ->
            byDay[today - back]?.sumOf { it.amountRial } ?: 0L
        }
        val spentNow = categories.sumOf { it.second }
        val spentBefore = totals[totals.size - 2].second
        val daysSoFar = today - from + 1
        val (yearFrom, _) = FinanceRules.monthRange(-11, now)
        val yearTotal = money.range(yearFrom, to).filter { !it.isIncome }.sumOf { it.amountRial }
        val top = money.range(from, to).filter { !it.isIncome }
            .sortedByDescending { it.amountRial }
            .take(5)
            .map { (it.note.ifBlank { it.category }) to it.amountRial }
        _state.value = ReportsState(
            monthLabel = FinanceRules.monthLabel(0, now),
            categories = categories,
            monthTotals = totals,
            daily = daily,
            changePercent = FinanceRules.changePercent(spentNow, spentBefore),
            dailyAverage = FinanceRules.dailyAverage(spentNow, daysSoFar),
            yearTotal = yearTotal,
            topExpenses = top
        )
    }

    companion object {
        fun factory(app: ShadowApp): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                ReportsViewModel(app.container.money, app.container.game) as T
        }
    }
}

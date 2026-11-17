package com.shadowsystem.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.shadowsystem.ShadowApp
import com.shadowsystem.data.repo.DayReport
import com.shadowsystem.data.repo.GameRepository
import com.shadowsystem.data.repo.MoneyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DailyReportUi(
    val loaded: Boolean = false,
    val report: DayReport? = null,
    val spentRial: Long = 0L,
    val spentCount: Int = 0
)

/** Full daily report for today: game numbers plus today's spending. Spending never adds XP or Gold. */
class DailyReportViewModel(private val game: GameRepository, private val money: MoneyRepository) : ViewModel() {

    private val _state = MutableStateFlow(DailyReportUi())
    val state: StateFlow<DailyReportUi> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val day = game.today()
            val report = game.dailyReport(day)
            val expenses = money.range(day, day).filter { !it.isIncome }
            _state.value = DailyReportUi(true, report, expenses.sumOf { it.amountRial }, expenses.size)
        }
    }

    companion object {
        fun factory(app: ShadowApp): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                DailyReportViewModel(app.container.game, app.container.money) as T
        }
    }
}

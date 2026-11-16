package com.shadowsystem.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.shadowsystem.ShadowApp
import com.shadowsystem.core.Jalali
import com.shadowsystem.core.MoneyRules
import com.shadowsystem.core.PersianText
import com.shadowsystem.data.db.TransactionEntity
import com.shadowsystem.data.repo.GameRepository
import com.shadowsystem.data.repo.MoneyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MoneyForm(
    val amountText: String = "",
    val category: String = MoneyRules.CATEGORIES.first(),
    val note: String = "",
    val isIncome: Boolean = false,
    val error: Boolean = false
)

data class MoneyState(
    val form: MoneyForm = MoneyForm(),
    val entries: List<TransactionEntity> = emptyList(),
    val monthSpentRial: Long = 0,
    val monthIncomeRial: Long = 0,
    val todaySpentRial: Long = 0,
    val pendingDelete: TransactionEntity? = null,
    val message: String? = null
)

class MoneyViewModel(private val money: MoneyRepository, private val game: GameRepository) : ViewModel() {

    private val form = MutableStateFlow(MoneyForm())
    private val pendingDelete = MutableStateFlow<TransactionEntity?>(null)
    private val message = MutableStateFlow<String?>(null)

    private val monthFrom: Long
    private val monthTo: Long
    private val today: Long = game.today()

    init {
        val now = Jalali.fromMillis(System.currentTimeMillis())
        val start = Jalali.toGregorian(now.year, now.month, 1)
        val end = Jalali.toGregorian(now.year, now.month, Jalali.monthLength(now.year, now.month))
        monthFrom = start.toEpochDay()
        monthTo = end.toEpochDay()
    }

    private val entries = money.observeRange(monthFrom, monthTo)

    val state: StateFlow<MoneyState> = combine(
        form, entries, pendingDelete, message
    ) { f, list, pending, msg ->
        val spent = list.filter { !it.isIncome }
        MoneyState(
            form = f,
            entries = list,
            monthSpentRial = spent.sumOf { it.amountRial },
            monthIncomeRial = list.filter { it.isIncome }.sumOf { it.amountRial },
            todaySpentRial = spent.filter { it.epochDay == today }.sumOf { it.amountRial },
            pendingDelete = pending,
            message = msg
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MoneyState())

    fun onAmountChange(text: String) {
        form.value = form.value.copy(amountText = PersianText.parseDigits(text).filter { it.isDigit() }, error = false)
    }

    fun onCategory(category: String) {
        form.value = form.value.copy(category = category)
    }

    fun onNoteChange(note: String) {
        form.value = form.value.copy(note = note.take(60))
    }

    fun onIncomeToggle(income: Boolean) {
        form.value = form.value.copy(isIncome = income)
    }

    fun save() {
        val f = form.value
        val toman = PersianText.parseLong(f.amountText)
        if (!MoneyRules.isValidAmount(toman)) {
            form.value = f.copy(error = true)
            return
        }
        viewModelScope.launch {
            money.add(toman!!, f.category, f.note, f.isIncome)
            form.value = f.copy(amountText = "", note = "", error = false)
            message.value = null
        }
    }

    fun askDelete(tx: TransactionEntity) {
        pendingDelete.value = tx
    }

    fun cancelDelete() {
        pendingDelete.value = null
    }

    fun confirmDelete() {
        val tx = pendingDelete.value ?: return
        pendingDelete.value = null
        viewModelScope.launch { money.delete(tx) }
    }

    companion object {
        fun factory(app: ShadowApp): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                MoneyViewModel(app.container.money, app.container.game) as T
        }
    }
}

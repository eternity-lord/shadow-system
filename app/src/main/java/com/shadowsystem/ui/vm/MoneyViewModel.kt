package com.shadowsystem.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.shadowsystem.ShadowApp
import com.shadowsystem.core.FinanceRules
import com.shadowsystem.core.MoneyRules
import com.shadowsystem.core.PersianText
import com.shadowsystem.data.db.TransactionEntity
import com.shadowsystem.data.prefs.AppPrefs
import com.shadowsystem.data.repo.GameRepository
import com.shadowsystem.data.repo.MoneyRepository
import com.shadowsystem.data.repo.PlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TypeFilter { ALL, EXPENSE, INCOME }

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
    val monthLabel: String = "",
    val monthSpentRial: Long = 0,
    val monthIncomeRial: Long = 0,
    val todaySpentRial: Long = 0,
    val pendingDelete: TransactionEntity? = null,
    val hidden: Boolean = false,
    val query: String = "",
    val filter: TypeFilter = TypeFilter.ALL,
    val canGoForward: Boolean = false
)

private data class Controls(val offset: Int, val query: String, val filter: TypeFilter, val hidden: Boolean)

/** Money tab: quick entry, month navigation, search and filters. Recurring rules are posted on start. */
class MoneyViewModel(
    private val money: MoneyRepository,
    private val game: GameRepository,
    private val plan: PlanRepository,
    private val prefs: AppPrefs
) : ViewModel() {

    private val form = MutableStateFlow(MoneyForm())
    private val pendingDelete = MutableStateFlow<TransactionEntity?>(null)
    private val offset = MutableStateFlow(0)
    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(TypeFilter.ALL)
    private val today = game.today()

    private val controls = combine(offset, query, filter, prefs.hidden) { o, q, f, h -> Controls(o, q, f, h) }

    private val monthEntries = offset.flatMapLatest { o ->
        val (from, to) = FinanceRules.monthRange(o, System.currentTimeMillis())
        money.observeRange(from, to)
    }

    val state: StateFlow<MoneyState> = combine(form, pendingDelete, controls, monthEntries) { f, pending, c, all ->
        val now = System.currentTimeMillis()
        val q = c.query.trim()
        val shown = all.filter { t ->
            (q.isEmpty() || t.note.contains(q, ignoreCase = true) || t.category.contains(q, ignoreCase = true)) &&
                when (c.filter) {
                    TypeFilter.ALL -> true
                    TypeFilter.EXPENSE -> !t.isIncome
                    TypeFilter.INCOME -> t.isIncome
                }
        }
        val spent = all.filter { !it.isIncome }
        MoneyState(
            form = f,
            entries = shown,
            monthLabel = FinanceRules.monthLabel(c.offset, now),
            monthSpentRial = spent.sumOf { it.amountRial },
            monthIncomeRial = all.filter { it.isIncome }.sumOf { it.amountRial },
            todaySpentRial = spent.filter { it.epochDay == today }.sumOf { it.amountRial },
            pendingDelete = pending,
            hidden = c.hidden,
            query = c.query,
            filter = c.filter,
            canGoForward = c.offset < 0
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MoneyState())

    init {
        viewModelScope.launch { plan.processRecurring() }
    }

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

    fun onQuery(text: String) {
        query.value = text.take(40)
    }

    fun onFilter(value: TypeFilter) {
        filter.value = value
    }

    fun previousMonth() {
        offset.value = offset.value - 1
    }

    fun nextMonth() {
        if (offset.value < 0) offset.value = offset.value + 1
    }

    fun toggleHidden() {
        prefs.setHideBalances(!prefs.hidden.value)
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
                MoneyViewModel(app.container.money, app.container.game, app.container.plan, app.container.prefs) as T
        }
    }
}

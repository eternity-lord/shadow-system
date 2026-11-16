package com.shadowsystem.core

/** Budget, savings and debt rules plus month ranges (pure, unit tested). */
object FinanceRules {

    enum class BudgetState { OK, WARN, OVER }

    const val WARN_PERCENT = 80

    fun budgetState(spent: Long, limit: Long): BudgetState = when {
        limit <= 0L -> BudgetState.OK
        spent >= limit -> BudgetState.OVER
        spent * 100 >= limit * WARN_PERCENT -> BudgetState.WARN
        else -> BudgetState.OK
    }

    fun goalPercent(saved: Long, target: Long): Int =
        if (target <= 0L) 0 else ((saved * 100) / target).coerceIn(0L, 100L).toInt()

    fun debtRemaining(total: Long, paid: Long): Long = (total - paid).coerceAtLeast(0L)

    /** Epoch-day range [first, last] of the Jalali month [offset] months away from [nowMillis]. */
    fun monthRange(offset: Int, nowMillis: Long): Pair<Long, Long> {
        val base = monthStart(offset, nowMillis)
        val last = Jalali.monthLength(base.year, base.month)
        val first = Jalali.toGregorian(base.year, base.month, 1).toEpochDay()
        val end = Jalali.toGregorian(base.year, base.month, last).toEpochDay()
        return first to end
    }

    fun monthLabel(offset: Int, nowMillis: Long): String {
        val base = monthStart(offset, nowMillis)
        return "${Jalali.monthName(base.month)} ${PersianText.toFa(base.year)}"
    }

    private fun monthStart(offset: Int, nowMillis: Long): Jalali.Date =
        Jalali.addMonths(Jalali.fromMillis(nowMillis), offset).copy(day = 1)
}

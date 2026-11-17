package com.shadowsystem.core

/** Budget, savings and debt rules plus month ranges (pure, unit tested). */
object FinanceRules {

    enum class BudgetState { OK, WARN, OVER }

    const val WARN_PERCENT = 80

    /**
     * Rollover: the unspent part of last month's limit is added to this month's limit,
     * but the carried amount never exceeds one full base limit (so a saved-up month cannot
     * make the next month unlimited). Overspending does not carry a debt forward.
     */
    fun rolloverLimit(baseLimit: Long, prevLimit: Long, prevSpent: Long): Long {
        if (baseLimit <= 0L) return 0L
        val carried = (prevLimit - prevSpent).coerceIn(0L, baseLimit)
        return baseLimit + carried
    }

    /** Amount of installment [index] (0-based). The last one absorbs the rounding remainder. */
    fun installmentPart(totalRial: Long, count: Int, index: Int): Long {
        if (count <= 0) return totalRial
        val base = totalRial / count
        return if (index >= count - 1) totalRial - base * (count - 1) else base
    }

    fun budgetState(spent: Long, limit: Long): BudgetState = when {
        limit <= 0L -> BudgetState.OK
        spent >= limit -> BudgetState.OVER
        spent * 100 >= limit * WARN_PERCENT -> BudgetState.WARN
        else -> BudgetState.OK
    }

    fun goalPercent(saved: Long, target: Long): Int =
        if (target <= 0L) 0 else ((saved * 100) / target).coerceIn(0L, 100L).toInt()

    /**
     * Days until a savings goal is reached at the average daily saving rate.
     * Null when there is no saving yet (no estimate is possible). 0 when the goal is already met.
     */
    /** True only on the deposit that first reaches the target. The savings-goal heavy reward is granted once. */
    fun goalJustCompleted(targetRial: Long, savedBefore: Long, savedAfter: Long): Boolean =
        targetRial > 0 && savedBefore < targetRial && savedAfter >= targetRial

    fun savingsEstimateDays(remaining: Long, savedPerDay: Double): Long? = when {
        remaining <= 0L -> 0L
        savedPerDay <= 0.0 || savedPerDay.isNaN() -> null
        else -> Math.ceil(remaining / savedPerDay).toLong()
    }

    /** Percent change from [previous] to [current], rounded. Null when there was no spending before. */
    fun changePercent(current: Long, previous: Long): Int? =
        if (previous <= 0L) null else Math.round((current - previous) * 100.0 / previous).toInt()

    /** Average per day so far this month. Zero before the first day. */
    fun dailyAverage(total: Long, daysSoFar: Long): Long = if (daysSoFar <= 0L) 0L else total / daysSoFar

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

    /** Amount the linked wallet is short of the rest of a goal. Zero when there is no wallet or it covers the rest. */
    fun walletShortfall(remaining: Long, balance: Long?): Long =
        if (balance == null) 0L else (remaining - balance).coerceAtLeast(0L)
}

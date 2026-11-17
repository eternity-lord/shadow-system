package com.shadowsystem.core

/**
 * Due dates for debts and installments (spec section 9). Installments fall on the same day each Jalali month,
 * counted from the debt's first due day. Cheques reuse [ChequeRules].
 */
object DueRules {

    /** Day of the installment with [index] (0 is the first), in epoch days. */
    fun installmentDay(firstDueDay: Long, index: Int): Long {
        if (index <= 0) return firstDueDay
        val base = Jalali.fromMillis(firstDueDay * DAY_MS + NOON_MS)
        val next = Jalali.addMonths(base, index)
        return Jalali.toGregorian(next.year, next.month, next.day).toEpochDay()
    }

    /**
     * Next due day for a debt. For an installment debt this is the next unpaid installment; for a single payment it
     * is the first due day. Null when the debt has no due date.
     */
    fun debtDueDay(firstDueDay: Long?, installmentCount: Int, installmentsPaid: Int): Long? {
        if (firstDueDay == null) return null
        return if (installmentCount > 0) installmentDay(firstDueDay, installmentsPaid) else firstDueDay
    }

    /** True when an unpaid item is due within [ChequeRules.SOON_DAYS] days or already overdue. */
    fun needsAttention(dueDay: Long, today: Long, done: Boolean): Boolean =
        !done && dueDay - today <= ChequeRules.SOON_DAYS

    private const val DAY_MS = 86_400_000L
    private const val NOON_MS = 43_200_000L
}

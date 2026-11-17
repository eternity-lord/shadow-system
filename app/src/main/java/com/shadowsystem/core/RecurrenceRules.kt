package com.shadowsystem.core

import java.time.LocalDate

/** How often a repeating money rule posts. Stored by name in the database and in backups. */
enum class RecurrencePeriod {
    DAILY, WEEKLY, MONTHLY, YEARLY;

    companion object {
        /** Parses a stored name. Unknown values fall back to monthly. */
        fun parse(name: String): RecurrencePeriod = values().firstOrNull { it.name == name } ?: MONTHLY
    }
}

/**
 * Occurrences of a repeating money rule. Monthly and yearly rules follow the Jalali calendar and are always
 * computed from the anchor date, so day 31 does not drift.
 */
object RecurrenceRules {

    const val MAX_CATCH_UP = 60

    fun occurrence(anchorEpochDay: Long, index: Int, period: RecurrencePeriod): Long = when (period) {
        RecurrencePeriod.DAILY -> anchorEpochDay + index
        RecurrencePeriod.WEEKLY -> anchorEpochDay + 7L * index
        RecurrencePeriod.MONTHLY -> monthsAfter(anchorEpochDay, index)
        RecurrencePeriod.YEARLY -> monthsAfter(anchorEpochDay, 12 * index)
    }

    private fun monthsAfter(anchorEpochDay: Long, months: Int): Long {
        val anchor = Jalali.fromGregorian(LocalDate.ofEpochDay(anchorEpochDay))
        val d = Jalali.addMonths(anchor, months)
        return Jalali.toGregorian(d.year, d.month, d.day).toEpochDay()
    }

    /** How many not-yet-posted occurrences are due on or before [today]. Capped for safety. */
    fun dueCount(anchorEpochDay: Long, posted: Int, today: Long, period: RecurrencePeriod): Int {
        var n = 0
        while (n < MAX_CATCH_UP && occurrence(anchorEpochDay, posted + n, period) <= today) n++
        return n
    }
}

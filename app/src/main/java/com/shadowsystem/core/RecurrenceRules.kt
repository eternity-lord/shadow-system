package com.shadowsystem.core

import java.time.LocalDate

/**
 * Occurrences of a repeating money rule. Monthly rules follow the Jalali calendar
 * and are always computed from the anchor date, so day 31 does not drift.
 */
object RecurrenceRules {

    const val MAX_CATCH_UP = 60

    fun occurrence(anchorEpochDay: Long, index: Int, monthly: Boolean): Long {
        if (!monthly) return anchorEpochDay + 7L * index
        val anchor = Jalali.fromGregorian(LocalDate.ofEpochDay(anchorEpochDay))
        val d = Jalali.addMonths(anchor, index)
        return Jalali.toGregorian(d.year, d.month, d.day).toEpochDay()
    }

    /** How many not-yet-posted occurrences are due on or before [today]. Capped for safety. */
    fun dueCount(anchorEpochDay: Long, posted: Int, today: Long, monthly: Boolean): Int {
        var n = 0
        while (n < MAX_CATCH_UP && occurrence(anchorEpochDay, posted + n, monthly) <= today) n++
        return n
    }
}

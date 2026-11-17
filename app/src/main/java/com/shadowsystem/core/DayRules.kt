package com.shadowsystem.core

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/** Game-day rules: the day rolls over at 04:00 local time (spec section 3). */
object DayRules {
    const val DAY_BOUNDARY_HOUR = 4
    const val COMEBACK_GAP_DAYS = 3 // missed >= 2 full days
    const val COMEBACK_QUESTS = 3
    const val COMEBACK_MULTIPLIER = 1.5

    fun gameDay(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): Long {
        val local = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), zone)
        return local.minusHours(DAY_BOUNDARY_HOUR.toLong()).toLocalDate().toEpochDay()
    }

    fun dateOf(gameDay: Long): LocalDate = LocalDate.ofEpochDay(gameDay)

    /** True when the user returns after missing at least two full days. */
    fun isComeback(lastActiveDay: Long, today: Long): Boolean =
        lastActiveDay >= 0 && today - lastActiveDay >= COMEBACK_GAP_DAYS
}

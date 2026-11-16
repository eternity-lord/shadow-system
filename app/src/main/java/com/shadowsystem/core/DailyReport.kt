package com.shadowsystem.core

/** Morning and night report rules (pure, unit tested). The day boundary is 04:00 (see DayRules). */
object DailyReport {
    enum class Trend { UP, SAME, DOWN, NONE }

    /** Score change from yesterday, or null when there is no yesterday score. */
    fun delta(score: Int, yesterday: Int?): Int? = yesterday?.let { score - it }

    fun trend(score: Int, yesterday: Int?): Trend = when (val d = delta(score, yesterday)) {
        null -> Trend.NONE
        0 -> Trend.SAME
        else -> if (d > 0) Trend.UP else Trend.DOWN
    }

    /** Night report from 20:00 until the day boundary; morning briefing otherwise. */
    fun isNightHour(hour: Int): Boolean = hour >= 20 || hour < 4
}

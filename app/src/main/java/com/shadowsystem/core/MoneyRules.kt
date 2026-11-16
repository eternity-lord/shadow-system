package com.shadowsystem.core

/** Money categories and the rules that connect spending to the game (spec section 9). */
object MoneyRules {

    val CATEGORIES: List<String> = listOf("خوراک", "حمل‌ونقل", "خانه", "تفریح", "سلامت", "سایر")

    /** Logging a transaction gives small INT XP, capped at 10 XP per day. */
    const val LOG_XP = 2
    const val MAX_LOG_XP_PER_DAY = 10

    fun logXpFor(loggedTodayBefore: Int): Int =
        if (loggedTodayBefore * LOG_XP >= MAX_LOG_XP_PER_DAY) 0 else LOG_XP

    /** Parses a toman amount typed by the user and returns rials (stored internally). */
    fun tomanToRial(toman: Long): Long = toman * 10

    fun isValidAmount(toman: Long?): Boolean = toman != null && toman in 1..1_000_000_000_000L
}

package com.shadowsystem.core

/**
 * Weekly boss (spec 5): a target of dungeon minutes plus 3 completed quests. Each part takes half of the HP bar.
 * A week is a block of 7 game days (weekId = gameDay / 7).
 */
object WeeklyBoss {
    const val TARGET_MINUTES = 120
    const val TARGET_QUESTS = 3
    const val REWARD_XP = 200
    const val REWARD_GOLD = 150

    data class Status(val minutes: Int, val quests: Int, val hpPercent: Int, val defeated: Boolean)

    fun status(minutes: Int, quests: Int): Status {
        val minutePart = (minutes.coerceAtLeast(0).coerceAtMost(TARGET_MINUTES) * 50) / TARGET_MINUTES
        val questPart = (quests.coerceAtLeast(0).coerceAtMost(TARGET_QUESTS) * 50) / TARGET_QUESTS
        val hp = (100 - minutePart - questPart).coerceIn(0, 100)
        return Status(minutes, quests, hp, defeated = hp == 0)
    }

    fun weekId(gameDay: Long): Long = gameDay / 7

    /** The heavy cosmetic unlocked by a defeated boss. Alternates between weeks. */
    fun heavyRewardFor(week: Long): String = if (week % 2 == 0L) "outfit_shadow" else "bg_throne"
}

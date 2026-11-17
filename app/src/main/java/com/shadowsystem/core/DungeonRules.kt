package com.shadowsystem.core

/** Dungeon monster rules: HP drains as time passes, and a small chance of rare loot. */
object DungeonRules {
    const val RARE_LOOT_PERCENT = 10

    /** Monster HP from 0 to 100: full at the start, empty when the planned time is over. */
    fun monsterHpPercent(plannedSeconds: Int, elapsedSeconds: Int): Int {
        if (plannedSeconds <= 0) return 0
        val left = (plannedSeconds - elapsedSeconds).coerceAtLeast(0)
        return (left * 100 / plannedSeconds).coerceIn(0, 100)
    }

    /** [roll] is a random whole number from 0 to 99. */
    fun isRareLoot(roll: Int): Boolean = roll < RARE_LOOT_PERCENT
}

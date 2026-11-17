package com.shadowsystem.core

/** Gate length chosen by the user: 5 to 120 minutes in steps of 5 (pure, unit tested). */
object GateRules {
    const val MIN = 5
    const val MAX = 120
    const val STEP = 5

    /** Rounds to the nearest step and keeps the value inside the allowed range. */
    fun clamp(minutes: Int): Int = ((minutes + STEP / 2) / STEP * STEP).coerceIn(MIN, MAX)

    fun minutesFor(rank: GameMath.Rank, overrides: Map<String, Int>): Int =
        overrides[rank.name]?.let { clamp(it) } ?: GameMath.dungeonMinutes(rank)
}

package com.shadowsystem.core

/**
 * Rank-up gate. Reaching the level of a new rank is not enough: the player must first pass that
 * rank's weekly trial (5 finished quests within 7 days). Pure rules; persistence lives in GameRepository.
 */
object RankTrial {
    const val TARGET_QUESTS = 5
    const val WINDOW_DAYS = 7L
    const val KEY_UNLOCKED = "rank_unlocked_index"
    const val KEY_START = "rank_trial_start"

    private val order = GameMath.Rank.values()

    fun levelRankIndex(totalXp: Long): Int = order.indexOf(GameMath.rankForLevel(GameMath.levelForXp(totalXp)))

    /** A trial is pending when the level has reached a rank that has not been unlocked yet. */
    fun isPending(levelIdx: Int, unlockedIdx: Int): Boolean = levelIdx > unlockedIdx

    /** The rank the player may actually use: the level rank, capped by the unlocked rank. */
    fun effectiveRank(levelIdx: Int, unlockedIdx: Int): GameMath.Rank =
        order[minOf(levelIdx, unlockedIdx).coerceIn(0, order.lastIndex)]

    fun targetRank(unlockedIdx: Int): GameMath.Rank = order[(unlockedIdx + 1).coerceIn(0, order.lastIndex)]

    fun windowExpired(start: Long, today: Long): Boolean = today - start >= WINDOW_DAYS
}

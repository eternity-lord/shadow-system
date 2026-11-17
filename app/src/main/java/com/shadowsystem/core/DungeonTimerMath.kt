package com.shadowsystem.core

/** Pure timer math for dungeon runs. Pause time does not count toward work. */
object DungeonTimerMath {
    const val REGEN_INTERVAL_SECONDS = 30
    const val REGEN_CAP_PERCENT = 10

    fun elapsedMillis(startedAt: Long, pausedTotal: Long, pausedAt: Long?, now: Long): Long {
        val end = pausedAt ?: now
        return (end - startedAt - pausedTotal).coerceAtLeast(0L)
    }

    fun elapsedSeconds(startedAt: Long, pausedTotal: Long, pausedAt: Long?, now: Long): Int =
        (elapsedMillis(startedAt, pausedTotal, pausedAt, now) / 1000L).toInt()

    fun pausedSeconds(pausedTotal: Long, pausedAt: Long?, now: Long): Int {
        val current = pausedAt?.let { (now - it).coerceAtLeast(0L) } ?: 0L
        return ((pausedTotal + current) / 1000L).toInt()
    }

    /** Cosmetic: the monster recovers 1% per 30 paused seconds, up to 10%. */
    fun regenPercent(pausedSeconds: Int): Int =
        (pausedSeconds / REGEN_INTERVAL_SECONDS).coerceIn(0, REGEN_CAP_PERCENT)

    fun displayedHpPercent(plannedSeconds: Int, elapsedSeconds: Int, pausedSeconds: Int): Int =
        (DungeonRules.monsterHpPercent(plannedSeconds, elapsedSeconds) + regenPercent(pausedSeconds)).coerceIn(0, 100)
}

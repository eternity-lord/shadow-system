package com.shadowsystem.data.repo

import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.shadowsystem.core.GameMath.Stat
import com.shadowsystem.core.WeeklyBoss
import com.shadowsystem.data.db.AppDatabase
import com.shadowsystem.data.db.KeyValueEntity

/** Weekly boss progress (from this week's dungeon minutes and completed quests) and its one-time reward. */
class BossRepository(private val db: AppDatabase, private val game: GameRepository) {
    private val kv = db.keyValueDao()

    /** Raw one-value query. Runs off the main thread: Room refuses direct queries there. */
    private suspend fun scalar(sql: String): Long = withContext(Dispatchers.IO) {
        db.query(SimpleSQLiteQuery(sql)).use { c -> if (c.moveToFirst()) c.getLong(0) else 0L }
    }

    private fun claimKey(week: Long) = "boss_claimed_$week"

    suspend fun status(): WeeklyBoss.Status {
        val week = WeeklyBoss.weekId(game.today())
        val start = week * 7
        val end = start + 7
        val seconds = scalar("SELECT COALESCE(SUM(doneSeconds), 0) FROM dungeon_sessions WHERE gameDay >= $start AND gameDay < $end")
        val quests = scalar("SELECT COUNT(*) FROM quests WHERE status = 'DONE' AND gameDay >= $start AND gameDay < $end")
        return WeeklyBoss.status((seconds / 60).toInt(), quests.toInt())
    }

    suspend fun claimedThisWeek(): Boolean = kv.get(claimKey(WeeklyBoss.weekId(game.today())))?.value == 1L

    /** Pays the reward once per week, only when the boss is defeated. */
    suspend fun claim(): Boolean {
        val week = WeeklyBoss.weekId(game.today())
        if (claimedThisWeek() || !status().defeated) return false
        kv.upsert(KeyValueEntity(claimKey(week), 1L))
        kv.upsert(KeyValueEntity("owned_${WeeklyBoss.heavyRewardFor(week)}", 1L))
        game.addXp(WeeklyBoss.REWARD_XP, Stat.INT.name, WeeklyBoss.REWARD_GOLD)
        return true
    }
}

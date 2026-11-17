package com.shadowsystem.data.repo

import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.shadowsystem.core.GameMath
import com.shadowsystem.core.TitleRules
import com.shadowsystem.data.db.AppDatabase

/** Collects the facts that title and achievement rules need. Read-only. */
class AchievementRepository(private val db: AppDatabase, private val game: GameRepository) {

    /** Raw one-value query. Runs off the main thread: Room refuses direct queries there. */
    private suspend fun scalar(sql: String): Long = withContext(Dispatchers.IO) {
        db.query(SimpleSQLiteQuery(sql)).use { c -> if (c.moveToFirst()) c.getLong(0) else 0L }
    }

    suspend fun facts(): TitleRules.Facts {
        val profile = game.profile()
        val level = GameMath.levelForXp(profile.totalXp)
        val rankIndex = GameMath.Rank.values().indexOf(GameMath.rankForLevel(level))
        return TitleRules.Facts(
            level = level,
            activeDays = profile.activeDays,
            rankIndex = rankIndex,
            questsDone = db.questDao().doneSince(0),
            dungeonsCompleted = scalar("SELECT COUNT(*) FROM dungeon_sessions WHERE completed = 1").toInt(),
            transactions = scalar("SELECT COUNT(*) FROM transactions").toInt(),
            bestScore = scalar("SELECT COALESCE(MAX(score), 0) FROM daily_scores").toInt(),
            beatYesterday = scalar(
                "SELECT COUNT(*) FROM daily_scores a INNER JOIN daily_scores b ON a.gameDay = b.gameDay + 1 " +
                    "WHERE a.score > b.score AND a.score > 0"
            ) > 0,
            goalDone = scalar("SELECT COUNT(*) FROM savings_goals WHERE targetRial > 0 AND savedRial >= targetRial") > 0,
            soldiersSummoned = scalar("SELECT COUNT(*) FROM soldiers WHERE summonedAt IS NOT NULL").toInt(),
            bossDone = scalar("SELECT COUNT(*) FROM kv WHERE `key` LIKE 'boss_claimed_%'") > 0
        )
    }
}

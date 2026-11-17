package com.shadowsystem.debug

import com.shadowsystem.core.DayRules
import com.shadowsystem.core.Mood
import com.shadowsystem.core.MoneyRules
import com.shadowsystem.data.db.AppDatabase
import com.shadowsystem.data.db.DailyScoreEntity
import com.shadowsystem.data.db.DungeonSessionEntity
import com.shadowsystem.data.db.TransactionEntity
import kotlin.random.Random

/** Debug builds only: 60 days of fake scores, dungeon runs and expenses, to test charts. */
object DebugSeed {
    suspend fun seed(db: AppDatabase) {
        val random = Random(7)
        val today = DayRules.gameDay(System.currentTimeMillis())
        val categories = MoneyRules.CATEGORIES
        val moods = Mood.values()
        for (i in 0 until 60) {
            val day = today - i
            val minutes = if (random.nextInt(3) == 0) 0 else 10 + random.nextInt(40)
            db.dailyScoreDao().upsert(
                DailyScoreEntity(
                    gameDay = day,
                    score = 30 + random.nextInt(71),
                    mood = moods[random.nextInt(moods.size)].name,
                    dungeonMinutes = minutes,
                    loggedMoney = random.nextBoolean()
                )
            )
            if (minutes > 0) {
                db.dungeonDao().insert(
                    DungeonSessionEntity(
                        gameDay = day,
                        rank = "E",
                        plannedMinutes = 15,
                        doneSeconds = minutes * 60,
                        completed = minutes >= 15,
                        xpGained = minutes * 2,
                        createdAt = day * DAY_MILLIS
                    )
                )
            }
            repeat(random.nextInt(4)) {
                val toman = 5_000L + random.nextInt(495) * 1_000L
                db.transactionDao().insert(
                    TransactionEntity(
                        amountRial = toman * 10,
                        category = categories[random.nextInt(categories.size)],
                        note = "",
                        isIncome = false,
                        epochDay = day,
                        createdAt = day * DAY_MILLIS
                    )
                )
            }
        }
    }

    private const val DAY_MILLIS = 86_400_000L
}

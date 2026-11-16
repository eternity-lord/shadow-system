package com.shadowsystem.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProfileEntity::class,
        StatProgressEntity::class,
        QuestEntity::class,
        QuestStepEntity::class,
        DailyScoreEntity::class,
        DungeonSessionEntity::class,
        TransactionEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun statDao(): StatDao
    abstract fun questDao(): QuestDao
    abstract fun dailyScoreDao(): DailyScoreDao
    abstract fun dungeonDao(): DungeonDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "shadow_system.db")
                .build()
    }
}

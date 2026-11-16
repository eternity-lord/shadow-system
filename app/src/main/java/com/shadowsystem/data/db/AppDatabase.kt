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
        TransactionEntity::class,
        BudgetEntity::class,
        RecurringEntity::class,
        GoalEntity::class,
        DebtEntity::class,
        KeyValueEntity::class,
        InboxEntity::class,
        SoldierEntity::class
    ],
    version = 4,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun statDao(): StatDao
    abstract fun questDao(): QuestDao
    abstract fun dailyScoreDao(): DailyScoreDao
    abstract fun dungeonDao(): DungeonDao
    abstract fun inboxDao(): InboxDao
    abstract fun soldierDao(): SoldierDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringDao(): RecurringDao
    abstract fun goalDao(): GoalDao
    abstract fun debtDao(): DebtDao
    abstract fun keyValueDao(): KeyValueDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "shadow_system.db")
                .addMigrations(Migrations.MIGRATION_1_2, Migrations.MIGRATION_2_3, Migrations.MIGRATION_3_4)
                .build()
    }
}

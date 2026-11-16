package com.shadowsystem.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 1,
    val totalXp: Long = 0,
    val gold: Int = 0,
    val activeDays: Int = 0,
    val lastActiveDay: Long = -1,
    val onboarded: Boolean = false
)

@Entity(tableName = "stat_progress")
data class StatProgressEntity(
    @PrimaryKey val stat: String,
    val xp: Long = 0
)

@Entity(
    tableName = "quests",
    indices = [Index("gameDay")]
)
data class QuestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val stat: String,
    val rank: String,
    val minutes: Int,
    val mandatory: Boolean,
    val lowEnergy: Boolean,
    val gameDay: Long,
    val status: String = STATUS_OPEN,
    val xpReward: Int,
    val goldReward: Int,
    val xpAwarded: Int = 0,
    val stepsDone: Int = 0,
    val stepsTotal: Int,
    val comebackApplied: Boolean = false,
    val completedAt: Long = 0
) {
    companion object {
        const val STATUS_OPEN = "OPEN"
        const val STATUS_DONE = "DONE"
    }
}

@Entity(
    tableName = "quest_steps",
    foreignKeys = [
        ForeignKey(
            entity = QuestEntity::class,
            parentColumns = ["id"],
            childColumns = ["questId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("questId")]
)
data class QuestStepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questId: Long,
    val position: Int,
    val text: String,
    val done: Boolean = false
)

@Entity(tableName = "daily_scores")
data class DailyScoreEntity(
    @PrimaryKey val gameDay: Long,
    val score: Int,
    val mood: String,
    val dungeonMinutes: Int = 0,
    val loggedMoney: Boolean = false
)

@Entity(tableName = "dungeon_sessions")
data class DungeonSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameDay: Long,
    val rank: String,
    val plannedMinutes: Int,
    val doneSeconds: Int,
    val completed: Boolean,
    val xpGained: Int,
    val createdAt: Long
)

@Entity(
    tableName = "transactions",
    indices = [Index("epochDay")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountRial: Long,
    val category: String,
    val note: String,
    val isIncome: Boolean,
    val epochDay: Long,
    val createdAt: Long
)

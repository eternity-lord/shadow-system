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
    val completedAt: Long = 0,
    val soldierKey: String? = null
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
    val createdAt: Long,
    val note: String? = null
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

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val category: String,
    val limitRial: Long
)

@Entity(tableName = "recurring_rules")
data class RecurringEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amountRial: Long,
    val category: String,
    val isIncome: Boolean,
    val monthly: Boolean,
    val anchorEpochDay: Long,
    val posted: Int,
    val active: Boolean
)

@Entity(tableName = "savings_goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetRial: Long,
    val savedRial: Long,
    val createdDay: Long
)

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val totalRial: Long,
    val paidRial: Long,
    val iOwe: Boolean
)

@Entity(tableName = "kv")
data class KeyValueEntity(
    @PrimaryKey val key: String,
    val value: Long
)

@Entity(tableName = "inbox")
data class InboxEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val createdAt: Long
)

/** Only what cannot be derived from quests: the custom name and when the soldier was summoned. */
@Entity(tableName = "soldiers")
data class SoldierEntity(
    @PrimaryKey val key: String,
    val customName: String? = null,
    val summonedAt: Long? = null
)

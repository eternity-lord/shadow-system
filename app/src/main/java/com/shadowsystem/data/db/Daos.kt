package com.shadowsystem.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile WHERE id = 1")
    fun observe(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profile WHERE id = 1")
    suspend fun get(): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: ProfileEntity)
}

@Dao
interface StatDao {
    @Query("SELECT * FROM stat_progress")
    suspend fun all(): List<StatProgressEntity>

    @Insert
    suspend fun insertAll(list: List<StatProgressEntity>)

    @Query("SELECT * FROM stat_progress")
    fun observeAll(): Flow<List<StatProgressEntity>>

    @Query("SELECT * FROM stat_progress WHERE stat = :stat")
    suspend fun get(stat: String): StatProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stat: StatProgressEntity)
}

/** A completed quest that belongs to a soldier. */
data class LinkedQuestRow(
    val soldierKey: String?,
    val gameDay: Long,
    val title: String,
    val stepsDone: Int
)

@Dao
interface QuestDao {
    @Query("SELECT soldierKey, gameDay, title, stepsDone FROM quests WHERE status = 'DONE' AND soldierKey IS NOT NULL")
    fun observeLinkedDone(): Flow<List<LinkedQuestRow>>

    @Query("SELECT DISTINCT gameDay FROM quests WHERE status = 'DONE'")
    fun observeActiveDays(): Flow<List<Long>>

    @Query("DELETE FROM quest_steps WHERE questId = :questId AND position > :position AND done = 0")
    suspend fun deleteUndoneStepsAfter(questId: Long, position: Int)

    @Query("SELECT * FROM quests WHERE gameDay = :day ORDER BY mandatory DESC, id ASC")
    fun observeDay(day: Long): Flow<List<QuestEntity>>

    @Query("SELECT * FROM quests WHERE id = :id")
    suspend fun get(id: Long): QuestEntity?

    @Query("SELECT COUNT(*) FROM quests WHERE status = 'DONE' AND gameDay >= :from")
    suspend fun doneSince(from: Long): Int

    @Query("SELECT COUNT(*) FROM quests WHERE gameDay = :day")
    suspend fun countForDay(day: Long): Int

    @Insert
    suspend fun insertQuest(quest: QuestEntity): Long

    @Insert
    suspend fun insertSteps(steps: List<QuestStepEntity>)

    @Update
    suspend fun updateQuest(quest: QuestEntity)

    @Query("SELECT * FROM quest_steps WHERE questId = :questId ORDER BY position")
    fun observeSteps(questId: Long): Flow<List<QuestStepEntity>>

    @Query("SELECT * FROM quest_steps WHERE questId = :questId ORDER BY position")
    suspend fun steps(questId: Long): List<QuestStepEntity>

    @Query("SELECT * FROM quest_steps WHERE questId IN (SELECT id FROM quests WHERE gameDay = :day)")
    fun observeStepsForDay(day: Long): Flow<List<QuestStepEntity>>

    @Update
    suspend fun updateStep(step: QuestStepEntity)

    @Query("SELECT COUNT(*) FROM quests WHERE gameDay = :day AND status = 'DONE' AND stat = :stat")
    suspend fun doneCountForStat(day: Long, stat: String): Int

    @Query("SELECT * FROM quests WHERE gameDay = :day AND status = 'DONE'")
    suspend fun doneForDay(day: Long): List<QuestEntity>

    @Query("SELECT COALESCE(SUM(xpAwarded), 0) FROM quests WHERE gameDay = :day")
    suspend fun xpAwardedForDay(day: Long): Int

    @Query("SELECT COALESCE(SUM(xpReward), 0) FROM quests WHERE gameDay = :day AND mandatory = 1")
    suspend fun plannedMandatoryXp(day: Long): Int

    @Query("SELECT * FROM quests WHERE gameDay = :day")
    suspend fun forDay(day: Long): List<QuestEntity>

    @Query("SELECT MAX(gameDay) FROM quests WHERE gameDay < :day")
    suspend fun previousDay(day: Long): Long?

    @Query("SELECT * FROM quests WHERE status = 'DONE' ORDER BY completedAt DESC LIMIT :limit")
    suspend fun recentDone(limit: Int): List<QuestEntity>
}

@Dao
interface DailyScoreDao {
    @Query("SELECT * FROM daily_scores WHERE gameDay = :day")
    fun observe(day: Long): Flow<DailyScoreEntity?>

    @Query("SELECT * FROM daily_scores WHERE gameDay = :day")
    suspend fun get(day: Long): DailyScoreEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(score: DailyScoreEntity)
}

@Dao
interface DungeonDao {
    @Insert
    suspend fun insert(session: DungeonSessionEntity): Long

    @Query("UPDATE dungeon_sessions SET note = :note WHERE id = :id")
    suspend fun setNote(id: Long, note: String?)

    @Query("SELECT COALESCE(SUM(doneSeconds), 0) FROM dungeon_sessions WHERE gameDay = :day")
    fun observeSecondsForDay(day: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(doneSeconds), 0) FROM dungeon_sessions WHERE gameDay = :day")
    suspend fun secondsForDay(day: Long): Int

    @Query("SELECT COUNT(*) FROM dungeon_sessions WHERE completed = 1")
    suspend fun completedCount(): Int
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY epochDay, id")
    suspend fun all(): List<TransactionEntity>

    @Insert
    suspend fun insertAll(list: List<TransactionEntity>)

    @Query("SELECT COUNT(*) FROM transactions WHERE epochDay = :day")
    suspend fun dayCount(day: Long): Int

    @Query("SELECT * FROM transactions WHERE epochDay BETWEEN :from AND :to ORDER BY createdAt DESC")
    fun observeRange(from: Long, to: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE epochDay BETWEEN :from AND :to")
    suspend fun range(from: Long, to: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE epochDay = :day")
    suspend fun countForDay(day: Long): Int

    @Insert
    suspend fun insert(tx: TransactionEntity): Long

    @Delete
    suspend fun delete(tx: TransactionEntity)
}

@Dao
interface SoldierDao {
    @Query("SELECT * FROM soldiers")
    fun observeAll(): Flow<List<SoldierEntity>>

    @Query("SELECT * FROM soldiers WHERE `key` = :key")
    suspend fun get(key: String): SoldierEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(soldier: SoldierEntity)
}

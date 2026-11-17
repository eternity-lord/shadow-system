package com.shadowsystem.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets")
    fun observeAll(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets")
    suspend fun all(): List<BudgetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: BudgetEntity)

    @Insert
    suspend fun insertAll(list: List<BudgetEntity>)

    @Query("DELETE FROM budgets WHERE category = :category")
    suspend fun delete(category: String)
}

@Dao
interface RecurringDao {
    @Query("SELECT * FROM recurring_rules ORDER BY id DESC")
    fun observeAll(): Flow<List<RecurringEntity>>

    @Query("SELECT * FROM recurring_rules WHERE active = 1")
    suspend fun activeRules(): List<RecurringEntity>

    @Query("SELECT * FROM recurring_rules")
    suspend fun all(): List<RecurringEntity>

    @Insert
    suspend fun insert(rule: RecurringEntity): Long

    @Update
    suspend fun update(rule: RecurringEntity)

    @Insert
    suspend fun insertAll(list: List<RecurringEntity>)

    @Query("DELETE FROM recurring_rules WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM savings_goals ORDER BY id DESC")
    fun observeAll(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM savings_goals")
    suspend fun all(): List<GoalEntity>

    @Insert
    suspend fun insert(goal: GoalEntity): Long

    @Update
    suspend fun update(goal: GoalEntity)

    @Insert
    suspend fun insertAll(list: List<GoalEntity>)

    @Query("DELETE FROM savings_goals WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY id DESC")
    fun observeAll(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts")
    suspend fun all(): List<DebtEntity>

    @Insert
    suspend fun insert(debt: DebtEntity): Long

    @Update
    suspend fun update(debt: DebtEntity)

    @Insert
    suspend fun insertAll(list: List<DebtEntity>)

    @Query("DELETE FROM debts WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface KeyValueDao {
    @Query("SELECT * FROM kv WHERE `key` = :key")
    suspend fun get(key: String): KeyValueEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: KeyValueEntity)
}

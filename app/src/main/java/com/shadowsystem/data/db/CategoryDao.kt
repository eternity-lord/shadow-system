package com.shadowsystem.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sortOrder, name")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE archived = 0 AND isIncome = :isIncome ORDER BY sortOrder, name")
    fun observeActive(isIncome: Boolean): Flow<List<CategoryEntity>>

    @Query("SELECT name FROM categories")
    suspend fun names(): List<String>

    @Query("SELECT COALESCE(MAX(sortOrder), 0) + 1 FROM categories")
    suspend fun nextSortOrder(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(category: CategoryEntity)

    @Query("UPDATE categories SET archived = :archived WHERE name = :name")
    suspend fun setArchived(name: String, archived: Boolean)

    @Query("UPDATE categories SET name = :to WHERE name = :from")
    suspend fun renameCategory(from: String, to: String)

    @Query("UPDATE transactions SET category = :to WHERE category = :from")
    suspend fun renameTransactions(from: String, to: String)

    @Query("UPDATE budgets SET category = :to WHERE category = :from")
    suspend fun renameBudgets(from: String, to: String)

    @Query("UPDATE recurring_rules SET category = :to WHERE category = :from")
    suspend fun renameRecurring(from: String, to: String)
}

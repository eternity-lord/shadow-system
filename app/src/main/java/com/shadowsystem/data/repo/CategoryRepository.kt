package com.shadowsystem.data.repo

import androidx.room.withTransaction
import com.shadowsystem.core.CategoryRules
import com.shadowsystem.data.db.AppDatabase
import com.shadowsystem.data.db.CategoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** Adds, renames and archives money categories. Renaming also updates transactions, budgets and recurring rules. */
class CategoryRepository(private val db: AppDatabase) {
    private val dao = db.categoryDao()

    fun observeAll(): Flow<List<CategoryEntity>> = dao.observeAll()

    /** Names of categories the user can pick now (not archived), for one kind. */
    fun observeActive(isIncome: Boolean): Flow<List<String>> =
        dao.observeActive(isIncome).map { rows -> rows.map { it.name } }

    suspend fun add(name: String, isIncome: Boolean): CategoryRules.Check = withContext(Dispatchers.IO) {
        val clean = name.trim()
        val check = CategoryRules.check(clean, dao.names().toSet())
        if (check == CategoryRules.Check.OK) {
            dao.insert(
                CategoryEntity(
                    name = clean,
                    isIncome = isIncome,
                    archived = false,
                    colorArgb = CUSTOM_COLOR,
                    sortOrder = dao.nextSortOrder()
                )
            )
        }
        check
    }

    suspend fun rename(from: String, to: String): CategoryRules.Check = withContext(Dispatchers.IO) {
        val clean = to.trim()
        val check = CategoryRules.check(clean, dao.names().toSet(), renamingFrom = from)
        if (check == CategoryRules.Check.OK && clean != from) {
            db.withTransaction {
                dao.renameCategory(from, clean)
                dao.renameTransactions(from, clean)
                dao.renameBudgets(from, clean)
                dao.renameRecurring(from, clean)
            }
        }
        check
    }

    suspend fun setArchived(name: String, archived: Boolean) = withContext(Dispatchers.IO) {
        dao.setArchived(name, archived)
    }

    private companion object {
        const val CUSTOM_COLOR = 0xFF90A4AEL
    }
}

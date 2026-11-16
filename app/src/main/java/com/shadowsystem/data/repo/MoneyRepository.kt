package com.shadowsystem.data.repo

import com.shadowsystem.core.GameMath
import com.shadowsystem.core.MoneyRules
import com.shadowsystem.data.db.AppDatabase
import com.shadowsystem.data.db.TransactionEntity
import kotlinx.coroutines.flow.Flow

/** Real money. Strictly separate from Gold: spending never gives XP or Gold. */
class MoneyRepository(
    private val db: AppDatabase,
    private val game: GameRepository,
    private val clock: () -> Long = System::currentTimeMillis
) {
    private val dao = db.transactionDao()

    fun observeRange(fromDay: Long, toDay: Long): Flow<List<TransactionEntity>> = dao.observeRange(fromDay, toDay)

    suspend fun range(fromDay: Long, toDay: Long): List<TransactionEntity> = dao.range(fromDay, toDay)

    /**
     * Stores a transaction. Logging gives a little INT XP (capped per day).
     * Returns the XP awarded.
     */
    suspend fun add(toman: Long, category: String, note: String, isIncome: Boolean): Int {
        val day = game.today()
        val loggedBefore = dao.countForDay(day)
        dao.insert(
            TransactionEntity(
                amountRial = MoneyRules.tomanToRial(toman),
                category = category,
                note = note.trim(),
                isIncome = isIncome,
                epochDay = day,
                createdAt = clock()
            )
        )
        val xp = MoneyRules.logXpFor(loggedBefore)
        if (xp > 0) game.addXp(xp, GameMath.Stat.INT.name, gold = 0)
        game.refreshScore()
        return xp
    }

    suspend fun delete(tx: TransactionEntity) {
        dao.delete(tx)
        game.refreshScore()
    }
}

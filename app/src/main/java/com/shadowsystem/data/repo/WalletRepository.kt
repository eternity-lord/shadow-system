package com.shadowsystem.data.repo

import com.shadowsystem.core.WalletRules
import com.shadowsystem.data.db.AppDatabase
import com.shadowsystem.data.db.ChequeEntity
import com.shadowsystem.data.db.TransferEntity
import com.shadowsystem.data.db.WalletEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Wallets, transfers and cheques. Money only; never gives XP or Gold. */
class WalletRepository(private val db: AppDatabase, private val game: GameRepository) {
    private val wallets = db.walletDao()
    private val transfers = db.transferDao()
    private val cheques = db.chequeDao()
    private val txs = db.transactionDao()

    fun observeWallets(): Flow<List<WalletEntity>> = wallets.observeAll()

    fun observeCheques(): Flow<List<ChequeEntity>> = cheques.observeAll()

    /** Live balance per wallet id. Transactions without a wallet count toward the first wallet. */
    fun observeBalances(): Flow<Map<Long, Long>> = combine(wallets.observeAll(), txs.observeAll(), transfers.observeAll()) { ws, t, m ->
        val first = ws.minByOrNull { it.id }?.id
        WalletRules.balances(
            openings = ws.associate { it.id to it.openingRial },
            txs = t.mapNotNull { x -> (x.walletId ?: first)?.let { WalletRules.Tx(it, x.amountRial, x.isIncome) } },
            moves = m.map { WalletRules.Move(it.fromWalletId, it.toWalletId, it.amountRial) }
        )
    }

    /** Creates the cash wallet on first use. */
    suspend fun ensureDefault() {
        if (wallets.count() == 0) wallets.insert(WalletEntity(name = DEFAULT_NAME))
    }

    suspend fun addWallet(name: String, openingRial: Long): Boolean {
        val clean = name.trim().take(MAX_NAME)
        if (clean.isEmpty()) return false
        wallets.insert(WalletEntity(name = clean, openingRial = openingRial))
        return true
    }

    suspend fun transfer(from: Long, to: Long, amountRial: Long): Boolean {
        if (!WalletRules.canTransfer(from, to, amountRial)) return false
        transfers.insert(
            TransferEntity(
                fromWalletId = from,
                toWalletId = to,
                amountRial = amountRial,
                epochDay = game.today(),
                createdAt = System.currentTimeMillis()
            )
        )
        return true
    }

    suspend fun addCheque(title: String, amountRial: Long, dueDay: Long, issuedByMe: Boolean): Boolean {
        val clean = title.trim().take(MAX_NAME)
        if (clean.isEmpty() || amountRial <= 0L) return false
        cheques.insert(ChequeEntity(title = clean, amountRial = amountRial, dueEpochDay = dueDay, issuedByMe = issuedByMe))
        return true
    }

    suspend fun setChequeCleared(id: Long, cleared: Boolean) = cheques.setCleared(id, cleared)

    suspend fun deleteCheque(id: Long) = cheques.delete(id)

    companion object {
        const val DEFAULT_NAME = "نقدی"
        const val MAX_NAME = 30
    }
}

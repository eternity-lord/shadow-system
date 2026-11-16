package com.shadowsystem.data.backup

import androidx.room.withTransaction
import com.shadowsystem.core.Crypto
import com.shadowsystem.data.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Export and restore of encrypted backups and CSV. All heavy work runs off the main thread. */
class BackupService(private val db: AppDatabase) {

    suspend fun snapshot(): BackupSnapshot = BackupSnapshot(
        profile = db.profileDao().get(),
        stats = db.statDao().all(),
        transactions = db.transactionDao().all(),
        budgets = db.budgetDao().all(),
        recurring = db.recurringDao().all(),
        goals = db.goalDao().all(),
        debts = db.debtDao().all(),
        wallets = db.walletDao().all(),
        transfers = db.transferDao().all(),
        cheques = db.chequeDao().all()
    )

    suspend fun exportEncrypted(password: CharArray): ByteArray = withContext(Dispatchers.IO) {
        Crypto.encrypt(BackupCodec.encode(snapshot()).toByteArray(Charsets.UTF_8), password)
    }

    suspend fun exportCsv(): String = withContext(Dispatchers.IO) {
        CsvExport.transactions(db.transactionDao().all(), db.walletDao().all())
    }

    /** Replaces all local data with the backup. Decryption fails before anything is touched. */
    suspend fun restore(data: ByteArray, password: CharArray) = withContext(Dispatchers.IO) {
        val text = String(Crypto.decrypt(data, password), Charsets.UTF_8)
        val s = BackupCodec.decode(text)
        db.withTransaction {
            db.clearAllTables()
            s.profile?.let { db.profileDao().upsert(it) }
            db.statDao().insertAll(s.stats)
            db.transactionDao().insertAll(s.transactions)
            db.budgetDao().insertAll(s.budgets)
            db.recurringDao().insertAll(s.recurring)
            db.goalDao().insertAll(s.goals)
            db.debtDao().insertAll(s.debts)
            db.walletDao().insertAll(s.wallets)
            db.transferDao().insertAll(s.transfers)
            db.chequeDao().insertAll(s.cheques)
        }
    }
}

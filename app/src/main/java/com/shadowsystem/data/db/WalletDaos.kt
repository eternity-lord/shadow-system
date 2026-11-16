package com.shadowsystem.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallets ORDER BY id")
    fun observeAll(): Flow<List<WalletEntity>>

    @Query("SELECT COUNT(*) FROM wallets")
    suspend fun count(): Int

    @Insert
    suspend fun insert(wallet: WalletEntity): Long
}

@Dao
interface TransferDao {
    @Query("SELECT * FROM transfers ORDER BY epochDay DESC, id DESC")
    fun observeAll(): Flow<List<TransferEntity>>

    @Insert
    suspend fun insert(transfer: TransferEntity): Long
}

@Dao
interface ChequeDao {
    @Query("SELECT * FROM cheques ORDER BY cleared, dueEpochDay, id")
    fun observeAll(): Flow<List<ChequeEntity>>

    @Insert
    suspend fun insert(cheque: ChequeEntity): Long

    @Query("UPDATE cheques SET cleared = :cleared WHERE id = :id")
    suspend fun setCleared(id: Long, cleared: Boolean)

    @Query("DELETE FROM cheques WHERE id = :id")
    suspend fun delete(id: Long)
}

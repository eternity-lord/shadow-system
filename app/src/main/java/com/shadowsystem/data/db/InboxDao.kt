package com.shadowsystem.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Quick notes parked on the Now tab until the user triages them. */
@Dao
interface InboxDao {
    @Query("SELECT * FROM inbox ORDER BY createdAt DESC, id DESC")
    fun observeAll(): Flow<List<InboxEntity>>

    @Insert
    suspend fun insert(item: InboxEntity): Long

    @Query("DELETE FROM inbox WHERE id = :id")
    suspend fun delete(id: Long)
}

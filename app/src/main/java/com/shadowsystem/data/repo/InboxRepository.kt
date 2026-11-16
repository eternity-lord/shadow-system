package com.shadowsystem.data.repo

import com.shadowsystem.data.db.AppDatabase
import com.shadowsystem.data.db.InboxEntity
import kotlinx.coroutines.flow.Flow

/** Quick notes ("park a thought"). Stored locally, triaged by the user. */
class InboxRepository(db: AppDatabase) {
    private val dao = db.inboxDao()

    fun observe(): Flow<List<InboxEntity>> = dao.observeAll()

    /** Adds a note. Blank text is ignored. Returns true when something was saved. */
    suspend fun add(text: String, now: Long): Boolean {
        val clean = text.trim().take(MAX_LENGTH)
        if (clean.isEmpty()) return false
        dao.insert(InboxEntity(text = clean, createdAt = now))
        return true
    }

    suspend fun delete(id: Long) = dao.delete(id)

    companion object {
        const val MAX_LENGTH = 200
    }
}

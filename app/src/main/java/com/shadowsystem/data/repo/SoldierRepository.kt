package com.shadowsystem.data.repo

import com.shadowsystem.core.LinkedQuest
import com.shadowsystem.core.SavedSoldier
import com.shadowsystem.core.SoldierData
import com.shadowsystem.data.db.AppDatabase
import com.shadowsystem.data.db.KeyValueEntity
import com.shadowsystem.data.db.SoldierEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Soldier roster storage. Progress is derived from quests; only the name, summon time and last seen stage are stored. */
class SoldierRepository(db: AppDatabase) {
    private val soldiers = db.soldierDao()
    private val quests = db.questDao()
    private val kv = db.keyValueDao()

    fun observeSaved(): Flow<List<SoldierEntity>> = soldiers.observeAll()

    fun observeLinked(): Flow<List<LinkedQuest>> = quests.observeLinkedDone().map { rows ->
        rows.mapNotNull { r -> r.soldierKey?.let { LinkedQuest(it, r.gameDay, r.title, r.stepsDone) } }
    }

    fun observeActiveDays(): Flow<List<Long>> = quests.observeActiveDays()

    suspend fun summon(key: String, now: Long) {
        val cur = soldiers.get(key) ?: SoldierEntity(key)
        soldiers.upsert(cur.copy(summonedAt = cur.summonedAt ?: now))
    }

    suspend fun rename(key: String, name: String) {
        val cur = soldiers.get(key) ?: SoldierEntity(key)
        soldiers.upsert(cur.copy(customName = name.trim().take(MAX_NAME).ifBlank { null }))
    }

    /** The last stage the user has seen (used to decide when to play the evolution cutscene). */
    suspend fun loadSeen(): Map<String, Int> = SoldierData.ALL.mapNotNull { def ->
        kv.get(seenKey(def.key))?.let { def.key to it.value.toInt() }
    }.toMap()

    suspend fun markSeen(key: String, stage: Int) = kv.upsert(KeyValueEntity(seenKey(key), stage.toLong()))

    companion object {
        const val MAX_NAME = 20
        private fun seenKey(key: String) = "soldier_seen_$key"
    }
}

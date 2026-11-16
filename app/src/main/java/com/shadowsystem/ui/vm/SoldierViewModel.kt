package com.shadowsystem.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.shadowsystem.ShadowApp
import com.shadowsystem.core.GameMath
import com.shadowsystem.core.SavedSoldier
import com.shadowsystem.core.SoldierData
import com.shadowsystem.core.SoldierRoster
import com.shadowsystem.core.SoldierRow
import com.shadowsystem.data.repo.GameRepository
import com.shadowsystem.data.repo.SoldierRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Soldier roster, summoning ritual and evolution cutscene. */
class SoldierViewModel(
    private val game: GameRepository,
    private val repo: SoldierRepository
) : ViewModel() {
    private val seen = MutableStateFlow<Map<String, Int>>(emptyMap())
    private val ritualKey = MutableStateFlow<String?>(null)

    val roster: StateFlow<List<SoldierRow>> = combine(
        repo.observeSaved(),
        repo.observeLinked(),
        repo.observeActiveDays(),
        game.observeProfile()
    ) { saved, linked, days, profile ->
        SoldierRoster.build(
            defs = SoldierData.ALL,
            saved = saved.associate { it.key to SavedSoldier(it.customName, it.summonedAt) },
            linked = linked,
            totalActiveDays = days.size,
            isRankS = GameMath.rankForLevel(GameMath.levelForXp(profile.totalXp)) == GameMath.Rank.S,
            today = game.today()
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** A summoned soldier whose stage is above the last stage the user has seen. Missing record means stage 1. */
    val evolution: StateFlow<SoldierRow?> = combine(roster, seen) { rows, seenMap ->
        rows.firstOrNull { it.summoned && it.progress.stage > (seenMap[it.def.key] ?: 1) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** The soldier currently being summoned, if the ritual is running. */
    val ritual: StateFlow<SoldierRow?> = combine(roster, ritualKey) { rows, key ->
        rows.firstOrNull { it.def.key == key }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch { seen.value = repo.loadSeen() }
    }

    fun startRitual(key: String) {
        ritualKey.value = key
    }

    /** Ends the ritual (after 5 s or on skip) and summons the soldier. */
    fun finishRitual() {
        val key = ritualKey.value ?: return
        ritualKey.value = null
        viewModelScope.launch {
            repo.summon(key, System.currentTimeMillis())
            repo.markSeen(key, 1)
            seen.value = seen.value + (key to 1)
        }
    }

    fun rename(key: String, name: String) {
        viewModelScope.launch { repo.rename(key, name) }
    }

    /** Ends the evolution cutscene and records the new stage as seen. */
    fun finishEvolution(row: SoldierRow) {
        val stage = row.progress.stage
        viewModelScope.launch {
            repo.markSeen(row.def.key, stage)
            seen.value = seen.value + (row.def.key to stage)
        }
    }

    companion object {
        fun factory(app: ShadowApp): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                SoldierViewModel(app.container.game, app.container.soldiers) as T
        }
    }
}

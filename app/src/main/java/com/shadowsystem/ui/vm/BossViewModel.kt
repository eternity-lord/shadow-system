package com.shadowsystem.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.shadowsystem.ShadowApp
import com.shadowsystem.core.WeeklyBoss
import com.shadowsystem.data.repo.BossRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BossState(val status: WeeklyBoss.Status? = null, val claimed: Boolean = false)

class BossViewModel(private val boss: BossRepository) : ViewModel() {
    private val _state = MutableStateFlow(BossState())
    val state: StateFlow<BossState> = _state.asStateFlow()

    init {
        refresh()
    }

    /** Recomputes the boss from this week's data. Call when dungeon time or quests change. */
    fun refresh() {
        viewModelScope.launch {
            _state.value = BossState(boss.status(), boss.claimedThisWeek())
        }
    }

    fun claim() {
        viewModelScope.launch {
            boss.claim()
            _state.value = BossState(boss.status(), boss.claimedThisWeek())
        }
    }

    companion object {
        fun factory(app: ShadowApp): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                BossViewModel(app.container.boss) as T
        }
    }
}

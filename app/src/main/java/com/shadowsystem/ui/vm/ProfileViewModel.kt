package com.shadowsystem.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.shadowsystem.ShadowApp
import com.shadowsystem.core.GameMath.Stat
import com.shadowsystem.core.ShopItem
import com.shadowsystem.core.TitleRules
import com.shadowsystem.data.repo.AchievementRepository
import com.shadowsystem.data.repo.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Profile extras: titles, achievements, the weekly class stats and the avatar shop. */
data class ProfileState(
    val titles: Set<String> = emptySet(),
    val achievements: Set<String> = emptySet(),
    val weeklyTop: Set<Stat> = emptySet(),
    val owned: Set<String> = emptySet(),
    val equipped: Set<String> = emptySet()
)

class ProfileViewModel(
    private val game: GameRepository,
    private val achievements: AchievementRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    /** Reloads everything. Call when the Hunter tab opens and after each shop action. */
    fun refresh(topStats: Set<Stat>) {
        viewModelScope.launch {
            val facts = achievements.facts()
            val weekly = game.weeklyTopStats(topStats)
            _state.update {
                it.copy(
                    titles = TitleRules.titles(facts),
                    achievements = TitleRules.achievements(facts),
                    weeklyTop = weekly,
                    owned = game.ownedAvatarKeys(),
                    equipped = game.equippedAvatarKeys()
                )
            }
        }
    }

    fun buy(item: ShopItem, topStats: Set<Stat>, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = game.buyAvatarItem(item)
            refreshShop()
            onResult(ok)
        }
    }

    fun toggleEquip(item: ShopItem) {
        viewModelScope.launch {
            game.toggleEquip(item)
            refreshShop()
        }
    }

    private suspend fun refreshShop() {
        _state.update { it.copy(owned = game.ownedAvatarKeys(), equipped = game.equippedAvatarKeys()) }
    }

    companion object {
        fun factory(app: ShadowApp): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                ProfileViewModel(app.container.game, app.container.achievements) as T
        }
    }
}

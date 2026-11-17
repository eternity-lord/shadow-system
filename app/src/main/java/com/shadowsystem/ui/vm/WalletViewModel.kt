package com.shadowsystem.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.shadowsystem.ShadowApp
import com.shadowsystem.core.ChequeRules
import com.shadowsystem.core.Jalali
import com.shadowsystem.core.MoneyRules
import com.shadowsystem.core.PersianText
import com.shadowsystem.data.db.ChequeEntity
import com.shadowsystem.data.db.WalletEntity
import com.shadowsystem.data.prefs.AppPrefs
import com.shadowsystem.data.repo.GameRepository
import com.shadowsystem.data.repo.WalletRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class WalletState(
    val wallets: List<WalletEntity> = emptyList(),
    val balances: Map<Long, Long> = emptyMap(),
    val cheques: List<ChequeEntity> = emptyList(),
    val hidden: Boolean = false,
    val today: Long = 0L
)

/** Wallets, transfers and cheques. None of these actions give XP or Gold. */
class WalletViewModel(
    private val wallets: WalletRepository,
    private val game: GameRepository,
    private val prefs: AppPrefs
) : ViewModel() {

    val state: StateFlow<WalletState> = combine(
        wallets.observeWallets(),
        wallets.observeBalances(),
        wallets.observeCheques(),
        prefs.hidden
    ) { ws, b, c, h -> WalletState(ws, b, c, h, game.today()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, WalletState())

    init {
        viewModelScope.launch { wallets.ensureDefault() }
    }

    /** Returns false when the input is invalid. */
    fun addWallet(name: String, openingText: String, onDone: (Boolean) -> Unit) {
        val opening = PersianText.parseLong(openingText.ifBlank { "0" }) ?: run { onDone(false); return }
        viewModelScope.launch { onDone(wallets.addWallet(name, MoneyRules.tomanToRial(opening))) }
    }

    fun transfer(from: Long?, to: Long?, amountText: String, onDone: (Boolean) -> Unit) {
        val toman = PersianText.parseLong(amountText)
        if (from == null || to == null || !MoneyRules.isValidAmount(toman)) { onDone(false); return }
        viewModelScope.launch { onDone(wallets.transfer(from, to, MoneyRules.tomanToRial(toman!!))) }
    }

    fun addCheque(title: String, amountText: String, due: Jalali.Date, issuedByMe: Boolean, onDone: (Boolean) -> Unit) {
        val toman = PersianText.parseLong(amountText)
        if (!MoneyRules.isValidAmount(toman)) { onDone(false); return }
        val dueDay = Jalali.toGregorian(due.year, due.month, due.day).toEpochDay()
        viewModelScope.launch {
            onDone(wallets.addCheque(title, MoneyRules.tomanToRial(toman!!), dueDay, issuedByMe))
        }
    }

    fun setCleared(id: Long, cleared: Boolean) {
        viewModelScope.launch { wallets.setChequeCleared(id, cleared) }
    }

    fun deleteCheque(id: Long) {
        viewModelScope.launch { wallets.deleteCheque(id) }
    }

    /** Status label key for a cheque, computed from the game day. */
    fun statusOf(cheque: ChequeEntity, today: Long): ChequeRules.Status =
        ChequeRules.status(cheque.dueEpochDay, today, cheque.cleared)

    fun todayJalali(today: Long): Jalali.Date = Jalali.fromGregorian(LocalDate.ofEpochDay(today))

    companion object {
        fun factory(app: ShadowApp): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                WalletViewModel(app.container.wallets, app.container.game, app.container.prefs) as T
        }
    }
}

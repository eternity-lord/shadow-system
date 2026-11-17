package com.shadowsystem.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.shadowsystem.ShadowApp
import com.shadowsystem.data.backup.BackupService
import com.shadowsystem.data.prefs.AppPrefs
import com.shadowsystem.data.prefs.LockState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SettingsNotice {
    PIN_SAVED, PIN_INVALID, PIN_WRONG, PIN_REMOVED, PASSWORD_SHORT, BACKUP_READY, RESTORE_DONE, RESTORE_FAILED, DEBUG_SEEDED
}

/** Privacy, PIN lock, encrypted backup and CSV export. */
class SettingsViewModel(
    private val prefs: AppPrefs,
    private val lock: LockState,
    private val backup: BackupService,
    private val onRemindersChanged: (Boolean) -> Unit,
    private val seedDebug: suspend () -> Unit
) : ViewModel() {

    /** Hunter profile from onboarding, editable here. */
    val hunterName = MutableStateFlow(prefs.hunterName())
    val avatarBase = MutableStateFlow(prefs.avatarBase())
    val wakeHour = MutableStateFlow(prefs.wakeHour())

    fun setHunterName(value: String) {
        prefs.setHunterName(value)
        hunterName.value = prefs.hunterName()
    }

    fun setAvatarBase(value: Int) {
        prefs.setAvatarBase(value)
        avatarBase.value = value
    }

    /** Changing the wake hour moves the morning reminder right away when reminders are on. */
    fun setWakeHour(hour: Int) {
        prefs.setWakeHour(hour)
        wakeHour.value = hour
        if (prefs.reminders.value) onRemindersChanged(true)
    }

    /** Debug builds only: fills the database with 60 days of test data. */
    fun seedDebugData() {
        viewModelScope.launch {
            seedDebug()
            notify(SettingsNotice.DEBUG_SEEDED)
        }
    }

    val reminders: StateFlow<Boolean> = prefs.reminders

    /** Called after the notification permission has been granted or the toggle is turned off. */
    fun setReminders(on: Boolean) {
        prefs.setReminders(on)
        onRemindersChanged(on)
    }

    val quietStart: StateFlow<Int> = prefs.quietStart
    val quietEnd: StateFlow<Int> = prefs.quietEnd

    fun shiftQuietStart(delta: Int) = prefs.shiftQuietStart(delta)

    fun shiftQuietEnd(delta: Int) = prefs.shiftQuietEnd(delta)

    private val _pinSet = MutableStateFlow(prefs.isPinSet)
    val pinSet: StateFlow<Boolean> = _pinSet.asStateFlow()
    val hidden: StateFlow<Boolean> = prefs.hidden

    private val _notice = MutableStateFlow<SettingsNotice?>(null)
    val notice: StateFlow<SettingsNotice?> = _notice.asStateFlow()

    val reduceMotion: StateFlow<Boolean> = prefs.reduceMotion

    fun toggleReduceMotion() {
        prefs.setReduceMotion(!prefs.reduceMotion.value)
    }

    fun toggleHidden() {
        prefs.setHideBalances(!prefs.hidden.value)
    }

    fun setPin(pin: String) {
        if (!isValidPin(pin)) return notify(SettingsNotice.PIN_INVALID)
        prefs.setPin(pin)
        _pinSet.value = true
        notify(SettingsNotice.PIN_SAVED)
    }

    fun removePin(pin: String) {
        if (!prefs.checkPin(pin)) return notify(SettingsNotice.PIN_WRONG)
        prefs.clearPin()
        _pinSet.value = false
        lock.locked.value = false
        notify(SettingsNotice.PIN_REMOVED)
    }

    fun exportBackup(password: String, onReady: (ByteArray) -> Unit) {
        if (password.length < MIN_PASSWORD) return notify(SettingsNotice.PASSWORD_SHORT)
        viewModelScope.launch {
            onReady(backup.exportEncrypted(password.toCharArray()))
            notify(SettingsNotice.BACKUP_READY)
        }
    }

    fun exportCsv(onReady: (String) -> Unit) {
        viewModelScope.launch { onReady(backup.exportCsv()) }
    }

    fun restore(bytes: ByteArray, password: String) {
        if (password.length < MIN_PASSWORD) return notify(SettingsNotice.PASSWORD_SHORT)
        viewModelScope.launch {
            try {
                backup.restore(bytes, password.toCharArray())
                notify(SettingsNotice.RESTORE_DONE)
            } catch (e: Exception) {
                notify(SettingsNotice.RESTORE_FAILED)
            }
        }
    }

    fun clearNotice() {
        _notice.value = null
    }

    private fun notify(n: SettingsNotice) {
        _notice.value = n
    }

    companion object {
        const val MIN_PASSWORD = 6

        fun isValidPin(pin: String): Boolean = pin.length == 4 && pin.all { it in '0'..'9' }

        fun factory(app: ShadowApp): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                SettingsViewModel(
                    app.container.prefs,
                    app.container.lock,
                    app.container.backup,
                    { on -> com.shadowsystem.util.Reminders.apply(app, on) },
                    { com.shadowsystem.debug.DebugSeed.seed(app.container.database) }
                ) as T
        }
    }
}

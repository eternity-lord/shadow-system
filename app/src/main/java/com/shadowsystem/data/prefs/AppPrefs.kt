package com.shadowsystem.data.prefs

import android.content.Context
import com.shadowsystem.core.Crypto
import com.shadowsystem.core.QuietHours
import kotlinx.coroutines.flow.MutableStateFlow

/** Small device-local settings: balance privacy and the PIN lock. */
class AppPrefs(context: Context) {
    private val sp = context.getSharedPreferences("shadow_prefs", Context.MODE_PRIVATE)

    /** Shared so every screen reacts when balances are hidden or shown. */
    val hidden = MutableStateFlow(sp.getBoolean(KEY_HIDE, false))

    fun setHideBalances(value: Boolean) {
        sp.edit().putBoolean(KEY_HIDE, value).apply()
        hidden.value = value
    }

    /** Whether the daily morning and night reminders are enabled. */
    val reminders = MutableStateFlow(sp.getBoolean(KEY_REMINDERS, false))

    fun setReminders(value: Boolean) {
        sp.edit().putBoolean(KEY_REMINDERS, value).apply()
        reminders.value = value
    }

    /** Hunter name, avatar base and usual wake hour. Set during onboarding and kept on this device. */
    fun hunterName(): String = sp.getString(KEY_NAME, "") ?: ""

    fun setHunterName(value: String) {
        sp.edit().putString(KEY_NAME, value.trim().take(MAX_NAME)).apply()
    }

    fun avatarBase(): Int = sp.getInt(KEY_AVATAR_BASE, 0)

    fun setAvatarBase(value: Int) {
        sp.edit().putInt(KEY_AVATAR_BASE, value).apply()
    }

    /** Usual wake hour (0–23). The morning reminder fires at this hour. */
    fun wakeHour(): Int = sp.getInt(KEY_WAKE_HOUR, DEFAULT_WAKE_HOUR)

    fun setWakeHour(value: Int) {
        sp.edit().putInt(KEY_WAKE_HOUR, value).apply()
    }

    /** Quiet hours window (0–23). Reminders are skipped inside it. */
    val quietStart = MutableStateFlow(sp.getInt(KEY_QUIET_START, QuietHours.DEFAULT_START))
    val quietEnd = MutableStateFlow(sp.getInt(KEY_QUIET_END, QuietHours.DEFAULT_END))

    fun shiftQuietStart(delta: Int) {
        val v = QuietHours.shift(quietStart.value, delta)
        sp.edit().putInt(KEY_QUIET_START, v).apply()
        quietStart.value = v
    }

    fun shiftQuietEnd(delta: Int) {
        val v = QuietHours.shift(quietEnd.value, delta)
        sp.edit().putInt(KEY_QUIET_END, v).apply()
        quietEnd.value = v
    }

    /** Calmer UI: short or no transitions for overlays and the soldier animation. */
    /** Show money in rial instead of toman (spec section 8). */
    val rialMode = MutableStateFlow(sp.getBoolean(KEY_RIAL_MODE, false))

    fun setRialMode(value: Boolean) {
        sp.edit().putBoolean(KEY_RIAL_MODE, value).apply()
        rialMode.value = value
        com.shadowsystem.core.PersianText.rialMode = value
    }

    /** The wallet the user last picked on the money form; null if none was picked yet. */
    fun lastWallet(): Long? = sp.getLong(KEY_LAST_WALLET, -1L).takeIf { it >= 0 }

    fun setLastWallet(id: Long) {
        sp.edit().putLong(KEY_LAST_WALLET, id).apply()
    }

    val reduceMotion = MutableStateFlow(sp.getBoolean(KEY_REDUCE_MOTION, false))

    fun setReduceMotion(value: Boolean) {
        sp.edit().putBoolean(KEY_REDUCE_MOTION, value).apply()
        reduceMotion.value = value
    }

    val isPinSet: Boolean
        get() = sp.contains(KEY_PIN_HASH)

    fun setPin(pin: String) {
        val salt = Crypto.newSalt()
        sp.edit()
            .putString(KEY_PIN_SALT, Crypto.toHex(salt))
            .putString(KEY_PIN_HASH, Crypto.hashPin(pin, salt))
            .apply()
    }

    fun checkPin(pin: String): Boolean {
        val salt = sp.getString(KEY_PIN_SALT, null) ?: return false
        val hash = sp.getString(KEY_PIN_HASH, null) ?: return false
        return Crypto.hashPin(pin, Crypto.fromHex(salt)) == hash
    }

    fun clearPin() {
        sp.edit().remove(KEY_PIN_HASH).remove(KEY_PIN_SALT).apply()
    }

    companion object {
        const val KEY_RIAL_MODE = "money_rial_mode"
        const val KEY_LAST_WALLET = "money_last_wallet"
        const val KEY_NAME = "hunter_name"
        const val KEY_AVATAR_BASE = "avatar_base"
        const val KEY_WAKE_HOUR = "wake_hour"
        const val DEFAULT_WAKE_HOUR = 8
        const val MAX_NAME = 20
        const val KEY_HIDE = "hide_balances"
        const val KEY_REDUCE_MOTION = "reduce_motion"
        const val KEY_REMINDERS = "reminders_on"
        const val KEY_QUIET_START = "quiet_start"
        const val KEY_QUIET_END = "quiet_end"
        const val KEY_PIN_HASH = "pin_hash"
        const val KEY_PIN_SALT = "pin_salt"
    }
}

/** Whether the app is currently behind the PIN lock. */
class LockState(initiallyLocked: Boolean) {
    val locked = MutableStateFlow(initiallyLocked)
}

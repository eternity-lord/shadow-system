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

    private companion object {
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

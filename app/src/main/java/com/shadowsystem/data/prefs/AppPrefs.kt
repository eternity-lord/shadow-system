package com.shadowsystem.data.prefs

import android.content.Context
import com.shadowsystem.core.Crypto
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
        const val KEY_PIN_HASH = "pin_hash"
        const val KEY_PIN_SALT = "pin_salt"
    }
}

/** Whether the app is currently behind the PIN lock. */
class LockState(initiallyLocked: Boolean) {
    val locked = MutableStateFlow(initiallyLocked)
}

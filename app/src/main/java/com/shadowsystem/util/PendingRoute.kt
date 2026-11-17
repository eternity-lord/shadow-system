package com.shadowsystem.util

import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Screen a reminder tap should open. Set from the notification intent, then consumed by the main navigation
 * once it is ready (so it also works after the PIN lock or when the app is already open).
 */
object PendingRoute {
    const val EXTRA = "open_route"
    const val NOW = "now"
    const val REPORT = "daily_report"

    val route = MutableStateFlow<String?>(null)

    /** Reads the target from an incoming intent, if it carries one. */
    fun from(intent: Intent?) {
        intent?.getStringExtra(EXTRA)?.let { route.value = it }
    }
}

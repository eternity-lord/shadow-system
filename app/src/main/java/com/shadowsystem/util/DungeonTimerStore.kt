package com.shadowsystem.util

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.shadowsystem.core.DungeonTimerMath
import com.shadowsystem.service.DungeonService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Persists the active dungeon run in SharedPreferences, so the timer survives
 * the app being closed or the process being killed. Starting a run also starts
 * the foreground service that shows the timer notification.
 */
class DungeonTimerStore(private val context: Context) {
    data class Snapshot(
        val rank: String,
        val plannedMinutes: Int,
        val startedAt: Long,
        val pausedTotal: Long,
        val pausedAt: Long?
    )

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val current = MutableStateFlow(load())
    val snapshot: StateFlow<Snapshot?> = current.asStateFlow()

    fun start(rank: String, plannedMinutes: Int, now: Long) {
        write(Snapshot(rank, plannedMinutes, now, 0L, null))
        startService()
    }

    fun pause(now: Long) {
        val s = current.value ?: return
        if (s.pausedAt != null) return
        write(s.copy(pausedAt = now))
    }

    fun resume(now: Long) {
        val s = current.value ?: return
        val at = s.pausedAt ?: return
        write(s.copy(pausedTotal = s.pausedTotal + (now - at).coerceAtLeast(0L), pausedAt = null))
    }

    /** Ends the run. The service sees the null state and stops itself. */
    fun clear() = write(null)

    /** Restarts the notification service after the process was killed mid-run. */
    fun ensureService() {
        if (current.value != null) startService()
    }

    private fun startService() {
        try {
            ContextCompat.startForegroundService(context, Intent(context, DungeonService::class.java))
        } catch (e: RuntimeException) {
            // Background start refused by the system. The timer still runs; the notification appears next time the app is opened.
        }
    }

    private fun write(s: Snapshot?) {
        if (s == null) {
            prefs.edit().clear().apply()
        } else {
            prefs.edit()
                .putString(K_RANK, s.rank)
                .putInt(K_MINUTES, s.plannedMinutes)
                .putLong(K_STARTED, s.startedAt)
                .putLong(K_PAUSED_TOTAL, s.pausedTotal)
                .putLong(K_PAUSED_AT, s.pausedAt ?: NONE)
                .apply()
        }
        current.value = s
    }

    private fun load(): Snapshot? {
        if (!prefs.contains(K_RANK)) return null
        val pausedAt = prefs.getLong(K_PAUSED_AT, NONE)
        return Snapshot(
            rank = prefs.getString(K_RANK, "E") ?: "E",
            plannedMinutes = prefs.getInt(K_MINUTES, 15),
            startedAt = prefs.getLong(K_STARTED, 0L),
            pausedTotal = prefs.getLong(K_PAUSED_TOTAL, 0L),
            pausedAt = pausedAt.takeIf { it != NONE }
        )
    }

    companion object {
        private const val PREFS = "dungeon_timer"
        private const val K_RANK = "rank"
        private const val K_MINUTES = "minutes"
        private const val K_STARTED = "started"
        private const val K_PAUSED_TOTAL = "paused_total"
        private const val K_PAUSED_AT = "paused_at"
        private const val NONE = -1L

        /** Elapsed work seconds for a snapshot at [now]. */
        fun elapsedSeconds(s: Snapshot, now: Long): Int =
            DungeonTimerMath.elapsedSeconds(s.startedAt, s.pausedTotal, s.pausedAt, now)
    }
}

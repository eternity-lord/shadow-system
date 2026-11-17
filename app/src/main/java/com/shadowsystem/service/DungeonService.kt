package com.shadowsystem.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.shadowsystem.R
import com.shadowsystem.ShadowApp
import com.shadowsystem.core.PersianText
import com.shadowsystem.util.DungeonTimerStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Foreground service that keeps the dungeon timer visible in the notification shade.
 * It stops itself when the run ends (the store becomes null).
 */
class DungeonService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var loop: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ensureChannel()
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(getString(R.string.dungeon_notif_title)),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )
        if (loop == null) loop = scope.launch { watch() }
        return START_NOT_STICKY
    }

    private suspend fun watch() {
        val store = (application as ShadowApp).container.timer
        while (scope.isActive) {
            val snap = store.snapshot.value
            if (snap == null) {
                ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
                stopSelf()
                return
            }
            updateNotification(textFor(store, snap))
            delay(1000)
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateNotification(text: String) {
        val manager = NotificationManagerCompat.from(this)
        if (manager.areNotificationsEnabled()) manager.notify(NOTIFICATION_ID, buildNotification(text))
    }

    private fun textFor(store: DungeonTimerStore, snap: DungeonTimerStore.Snapshot): String {
        if (snap.pausedAt != null) return getString(R.string.dungeon_notif_paused)
        val elapsed = DungeonTimerStore.elapsedSeconds(snap, System.currentTimeMillis())
        val planned = snap.plannedMinutes * 60
        if (elapsed >= planned) return getString(R.string.dungeon_notif_done)
        val left = planned - elapsed
        val clock = String.format("%02d:%02d", left / 60, left % 60)
        return getString(R.string.dungeon_notif_running, PersianText.toFa(clock))
    }

    private fun buildNotification(text: String): Notification {
        val open = packageManager.getLaunchIntentForPackage(packageName) ?: Intent()
        val pending = android.app.PendingIntent.getActivity(
            this, 0, open, android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(getString(R.string.dungeon_notif_title))
            .setContentText(text)
            .setContentIntent(pending)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .build()
    }

    private fun ensureChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, getString(R.string.dungeon_channel), NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "dungeon_timer"
        const val NOTIFICATION_ID = 4101
    }
}

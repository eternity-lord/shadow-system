package com.shadowsystem.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

/**
 * Daily morning and night reminders. Uses inexact repeating alarms (no exact-alarm permission).
 * Alarms do not survive reboot, so [apply] is also called from app start when reminders are on.
 */
object Reminders {
    const val CHANNEL_ID = "daily_reminders"
    const val EXTRA_KIND = "reminder_kind"
    const val KIND_MORNING = 0
    const val KIND_NIGHT = 1
    private const val NIGHT_HOUR = 21

    /** Enables or disables both reminders. */
    fun apply(context: Context, enabled: Boolean) {
        val alarms = context.getSystemService(AlarmManager::class.java) ?: return
        if (!enabled) {
            alarms.cancel(pendingIntent(context, KIND_MORNING))
            alarms.cancel(pendingIntent(context, KIND_NIGHT))
            return
        }
        ensureChannel(context)
        schedule(context, alarms, KIND_MORNING, morningHour(context))
        schedule(context, alarms, KIND_NIGHT, NIGHT_HOUR)
    }

    /** The morning reminder fires at the wake hour chosen in onboarding (default 08:00). */
    private fun morningHour(context: Context): Int =
        context.getSharedPreferences("shadow_prefs", Context.MODE_PRIVATE)
            .getInt(com.shadowsystem.data.prefs.AppPrefs.KEY_WAKE_HOUR, com.shadowsystem.data.prefs.AppPrefs.DEFAULT_WAKE_HOUR)

    private fun schedule(context: Context, alarms: AlarmManager, kind: Int, hour: Int) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        alarms.setInexactRepeating(
            AlarmManager.RTC, cal.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent(context, kind)
        )
    }

    private fun pendingIntent(context: Context, kind: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).putExtra(EXTRA_KIND, kind)
        return PendingIntent.getBroadcast(
            context, kind, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID, context.getString(com.shadowsystem.R.string.reminder_channel), NotificationManager.IMPORTANCE_DEFAULT
        )
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }
}

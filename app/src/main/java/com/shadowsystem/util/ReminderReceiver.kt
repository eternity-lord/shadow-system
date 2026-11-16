package com.shadowsystem.util

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.shadowsystem.MainActivity
import com.shadowsystem.R
import com.shadowsystem.ShadowApp
import com.shadowsystem.core.PersianText
import com.shadowsystem.core.QuietHours
import com.shadowsystem.data.repo.DayReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Shows the morning briefing or night report. The text uses today's real numbers when they can be read;
 * otherwise it falls back to the plain text. Silently skipped when notifications are not allowed.
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = (context.applicationContext as ShadowApp).container.prefs
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        if (QuietHours.isQuiet(hour, prefs.quietStart.value, prefs.quietEnd.value)) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val morning = intent.getIntExtra(Reminders.EXTRA_KIND, Reminders.KIND_MORNING) == Reminders.KIND_MORNING
        val pending = goAsync()
        val app = context.applicationContext as ShadowApp
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val game = app.container.game
                val report = game.dailyReport(game.today())
                val (title, text) = textFor(context, morning, report)
                show(context, morning, title, text)
            } finally {
                pending.finish()
            }
        }
    }

    private fun textFor(context: Context, morning: Boolean, r: DayReport): Pair<String, String> {
        val title = context.getString(if (morning) R.string.reminder_morning_title else R.string.reminder_night_title)
        val text = if (morning) {
            val left = (r.questsTotal - r.questsDone).coerceAtLeast(0)
            if (r.questsTotal == 0) context.getString(R.string.reminder_morning_text)
            else context.getString(R.string.reminder_morning_stats, PersianText.toFa(left), PersianText.toFa(r.questsTotal))
        } else {
            val score = r.score
            if (score == null) context.getString(R.string.reminder_night_text)
            else context.getString(R.string.reminder_night_score, PersianText.toFa(score), PersianText.toFa(r.questsDone))
        }
        return title to text
    }

    private fun show(context: Context, morning: Boolean, title: String, text: String) {
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification: Notification = NotificationCompat.Builder(context, Reminders.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(if (morning) 1 else 2, notification)
    }
}

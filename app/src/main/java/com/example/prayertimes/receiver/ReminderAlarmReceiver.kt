package com.example.prayertimes.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.prayertimes.utils.NotificationHelper

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra("reminder_type") ?: return

        when (type) {
            "early" -> {
                val prayerName = intent.getStringExtra("prayer_name") ?: "Prayer"
                val prayerTime = intent.getStringExtra("prayer_time") ?: ""
                val mins = intent.getIntExtra("early_mins", 15)
                NotificationHelper.showReminderNotification(
                    context = context,
                    title = "Upcoming Prayer: $prayerName",
                    text = "$prayerName is starting in $mins minutes at $prayerTime.",
                    channelId = NotificationHelper.CHANNEL_ID_REMINDERS,
                    notificationId = NotificationHelper.NOTIFICATION_BASE_ID + prayerName.hashCode() + 1000
                )
            }
            "sehri" -> {
                val fajrTime = intent.getStringExtra("fajr_time") ?: ""
                NotificationHelper.showReminderNotification(
                    context = context,
                    title = "Sehri Ending Soon",
                    text = "Fajr begins at $fajrTime. Please wrap up your Sehri.",
                    channelId = NotificationHelper.CHANNEL_ID_SEHRI,
                    notificationId = NotificationHelper.NOTIFICATION_BASE_ID + 2000
                )
            }
            "jummah" -> {
                val dhuhrTime = intent.getStringExtra("dhuhr_time") ?: ""
                NotificationHelper.showReminderNotification(
                    context = context,
                    title = "Jummah Reminder",
                    text = "It's Friday! Prepare for Jummah prayer at $dhuhrTime. Don't forget to read Surah Al-Kahf.",
                    channelId = NotificationHelper.CHANNEL_ID_REMINDERS,
                    notificationId = NotificationHelper.NOTIFICATION_BASE_ID + 3000
                )
            }
            "kahf" -> {
                val title = intent.getStringExtra("kahf_title") ?: "Surah Al-Kahf Reminder"
                val msg = intent.getStringExtra("kahf_msg") ?: "Don't forget to read Surah Al-Kahf."
                NotificationHelper.showReminderNotification(
                    context = context,
                    title = title,
                    text = msg,
                    channelId = NotificationHelper.CHANNEL_ID_KAHF,
                    notificationId = NotificationHelper.NOTIFICATION_BASE_ID + 4000
                )
            }
            "hadith" -> {
                // To keep it simple, we just show a daily generic reminder to read the Hadith,
                // or if we had a specific Hadith fetched, we would show it.
                // Since this runs exactly on time, we just remind the user to open the app.
                NotificationHelper.showReminderNotification(
                    context = context,
                    title = "Daily Hadith",
                    text = "Tap to read your daily Hadith and reflect on the Prophet's (PBUH) teachings.",
                    channelId = NotificationHelper.CHANNEL_ID_HADITH,
                    notificationId = NotificationHelper.NOTIFICATION_BASE_ID + 5000
                )
            }
        }
    }
}

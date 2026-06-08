package com.example.prayertimes.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.example.prayertimes.MainActivity
import com.example.prayertimes.R

/**
 * Helper for creating and posting prayer time notifications.
 */
object NotificationHelper {

    const val CHANNEL_ID_MAKKAH = "prayer_times_makkah_v2"
    const val CHANNEL_ID_MADINAH = "prayer_times_madinah_v2"
    const val CHANNEL_ID_SHORT_BEEP = "prayer_times_short_beep_v2"
    const val CHANNEL_ID_SILENT = "prayer_times_silent_channel_v2"
    
    // Batch 2 Reminder Channels
    const val CHANNEL_ID_REMINDERS = "prayer_reminders"
    const val CHANNEL_ID_KAHF = "kahf_reminders"
    const val CHANNEL_ID_HADITH = "hadith_reminders"
    const val CHANNEL_ID_SEHRI = "sehri_reminders"
    private const val NOTIFICATION_BASE_ID = 1000

    /**
     * Creates notification channels for prayer alarms.
     * Must be called during app initialization.
     */
    fun createNotificationChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        // Makkah channel
        val makkahChannel = NotificationChannel(CHANNEL_ID_MAKKAH, "Prayer Time Alerts (Makkah)", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Notifications for prayer times with Makkah Azan"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500)
            setSound(null, null)
        }

        // Madinah channel
        val madinahChannel = NotificationChannel(CHANNEL_ID_MADINAH, "Prayer Time Alerts (Madinah)", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Notifications for prayer times with Madinah Azan"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500)
            setSound(null, null)
        }

        // Short beep channel
        val beepChannel = NotificationChannel(CHANNEL_ID_SHORT_BEEP, "Prayer Time Alerts (Short Beep)", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Notifications for prayer times with a short beep"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500)
            setSound(null, null)
        }

        // Silent channel
        val silentChannel = NotificationChannel(CHANNEL_ID_SILENT, "Prayer Time Alerts (Silent)", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Silent notifications for prayer times"
            setSound(null, null)
            enableVibration(true)
        }

        manager.createNotificationChannel(makkahChannel)
        manager.createNotificationChannel(madinahChannel)
        manager.createNotificationChannel(beepChannel)
        manager.createNotificationChannel(silentChannel)

        // Batch 2 Channels
        val reminderChannel = NotificationChannel(CHANNEL_ID_REMINDERS, "Early Prayer Reminders", NotificationManager.IMPORTANCE_DEFAULT)
        val kahfChannel = NotificationChannel(CHANNEL_ID_KAHF, "Surah Al-Kahf Reminders", NotificationManager.IMPORTANCE_DEFAULT)
        val hadithChannel = NotificationChannel(CHANNEL_ID_HADITH, "Daily Hadith", NotificationManager.IMPORTANCE_DEFAULT)
        
        val sehriChannel = NotificationChannel(CHANNEL_ID_SEHRI, "Sehri Warning", NotificationManager.IMPORTANCE_HIGH).apply {
            val alarmUri = android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
            val attrs = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
            setSound(alarmUri, attrs)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 500, 500, 500, 500)
        }

        manager.createNotificationChannel(reminderChannel)
        manager.createNotificationChannel(kahfChannel)
        manager.createNotificationChannel(hadithChannel)
        manager.createNotificationChannel(sehriChannel)
    }

    /**
     * Posts a prayer time notification.
     */
    fun showPrayerNotification(
        context: Context,
        prayerName: String,
        prayerTime: String,
        azanSound: String
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = when (azanSound) {
            "Makkah", "Fajr Special" -> CHANNEL_ID_MAKKAH
            "Madinah" -> CHANNEL_ID_MADINAH
            "Short Beep" -> CHANNEL_ID_SHORT_BEEP
            else -> CHANNEL_ID_SILENT
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_mosque)
            .setContentTitle(prayerName)
            .setContentText("It's time for $prayerName prayer ($prayerTime)")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("It's time for $prayerName prayer.\nScheduled time: $prayerTime")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()

        val notificationId = NOTIFICATION_BASE_ID + prayerName.hashCode()
        manager.notify(notificationId, notification)
    }

    /**
     * Posts a generic reminder notification.
     */
    fun showReminderNotification(
        context: Context,
        title: String,
        text: String,
        channelId: String,
        notificationId: Int
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_mosque)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(if (channelId == CHANNEL_ID_SEHRI) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(notificationId, notification)
    }
}

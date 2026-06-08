package com.example.prayertimes.utils

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.prayertimes.data.model.Prayer
import com.example.prayertimes.worker.PrayerAlarmWorker
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.concurrent.TimeUnit
import java.util.Calendar
import com.example.prayertimes.data.model.AppSettings

/**
 * Schedules prayer alarm notifications using WorkManager for reliability.
 */
object PrayerAlarmScheduler {

    private const val WORK_TAG = "prayer_alarm"

    /**
     * Schedules notifications for all enabled prayers.
     * Cancels any existing scheduled alarms first.
     *
     * @param context Application context
     * @param prayerTimes Map of Prayer to their Instant times
     * @param enabledPrayers Set of prayers that have notifications enabled
     * @param azanSound The selected Azan sound name
     * @param latitude User latitude for recalculation after alarm
     * @param longitude User longitude for recalculation after alarm
     */
    fun schedulePrayerAlarms(
        context: Context,
        prayerTimes: Map<Prayer, Pair<Instant, String>>,
        enabledPrayers: Set<Prayer>,
        settings: AppSettings
    ) {
        val workManager = WorkManager.getInstance(context)

        // Cancel all existing prayer alarms
        workManager.cancelAllWorkByTag(WORK_TAG)

        val now = System.currentTimeMillis()

        for ((prayer, timeInfo) in prayerTimes) {
            // Skip sunrise — no notification for sunrise
            if (prayer == Prayer.SUNRISE) continue

            // Skip disabled prayers
            if (prayer !in enabledPrayers) continue

            val (instant, formattedTime) = timeInfo
            val prayerTimeMillis = instant.toEpochMilliseconds()
            val delay = prayerTimeMillis - now

            // Only schedule future prayers
            if (delay <= 0) continue

            val soundToUse = when (prayer) {
                Prayer.FAJR -> settings.fajrSound
                Prayer.DHUHR -> settings.dhuhrSound
                Prayer.ASR -> settings.asrSound
                Prayer.MAGHRIB -> settings.maghribSound
                Prayer.ISHA -> settings.ishaSound
                else -> "Makkah" // Default fallback
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = android.content.Intent(context, com.example.prayertimes.receiver.PrayerAlarmReceiver::class.java).apply {
                putExtra("prayer_name", prayer.displayName)
                putExtra("prayer_time", formattedTime)
                putExtra("azan_sound", soundToUse)
            }
            val pendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                prayer.ordinal, // unique ID per prayer
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            
            // Cancel any existing alarm
            alarmManager.cancel(pendingIntent)
            
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, prayerTimeMillis, pendingIntent)
                } else {
                    alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, prayerTimeMillis, pendingIntent)
                }
            } catch (e: SecurityException) {
                alarmManager.setAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, prayerTimeMillis, pendingIntent)
            }

            // Early reminder
            if (settings.earlyReminderMins > 0) {
                val earlyDelay = delay - (settings.earlyReminderMins * 60 * 1000L)
                if (earlyDelay > 0) {
                    val earlyData = Data.Builder()
                        .putString("prayer_name", prayer.displayName)
                        .putString("prayer_time", formattedTime)
                        .putInt("early_mins", settings.earlyReminderMins)
                        .build()
                    val earlyRequest = OneTimeWorkRequestBuilder<com.example.prayertimes.worker.EarlyReminderWorker>()
                        .setInitialDelay(earlyDelay, TimeUnit.MILLISECONDS)
                        .setInputData(earlyData)
                        .addTag(WORK_TAG)
                        .build()
                    workManager.enqueueUniqueWork(
                        "early_reminder_${prayer.name}",
                        ExistingWorkPolicy.REPLACE,
                        earlyRequest
                    )
                }
            }
            
            // Jummah reminder
            if (prayer == Prayer.DHUHR && settings.jummahReminder) {
                val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                if (localDate.dayOfWeek == kotlinx.datetime.DayOfWeek.FRIDAY) {
                    val jummahDelay = delay - (60 * 60 * 1000L)
                    if (jummahDelay > 0) {
                        val jummahData = Data.Builder()
                            .putString("dhuhr_time", formattedTime)
                            .build()
                        val jummahRequest = OneTimeWorkRequestBuilder<com.example.prayertimes.worker.JummahReminderWorker>()
                            .setInitialDelay(jummahDelay, TimeUnit.MILLISECONDS)
                            .setInputData(jummahData)
                            .addTag(WORK_TAG)
                            .build()
                        workManager.enqueueUniqueWork(
                            "jummah_reminder",
                            ExistingWorkPolicy.REPLACE,
                            jummahRequest
                        )
                    }
                }
            }
            
            // Sehri Warning
            if (prayer == Prayer.FAJR && settings.ramadanMode) {
                val sehriDelay = delay - (20 * 60 * 1000L)
                if (sehriDelay > 0) {
                    val sehriData = Data.Builder()
                        .putString("fajr_time", formattedTime)
                        .build()
                    val sehriRequest = OneTimeWorkRequestBuilder<com.example.prayertimes.worker.SehriWarningWorker>()
                        .setInitialDelay(sehriDelay, TimeUnit.MILLISECONDS)
                        .setInputData(sehriData)
                        .addTag(WORK_TAG)
                        .build()
                    workManager.enqueueUniqueWork(
                        "sehri_warning",
                        ExistingWorkPolicy.REPLACE,
                        sehriRequest
                    )
                }
            }
        }
        
        // Surah Al-Kahf Reminder
        if (settings.kahfReminder) {
            scheduleKahfReminders(context)
        } else {
            workManager.cancelUniqueWork("kahf_thursday")
            workManager.cancelUniqueWork("kahf_friday")
        }
        
        // Daily Hadith Notification
        if (settings.hadithNotificationEnabled) {
            scheduleDailyHadith(context, settings.hadithNotificationTime)
        } else {
            workManager.cancelUniqueWork("daily_hadith")
        }
    }
    
    private fun scheduleKahfReminders(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val now = Calendar.getInstance()
        
        // Thursday 20:00
        val thursCal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(now)) add(Calendar.WEEK_OF_YEAR, 1)
        }
        val thursDelay = thursCal.timeInMillis - now.timeInMillis
        val thursRequest = OneTimeWorkRequestBuilder<com.example.prayertimes.worker.KahfThursdayWorker>()
            .setInitialDelay(thursDelay, TimeUnit.MILLISECONDS)
            .addTag(WORK_TAG)
            .build()
        workManager.enqueueUniqueWork("kahf_thursday", ExistingWorkPolicy.REPLACE, thursRequest)
        
        // Friday 08:00
        val friCal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(now)) add(Calendar.WEEK_OF_YEAR, 1)
        }
        val friDelay = friCal.timeInMillis - now.timeInMillis
        val friRequest = OneTimeWorkRequestBuilder<com.example.prayertimes.worker.KahfFridayWorker>()
            .setInitialDelay(friDelay, TimeUnit.MILLISECONDS)
            .addTag(WORK_TAG)
            .build()
        workManager.enqueueUniqueWork("kahf_friday", ExistingWorkPolicy.REPLACE, friRequest)
    }
    
    private fun scheduleDailyHadith(context: Context, timeStr: String) {
        val workManager = WorkManager.getInstance(context)
        val parts = timeStr.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 7
        val min = parts.getOrNull(1)?.toIntOrNull() ?: 0
        
        val now = Calendar.getInstance()
        val nextRun = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, min)
            set(Calendar.SECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        val delay = nextRun.timeInMillis - now.timeInMillis
        
        val request = OneTimeWorkRequestBuilder<com.example.prayertimes.worker.DailyHadithWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(WORK_TAG)
            .build()
        workManager.enqueueUniqueWork("daily_hadith", ExistingWorkPolicy.REPLACE, request)
    }

    /**
     * Cancels all scheduled prayer alarms.
     */
    fun cancelAllAlarms(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        for (prayer in Prayer.values()) {
            val intent = android.content.Intent(context, com.example.prayertimes.receiver.PrayerAlarmReceiver::class.java)
            val pendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                prayer.ordinal,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
}

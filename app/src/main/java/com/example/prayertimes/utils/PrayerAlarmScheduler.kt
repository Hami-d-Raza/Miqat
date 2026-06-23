package com.example.prayertimes.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.prayertimes.data.model.AppSettings
import com.example.prayertimes.data.model.Prayer
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Calendar

/**
 * Schedules prayer alarm notifications and reminders using AlarmManager.setAlarmClock for exact delivery.
 */
object PrayerAlarmScheduler {

    /**
     * Schedules notifications for all enabled prayers and reminders.
     * Cancels any existing scheduled alarms first.
     */
    fun schedulePrayerAlarms(
        context: Context,
        prayerTimes: Map<Prayer, Pair<Instant, String>>,
        enabledPrayers: Set<Prayer>,
        settings: AppSettings
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = System.currentTimeMillis()

        // 1. Schedule Prayers and their related reminders (Early, Sehri, Jummah)
        for ((prayer, timeInfo) in prayerTimes) {
            val (instant, formattedTime) = timeInfo
            val prayerTimeMillis = instant.toEpochMilliseconds()
            val delay = prayerTimeMillis - now

            // Base intent for prayer
            val prayerIntent = Intent(context, com.example.prayertimes.receiver.PrayerAlarmReceiver::class.java).apply {
                putExtra("prayer_name", prayer.displayName)
                putExtra("prayer_time", formattedTime)
                val soundToUse = when (prayer) {
                    Prayer.FAJR -> settings.fajrSound
                    Prayer.DHUHR -> settings.dhuhrSound
                    Prayer.ASR -> settings.asrSound
                    Prayer.MAGHRIB -> settings.maghribSound
                    Prayer.ISHA -> settings.ishaSound
                    else -> "Makkah" // Default fallback
                }
                putExtra("azan_sound", soundToUse)
            }
            val prayerPendingIntent = PendingIntent.getBroadcast(
                context,
                prayer.ordinal, // unique ID per prayer
                prayerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Cancel any existing alarm for this prayer
            alarmManager.cancel(prayerPendingIntent)

            // Skip past prayers or disabled ones
            if (delay > 0 && prayer != Prayer.SUNRISE && prayer in enabledPrayers) {
                setExactAlarm(alarmManager, prayerTimeMillis, prayerPendingIntent)
            }

            // Early reminder
            val earlyIdOffset = 100
            val earlyPendingIntent = PendingIntent.getBroadcast(
                context,
                prayer.ordinal + earlyIdOffset,
                Intent(context, com.example.prayertimes.receiver.ReminderAlarmReceiver::class.java).apply {
                    putExtra("reminder_type", "early")
                    putExtra("prayer_name", prayer.displayName)
                    putExtra("prayer_time", formattedTime)
                    putExtra("early_mins", settings.earlyReminderMins)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(earlyPendingIntent)

            if (settings.earlyReminderMins > 0 && delay > (settings.earlyReminderMins * 60 * 1000L)) {
                val earlyTimeMillis = prayerTimeMillis - (settings.earlyReminderMins * 60 * 1000L)
                setExactAlarm(alarmManager, earlyTimeMillis, earlyPendingIntent)
            }

            // Sehri Warning
            if (prayer == Prayer.FAJR) {
                val sehriPendingIntent = PendingIntent.getBroadcast(
                    context,
                    200,
                    Intent(context, com.example.prayertimes.receiver.ReminderAlarmReceiver::class.java).apply {
                        putExtra("reminder_type", "sehri")
                        putExtra("fajr_time", formattedTime)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(sehriPendingIntent)

                if (settings.ramadanMode && delay > (20 * 60 * 1000L)) {
                    val sehriTimeMillis = prayerTimeMillis - (20 * 60 * 1000L)
                    setExactAlarm(alarmManager, sehriTimeMillis, sehriPendingIntent)
                }
            }

            // Jummah Reminder
            if (prayer == Prayer.DHUHR) {
                val jummahPendingIntent = PendingIntent.getBroadcast(
                    context,
                    300,
                    Intent(context, com.example.prayertimes.receiver.ReminderAlarmReceiver::class.java).apply {
                        putExtra("reminder_type", "jummah")
                        putExtra("dhuhr_time", formattedTime)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(jummahPendingIntent)

                if (settings.jummahReminder) {
                    val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                    if (localDate.dayOfWeek == kotlinx.datetime.DayOfWeek.FRIDAY && delay > (60 * 60 * 1000L)) {
                        val jummahTimeMillis = prayerTimeMillis - (60 * 60 * 1000L)
                        setExactAlarm(alarmManager, jummahTimeMillis, jummahPendingIntent)
                    }
                }
            }
        }

        // 2. Surah Al-Kahf Reminders
        val thursPendingIntent = PendingIntent.getBroadcast(
            context,
            400,
            Intent(context, com.example.prayertimes.receiver.ReminderAlarmReceiver::class.java).apply {
                putExtra("reminder_type", "kahf")
                putExtra("kahf_title", "Thursday Night Kahf")
                putExtra("kahf_msg", "Thursday night has begun. It's highly recommended to recite Surah Al-Kahf.")
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val friPendingIntent = PendingIntent.getBroadcast(
            context,
            401,
            Intent(context, com.example.prayertimes.receiver.ReminderAlarmReceiver::class.java).apply {
                putExtra("reminder_type", "kahf")
                putExtra("kahf_title", "Friday Kahf Reminder")
                putExtra("kahf_msg", "Don't forget to read Surah Al-Kahf before Maghrib.")
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(thursPendingIntent)
        alarmManager.cancel(friPendingIntent)

        if (settings.kahfReminder) {
            val thursCal = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
                set(Calendar.HOUR_OF_DAY, 20)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                if (timeInMillis <= now) add(Calendar.WEEK_OF_YEAR, 1)
            }
            setExactAlarm(alarmManager, thursCal.timeInMillis, thursPendingIntent)

            val friCal = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
                set(Calendar.HOUR_OF_DAY, 8)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                if (timeInMillis <= now) add(Calendar.WEEK_OF_YEAR, 1)
            }
            setExactAlarm(alarmManager, friCal.timeInMillis, friPendingIntent)
        }

        // 3. Daily Hadith Reminder
        val hadithPendingIntent = PendingIntent.getBroadcast(
            context,
            500,
            Intent(context, com.example.prayertimes.receiver.ReminderAlarmReceiver::class.java).apply {
                putExtra("reminder_type", "hadith")
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(hadithPendingIntent)

        if (settings.hadithNotificationEnabled) {
            val parts = settings.hadithNotificationTime.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 7
            val min = parts.getOrNull(1)?.toIntOrNull() ?: 0

            val hadithCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, min)
                set(Calendar.SECOND, 0)
                if (timeInMillis <= now) add(Calendar.DAY_OF_YEAR, 1)
            }
            setExactAlarm(alarmManager, hadithCal.timeInMillis, hadithPendingIntent)
        }
    }

    private fun setExactAlarm(alarmManager: AlarmManager, timeMillis: Long, pendingIntent: PendingIntent) {
        try {
            // setAlarmClock completely bypasses Doze mode and guarantees exact delivery.
            val alarmClockInfo = AlarmManager.AlarmClockInfo(timeMillis, pendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } catch (e: Exception) {
            // Fallback just in case
            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
            } catch (e2: SecurityException) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent)
            }
        }
    }

    /**
     * Cancels all scheduled prayer alarms.
     */
    fun cancelAllAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Cancel prayers and their early reminders
        for (prayer in Prayer.values()) {
            val prayerIntent = Intent(context, com.example.prayertimes.receiver.PrayerAlarmReceiver::class.java)
            val prayerPendingIntent = PendingIntent.getBroadcast(
                context,
                prayer.ordinal,
                prayerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(prayerPendingIntent)

            val earlyIntent = Intent(context, com.example.prayertimes.receiver.ReminderAlarmReceiver::class.java)
            val earlyPendingIntent = PendingIntent.getBroadcast(
                context,
                prayer.ordinal + 100,
                earlyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(earlyPendingIntent)
        }

        val baseIntent = Intent(context, com.example.prayertimes.receiver.ReminderAlarmReceiver::class.java)
        
        // Sehri
        alarmManager.cancel(PendingIntent.getBroadcast(context, 200, baseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        
        // Jummah
        alarmManager.cancel(PendingIntent.getBroadcast(context, 300, baseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        
        // Kahf
        alarmManager.cancel(PendingIntent.getBroadcast(context, 400, baseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        alarmManager.cancel(PendingIntent.getBroadcast(context, 401, baseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        
        // Hadith
        alarmManager.cancel(PendingIntent.getBroadcast(context, 500, baseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
    }
}

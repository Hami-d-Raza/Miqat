package com.example.prayertimes.repository

import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.Qibla
import com.batoulapps.adhan.data.DateComponents
import com.example.prayertimes.data.model.AppSettings
import com.example.prayertimes.data.model.DailyPrayerTimes
import com.example.prayertimes.data.model.Prayer
import com.example.prayertimes.data.model.PrayerTimeInfo
import com.example.prayertimes.utils.HijriDateUtil
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repository for computing prayer times, Qibla direction, and Hijri dates.
 * All calculations are performed offline using the adhan2 library.
 */
class PrayerRepository {

    /**
     * Calculates all prayer times for today at the given location with the given settings.
     */
    fun getDailyPrayerTimes(settings: AppSettings, targetDate: Date? = null): DailyPrayerTimes {
        val coordinates = Coordinates(settings.latitude, settings.longitude)
        
        val localDate = if (targetDate != null) {
            val cal = java.util.Calendar.getInstance().apply { time = targetDate }
            kotlinx.datetime.LocalDateTime(cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1, cal.get(java.util.Calendar.DAY_OF_MONTH), 0, 0)
        } else {
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        }

        val dateComponents = DateComponents(localDate.year, localDate.monthNumber, localDate.dayOfMonth)
        val params = settings.calculationMethod.adhanMethod.parameters
        params.madhab = settings.madhab.adhanMadhab

        val prayerTimes = PrayerTimes(coordinates, dateComponents, params)

        val timeZone = TimeZone.currentSystemDefault()
        
        val fajrOffsetMs = settings.fajrOffset * 60 * 1000L
        val dhuhrOffsetMs = settings.dhuhrOffset * 60 * 1000L
        val asrOffsetMs = settings.asrOffset * 60 * 1000L
        val maghribOffsetMs = settings.maghribOffset * 60 * 1000L
        val ishaOffsetMs = settings.ishaOffset * 60 * 1000L
        val currentMillis = if (targetDate == null) Clock.System.now().toEpochMilliseconds() else targetDate.time

        data class PrayerEntry(val prayer: Prayer, val instant: Instant)

        val entries = mutableListOf(
            PrayerEntry(Prayer.FAJR, Instant.fromEpochMilliseconds(prayerTimes.fajr.time + fajrOffsetMs)),
            PrayerEntry(Prayer.SUNRISE, Instant.fromEpochMilliseconds(prayerTimes.sunrise.time)),
            PrayerEntry(Prayer.DHUHR, Instant.fromEpochMilliseconds(prayerTimes.dhuhr.time + dhuhrOffsetMs)),
            PrayerEntry(Prayer.ASR, Instant.fromEpochMilliseconds(prayerTimes.asr.time + asrOffsetMs)),
            PrayerEntry(Prayer.MAGHRIB, Instant.fromEpochMilliseconds(prayerTimes.maghrib.time + maghribOffsetMs)),
            PrayerEntry(Prayer.ISHA, Instant.fromEpochMilliseconds(prayerTimes.isha.time + ishaOffsetMs))
        )

        // Determine next prayer
        var nextPrayer: Prayer? = null
        var countdownToNext: Long = 0

        for (entry in entries) {
            if (entry.instant.toEpochMilliseconds() > currentMillis) {
                nextPrayer = entry.prayer
                countdownToNext = entry.instant.toEpochMilliseconds() - currentMillis
                break
            }
        }

        // If no next prayer today, next is Fajr tomorrow
        if (nextPrayer == null) {
            // Calculate tomorrow's Fajr
            val cal = java.util.Calendar.getInstance()
            if (targetDate != null) {
                cal.time = targetDate
            }
            cal.add(java.util.Calendar.DAY_OF_MONTH, 1)
            val tomorrowYear = cal.get(java.util.Calendar.YEAR)
            val tomorrowMonth = cal.get(java.util.Calendar.MONTH) + 1
            val tomorrowDay = cal.get(java.util.Calendar.DAY_OF_MONTH)
            val tomorrowDate = DateComponents(tomorrowYear, tomorrowMonth, tomorrowDay)
            
            val tomorrowTimes = PrayerTimes(coordinates, tomorrowDate, params)
            nextPrayer = Prayer.FAJR
            val tomorrowFajrTime = tomorrowTimes.fajr.time + fajrOffsetMs
            countdownToNext = tomorrowFajrTime - currentMillis
            
            val fajrIndex = entries.indexOfFirst { it.prayer == Prayer.FAJR }
            if (fajrIndex != -1) {
                entries[fajrIndex] = PrayerEntry(Prayer.FAJR, Instant.fromEpochMilliseconds(tomorrowFajrTime))
            }
        }

        val prayers = entries.map { entry ->
            PrayerTimeInfo(
                prayer = entry.prayer,
                name = entry.prayer.displayName,
                time = entry.instant,
                formattedTime = formatTime(entry.instant, settings.use24hrFormat),
                isNext = entry.prayer == nextPrayer,
                isPassed = entry.instant.toEpochMilliseconds() < currentMillis
            )
        }

        // Dates
        val gregorianFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        val gregorianDate = gregorianFormat.format(Date())
        val hijriDate = HijriDateUtil.getCurrentHijriDate()

        return DailyPrayerTimes(
            date = gregorianDate,
            hijriDate = hijriDate,
            prayers = prayers,
            nextPrayer = nextPrayer,
            countdownToNext = countdownToNext
        )
    }

    /**
     * Gets the Qibla direction in degrees from North for the given coordinates.
     */
    fun getQiblaDirection(latitude: Double, longitude: Double): Double {
        val coordinates = Coordinates(latitude, longitude)
        val qibla = Qibla(coordinates)
        return qibla.direction
    }

    /**
     * Returns prayer times as a map for notification scheduling.
     */
    fun getPrayerTimesMap(settings: AppSettings): Map<Prayer, Pair<Instant, String>> {
        val dailyTimes = getDailyPrayerTimes(settings)
        return dailyTimes.prayers.associate { it.prayer to Pair(it.time, it.formattedTime) }
    }

    /**
     * Formats an instant into a time string based on 24-hour preference.
     */
    fun formatTime(instant: Instant, use24hr: Boolean): String {
        val pattern = if (use24hr) "HH:mm" else "hh:mm a"
        val format = SimpleDateFormat(pattern, Locale.getDefault())
        return format.format(Date(instant.toEpochMilliseconds()))
    }
}

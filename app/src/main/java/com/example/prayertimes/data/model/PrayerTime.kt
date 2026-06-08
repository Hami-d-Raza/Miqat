package com.example.prayertimes.data.model

import kotlinx.datetime.Instant

/**
 * Represents a single prayer time entry.
 */
data class PrayerTimeInfo(
    val prayer: Prayer,
    val name: String,
    val time: Instant,
    val formattedTime: String,
    val isNext: Boolean = false,
    val isPassed: Boolean = false,
    val isOffered: Boolean = false
)

/**
 * Contains all prayer times for a given day.
 */
data class DailyPrayerTimes(
    val date: String,
    val hijriDate: String,
    val prayers: List<PrayerTimeInfo>,
    val nextPrayer: Prayer?,
    val countdownToNext: Long
)

/**
 * Enum of all prayer times tracked by the app.
 */
enum class Prayer(val displayName: String) {
    FAJR("Fajr"),
    SUNRISE("Sunrise"),
    DHUHR("Dhuhr"),
    ASR("Asr"),
    MAGHRIB("Maghrib"),
    ISHA("Isha")
}

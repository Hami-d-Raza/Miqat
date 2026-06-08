package com.example.prayertimes.utils

import android.icu.util.IslamicCalendar
import java.util.Date

/**
 * Utility for converting Gregorian dates to Hijri (Islamic) dates
 * using Android's built-in ICU IslamicCalendar (API 24+).
 */
object HijriDateUtil {

    private val hijriMonths = arrayOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Ula", "Jumada al-Thani", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhul Qi'dah", "Dhul Hijjah"
    )

    /**
     * Returns the current Hijri date formatted as "DD MonthName YYYY AH".
     */
    fun getCurrentHijriDate(): String {
        return getHijriDate(Date())
    }

    /**
     * Converts a given date to Hijri format.
     */
    fun getHijriDate(date: Date): String {
        val locale = android.icu.util.ULocale("en-US@calendar=islamic-umalqura")
        val islamicCalendar = android.icu.util.Calendar.getInstance(locale) as IslamicCalendar
        islamicCalendar.time = date

        val day = islamicCalendar.get(IslamicCalendar.DAY_OF_MONTH)
        val monthIndex = islamicCalendar.get(IslamicCalendar.MONTH)
        val year = islamicCalendar.get(IslamicCalendar.YEAR)

        val monthName = if (monthIndex in hijriMonths.indices) hijriMonths[monthIndex] else "Unknown"
        return "$day $monthName $year AH"
    }
}

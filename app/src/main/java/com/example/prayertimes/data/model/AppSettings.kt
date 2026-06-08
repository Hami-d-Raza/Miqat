package com.example.prayertimes.data.model

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab

/**
 * Represents all user-configurable settings persisted in DataStore.
 */
data class AppSettings(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val cityName: String = "",
    val calculationMethod: CalculationMethodOption = CalculationMethodOption.MUSLIM_WORLD_LEAGUE,
    val madhab: MadhabOption = MadhabOption.SHAFI,
    val fajrNotification: Boolean = true,
    val dhuhrNotification: Boolean = true,
    val asrNotification: Boolean = true,
    val maghribNotification: Boolean = true,
    val ishaNotification: Boolean = true,
    val azanSound: String = "Makkah",
    val ramadanMode: Boolean = false,
    val isLocationSet: Boolean = false,
    val earlyReminderMins: Int = 0,
    val jummahReminder: Boolean = false,
    val kahfReminder: Boolean = false,
    val hadithNotificationEnabled: Boolean = false,
    val hadithNotificationTime: String = "07:00",
    val fajrOffset: Int = 0,
    val dhuhrOffset: Int = 0,
    val asrOffset: Int = 0,
    val maghribOffset: Int = 0,
    val ishaOffset: Int = 0,
    val use24hrFormat: Boolean = false,
    val savedLocations: String = "[]",
    val batteryWarningDismissed: Boolean = false,
    val lastLocationCheckTime: Long = 0L,
    val onboardingComplete: Boolean = false,
    val fontSizeMultiplier: Float = 1.0f,
    val fajrSound: String = "Fajr Special",
    val dhuhrSound: String = "Makkah",
    val asrSound: String = "Makkah",
    val maghribSound: String = "Makkah",
    val ishaSound: String = "Makkah",
    val isDarkMode: Boolean = false,
    val audioQuality: String = "128"
)

enum class CalculationMethodOption(val displayName: String, val adhanMethod: CalculationMethod) {
    MUSLIM_WORLD_LEAGUE("Muslim World League", CalculationMethod.MUSLIM_WORLD_LEAGUE),
    ISNA("ISNA (North America)", CalculationMethod.NORTH_AMERICA),
    KARACHI("Karachi (University of Islamic Sciences)", CalculationMethod.KARACHI),
    EGYPTIAN("Egyptian General Authority", CalculationMethod.EGYPTIAN),
    UMM_AL_QURA("Umm Al-Qura (Makkah)", CalculationMethod.UMM_AL_QURA),
    DUBAI("Dubai", CalculationMethod.DUBAI),
    MOON_SIGHTING("Moon Sighting Committee", CalculationMethod.MOON_SIGHTING_COMMITTEE),
    KUWAIT("Kuwait", CalculationMethod.KUWAIT),
    QATAR("Qatar", CalculationMethod.QATAR),
    SINGAPORE("Singapore", CalculationMethod.SINGAPORE);
}

enum class MadhabOption(val displayName: String, val adhanMadhab: Madhab) {
    SHAFI("Shafi'i / Maliki / Hanbali", Madhab.SHAFI),
    HANAFI("Hanafi", Madhab.HANAFI)
}

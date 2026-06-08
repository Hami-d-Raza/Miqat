package com.example.prayertimes.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.prayertimes.data.model.AppSettings
import com.example.prayertimes.data.model.CalculationMethodOption
import com.example.prayertimes.data.model.MadhabOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "prayer_settings")

class SettingsDataStore(private val context: Context) {
    val dataStore: DataStore<Preferences> = context.dataStore

    companion object {
        private val KEY_LATITUDE = doublePreferencesKey("latitude")
        private val KEY_LONGITUDE = doublePreferencesKey("longitude")
        private val KEY_CITY_NAME = stringPreferencesKey("city_name")
        private val KEY_CALC_METHOD = intPreferencesKey("calculation_method")
        private val KEY_MADHAB = intPreferencesKey("madhab")

        private val KEY_FAJR_NOTIF = booleanPreferencesKey("fajr_notification")
        private val KEY_DHUHR_NOTIF = booleanPreferencesKey("dhuhr_notification")
        private val KEY_ASR_NOTIF = booleanPreferencesKey("asr_notification")
        private val KEY_MAGHRIB_NOTIF = booleanPreferencesKey("maghrib_notification")
        private val KEY_ISHA_NOTIF = booleanPreferencesKey("isha_notification")
        private val KEY_AZAN_SOUND = stringPreferencesKey("azan_sound")
        private val KEY_RAMADAN_MODE = booleanPreferencesKey("ramadan_mode")
        private val KEY_LOCATION_SET = booleanPreferencesKey("is_location_set")
        private val KEY_EARLY_REMINDER_MINS = intPreferencesKey("early_reminder_minutes")
        private val KEY_JUMMAH_REMINDER = booleanPreferencesKey("jummah_reminder")
        private val KEY_KAHF_REMINDER = booleanPreferencesKey("kahf_reminder")
        private val KEY_HADITH_NOTIF_ENABLED = booleanPreferencesKey("hadith_notification_enabled")
        private val KEY_HADITH_NOTIF_TIME = stringPreferencesKey("hadith_notification_time")
        private val KEY_FAJR_OFFSET = intPreferencesKey("fajr_offset")
        private val KEY_DHUHR_OFFSET = intPreferencesKey("dhuhr_offset")
        private val KEY_ASR_OFFSET = intPreferencesKey("asr_offset")
        private val KEY_MAGHRIB_OFFSET = intPreferencesKey("maghrib_offset")
        private val KEY_ISHA_OFFSET = intPreferencesKey("isha_offset")
        private val KEY_USE_24HR_FORMAT = booleanPreferencesKey("use_24hr_format")
        private val KEY_SAVED_LOCATIONS = stringPreferencesKey("saved_locations")
        private val KEY_BATTERY_WARNING_DISMISSED = booleanPreferencesKey("battery_warning_dismissed")
        private val KEY_LAST_LOCATION_CHECK_TIME = androidx.datastore.preferences.core.longPreferencesKey("last_location_check_time")
        private val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        private val KEY_FONT_SIZE_MULTIPLIER = androidx.datastore.preferences.core.floatPreferencesKey("font_size_multiplier")
        private val KEY_FAJR_SOUND = stringPreferencesKey("fajr_sound")
        private val KEY_DHUHR_SOUND = stringPreferencesKey("dhuhr_sound")
        private val KEY_ASR_SOUND = stringPreferencesKey("asr_sound")
        private val KEY_MAGHRIB_SOUND = stringPreferencesKey("maghrib_sound")
        private val KEY_ISHA_SOUND = stringPreferencesKey("isha_sound")
        private val KEY_IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        private val KEY_AUDIO_QUALITY = stringPreferencesKey("audio_quality")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data
        .catch { emit(androidx.datastore.preferences.core.emptyPreferences()) }
        .map { prefs ->
        AppSettings(
            latitude = prefs[KEY_LATITUDE] ?: 0.0,
            longitude = prefs[KEY_LONGITUDE] ?: 0.0,
            cityName = prefs[KEY_CITY_NAME] ?: "",
            calculationMethod = CalculationMethodOption.values().getOrElse(
                prefs[KEY_CALC_METHOD] ?: 0
            ) { CalculationMethodOption.MUSLIM_WORLD_LEAGUE },
            madhab = MadhabOption.values().getOrElse(
                prefs[KEY_MADHAB] ?: 0
            ) { MadhabOption.SHAFI },
            fajrNotification = prefs[KEY_FAJR_NOTIF] ?: true,
            dhuhrNotification = prefs[KEY_DHUHR_NOTIF] ?: true,
            asrNotification = prefs[KEY_ASR_NOTIF] ?: true,
            maghribNotification = prefs[KEY_MAGHRIB_NOTIF] ?: true,
            ishaNotification = prefs[KEY_ISHA_NOTIF] ?: true,
            azanSound = prefs[KEY_AZAN_SOUND] ?: "Makkah",
            ramadanMode = prefs[KEY_RAMADAN_MODE] ?: false,
            isLocationSet = prefs[KEY_LOCATION_SET] ?: false,
            earlyReminderMins = prefs[KEY_EARLY_REMINDER_MINS] ?: 0,
            jummahReminder = prefs[KEY_JUMMAH_REMINDER] ?: false,
            kahfReminder = prefs[KEY_KAHF_REMINDER] ?: false,
            hadithNotificationEnabled = prefs[KEY_HADITH_NOTIF_ENABLED] ?: false,
            hadithNotificationTime = prefs[KEY_HADITH_NOTIF_TIME] ?: "07:00",
            fajrOffset = prefs[KEY_FAJR_OFFSET] ?: 0,
            dhuhrOffset = prefs[KEY_DHUHR_OFFSET] ?: 0,
            asrOffset = prefs[KEY_ASR_OFFSET] ?: 0,
            maghribOffset = prefs[KEY_MAGHRIB_OFFSET] ?: 0,
            ishaOffset = prefs[KEY_ISHA_OFFSET] ?: 0,
            use24hrFormat = prefs[KEY_USE_24HR_FORMAT] ?: false,
            savedLocations = prefs[KEY_SAVED_LOCATIONS] ?: "[]",
            batteryWarningDismissed = prefs[KEY_BATTERY_WARNING_DISMISSED] ?: false,
            lastLocationCheckTime = prefs[KEY_LAST_LOCATION_CHECK_TIME] ?: 0L,
            onboardingComplete = prefs[KEY_ONBOARDING_COMPLETE] ?: false,
            fontSizeMultiplier = prefs[KEY_FONT_SIZE_MULTIPLIER] ?: 1.0f,
            fajrSound = prefs[KEY_FAJR_SOUND] ?: "Fajr Special",
            dhuhrSound = prefs[KEY_DHUHR_SOUND] ?: "Makkah",
            asrSound = prefs[KEY_ASR_SOUND] ?: "Makkah",
            maghribSound = prefs[KEY_MAGHRIB_SOUND] ?: "Makkah",
            ishaSound = prefs[KEY_ISHA_SOUND] ?: "Makkah",
            isDarkMode = prefs[KEY_IS_DARK_MODE] ?: false,
            audioQuality = prefs[KEY_AUDIO_QUALITY] ?: "128"
        )
    }

    suspend fun updateLocation(latitude: Double, longitude: Double, cityName: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LATITUDE] = latitude
            prefs[KEY_LONGITUDE] = longitude
            prefs[KEY_CITY_NAME] = cityName
            prefs[KEY_LOCATION_SET] = true
        }
    }

    suspend fun updateCalculationMethod(method: CalculationMethodOption) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CALC_METHOD] = method.ordinal
        }
    }

    suspend fun updateMadhab(madhab: MadhabOption) {
        context.dataStore.edit { prefs ->
            prefs[KEY_MADHAB] = madhab.ordinal
        }
    }



    suspend fun updatePrayerNotification(prayer: String, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            when (prayer) {
                "fajr" -> prefs[KEY_FAJR_NOTIF] = enabled
                "dhuhr" -> prefs[KEY_DHUHR_NOTIF] = enabled
                "asr" -> prefs[KEY_ASR_NOTIF] = enabled
                "maghrib" -> prefs[KEY_MAGHRIB_NOTIF] = enabled
                "isha" -> prefs[KEY_ISHA_NOTIF] = enabled
            }
        }
    }

    suspend fun updateAzanSound(sound: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AZAN_SOUND] = sound
        }
    }

    suspend fun updateRamadanMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_RAMADAN_MODE] = enabled
        }
    }



    suspend fun updateEarlyReminderMins(mins: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_EARLY_REMINDER_MINS] = mins
        }
    }

    suspend fun updateJummahReminder(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_JUMMAH_REMINDER] = enabled
        }
    }

    suspend fun updateKahfReminder(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_KAHF_REMINDER] = enabled
        }
    }

    suspend fun updateHadithNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HADITH_NOTIF_ENABLED] = enabled
        }
    }

    suspend fun updateHadithNotificationTime(timeStr: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HADITH_NOTIF_TIME] = timeStr
        }
    }

    suspend fun updatePrayerOffset(prayer: String, offset: Int) {
        context.dataStore.edit { prefs ->
            when (prayer.lowercase()) {
                "fajr" -> prefs[KEY_FAJR_OFFSET] = offset
                "dhuhr" -> prefs[KEY_DHUHR_OFFSET] = offset
                "asr" -> prefs[KEY_ASR_OFFSET] = offset
                "maghrib" -> prefs[KEY_MAGHRIB_OFFSET] = offset
                "isha" -> prefs[KEY_ISHA_OFFSET] = offset
            }
        }
    }

    suspend fun updateUse24hrFormat(use24hr: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USE_24HR_FORMAT] = use24hr
        }
    }

    suspend fun updateSavedLocations(json: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SAVED_LOCATIONS] = json
        }
    }

    suspend fun updateBatteryWarningDismissed(dismissed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BATTERY_WARNING_DISMISSED] = dismissed
        }
    }

    suspend fun updateLastLocationCheckTime(timestamp: Long) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_LOCATION_CHECK_TIME] = timestamp
        }
    }

    suspend fun updateOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETE] = complete
        }
    }

    suspend fun updateFontSizeMultiplier(multiplier: Float) {
        context.dataStore.edit { prefs ->
            prefs[KEY_FONT_SIZE_MULTIPLIER] = multiplier
        }
    }

    suspend fun updatePrayerSound(prayer: String, sound: String) {
        context.dataStore.edit { prefs ->
            when (prayer.lowercase()) {
                "fajr" -> prefs[KEY_FAJR_SOUND] = sound
                "dhuhr" -> prefs[KEY_DHUHR_SOUND] = sound
                "asr" -> prefs[KEY_ASR_SOUND] = sound
                "maghrib" -> prefs[KEY_MAGHRIB_SOUND] = sound
                "isha" -> prefs[KEY_ISHA_SOUND] = sound
            }
        }
    }

    suspend fun updateIsDarkMode(isDark: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_DARK_MODE] = isDark
        }
    }

    suspend fun updateAudioQuality(quality: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUDIO_QUALITY] = quality
        }
    }
}

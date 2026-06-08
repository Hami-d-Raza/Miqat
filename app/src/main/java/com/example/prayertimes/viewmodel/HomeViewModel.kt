package com.example.prayertimes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.prayertimes.data.datastore.SettingsDataStore
import com.example.prayertimes.data.model.AppSettings
import com.example.prayertimes.data.model.DailyPrayerTimes
import com.example.prayertimes.data.model.Prayer
import com.example.prayertimes.repository.PrayerRepository
import com.example.prayertimes.utils.PrayerAlarmScheduler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

/**
 * ViewModel for the Home screen showing prayer times and countdown.
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsDataStore = SettingsDataStore(application)
    private val prayerRepository = PrayerRepository()
    private val prayerTrackingRepository = com.example.prayertimes.repository.PrayerTrackingRepository(
        com.example.prayertimes.data.local.PrayerTrackerDatabase.getDatabase(application).prayerRecordDao()
    )

    private val _prayerTimes = MutableStateFlow<DailyPrayerTimes?>(null)
    
    private val _todayRecords = MutableStateFlow<List<com.example.prayertimes.data.model.PrayerRecord>>(emptyList())

    val prayerTimes: StateFlow<DailyPrayerTimes?> = combine(_prayerTimes, _todayRecords) { daily, records ->
        if (daily == null) return@combine null
        val newPrayers = daily.prayers.map { info ->
            val record = records.find { it.prayerName == info.prayer.name }
            info.copy(isOffered = record?.isOffered == true)
        }
        daily.copy(prayers = newPrayers)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _countdownText = MutableStateFlow("")
    val countdownText: StateFlow<String> = _countdownText.asStateFlow()

    private val _nextEventName = MutableStateFlow("")
    val nextEventName: StateFlow<String> = _nextEventName.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _locationUpdatePrompt = MutableStateFlow<com.example.prayertimes.repository.LocationResult?>(null)
    val locationUpdatePrompt: StateFlow<com.example.prayertimes.repository.LocationResult?> = _locationUpdatePrompt.asStateFlow()

    private var countdownJob: Job? = null

    init {
        viewModelScope.launch {
            val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            prayerTrackingRepository.getRecordsForDate(todayDate).collect { records ->
                _todayRecords.value = records
            }
        }
        viewModelScope.launch {
            settingsDataStore.settingsFlow.collect { newSettings ->
                _settings.value = newSettings
                if (newSettings.isLocationSet) {
                    refreshPrayerTimes(newSettings)
                }
                _isLoading.value = false
            }
        }
    }

    fun togglePrayerOffered(prayer: Prayer, isOffered: Boolean) {
        viewModelScope.launch {
            val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            prayerTrackingRepository.togglePrayerOffered(todayDate, prayer, isOffered)
        }
    }

    /**
     * Recalculates prayer times and starts countdown ticker.
     */
    fun refreshPrayerTimes(settings: AppSettings = _settings.value) {
        if (!settings.isLocationSet) return

        try {
            val dailyTimes = prayerRepository.getDailyPrayerTimes(settings)
            _prayerTimes.value = dailyTimes

            // Schedule notifications
            scheduleNotifications(settings)

            // Start countdown ticker
            startCountdown(dailyTimes)
        } catch (e: Exception) {
            // Handle calculation errors silently
        }
    }

    private var lastCalculatedDateStr = ""

    /**
     * Starts a coroutine that ticks every second to update the countdown text.
     */
    private fun startCountdown(dailyTimes: DailyPrayerTimes) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            lastCalculatedDateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            while (true) {
                val now = System.currentTimeMillis()
                
                val currentStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                if (currentStr != lastCalculatedDateStr) {
                    refreshPrayerTimes()
                    return@launch
                }
                
                val settingsVal = _settings.value
                
                var targetTime: Long? = null
                var targetName = ""
                
                if (settingsVal.ramadanMode) {
                    val fajrInfo = dailyTimes.prayers.find { it.prayer == Prayer.FAJR }
                    val maghribInfo = dailyTimes.prayers.find { it.prayer == Prayer.MAGHRIB }
                    
                    if (fajrInfo != null && maghribInfo != null) {
                        val fajrTime = fajrInfo.time.toEpochMilliseconds()
                        val maghribTime = maghribInfo.time.toEpochMilliseconds()
                        
                        if (now < fajrTime) {
                            targetTime = fajrTime
                            targetName = "Sehri"
                        } else if (now < maghribTime) {
                            targetTime = maghribTime
                            targetName = "Iftar"
                        } else {
                            // After Maghrib, target tomorrow's Sehri (which is in nextPrayer if it's FAJR)
                            val nextPrayerInfo = dailyTimes.prayers.find { it.isNext }
                            targetTime = nextPrayerInfo?.time?.toEpochMilliseconds()
                            targetName = "Sehri"
                        }
                    }
                } else {
                    val nextPrayerInfo = dailyTimes.prayers.find { it.isNext }
                    targetTime = nextPrayerInfo?.time?.toEpochMilliseconds()
                    targetName = nextPrayerInfo?.prayer?.displayName ?: ""
                }

                if (targetTime != null) {
                    val remaining = targetTime - now
                    if (remaining > 0) {
                        _countdownText.value = formatCountdown(remaining)
                        _nextEventName.value = targetName
                        _prayerTimes.value = dailyTimes.copy(countdownToNext = remaining)
                    } else {
                        refreshPrayerTimes()
                        return@launch
                    }
                }
                delay(1000)
            }
        }
    }

    /**
     * Formats milliseconds into HH:MM:SS string.
     */
    private fun formatCountdown(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    /**
     * Schedules prayer notification alarms based on current settings.
     */
    private fun scheduleNotifications(settings: AppSettings) {
        val dailyTimes = _prayerTimes.value ?: return
        val enabledPrayers = mutableSetOf<Prayer>()
        if (settings.fajrNotification) enabledPrayers.add(Prayer.FAJR)
        if (settings.dhuhrNotification) enabledPrayers.add(Prayer.DHUHR)
        if (settings.asrNotification) enabledPrayers.add(Prayer.ASR)
        if (settings.maghribNotification) enabledPrayers.add(Prayer.MAGHRIB)
        if (settings.ishaNotification) enabledPrayers.add(Prayer.ISHA)

        val prayerTimesMap = dailyTimes.prayers.associate {
            it.prayer to Pair(it.time, it.formattedTime)
        }

        PrayerAlarmScheduler.schedulePrayerAlarms(
            context = getApplication(),
            prayerTimes = prayerTimesMap,
            enabledPrayers = enabledPrayers,
            settings = settings
        )
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }

    fun isBatteryOptimizationIgnored(): Boolean {
        val pm = getApplication<Application>().getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
        return pm.isIgnoringBatteryOptimizations(getApplication<Application>().packageName)
    }

    fun dismissBatteryWarning() {
        viewModelScope.launch {
            settingsDataStore.updateBatteryWarningDismissed(true)
        }
    }

    fun checkAutoLocationUpdate() {
        viewModelScope.launch {
            val currentSettings = _settings.value
            val now = System.currentTimeMillis()
            if (now - currentSettings.lastLocationCheckTime < 3600000L) return@launch
            
            settingsDataStore.updateLastLocationCheckTime(now)
            
            try {
                val locRepo = com.example.prayertimes.repository.LocationRepository(getApplication())
                val newLoc = locRepo.getCurrentLocation()
                if (newLoc != null) {
                    val results = FloatArray(1)
                    android.location.Location.distanceBetween(
                        currentSettings.latitude, currentSettings.longitude,
                        newLoc.latitude, newLoc.longitude,
                        results
                    )
                    val distanceKm = results[0] / 1000f
                    if (distanceKm > 50f) {
                        _locationUpdatePrompt.value = newLoc
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun acceptLocationUpdate(newLoc: com.example.prayertimes.repository.LocationResult) {
        viewModelScope.launch {
            settingsDataStore.updateLocation(newLoc.latitude, newLoc.longitude, newLoc.cityName)
            _locationUpdatePrompt.value = null
        }
    }

    fun dismissLocationUpdate() {
        _locationUpdatePrompt.value = null
    }
}

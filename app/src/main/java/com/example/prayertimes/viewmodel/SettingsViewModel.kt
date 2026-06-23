package com.example.prayertimes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.prayertimes.data.datastore.SettingsDataStore
import com.example.prayertimes.data.model.AppSettings
import com.example.prayertimes.data.model.CalculationMethodOption
import com.example.prayertimes.data.model.MadhabOption
import com.example.prayertimes.data.model.ThemeMode
import com.example.prayertimes.repository.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import com.example.prayertimes.data.model.SavedLocation

/**
 * ViewModel for the Settings screen.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsDataStore = SettingsDataStore(application)
    private val locationRepository = LocationRepository(application)

    private val _isDetectingLocation = MutableStateFlow(false)
    val isDetectingLocation: StateFlow<Boolean> = _isDetectingLocation.asStateFlow()

    private val _locationError = MutableStateFlow<String?>(null)
    val locationError: StateFlow<String?> = _locationError.asStateFlow()

    val settings: StateFlow<AppSettings> = settingsDataStore.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSettings()
    )

    /**
     * Detects current location using GPS.
     */
    fun detectLocation() {
        viewModelScope.launch {
            _isDetectingLocation.value = true
            _locationError.value = null
            try {
                val result = locationRepository.getCurrentLocation()
                if (result != null) {
                    settingsDataStore.updateLocation(
                        latitude = result.latitude,
                        longitude = result.longitude,
                        cityName = result.cityName
                    )
                } else {
                    _locationError.value = "Could not detect location. Please check GPS settings."
                }
            } catch (e: Exception) {
                _locationError.value = "Location detection failed: ${e.message}"
            } finally {
                _isDetectingLocation.value = false
            }
        }
    }
    private val _citySuggestions = MutableStateFlow<List<String>>(emptyList())
    val citySuggestions: StateFlow<List<String>> = _citySuggestions.asStateFlow()

    private var searchJob: kotlinx.coroutines.Job? = null

    /**
     * Searches for a city by name and updates location.
     */
    fun searchCity(cityName: String) {
        viewModelScope.launch {
            _isDetectingLocation.value = true
            _locationError.value = null
            try {
                val result = locationRepository.getCoordinatesForCity(cityName)
                if (result != null) {
                    settingsDataStore.updateLocation(
                        latitude = result.latitude,
                        longitude = result.longitude,
                        cityName = result.cityName
                    )
                } else {
                    _locationError.value = "Could not find coordinates for $cityName."
                }
            } catch (e: Exception) {
                _locationError.value = "City search failed: ${e.message}"
            } finally {
                _isDetectingLocation.value = false
            }
        }
    }

    /**
     * Fetches city suggestions as user types.
     */
    fun fetchCitySuggestions(query: String) {
        searchJob?.cancel()
        if (query.length < 3) {
            _citySuggestions.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(500) // Debounce
            try {
                val geocoder = android.location.Geocoder(getApplication(), java.util.Locale.getDefault())
                val addresses = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    geocoder.getFromLocationName(query, 5)
                }
                val suggestions = addresses?.mapNotNull { address ->
                    buildString {
                        address.locality?.let { append(it) } ?: address.subAdminArea?.let { append(it) } ?: address.adminArea?.let { append(it) }
                        address.countryName?.let {
                            if (isNotEmpty()) append(", ")
                            append(it)
                        }
                    }.ifEmpty { null }
                }?.distinct() ?: emptyList()
                _citySuggestions.value = suggestions
            } catch (e: Exception) {
                _citySuggestions.value = emptyList()
            }
        }
    }

    /**
     * Manually sets coordinates.
     */
    fun setManualLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            settingsDataStore.updateLocation(
                latitude = latitude,
                longitude = longitude,
                cityName = "Custom Location"
            )
        }
    }

    fun updateCalculationMethod(method: CalculationMethodOption) {
        viewModelScope.launch {
            settingsDataStore.updateCalculationMethod(method)
        }
    }

    fun updateMadhab(madhab: MadhabOption) {
        viewModelScope.launch {
            settingsDataStore.updateMadhab(madhab)
        }
    }

    fun updatePrayerNotification(prayer: String, enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updatePrayerNotification(prayer, enabled)
        }
    }

    fun updateAzanSound(sound: String) {
        viewModelScope.launch {
            settingsDataStore.updateAzanSound(sound)
        }
    }

    fun updateRamadanMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updateRamadanMode(enabled)
        }
    }



    fun updateEarlyReminderMins(mins: Int) {
        viewModelScope.launch {
            settingsDataStore.updateEarlyReminderMins(mins)
        }
    }

    fun updateJummahReminder(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updateJummahReminder(enabled)
        }
    }

    fun updateKahfReminder(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updateKahfReminder(enabled)
        }
    }

    fun updateHadithNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updateHadithNotificationEnabled(enabled)
        }
    }

    fun updateHadithNotificationTime(timeStr: String) {
        viewModelScope.launch {
            settingsDataStore.updateHadithNotificationTime(timeStr)
        }
    }

    // BATCH 5 Additions

    fun updatePrayerOffset(prayer: String, offset: Int) {
        viewModelScope.launch {
            settingsDataStore.updatePrayerOffset(prayer, offset)
        }
    }

    fun updateUse24hrFormat(use24hr: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updateUse24hrFormat(use24hr)
        }
    }

    fun saveCurrentLocation(name: String) {
        viewModelScope.launch {
            val currentSettings = settings.value
            val newLocation = SavedLocation(
                name = name,
                latitude = currentSettings.latitude,
                longitude = currentSettings.longitude,
                cityName = currentSettings.cityName
            )
            
            val currentList = try {
                Json.decodeFromString<List<SavedLocation>>(currentSettings.savedLocations)
            } catch (e: Exception) {
                emptyList()
            }
            
            val updatedList = (currentList + newLocation).takeLast(3) // keep max 3
            val jsonString = Json.encodeToString(updatedList)
            settingsDataStore.updateSavedLocations(jsonString)
        }
    }

    fun deleteSavedLocation(id: String) {
        viewModelScope.launch {
            val currentSettings = settings.value
            val currentList = try {
                Json.decodeFromString<List<SavedLocation>>(currentSettings.savedLocations)
            } catch (e: Exception) {
                emptyList()
            }
            
            val updatedList = currentList.filter { it.id != id }
            val jsonString = Json.encodeToString(updatedList)
            settingsDataStore.updateSavedLocations(jsonString)
        }
    }

    fun selectSavedLocation(location: SavedLocation) {
        viewModelScope.launch {
            settingsDataStore.updateLocation(
                latitude = location.latitude,
                longitude = location.longitude,
                cityName = location.cityName
            )
        }
    }

    fun clearLocationError() {
        _locationError.value = null
    }

    // BATCH 6 Additions
    fun updateOnboardingComplete(complete: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updateOnboardingComplete(complete)
        }
    }

    fun updateFontSizeMultiplier(multiplier: Float) {
        viewModelScope.launch {
            settingsDataStore.updateFontSizeMultiplier(multiplier)
        }
    }

    fun updatePrayerSound(prayer: String, sound: String) {
        viewModelScope.launch {
            settingsDataStore.updatePrayerSound(prayer, sound)
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsDataStore.updateThemeMode(mode)
        }
    }

    fun updateAudioQuality(quality: String) {
        viewModelScope.launch {
            settingsDataStore.updateAudioQuality(quality)
        }
    }
}

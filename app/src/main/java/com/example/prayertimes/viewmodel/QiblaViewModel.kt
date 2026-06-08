package com.example.prayertimes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.prayertimes.data.datastore.SettingsDataStore
import com.example.prayertimes.data.model.CompassData
import com.example.prayertimes.data.model.SensorAccuracyLevel
import com.example.prayertimes.repository.PrayerRepository
import com.example.prayertimes.utils.CompassSensor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Qibla compass screen.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class QiblaViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsDataStore = SettingsDataStore(application)
    private val prayerRepository = PrayerRepository()
    private val compassSensor = CompassSensor(application)

    private val _qiblaAngle = MutableStateFlow(0.0)
    val qiblaAngle: StateFlow<Double> = _qiblaAngle.asStateFlow()

    private val _needsCalibration = MutableStateFlow(false)
    val needsCalibration: StateFlow<Boolean> = _needsCalibration.asStateFlow()

    private val _isLocationSet = MutableStateFlow(false)
    val isLocationSet: StateFlow<Boolean> = _isLocationSet.asStateFlow()

    private val _latitude = MutableStateFlow(0.0)
    val latitude: StateFlow<Double> = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow(0.0)
    val longitude: StateFlow<Double> = _longitude.asStateFlow()

    init {
        viewModelScope.launch {
            settingsDataStore.settingsFlow.collect { settings ->
                _isLocationSet.value = settings.isLocationSet
                if (settings.isLocationSet) {
                    _latitude.value = settings.latitude
                    _longitude.value = settings.longitude
                    _qiblaAngle.value = prayerRepository.getQiblaDirection(
                        settings.latitude, settings.longitude
                    )
                }
            }
        }
    }

    val compassData: StateFlow<CompassData> = combine(
        _isLocationSet,
        _qiblaAngle
    ) { isSet, angle ->
        isSet to angle
    }.flatMapLatest { (isSet, angle) ->
        if (isSet) {
            compassSensor.observeCompass(angle).onEach { data ->
                _needsCalibration.value = data.sensorAccuracy == SensorAccuracyLevel.LOW ||
                        data.sensorAccuracy == SensorAccuracyLevel.UNRELIABLE
            }
        } else {
            flowOf(CompassData())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CompassData())
}

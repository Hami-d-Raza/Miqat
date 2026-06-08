package com.example.prayertimes.viewmodel

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private val Application.tasbihDataStore: DataStore<Preferences> by preferencesDataStore(name = "tasbih_data")

class TasbihViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.tasbihDataStore

    companion object {
        private val KEY_SUBHANALLAH = intPreferencesKey("count_subhanallah")
        private val KEY_ALHAMDULILLAH = intPreferencesKey("count_alhamdulillah")
        private val KEY_ALLAHU_AKBAR = intPreferencesKey("count_allahu_akbar")
        private val KEY_ASTAGHFIRULLAH = intPreferencesKey("count_astaghfirullah")
        private val KEY_TODAY_DATE = intPreferencesKey("today_date_epoch")
        private val KEY_TODAY_TOTAL = intPreferencesKey("today_total")
        private val KEY_TARGET = intPreferencesKey("target")
    }

    enum class DhikrPreset(val label: String, val arabic: String) {
        SUBHANALLAH("SubhanAllah", "سُبْحَانَ اللَّهِ"),
        ALHAMDULILLAH("Alhamdulillah", "الْحَمْدُ لِلَّهِ"),
        ALLAHU_AKBAR("Allahu Akbar", "اللَّهُ أَكْبَرُ"),
        ASTAGHFIRULLAH("Astaghfirullah", "أَسْتَغْفِرُ اللَّهَ")
    }

    private val _selectedPreset = MutableStateFlow(DhikrPreset.SUBHANALLAH)
    val selectedPreset: StateFlow<DhikrPreset> = _selectedPreset.asStateFlow()

    private val _counts = MutableStateFlow(mapOf(
        DhikrPreset.SUBHANALLAH to 0,
        DhikrPreset.ALHAMDULILLAH to 0,
        DhikrPreset.ALLAHU_AKBAR to 0,
        DhikrPreset.ASTAGHFIRULLAH to 0
    ))
    val counts: StateFlow<Map<DhikrPreset, Int>> = _counts.asStateFlow()

    val currentCount: StateFlow<Int> = combine(_counts, _selectedPreset) { counts, preset ->
        counts[preset] ?: 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _target = MutableStateFlow(33)
    val target: StateFlow<Int> = _target.asStateFlow()

    private val _todayTotal = MutableStateFlow(0)
    val todayTotal: StateFlow<Int> = _todayTotal.asStateFlow()

    private val _goalReached = MutableStateFlow(false)
    val goalReached: StateFlow<Boolean> = _goalReached.asStateFlow()

    init {
        viewModelScope.launch {
            dataStore.data.collect { prefs ->
                // Reset today total if date changed
                val savedDate = prefs[KEY_TODAY_DATE] ?: 0
                val todayEpoch = getTodayEpoch()
                val todayTotal = if (savedDate == todayEpoch) prefs[KEY_TODAY_TOTAL] ?: 0 else 0

                _counts.value = mapOf(
                    DhikrPreset.SUBHANALLAH to (prefs[KEY_SUBHANALLAH] ?: 0),
                    DhikrPreset.ALHAMDULILLAH to (prefs[KEY_ALHAMDULILLAH] ?: 0),
                    DhikrPreset.ALLAHU_AKBAR to (prefs[KEY_ALLAHU_AKBAR] ?: 0),
                    DhikrPreset.ASTAGHFIRULLAH to (prefs[KEY_ASTAGHFIRULLAH] ?: 0)
                )
                _target.value = prefs[KEY_TARGET] ?: 33
                _todayTotal.value = todayTotal
            }
        }
    }

    fun increment() {
        val preset = _selectedPreset.value
        val newCount = (_counts.value[preset] ?: 0) + 1
        val wasAtTarget = newCount == _target.value

        val newCounts = _counts.value.toMutableMap()
        newCounts[preset] = newCount
        _counts.value = newCounts

        val newTotal = _todayTotal.value + 1
        _todayTotal.value = newTotal

        if (wasAtTarget) {
            _goalReached.value = true
        }

        // Persist
        viewModelScope.launch {
            dataStore.edit { prefs ->
                val key = when (preset) {
                    DhikrPreset.SUBHANALLAH -> KEY_SUBHANALLAH
                    DhikrPreset.ALHAMDULILLAH -> KEY_ALHAMDULILLAH
                    DhikrPreset.ALLAHU_AKBAR -> KEY_ALLAHU_AKBAR
                    DhikrPreset.ASTAGHFIRULLAH -> KEY_ASTAGHFIRULLAH
                }
                prefs[key] = newCount
                prefs[KEY_TODAY_DATE] = getTodayEpoch()
                prefs[KEY_TODAY_TOTAL] = newTotal
            }
        }
    }

    fun dismissGoal() {
        _goalReached.value = false
    }

    fun reset() {
        val preset = _selectedPreset.value
        val newCounts = _counts.value.toMutableMap()
        newCounts[preset] = 0
        _counts.value = newCounts

        viewModelScope.launch {
            dataStore.edit { prefs ->
                val key = when (preset) {
                    DhikrPreset.SUBHANALLAH -> KEY_SUBHANALLAH
                    DhikrPreset.ALHAMDULILLAH -> KEY_ALHAMDULILLAH
                    DhikrPreset.ALLAHU_AKBAR -> KEY_ALLAHU_AKBAR
                    DhikrPreset.ASTAGHFIRULLAH -> KEY_ASTAGHFIRULLAH
                }
                prefs[key] = 0
            }
        }
    }

    fun selectPreset(preset: DhikrPreset) {
        _selectedPreset.value = preset
        _goalReached.value = false
    }

    fun setTarget(target: Int) {
        _target.value = target
        viewModelScope.launch {
            dataStore.edit { prefs -> prefs[KEY_TARGET] = target }
        }
    }

    private fun getTodayEpoch(): Int {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.YEAR) * 10000 + (cal.get(Calendar.MONTH) + 1) * 100 + cal.get(Calendar.DAY_OF_MONTH)
    }
}

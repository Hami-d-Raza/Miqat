package com.example.prayertimes.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PrayerGuideViewModel : ViewModel() {
    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()

    private val _prayerStepIndex = MutableStateFlow(0)
    val prayerStepIndex: StateFlow<Int> = _prayerStepIndex.asStateFlow()

    private val _wuduStepIndex = MutableStateFlow(0)
    val wuduStepIndex: StateFlow<Int> = _wuduStepIndex.asStateFlow()

    fun selectTab(index: Int) {
        _selectedTabIndex.value = index
    }

    fun setPrayerStep(index: Int) {
        _prayerStepIndex.value = index
    }

    fun setWuduStep(index: Int) {
        _wuduStepIndex.value = index
    }
}

package com.example.prayertimes.viewmodel

import android.icu.util.Calendar
import android.icu.util.IslamicCalendar
import androidx.lifecycle.ViewModel
import com.example.prayertimes.data.model.HijriCalendarData
import com.example.prayertimes.data.model.IslamicEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HijriCalendarViewModel : ViewModel() {
    private val today = IslamicCalendar()
    
    private val _currentMonth = MutableStateFlow(today.get(Calendar.MONTH))
    val currentMonth: StateFlow<Int> = _currentMonth.asStateFlow()

    private val _currentYear = MutableStateFlow(today.get(Calendar.YEAR))
    val currentYear: StateFlow<Int> = _currentYear.asStateFlow()

    fun nextMonth() {
        val next = _currentMonth.value + 1
        if (next > 11) {
            _currentMonth.value = 0
            _currentYear.value += 1
        } else {
            _currentMonth.value = next
        }
    }

    fun previousMonth() {
        val prev = _currentMonth.value - 1
        if (prev < 0) {
            _currentMonth.value = 11
            _currentYear.value -= 1
        } else {
            _currentMonth.value = prev
        }
    }
    
    fun getEventsForDate(month: Int, day: Int): List<IslamicEvent> {
        return HijriCalendarData.events.filter { event ->
            event.month == month && day >= event.dayStart && day <= event.dayEnd
        }
    }
    
    fun isToday(year: Int, month: Int, day: Int): Boolean {
        return year == today.get(Calendar.YEAR) && 
               month == today.get(Calendar.MONTH) && 
               day == today.get(Calendar.DAY_OF_MONTH)
    }
}

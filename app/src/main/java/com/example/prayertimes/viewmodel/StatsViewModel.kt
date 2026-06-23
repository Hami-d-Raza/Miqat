package com.example.prayertimes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.prayertimes.data.local.PrayerTrackerDatabase
import com.example.prayertimes.data.model.PrayerRecord
import com.example.prayertimes.repository.PrayerTrackingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StatsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PrayerTrackingRepository(
        PrayerTrackerDatabase.getDatabase(application).prayerRecordDao()
    )

    private val _allRecords = MutableStateFlow<List<PrayerRecord>>(emptyList())
    val allRecords: StateFlow<List<PrayerRecord>> = _allRecords.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllRecords().collect { records ->
                _allRecords.value = records
            }
        }
    }

    /**
     * Gets total offered prayers for a specific date. Returns null if no records exist.
     */
    fun getOfferedCountForDate(dateStr: String): Int? {
        val recordsForDate = _allRecords.value.filter { it.date == dateStr }
        if (recordsForDate.isEmpty()) return null
        return recordsForDate.count { it.isOffered }
    }

    /**
     * Returns today's records.
     */
    fun getRecordsForDate(dateStr: String): List<PrayerRecord> {
        return _allRecords.value.filter { it.date == dateStr }
    }

    /**
     * Returns a list of pairs: (Date string, Offered Count) for the last N days.
     * Count is null if there is no data for that day.
     */
    fun getDailyCounts(days: Int): List<Pair<String, Int?>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -(days - 1))
        
        val result = mutableListOf<Pair<String, Int?>>()
        for (i in 0 until days) {
            val dStr = dateFormat.format(cal.time)
            val count = getOfferedCountForDate(dStr)
            result.add(Pair(dStr, count))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return result
    }

    /**
     * Calculates the current and best streaks (days with at least 1 prayer offered).
     */
    fun getStreaks(): Pair<Int, Int> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        val todayStr = dateFormat.format(cal.time)

        // Find all unique dates with at least 1 offered prayer
        val activeDates = _allRecords.value
            .filter { it.isOffered }
            .map { it.date }
            .distinct()
            .sortedDescending() // newest to oldest

        if (activeDates.isEmpty()) return Pair(0, 0)

        var currentStreak = 0
        var bestStreak = 0

        // Calculate best streak
        val allDatesAsc = activeDates.sorted()
        var tempBest = 1
        for (i in 1 until allDatesAsc.size) {
            val d1 = dateFormat.parse(allDatesAsc[i - 1])
            val d2 = dateFormat.parse(allDatesAsc[i])
            if (d1 != null && d2 != null) {
                val diffDays = (d2.time - d1.time) / (1000 * 60 * 60 * 24)
                if (diffDays == 1L) {
                    tempBest++
                } else {
                    if (tempBest > bestStreak) bestStreak = tempBest
                    tempBest = 1
                }
            }
        }
        if (tempBest > bestStreak) bestStreak = tempBest

        // Calculate current streak
        val calCurrent = Calendar.getInstance()
        var checkStr = dateFormat.format(calCurrent.time)
        
        // If today is not active, check if yesterday was (streak might still be alive)
        if (!activeDates.contains(checkStr)) {
            calCurrent.add(Calendar.DAY_OF_YEAR, -1)
            checkStr = dateFormat.format(calCurrent.time)
        }

        while (activeDates.contains(checkStr)) {
            currentStreak++
            calCurrent.add(Calendar.DAY_OF_YEAR, -1)
            checkStr = dateFormat.format(calCurrent.time)
        }

        return Pair(currentStreak, bestStreak)
    }

    /**
     * BATCH 4: Prayer Consistency Score
     */
    fun calculateConsistencyScore(): Float {
        val records = _allRecords.value
        if (records.isEmpty()) return 0f
        
        val firstRecord = records.minByOrNull { it.date } ?: return 0f
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startDate = dateFormat.parse(firstRecord.date) ?: return 0f
        val today = Calendar.getInstance().time
        
        val diffInMillies = Math.abs(today.time - startDate.time)
        val daysSinceInstall = (diffInMillies / (1000 * 60 * 60 * 24)).toInt() + 1
        
        val totalPossible = daysSinceInstall * 5
        val totalOffered = records.count { it.isOffered }
        
        return if (totalPossible == 0) 0f else (totalOffered.toFloat() / totalPossible) * 100f
    }

    fun getGrade(score: Float): String = when {
        score >= 90 -> "A+ Excellent"
        score >= 75 -> "A Good"
        score >= 60 -> "B Average"
        score >= 40 -> "C Needs Improvement"
        else -> "D Keep Trying"
    }

    /**
     * BATCH 4: Best/Worst Prayer Analysis
     */
    fun getPrayerConsistency(): Map<String, Float> {
        val records = _allRecords.value
        if (records.isEmpty()) return emptyMap()

        val consistencyMap = mutableMapOf<String, Float>()
        val prayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
        
        val firstRecord = records.minByOrNull { it.date }
        if (firstRecord == null) return emptyMap()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startDate = dateFormat.parse(firstRecord.date) ?: return emptyMap()
        val today = Calendar.getInstance().time
        val diffInMillies = Math.abs(today.time - startDate.time)
        val totalDays = (diffInMillies / (1000 * 60 * 60 * 24)).toInt() + 1

        for (prayer in prayers) {
            val offeredCount = records.count { it.prayerName.equals(prayer, ignoreCase = true) && it.isOffered }
            consistencyMap[prayer] = if (totalDays == 0) 0f else (offeredCount.toFloat() / totalDays) * 100f
        }

        return consistencyMap
    }

    /**
     * BATCH 4: Weekly Comparison
     */
    fun getWeeklyComparison(): Pair<Int, Int> {
        val records = _allRecords.value
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val startOfThisWeek = cal.time

        cal.add(Calendar.WEEK_OF_YEAR, -1)
        val startOfLastWeek = cal.time
        
        cal.add(Calendar.WEEK_OF_YEAR, 1)
        
        var thisWeekCount = 0
        var lastWeekCount = 0

        for (record in records) {
            if (record.isOffered) {
                val recordDate = dateFormat.parse(record.date)
                if (recordDate != null) {
                    if (recordDate >= startOfThisWeek) {
                        thisWeekCount++
                    } else if (recordDate >= startOfLastWeek && recordDate < startOfThisWeek) {
                        lastWeekCount++
                    }
                }
            }
        }
        
        return Pair(thisWeekCount, lastWeekCount)
    }

    /**
     * BATCH 4: All Time Stats
     */
    fun getAllTimeStats(): Triple<Int, Int, Int> {
        val records = _allRecords.value
        val totalOffered = records.count { it.isOffered }
        
        val firstRecord = records.minByOrNull { it.date }
        val daysActive = if (firstRecord != null) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val startDate = dateFormat.parse(firstRecord.date)
            if (startDate != null) {
                val today = Calendar.getInstance().time
                val diffInMillies = Math.abs(today.time - startDate.time)
                (diffInMillies / (1000 * 60 * 60 * 24)).toInt() + 1
            } else 0
        } else 0
        
        val totalPossible = daysActive * 5
        val totalMissed = if (daysActive > 0) totalPossible - totalOffered else 0
        
        return Triple(totalOffered, totalMissed, daysActive)
    }

    /**
     * BATCH 4: Monthly Comparison Chart
     * Returns a list of Pair(MonthAbbreviation, OfferedCount) for the last 6 months
     */
    fun getMonthlyStats(): List<Pair<String, Int>> {
        val records = _allRecords.value
        val result = mutableListOf<Pair<String, Int>>()
        
        val cal = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val fullDateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        
        // Go back 5 months to start (so we get 6 months total including current)
        cal.add(Calendar.MONTH, -5)
        
        for (i in 0 until 6) {
            val monthLabel = monthFormat.format(cal.time)
            val monthKey = fullDateFormat.format(cal.time)
            
            val count = records.count { 
                it.isOffered && it.date.startsWith(monthKey) 
            }
            
            result.add(Pair(monthLabel, count))
            cal.add(Calendar.MONTH, 1)
        }
        
        return result
    }
}

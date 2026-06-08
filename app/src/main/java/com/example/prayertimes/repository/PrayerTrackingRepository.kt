package com.example.prayertimes.repository

import com.example.prayertimes.data.local.PrayerRecordDao
import com.example.prayertimes.data.model.Prayer
import com.example.prayertimes.data.model.PrayerRecord
import kotlinx.coroutines.flow.Flow

class PrayerTrackingRepository(private val dao: PrayerRecordDao) {

    suspend fun togglePrayerOffered(date: String, prayer: Prayer, isOffered: Boolean) {
        val record = PrayerRecord(
            date = date,
            prayerName = prayer.name,
            isOffered = isOffered,
            timestamp = System.currentTimeMillis()
        )
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            dao.insertOrUpdate(record)
        }
    }

    fun getRecordsForDate(date: String): Flow<List<PrayerRecord>> {
        return dao.getRecordsForDate(date)
    }

    fun getRecordsForDateRange(startDate: String, endDate: String): Flow<List<PrayerRecord>> {
        return dao.getRecordsForDateRange(startDate, endDate)
    }

    fun getAllRecords(): Flow<List<PrayerRecord>> = dao.getAllRecords()

    fun getTotalOfferedCount(): Flow<Int> = dao.getTotalOfferedCount()

    fun getTotalMissedCount(): Flow<Int> = dao.getTotalMissedCount()

    fun getFirstRecordDate(): Flow<String?> = dao.getFirstRecordDate()

    fun getOfferedCountForDateRange(startDate: String, endDate: String): Flow<Int> = dao.getOfferedCountForDateRange(startDate, endDate)
}

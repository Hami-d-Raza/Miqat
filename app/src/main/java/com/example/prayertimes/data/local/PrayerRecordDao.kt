package com.example.prayertimes.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.prayertimes.data.model.PrayerRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(record: PrayerRecord)

    @Query("SELECT * FROM prayer_records WHERE date = :date")
    fun getRecordsForDate(date: String): Flow<List<PrayerRecord>>

    @Query("SELECT * FROM prayer_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getRecordsForDateRange(startDate: String, endDate: String): Flow<List<PrayerRecord>>

    @Query("SELECT * FROM prayer_records ORDER BY date ASC")
    fun getAllRecords(): Flow<List<PrayerRecord>>

    @Query("SELECT COUNT(*) FROM prayer_records WHERE isOffered = 1")
    fun getTotalOfferedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM prayer_records WHERE isOffered = 0")
    fun getTotalMissedCount(): Flow<Int>

    @Query("SELECT date FROM prayer_records ORDER BY date ASC LIMIT 1")
    fun getFirstRecordDate(): Flow<String?>

    @Query("SELECT COUNT(*) FROM prayer_records WHERE date BETWEEN :startDate AND :endDate AND isOffered = 1")
    fun getOfferedCountForDateRange(startDate: String, endDate: String): Flow<Int>
}

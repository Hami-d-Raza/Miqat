package com.example.prayertimes.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "prayer_records",
    indices = [Index(value = ["date", "prayerName"], unique = true)]
)
data class PrayerRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // Format: YYYY-MM-DD
    val prayerName: String, // String representation of Prayer enum
    val isOffered: Boolean,
    val timestamp: Long
)

package com.example.prayertimes.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarked_ayahs")
data class BookmarkedAyah(
    val surahNumber: Int,
    val ayahNumber: Int,
    val timestamp: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)

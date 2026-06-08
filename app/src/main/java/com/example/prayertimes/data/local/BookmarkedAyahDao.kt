package com.example.prayertimes.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.prayertimes.data.model.BookmarkedAyah
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkedAyahDao {
    @Query("SELECT * FROM bookmarked_ayahs ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkedAyah>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBookmark(bookmark: BookmarkedAyah)

    @Query("DELETE FROM bookmarked_ayahs WHERE surahNumber = :surahNumber AND ayahNumber = :ayahNumber")
    fun deleteBookmark(surahNumber: Int, ayahNumber: Int)
    
    @Query("SELECT * FROM bookmarked_ayahs WHERE surahNumber = :surahNumber AND ayahNumber = :ayahNumber LIMIT 1")
    fun getBookmark(surahNumber: Int, ayahNumber: Int): BookmarkedAyah?
}

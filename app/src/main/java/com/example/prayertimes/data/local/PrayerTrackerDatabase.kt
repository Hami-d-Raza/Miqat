package com.example.prayertimes.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.prayertimes.data.model.PrayerRecord
import com.example.prayertimes.data.model.BookmarkedAyah
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [PrayerRecord::class, BookmarkedAyah::class], version = 2, exportSchema = false)
abstract class PrayerTrackerDatabase : RoomDatabase() {
    abstract fun prayerRecordDao(): PrayerRecordDao
    abstract fun bookmarkedAyahDao(): BookmarkedAyahDao

    companion object {
        @Volatile
        private var INSTANCE: PrayerTrackerDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `bookmarked_ayahs` (`surahNumber` INTEGER NOT NULL, `ayahNumber` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)")
            }
        }

        fun getDatabase(context: Context): PrayerTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PrayerTrackerDatabase::class.java,
                    "prayer_tracker_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

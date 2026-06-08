package com.example.prayertimes.repository

import android.content.Context
import com.example.prayertimes.data.model.Surah
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStreamReader

class QuranRepository(private val context: Context) {
    private var cachedSurahs: List<Surah>? = null

    suspend fun getSurahs(): List<Surah> = withContext(Dispatchers.IO) {
        if (cachedSurahs == null) {
            val inputStream = context.assets.open("data/quran.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val json = Json { ignoreUnknownKeys = true }
            cachedSurahs = json.decodeFromString<List<Surah>>(jsonString)
        }
        cachedSurahs!!
    }
    
    suspend fun getSurah(number: Int): Surah? {
        val surahs = getSurahs()
        return surahs.find { it.number == number }
    }
}

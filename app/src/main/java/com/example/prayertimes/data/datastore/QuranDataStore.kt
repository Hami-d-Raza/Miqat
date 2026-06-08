package com.example.prayertimes.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.quranDataStore: DataStore<Preferences> by preferencesDataStore(name = "quran_settings")

class QuranDataStore(private val context: Context) {

    companion object {
        val LAST_READ_SURAH = intPreferencesKey("last_read_surah")
        val LAST_READ_AYAH = intPreferencesKey("last_read_ayah")
        val QURAN_FONT_SIZE = floatPreferencesKey("quran_font_size")
        val QURAN_TRANSLATION_LANG = stringPreferencesKey("quran_translation_lang")
    }

    val lastReadSurah: Flow<Int?> = context.quranDataStore.data.map { it[LAST_READ_SURAH] }
    val lastReadAyah: Flow<Int?> = context.quranDataStore.data.map { it[LAST_READ_AYAH] }
    val fontSizeMultiplier: Flow<Float> = context.quranDataStore.data.map { it[QURAN_FONT_SIZE] ?: 1.0f }
    val translationLang: Flow<String> = context.quranDataStore.data.map { it[QURAN_TRANSLATION_LANG] ?: "en" }

    suspend fun saveLastRead(surah: Int, ayah: Int) {
        context.quranDataStore.edit { prefs ->
            prefs[LAST_READ_SURAH] = surah
            prefs[LAST_READ_AYAH] = ayah
        }
    }

    suspend fun clearLastRead() {
        context.quranDataStore.edit { prefs ->
            prefs.remove(LAST_READ_SURAH)
            prefs.remove(LAST_READ_AYAH)
        }
    }

    suspend fun setFontSizeMultiplier(multiplier: Float) {
        context.quranDataStore.edit { prefs ->
            prefs[QURAN_FONT_SIZE] = multiplier
        }
    }

    suspend fun setTranslationLang(lang: String) {
        context.quranDataStore.edit { prefs ->
            prefs[QURAN_TRANSLATION_LANG] = lang
        }
    }
}

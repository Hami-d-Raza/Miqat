package com.example.prayertimes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.prayertimes.data.datastore.QuranDataStore
import com.example.prayertimes.data.local.PrayerTrackerDatabase
import com.example.prayertimes.data.model.BookmarkedAyah
import com.example.prayertimes.data.model.Surah
import com.example.prayertimes.repository.QuranRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

sealed class SurahLoadState {
    object Idle : SurahLoadState()
    object Loading : SurahLoadState()
    data class Ready(val ayahs: List<com.example.prayertimes.data.model.Ayah>, val scrollToAyah: Int = 0) : SurahLoadState()
    data class Error(val message: String) : SurahLoadState()
}

class QuranViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = QuranRepository(application)
    private val dataStore = QuranDataStore(application)
    private val bookmarkDao = PrayerTrackerDatabase.getDatabase(application).bookmarkedAyahDao()

    private val _surahs = MutableStateFlow<List<Surah>>(emptyList())
    val surahs: StateFlow<List<Surah>> = _surahs.asStateFlow()

    private val _surahLoadingState = MutableStateFlow<SurahLoadState>(SurahLoadState.Idle)
    val surahLoadingState: StateFlow<SurahLoadState> = _surahLoadingState.asStateFlow()
    
    private val _currentSurah = MutableStateFlow<Surah?>(null)
    val currentSurah: StateFlow<Surah?> = _currentSurah.asStateFlow()

    val bookmarks = bookmarkDao.getAllBookmarks().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val lastReadSurah = dataStore.lastReadSurah.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val lastReadAyah = dataStore.lastReadAyah.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val fontSizeMultiplier = dataStore.fontSizeMultiplier.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 1.0f
    )

    val translationLang = dataStore.translationLang.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "en"
    )

    init {
        loadAllSurahs()
    }

    private fun loadAllSurahs() {
        viewModelScope.launch {
            _surahs.value = repository.getSurahs()
        }
    }

    fun loadSurah(number: Int, scrollToAyah: Int = 0) {
        viewModelScope.launch(Dispatchers.IO) {
            _surahLoadingState.update { SurahLoadState.Loading }
            try {
                val surah = repository.getSurah(number)
                if (surah != null) {
                    _currentSurah.value = surah // keep this for compatibility if needed
                    _surahLoadingState.update { 
                        SurahLoadState.Ready(ayahs = surah.ayahs, scrollToAyah = scrollToAyah) 
                    }
                } else {
                    _surahLoadingState.update { SurahLoadState.Error("Surah not found") }
                }
            } catch(e: Exception) {
                _surahLoadingState.update { SurahLoadState.Error(e.message ?: "Failed to load") }
            }
        }
    }
    
    fun resetSurahState() {
        _surahLoadingState.update { SurahLoadState.Idle }
        _currentSurah.value = null
    }

    fun saveLastRead(surah: Int, ayah: Int) {
        viewModelScope.launch {
            dataStore.saveLastRead(surah, ayah)
        }
    }

    fun toggleBookmark(surahNumber: Int, ayahNumber: Int) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val existing = bookmarkDao.getBookmark(surahNumber, ayahNumber)
            if (existing != null) {
                bookmarkDao.deleteBookmark(surahNumber, ayahNumber)
            } else {
                bookmarkDao.insertBookmark(BookmarkedAyah(surahNumber, ayahNumber))
            }
        }
    }

    fun setFontSizeMultiplier(multiplier: Float) {
        viewModelScope.launch {
            dataStore.setFontSizeMultiplier(multiplier)
        }
    }

    fun setTranslationLang(lang: String) {
        viewModelScope.launch {
            dataStore.setTranslationLang(lang)
        }
    }
}

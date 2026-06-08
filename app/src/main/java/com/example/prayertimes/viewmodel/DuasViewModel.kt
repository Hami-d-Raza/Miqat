package com.example.prayertimes.viewmodel

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.duasDataStore by preferencesDataStore(name = "duas_prefs")

class DuasViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.duasDataStore
    private val FAVORITES_KEY = stringSetPreferencesKey("favorite_duas")

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    init {
        viewModelScope.launch {
            dataStore.data.map { preferences ->
                preferences[FAVORITES_KEY] ?: emptySet()
            }.collect { favs ->
                _favorites.value = favs
            }
        }
    }

    fun toggleFavorite(duaId: String) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                val current = preferences[FAVORITES_KEY] ?: emptySet()
                val newSet = if (current.contains(duaId)) {
                    current - duaId
                } else {
                    current + duaId
                }
                preferences[FAVORITES_KEY] = newSet
            }
        }
    }
}

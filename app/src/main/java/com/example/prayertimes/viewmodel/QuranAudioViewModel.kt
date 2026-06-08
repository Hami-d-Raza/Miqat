package com.example.prayertimes.viewmodel

import android.content.ComponentName
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.prayertimes.audio.QuranAudioService
import com.example.prayertimes.data.model.Reciter
import com.example.prayertimes.data.repository.QuranAudioRepository
import com.example.prayertimes.repository.QuranRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AudioState(
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val currentSurah: Int = 0,
    val currentAyah: Int = 0,
    val selectedReciter: Reciter = Reciter.ALAFASY,
    val isAutoPlay: Boolean = true,
    val error: String? = null,
    val audioQuality: String = "128"
)

data class DownloadState(
    val isDownloading: Boolean = false,
    val progress: Float = 0f,
    val totalAyahs: Int = 0,
    val downloadedAyahs: Int = 0,
    val surahNumber: Int = 0,
    val isCancelled: Boolean = false,
    val downloadedSurahs: Set<Int> = emptySet()
)

class QuranAudioViewModel(application: android.app.Application) : androidx.lifecycle.AndroidViewModel(application) {
    
    private val audioRepository = QuranAudioRepository(application)
    private val quranRepository = QuranRepository(application)
    private val dataStore = com.example.prayertimes.data.datastore.SettingsDataStore(application).dataStore
    
    private val _audioState = MutableStateFlow(AudioState())
    val audioState: StateFlow<AudioState> = _audioState.asStateFlow()
    
    private val _downloadState = MutableStateFlow(DownloadState())
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()
    
    val currentPlayingAyah: StateFlow<Int> = _audioState
        .map { it.currentAyah }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)
    
    private var player: Player? = null
    private var controllerFuture: com.google.common.util.concurrent.ListenableFuture<MediaController>? = null
    private var playJob: kotlinx.coroutines.Job? = null
    
    init {
        viewModelScope.launch {
            dataStore.data.collect { prefs ->
                val name = prefs[stringPreferencesKey("selected_reciter")] ?: Reciter.ALAFASY.name
                val reciter = runCatching { Reciter.valueOf(name) }.getOrDefault(Reciter.ALAFASY)
                val quality = prefs[stringPreferencesKey("audio_quality")] ?: "128"
                _audioState.update { it.copy(selectedReciter = reciter, audioQuality = quality) }
                updateDownloadedSurahs()
            }
        }
    }
    
    fun updateDownloadedSurahs() {
        viewModelScope.launch {
            val downloaded = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                audioRepository.getDownloadedSurahs(_audioState.value.selectedReciter)
            }
            _downloadState.update { it.copy(downloadedSurahs = downloaded) }
        }
    }
    
    fun initPlayer(context: Context) {
        if (player != null) return
        val sessionToken = SessionToken(context, ComponentName(context, QuranAudioService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            val controller = controllerFuture?.get()
            player = controller
            controller?.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when(state) {
                        Player.STATE_BUFFERING -> _audioState.update { it.copy(isLoading = true) }
                        Player.STATE_READY -> _audioState.update { it.copy(isLoading = false) }
                        Player.STATE_ENDED -> {
                            _audioState.update { it.copy(isPlaying = false, isLoading = false) }
                            if(_audioState.value.isAutoPlay) playNextAyah(context)
                        }
                        Player.STATE_IDLE -> _audioState.update { it.copy(isLoading = false) }
                    }
                }
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _audioState.update { it.copy(isPlaying = isPlaying) }
                }
                override fun onPlayerError(error: PlaybackException) {
                    val msg = if (error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS) {
                        "Audio file not found (404). This reciter may not have this ayah."
                    } else if (error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED) {
                        "Network error. Check internet connection."
                    } else {
                        "Failed to load audio. Check internet connection."
                    }
                    _audioState.update { it.copy(error = msg, isLoading = false, isPlaying = false) }
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            })
        }, ContextCompat.getMainExecutor(context))
    }
    
    fun playAyah(context: Context, surahNumber: Int, ayahNumber: Int) {
        val reciter = _audioState.value.selectedReciter
        
        // Connectivity check
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val hasInternet = cm.activeNetwork != null && cm.getNetworkCapabilities(cm.activeNetwork)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        
        val quality = _audioState.value.audioQuality
        
        if (!hasInternet && !audioRepository.isAyahCached(reciter, surahNumber, ayahNumber, quality)) {
            Toast.makeText(context, "No internet connection. Download ayahs while online to listen offline.", Toast.LENGTH_LONG).show()
            return
        }

        playJob?.cancel()
        player?.stop()
        player?.clearMediaItems()

        _audioState.update { it.copy(currentSurah = surahNumber, currentAyah = ayahNumber, isLoading = true, error = null) }
        
        playJob = viewModelScope.launch {
            val file = audioRepository.downloadAndCacheAudio(reciter, surahNumber, ayahNumber, quality)
            val uri = if (file != null && file.exists()) {
                Uri.fromFile(file)
            } else {
                Uri.parse(audioRepository.getAudioUrl(reciter, surahNumber, ayahNumber, quality))
            }
            
            val surahName = quranRepository.getSurah(surahNumber)?.nameEnglish ?: "Surah $surahNumber"
            val metadata = MediaMetadata.Builder()
                .setTitle("$surahName - Ayah $ayahNumber")
                .setArtist(reciter.displayName)
                .build()

            val mediaItem = MediaItem.Builder()
                .setUri(uri)
                .setMediaMetadata(metadata)
                .build()
                
            player?.apply {
                clearMediaItems()
                addMediaItem(mediaItem)
                if (reciter == Reciter.ALAFASY_URDU || reciter == Reciter.MUAIQLY_URDU) {
                    val globalAyah = com.example.prayertimes.data.model.getGlobalAyahNumber(surahNumber, ayahNumber)
                    val urduUrl = "https://cdn.islamic.network/quran/audio/64/ur.khan/$globalAyah.mp3"
                    val urduMetadata = MediaMetadata.Builder()
                        .setTitle("$surahName - Ayah $ayahNumber (Urdu)")
                        .setArtist("Shamshad Ali Khan")
                        .build()
                    val urduItem = MediaItem.Builder()
                        .setUri(Uri.parse(urduUrl))
                        .setMediaMetadata(urduMetadata)
                        .build()
                    addMediaItem(urduItem)
                }
                prepare()
                play()
            }
        }
    }
    
    fun pauseResume() {
        player?.let {
            if(it.isPlaying) it.pause() else it.play()
        }
    }
    
    fun stop() {
        playJob?.cancel()
        player?.stop()
        player?.clearMediaItems()
        _audioState.update { it.copy(isPlaying = false, isLoading = false, currentAyah = 0, currentSurah = 0) }
    }
    
    fun playNextAyah(context: Context) {
        val state = _audioState.value
        if (state.currentSurah == 0 || state.currentAyah == 0) return
        
        viewModelScope.launch {
            val surah = quranRepository.getSurah(state.currentSurah) ?: return@launch
            if (state.currentAyah < surah.ayahCount) {
                playAyah(context, state.currentSurah, state.currentAyah + 1)
            } else {
                stop()
            }
        }
    }
    
    fun playPrevAyah(context: Context) {
        val state = _audioState.value
        if (state.currentSurah == 0 || state.currentAyah <= 1) return
        playAyah(context, state.currentSurah, state.currentAyah - 1)
    }
    
    fun selectReciter(reciter: Reciter) {
        stop()
        _audioState.update { it.copy(selectedReciter = reciter) }
        updateDownloadedSurahs()
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[stringPreferencesKey("selected_reciter")] = reciter.name
            }
        }
    }
    
    fun toggleAutoPlay() {
        _audioState.update { it.copy(isAutoPlay = !it.isAutoPlay) }
    }
    
    fun downloadSurah(context: Context, surahNumber: Int) {
        val reciter = _audioState.value.selectedReciter
        
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val hasInternet = cm.activeNetwork != null && cm.getNetworkCapabilities(cm.activeNetwork)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        
        if (!hasInternet) {
            Toast.makeText(context, "No internet connection to download surah.", Toast.LENGTH_LONG).show()
            return
        }

        viewModelScope.launch {
            val surah = quranRepository.getSurah(surahNumber) ?: return@launch
            val totalAyahs = surah.ayahCount
            _downloadState.update { it.copy(isDownloading = true, surahNumber = surahNumber, totalAyahs = totalAyahs, downloadedAyahs = 0, progress = 0f, isCancelled = false) }
            
            for (ayah in 1..totalAyahs) {
                if (_downloadState.value.isCancelled) {
                    break
                }
                audioRepository.downloadAndCacheAudio(reciter, surahNumber, ayah, _audioState.value.audioQuality)
                _downloadState.update { it.copy(downloadedAyahs = ayah, progress = ayah.toFloat() / totalAyahs) }
            }
            
            _downloadState.update { it.copy(isDownloading = false) }
            updateDownloadedSurahs()
        }
    }
    
    fun cancelDownload() {
        _downloadState.update { it.copy(isCancelled = true, isDownloading = false) }
    }
    
    override fun onCleared() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
        super.onCleared()
    }
}

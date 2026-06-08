package com.example.prayertimes.audio

import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class QuranAudioService : MediaSessionService() {
    
    private lateinit var mediaSession: MediaSession
    private lateinit var player: ExoPlayer
    
    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
        
        mediaSession = MediaSession.Builder(this, player)
            .build()
    }
    
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession
    
    override fun onDestroy() {
        mediaSession.release()
        player.release()
        super.onDestroy()
    }
}

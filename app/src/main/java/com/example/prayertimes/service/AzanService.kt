package com.example.prayertimes.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.prayertimes.MainActivity
import com.example.prayertimes.R

class AzanService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private val NOTIFICATION_ID = 2000
    private val CHANNEL_ID = "azan_foreground_channel"

    companion object {
        const val ACTION_START = "ACTION_START_AZAN"
        const val ACTION_STOP = "ACTION_STOP_AZAN"
        const val EXTRA_PRAYER_NAME = "EXTRA_PRAYER_NAME"
        const val EXTRA_AZAN_SOUND = "EXTRA_AZAN_SOUND"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopAzan()
            return START_NOT_STICKY
        }

        val prayerName = intent?.getStringExtra(EXTRA_PRAYER_NAME) ?: "Prayer"
        val azanSound = intent?.getStringExtra(EXTRA_AZAN_SOUND) ?: "Makkah Azan"

        if (azanSound != "Silent") {
            startForeground(NOTIFICATION_ID, createNotification(prayerName))
            playAzan(azanSound)
        } else {
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun playAzan(azanSound: String) {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }

        val resId = when (azanSound) {
            "Makkah Azan" -> R.raw.azan_asr_makkah
            "Makkah", "Fajr Special" -> R.raw.azan_makkah
            "Madinah" -> R.raw.azan_madinah
            "Short Beep" -> R.raw.short_beep
            else -> 0
        }

        if (resId != 0) {
            try {
                mediaPlayer = MediaPlayer.create(this, resId)
                mediaPlayer?.setOnCompletionListener {
                    stopAzan()
                }
                mediaPlayer?.start()
            } catch (e: Exception) {
                stopAzan()
            }
        } else {
            stopAzan()
        }
    }

    private fun stopAzan() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
        }
        mediaPlayer?.release()
        mediaPlayer = null
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAzan()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Azan Playing",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows when Azan is playing"
                setSound(null, null) // Sound is handled by MediaPlayer
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(prayerName: String): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AzanService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_mosque)
            .setContentTitle(prayerName)
            .setContentText("Azan is playing...")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_mosque, "Stop Azan", stopPendingIntent)
            .setOngoing(true)
            .build()
    }
}

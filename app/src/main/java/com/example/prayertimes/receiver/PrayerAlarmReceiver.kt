package com.example.prayertimes.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.prayertimes.service.AzanService
import com.example.prayertimes.utils.NotificationHelper

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("prayer_name") ?: return
        val prayerTime = intent.getStringExtra("prayer_time") ?: ""
        val azanSound = intent.getStringExtra("azan_sound") ?: "Makkah"

        NotificationHelper.showPrayerNotification(
            context = context,
            prayerName = prayerName,
            prayerTime = prayerTime,
            azanSound = azanSound
        )

        if (azanSound != "Silent") {
            val serviceIntent = Intent(context, AzanService::class.java).apply {
                putExtra(AzanService.EXTRA_PRAYER_NAME, prayerName)
                putExtra(AzanService.EXTRA_AZAN_SOUND, azanSound)
            }
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }
}

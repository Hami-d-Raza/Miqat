package com.example.prayertimes.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.prayertimes.utils.NotificationHelper

/**
 * Worker that fires when a prayer time arrives.
 * Posts a notification and optionally plays Azan sound.
 */
class PrayerAlarmWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prayerName = inputData.getString("prayer_name") ?: return Result.failure()
        val prayerTime = inputData.getString("prayer_time") ?: ""
        val azanSound = inputData.getString("azan_sound") ?: "Makkah"

        NotificationHelper.showPrayerNotification(
            context = applicationContext,
            prayerName = prayerName,
            prayerTime = prayerTime,
            azanSound = azanSound // Still passing azanSound to NotificationHelper to determine the channel
        )

        if (azanSound != "Silent") {
            val serviceIntent = android.content.Intent(applicationContext, com.example.prayertimes.service.AzanService::class.java).apply {
                putExtra(com.example.prayertimes.service.AzanService.EXTRA_PRAYER_NAME, prayerName)
                putExtra(com.example.prayertimes.service.AzanService.EXTRA_AZAN_SOUND, azanSound)
            }
            androidx.core.content.ContextCompat.startForegroundService(applicationContext, serviceIntent)
        }

        return Result.success()
    }
}

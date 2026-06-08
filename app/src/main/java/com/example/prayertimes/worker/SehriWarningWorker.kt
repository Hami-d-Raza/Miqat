package com.example.prayertimes.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.prayertimes.utils.NotificationHelper

class SehriWarningWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val fajrTime = inputData.getString("fajr_time") ?: return Result.failure()
        
        NotificationHelper.showReminderNotification(
            context = applicationContext,
            title = "⚠️ Sehri ends in 20 minutes!",
            text = "Fajr at $fajrTime — Eat and drink now",
            channelId = NotificationHelper.CHANNEL_ID_SEHRI,
            notificationId = "sehri_warning".hashCode()
        )

        return Result.success()
    }
}

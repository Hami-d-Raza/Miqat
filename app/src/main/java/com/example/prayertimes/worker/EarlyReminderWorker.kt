package com.example.prayertimes.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.prayertimes.utils.NotificationHelper

class EarlyReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prayerName = inputData.getString("prayer_name") ?: return Result.failure()
        val earlyMins = inputData.getInt("early_mins", 15)
        
        NotificationHelper.showReminderNotification(
            context = applicationContext,
            title = "$prayerName in $earlyMins minutes — Prepare for prayer \uD83D\uDD4C",
            text = "It's almost time for $prayerName prayer.",
            channelId = NotificationHelper.CHANNEL_ID_REMINDERS,
            notificationId = ("early_$prayerName").hashCode()
        )

        return Result.success()
    }
}

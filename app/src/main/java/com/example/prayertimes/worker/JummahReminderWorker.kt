package com.example.prayertimes.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.prayertimes.utils.NotificationHelper

class JummahReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val dhuhrTime = inputData.getString("dhuhr_time") ?: return Result.failure()
        
        NotificationHelper.showReminderNotification(
            context = applicationContext,
            title = "Jummah Mubarak! \uD83D\uDD4C",
            text = "Jummah prayer at $dhuhrTime",
            channelId = NotificationHelper.CHANNEL_ID_REMINDERS,
            notificationId = "jummah_reminder".hashCode()
        )

        return Result.success()
    }
}

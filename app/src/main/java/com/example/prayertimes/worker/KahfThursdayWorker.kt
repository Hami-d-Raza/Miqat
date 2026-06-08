package com.example.prayertimes.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.prayertimes.utils.NotificationHelper

class KahfThursdayWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        NotificationHelper.showReminderNotification(
            context = applicationContext,
            title = "Read Surah Al-Kahf tonight \uD83D\uDCD6",
            text = "(Friday starts at Maghrib)",
            channelId = NotificationHelper.CHANNEL_ID_KAHF,
            notificationId = "kahf_thursday".hashCode()
        )
        return Result.success()
    }
}

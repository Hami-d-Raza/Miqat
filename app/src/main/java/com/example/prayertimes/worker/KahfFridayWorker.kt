package com.example.prayertimes.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.prayertimes.utils.NotificationHelper

class KahfFridayWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        NotificationHelper.showReminderNotification(
            context = applicationContext,
            title = "Surah Al-Kahf Reminder",
            text = "Don't forget to read Surah Al-Kahf today \uD83D\uDCD6",
            channelId = NotificationHelper.CHANNEL_ID_KAHF,
            notificationId = "kahf_friday".hashCode()
        )
        return Result.success()
    }
}

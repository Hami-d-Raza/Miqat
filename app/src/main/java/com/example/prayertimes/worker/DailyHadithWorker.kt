package com.example.prayertimes.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.prayertimes.data.model.HadithData
import com.example.prayertimes.utils.NotificationHelper
import java.util.Calendar

class DailyHadithWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val hadithIndex = dayOfYear % HadithData.hadiths.size
        val hadith = HadithData.hadiths[hadithIndex]
        
        NotificationHelper.showReminderNotification(
            context = applicationContext,
            title = "Daily Hadith",
            text = "${hadith.text} — ${hadith.source}",
            channelId = NotificationHelper.CHANNEL_ID_HADITH,
            notificationId = "daily_hadith".hashCode()
        )

        // The scheduler in PrayerAlarmScheduler should have rescheduled this
        // but for safety we could also reschedule it here if we wanted.
        // Given that schedulePrayerAlarms runs every midnight and on boot,
        // it should handle the next day automatically.
        
        return Result.success()
    }
}

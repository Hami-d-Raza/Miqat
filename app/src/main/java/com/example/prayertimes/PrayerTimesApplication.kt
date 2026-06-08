package com.example.prayertimes

import android.app.Application
import com.example.prayertimes.utils.NotificationHelper

/**
 * Application class for initializing notification channels.
 */
class PrayerTimesApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Create notification channels
        NotificationHelper.createNotificationChannels(this)
    }
}

package com.example.prayertimes.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.prayertimes.data.datastore.SettingsDataStore
import com.example.prayertimes.data.model.Prayer
import com.example.prayertimes.repository.PrayerRepository
import com.example.prayertimes.utils.PrayerAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver that reschedules prayer alarms after device reboot.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val settingsDataStore = SettingsDataStore(context)
                    val settings = settingsDataStore.settingsFlow.first()

                    if (settings.isLocationSet) {
                        val repository = PrayerRepository()
                        val prayerTimesMap = repository.getPrayerTimesMap(settings)

                        val enabledPrayers = mutableSetOf<Prayer>()
                        if (settings.fajrNotification) enabledPrayers.add(Prayer.FAJR)
                        if (settings.dhuhrNotification) enabledPrayers.add(Prayer.DHUHR)
                        if (settings.asrNotification) enabledPrayers.add(Prayer.ASR)
                        if (settings.maghribNotification) enabledPrayers.add(Prayer.MAGHRIB)
                        if (settings.ishaNotification) enabledPrayers.add(Prayer.ISHA)

                        PrayerAlarmScheduler.schedulePrayerAlarms(
                            context = context,
                            prayerTimes = prayerTimesMap,
                            enabledPrayers = enabledPrayers,
                            settings = settings
                        )
                    }
                } catch (e: Exception) {
                    // Silently fail — alarms will be rescheduled when app opens
                }
            }
        }
    }
}

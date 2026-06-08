package com.example.prayertimes.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.prayertimes.MainActivity
import com.example.prayertimes.R
import com.example.prayertimes.data.datastore.SettingsDataStore
import com.example.prayertimes.repository.PrayerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Provides a home screen widget showing the next prayer time.
 */
class PrayerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Trigger a background update for all widgets
        CoroutineScope(Dispatchers.IO).launch {
            updateWidgets(context, appWidgetManager, appWidgetIds)
        }
    }

    private suspend fun updateWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val settingsDataStore = SettingsDataStore(context)
        val settings = settingsDataStore.settingsFlow.first()
        
        var title = "Next Prayer"
        var name = "Loading..."
        var time = "--:--"

        if (settings.isLocationSet) {
            val repository = PrayerRepository()
            try {
                val dailyTimes = repository.getDailyPrayerTimes(settings)
                
                if (settings.ramadanMode) {
                    title = "Next Ramadan Event"
                    val now = System.currentTimeMillis()
                    val fajrInfo = dailyTimes.prayers.find { it.prayer.name == "FAJR" }
                    val maghribInfo = dailyTimes.prayers.find { it.prayer.name == "MAGHRIB" }
                    
                    if (fajrInfo != null && maghribInfo != null) {
                        if (now < fajrInfo.time.toEpochMilliseconds()) {
                            name = "Sehri"
                            time = fajrInfo.formattedTime
                        } else if (now < maghribInfo.time.toEpochMilliseconds()) {
                            name = "Iftar"
                            time = maghribInfo.formattedTime
                        } else {
                            name = "Sehri"
                            // Next day's Fajr
                            val nextPrayerInfo = dailyTimes.prayers.find { it.isNext }
                            time = nextPrayerInfo?.formattedTime ?: "--:--"
                        }
                    }
                } else {
                    val nextPrayerInfo = dailyTimes.prayers.find { it.isNext }
                    if (nextPrayerInfo != null) {
                        name = nextPrayerInfo.name
                        time = nextPrayerInfo.formattedTime
                    }
                }
            } catch (e: Exception) {
                name = "Error"
            }
        } else {
            name = "Set Location"
        }

        // Update all widget instances
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_next_prayer)
            
            views.setTextViewText(R.id.widget_title, title)
            views.setTextViewText(R.id.widget_prayer_name, name)
            views.setTextViewText(R.id.widget_prayer_time, time)

            // Intent to open app
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

package com.example.prayertimes.data.model

import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

data class SimpleIslamicEvent(
    val name: String,
    val hijriMonth: Int,
    val hijriDay: Int,
    val description: String
)

data class UpcomingEvent(
    val event: SimpleIslamicEvent,
    val gregorianDate: LocalDate,
    val daysUntil: Long
)

object IslamicEventsData {
    val events = listOf(
        SimpleIslamicEvent("Islamic New Year", 1, 1, "First day of Muharram"),
        SimpleIslamicEvent("Day of Ashura", 1, 10, "Fasting recommended on this day"),
        SimpleIslamicEvent("Mawlid un Nabi", 3, 12, "Birth of Prophet Muhammad ﷺ"),
        SimpleIslamicEvent("Isra Wal Miraj", 7, 27, "Night Journey of the Prophet ﷺ"),
        SimpleIslamicEvent("Shab e Barat", 8, 15, "Night of forgiveness and mercy"),
        SimpleIslamicEvent("Ramadan Begins", 9, 1, "Month of fasting begins"),
        SimpleIslamicEvent("Laylatul Qadr", 9, 27, "Night better than a thousand months"),
        SimpleIslamicEvent("Eid ul Fitr", 10, 1, "Festival of breaking the fast"),
        SimpleIslamicEvent("Eid ul Adha", 12, 10, "Festival of sacrifice"),
        SimpleIslamicEvent("Hajj Day (Arafah)", 12, 9, "Day of Arafah — fasting recommended")
    )

    fun getNextEvent(): UpcomingEvent? {
        val todayGregorian = LocalDate.now()
        val todayHijri = HijrahDate.from(todayGregorian)
        val currentHijriYear = todayHijri.get(ChronoField.YEAR)

        val upcomingEvents = mutableListOf<UpcomingEvent>()

        for (event in events) {
            // Try current Hijri year
            var eventHijriDate: HijrahDate? = null
            try {
                eventHijriDate = HijrahDate.of(currentHijriYear, event.hijriMonth, event.hijriDay)
            } catch (e: Exception) {
                // Handle invalid dates (e.g. 30th of a 29-day month), try day 29 instead
                try {
                    eventHijriDate = HijrahDate.of(currentHijriYear, event.hijriMonth, event.hijriDay - 1)
                } catch (e2: Exception) {}
            }

            if (eventHijriDate != null) {
                val eventGregorian = LocalDate.from(eventHijriDate)
                if (!eventGregorian.isBefore(todayGregorian)) {
                    val daysUntil = java.time.temporal.ChronoUnit.DAYS.between(todayGregorian, eventGregorian)
                    upcomingEvents.add(UpcomingEvent(event, eventGregorian, daysUntil))
                } else {
                    // Try next Hijri year
                    try {
                        val nextYearHijriDate = HijrahDate.of(currentHijriYear + 1, event.hijriMonth, event.hijriDay)
                        val nextYearGregorian = LocalDate.from(nextYearHijriDate)
                        val daysUntil = java.time.temporal.ChronoUnit.DAYS.between(todayGregorian, nextYearGregorian)
                        upcomingEvents.add(UpcomingEvent(event, nextYearGregorian, daysUntil))
                    } catch (e: Exception) {
                        try {
                            val nextYearHijriDate = HijrahDate.of(currentHijriYear + 1, event.hijriMonth, event.hijriDay - 1)
                            val nextYearGregorian = LocalDate.from(nextYearHijriDate)
                            val daysUntil = java.time.temporal.ChronoUnit.DAYS.between(todayGregorian, nextYearGregorian)
                            upcomingEvents.add(UpcomingEvent(event, nextYearGregorian, daysUntil))
                        } catch (e2: Exception) {}
                    }
                }
            }
        }

        return upcomingEvents.minByOrNull { it.daysUntil }
    }
}

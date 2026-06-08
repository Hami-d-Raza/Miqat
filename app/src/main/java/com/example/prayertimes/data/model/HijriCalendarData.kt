package com.example.prayertimes.data.model

data class IslamicEvent(
    val name: String,
    val month: Int, // 0-indexed (0 = Muharram, 11 = Dhul Hijjah)
    val dayStart: Int,
    val dayEnd: Int,
    val description: String,
    val iconEmoji: String
)

object HijriCalendarData {
    val events = listOf(
        IslamicEvent(
            name = "Islamic New Year",
            month = 0,
            dayStart = 1,
            dayEnd = 1,
            description = "The beginning of the new Hijri year.",
            iconEmoji = "🌙"
        ),
        IslamicEvent(
            name = "Ashura",
            month = 0,
            dayStart = 10,
            dayEnd = 10,
            description = "The 10th day of Muharram. A day of fasting and reflection.",
            iconEmoji = "⭐"
        ),
        IslamicEvent(
            name = "Mawlid un Nabi",
            month = 2, // Rabi ul Awwal
            dayStart = 12,
            dayEnd = 12,
            description = "Observance of the birth of the Islamic prophet Muhammad.",
            iconEmoji = "🌸"
        ),
        IslamicEvent(
            name = "Isra Wal Miraj",
            month = 6, // Rajab
            dayStart = 27,
            dayEnd = 27,
            description = "The night journey and ascension of the Prophet Muhammad.",
            iconEmoji = "🌠"
        ),
        IslamicEvent(
            name = "Shab e Barat",
            month = 7, // Shaban
            dayStart = 15,
            dayEnd = 15,
            description = "The Night of Forgiveness.",
            iconEmoji = "✨"
        ),
        IslamicEvent(
            name = "Ramadan",
            month = 8, // Ramadan
            dayStart = 1,
            dayEnd = 30, // Covers the whole month
            description = "The holy month of fasting.",
            iconEmoji = "🌙"
        ),
        IslamicEvent(
            name = "Laylatul Qadr",
            month = 8,
            dayStart = 27,
            dayEnd = 27,
            description = "The Night of Decree, better than a thousand months.",
            iconEmoji = "⭐⭐"
        ),
        IslamicEvent(
            name = "Eid ul Fitr",
            month = 9, // Shawwal
            dayStart = 1,
            dayEnd = 1,
            description = "Festival of Breaking the Fast.",
            iconEmoji = "🎉"
        ),
        IslamicEvent(
            name = "Day of Arafah",
            month = 11, // Dhul Hijjah
            dayStart = 9,
            dayEnd = 9,
            description = "The 9th day of Dhul Hijjah. A crucial part of Hajj.",
            iconEmoji = "⭐"
        ),
        IslamicEvent(
            name = "Eid ul Adha",
            month = 11,
            dayStart = 10,
            dayEnd = 10,
            description = "Festival of Sacrifice.",
            iconEmoji = "🎉"
        )
    )

    val monthNames = listOf(
        "Muharram", "Safar", "Rabi al-Awwal", "Rabi al-Thani",
        "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )
}

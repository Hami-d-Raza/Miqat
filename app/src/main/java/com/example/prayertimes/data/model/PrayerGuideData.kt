package com.example.prayertimes.data.model

data class GuideStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val arabicText: String = "",
    val transliteration: String = "",
    val positionDescription: String = ""
)

object PrayerGuideData {
    val prayerSteps = listOf(
        GuideStep(
            stepNumber = 1,
            title = "Face the Qibla",
            description = "Stand facing the Qibla direction. Keep feet shoulder-width apart.",
            positionDescription = "Standing straight"
        ),
        GuideStep(
            stepNumber = 2,
            title = "Intention (Niyyah)",
            description = "Make intention in your heart for which prayer you are performing."
        ),
        GuideStep(
            stepNumber = 3,
            title = "Opening Takbeer",
            description = "Raise both hands to earlobes and say Allahu Akbar",
            arabicText = "اللَّهُ أَكْبَرُ",
            transliteration = "Allahu Akbar"
        ),
        GuideStep(
            stepNumber = 4,
            title = "Standing (Qiyam)",
            description = "Recite Surah Al-Fatiha then another Surah",
            arabicText = "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ",
            transliteration = "Alhamdu lillahi rabbil alameen..."
        ),
        GuideStep(
            stepNumber = 5,
            title = "Bowing (Ruku)",
            description = "Bow with back parallel to ground, hands on knees, recite 3 times",
            arabicText = "سُبْحَانَ رَبِّيَ الْعَظِيمِ",
            transliteration = "Subhana Rabbiyal Azeem"
        ),
        GuideStep(
            stepNumber = 6,
            title = "Prostration (Sujood)",
            description = "Prostrate with forehead, nose, both palms, knees and toes touching ground, recite 3 times",
            arabicText = "سُبْحَانَ رَبِّيَ الْأَعْلَى",
            transliteration = "Subhana Rabbiyal A'la"
        ),
        GuideStep(
            stepNumber = 7,
            title = "Final Sitting (Tashahhud)",
            description = "Sit and recite Tashahhud and Durood Ibrahim",
            arabicText = "التَّحِيَّاتُ لِلَّهِ وَالصَّلَوَاتُ وَالطَّيِّبَاتُ",
            transliteration = "At-tahiyyatu lillahi was-salawatu wat-tayyibat..."
        )
    )
}

object WuduGuideData {
    val wuduSteps = listOf(
        GuideStep(
            stepNumber = 1,
            title = "Intention (Niyyah)",
            description = "Make the intention in your heart to perform Wudu for purification."
        ),
        GuideStep(
            stepNumber = 2,
            title = "Say Bismillah",
            description = "Say Bismillah (In the Name of Allah) before starting.",
            arabicText = "بِسْمِ اللَّهِ",
            transliteration = "Bismillah"
        ),
        GuideStep(
            stepNumber = 3,
            title = "Wash Hands",
            description = "Wash both hands up to the wrists 3 times, making sure water reaches between the fingers."
        ),
        GuideStep(
            stepNumber = 4,
            title = "Rinse Mouth",
            description = "Take water into your mouth and rinse it thoroughly 3 times."
        ),
        GuideStep(
            stepNumber = 5,
            title = "Rinse Nose",
            description = "Sniff water into your nose and blow it out 3 times."
        ),
        GuideStep(
            stepNumber = 6,
            title = "Wash Face",
            description = "Wash your entire face from the hairline to the chin and from ear to ear 3 times."
        ),
        GuideStep(
            stepNumber = 7,
            title = "Wash Arms",
            description = "Wash your right arm up to the elbow 3 times, then the left arm 3 times."
        ),
        GuideStep(
            stepNumber = 8,
            title = "Wipe Head & Ears",
            description = "Wipe your wet hands over your head from front to back, then use index fingers to clean inside the ears and thumbs behind the ears (1 time)."
        ),
        GuideStep(
            stepNumber = 9,
            title = "Wash Feet",
            description = "Wash your right foot up to the ankle 3 times, making sure to clean between the toes, then do the same for the left foot."
        )
    )
}

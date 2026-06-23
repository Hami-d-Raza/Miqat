package com.example.prayertimes.data.model

import com.example.prayertimes.R

data class GuideStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val positionDescription: String = ""
)

object PrayerGuideData {
    val prayerSteps = listOf(
        GuideStep(
            stepNumber = 1,
            title = "Face the Qibla",
            description = "Stand facing the Qibla direction (Makkah). Keep your eyes focused on the place of prostration. Keep feet shoulder-width apart.",
            positionDescription = "Standing straight"
        ),
        GuideStep(
            stepNumber = 2,
            title = "Intention (Niyyah)",
            description = "Make intention in your heart for which prayer you are performing. It does not need to be said aloud."
        ),
        GuideStep(
            stepNumber = 3,
            title = "Opening Takbeer",
            description = "Raise both hands to your earlobes and say Allahu Akbar (Allah is the Greatest) to begin the prayer."
        ),
        GuideStep(
            stepNumber = 4,
            title = "Standing (Qiyam)",
            description = "Place your right hand over your left hand on your chest. Recite the opening supplication (Sana), followed by Surah Al-Fatiha."
        ),
        GuideStep(
            stepNumber = 5,
            title = "Bowing (Ruku)",
            description = "Say Allahu Akbar and bow down with your back parallel to the ground and your hands on your knees. Recite the glorification 3 times."
        ),
        GuideStep(
            stepNumber = 6,
            title = "Prostration (Sujood)",
            description = "Say Allahu Akbar and prostrate on the ground. Your forehead, nose, both palms, knees, and toes must touch the ground. Recite the glorification 3 times."
        ),
        GuideStep(
            stepNumber = 7,
            title = "Final Sitting (Tashahhud)",
            description = "Sit on your left foot while keeping your right foot upright. Recite the Tashahhud, sending blessings upon the Prophet (Durood Ibrahim), and finally say the Salam to the right and left."
        )
    )
}

object WuduGuideData {
    val wuduSteps = listOf(
        GuideStep(
            stepNumber = 1,
            title = "Intention (Niyyah)",
            description = "Make the intention in your heart to perform Wudu for purification before starting."
        ),
        GuideStep(
            stepNumber = 2,
            title = "Say Bismillah",
            description = "Say Bismillah (In the Name of Allah) before beginning to wash."
        ),
        GuideStep(
            stepNumber = 3,
            title = "Wash Hands",
            description = "Wash both hands up to the wrists 3 times, ensuring water reaches between all fingers."
        ),
        GuideStep(
            stepNumber = 4,
            title = "Rinse Mouth",
            description = "Take water into your mouth with your right hand and rinse it thoroughly 3 times."
        ),
        GuideStep(
            stepNumber = 5,
            title = "Rinse Nose",
            description = "Sniff water lightly into your nose with your right hand and blow it out using your left hand 3 times."
        ),
        GuideStep(
            stepNumber = 6,
            title = "Wash Face",
            description = "Wash your entire face from the hairline to the chin and from ear to ear 3 times."
        ),
        GuideStep(
            stepNumber = 7,
            title = "Wash Arms",
            description = "Wash your right arm completely from the fingertips up to and including the elbow 3 times, then repeat for the left arm 3 times."
        ),
        GuideStep(
            stepNumber = 8,
            title = "Wipe Head & Ears",
            description = "Wipe your wet hands over your head from front to back. Then use your index fingers to clean inside the ears and thumbs behind the ears (1 time)."
        ),
        GuideStep(
            stepNumber = 9,
            title = "Wash Feet",
            description = "Wash your right foot up to and including the ankle 3 times, making sure to clean between the toes with your pinky finger. Then do the same for the left foot 3 times."
        )
    )
}

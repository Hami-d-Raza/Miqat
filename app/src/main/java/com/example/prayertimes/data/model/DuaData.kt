package com.example.prayertimes.data.model

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.persistentListOf

/**
 * Represents a single Dua (supplication).
 */
@Stable
data class Dua(
    val id: Int,
    val title: String = "",
    val arabic: String = "",
    val transliteration: String = "",
    val translation: String = "",
    val category: DuaCategory
)

enum class DuaCategory(val displayName: String, val emoji: String) {
    AFTER_PRAYER("After Prayer", "🕌"),
    MORNING("Morning Duas", "🌅"),
    EVENING("Evening Duas", "🌆"),
    DAILY_LIFE("Daily Life", "🤲"),
    TRAVEL("Travel", "✈️")
}

/**
 * Complete hardcoded duas collection — fully offline.
 */
object DuasData {
    val duas = persistentListOf(

        // ── AFTER PRAYER ──────────────────────────────────────────────────────────
        Dua(
            id = 1,
            title = "Subhanallah (33x)",
            arabic = "سُبْحَانَ اللَّهِ",
            transliteration = "Subhaan-Allah",
            translation = "Glory be to Allah (recite 33 times)",
            category = DuaCategory.AFTER_PRAYER
        ),
        Dua(
            id = 2,
            title = "Alhamdulillah (33x)",
            arabic = "الْحَمْدُ لِلَّهِ",
            transliteration = "Alhamdu-lillah",
            translation = "All praise is due to Allah (recite 33 times)",
            category = DuaCategory.AFTER_PRAYER
        ),
        Dua(
            id = 3,
            title = "Allahu Akbar (33x)",
            arabic = "اللَّهُ أَكْبَرُ",
            transliteration = "Allahu Akbar",
            translation = "Allah is the Greatest (recite 33 times)",
            category = DuaCategory.AFTER_PRAYER
        ),
        Dua(
            id = 4,
            title = "Ayatul Kursi",
            arabic = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ",
            transliteration = "Allahu la ilaha illa huwal-Hayyul-Qayyum, la ta'khudhuhu sinatun wa la nawm...",
            translation = "Allah — there is no deity except Him, the Ever-Living, the Sustainer of existence. Neither drowsiness overtakes Him nor sleep. To Him belongs whatever is in the heavens and whatever is on the earth.",
            category = DuaCategory.AFTER_PRAYER
        ),
        Dua(
            id = 5,
            title = "Dua After Fardh Salah",
            arabic = "اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ",
            transliteration = "Allahumma antas-salam wa minkas-salam, tabarakta ya dhal-jalali wal-ikram",
            translation = "O Allah, You are Peace and from You is peace. Blessed are You, O Possessor of glory and honor.",
            category = DuaCategory.AFTER_PRAYER
        ),

        // ── MORNING ──────────────────────────────────────────────────────────────
        Dua(
            id = 6,
            title = "Dua Upon Waking Up",
            arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
            transliteration = "Alhamdu lillahil-lazi ahyana ba'da ma amatana wa ilayhin-nushur",
            translation = "All praise is due to Allah who has given us life after having taken it away, and unto Him is the resurrection.",
            category = DuaCategory.MORNING
        ),
        Dua(
            id = 7,
            title = "Morning Azkar (Asbahna)",
            arabic = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
            transliteration = "Asbahna wa asbahal-mulku lillah, walhamdu lillah, la ilaha illallahu wahdahu la sharika lah, lahul-mulku wa lahul-hamd, wa huwa 'ala kulli shay'in qadir",
            translation = "We have entered a new morning and with it all dominion belongs to Allah. All praise is for Allah. None has the right to be worshipped except Allah, alone, without partner.",
            category = DuaCategory.MORNING
        ),

        // ── EVENING ──────────────────────────────────────────────────────────────
        Dua(
            id = 8,
            title = "Dua Entering the Evening",
            arabic = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
            transliteration = "Amsayna wa amsal-mulku lillah, walhamdu lillah, la ilaha illallahu wahdahu la sharika lah",
            translation = "We have entered the evening, and sovereignty belongs to Allah. All praise is for Allah. None has the right to be worshipped except Allah, alone, without partner.",
            category = DuaCategory.EVENING
        ),
        Dua(
            id = 9,
            title = "Evening Protection",
            arabic = "اللَّهُمَّ بِكَ أَمْسَيْنَا، وَبِكَ أَصْبَحْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ الْمَصِيرُ",
            transliteration = "Allahumma bika amsayna wa bika asbahna wa bika nahya wa bika namutu wa ilaykal-masir",
            translation = "O Allah, by Your leave we have reached the evening and by Your leave we have reached the morning. By Your leave we live and die and unto You is our return.",
            category = DuaCategory.EVENING
        ),

        // ── DAILY LIFE ────────────────────────────────────────────────────────────
        Dua(
            id = 10,
            title = "Dua Before Eating",
            arabic = "بِسْمِ اللَّهِ",
            transliteration = "Bismillah",
            translation = "In the name of Allah.",
            category = DuaCategory.DAILY_LIFE
        ),
        Dua(
            id = 11,
            title = "Dua After Eating",
            arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنَا وَسَقَانَا وَجَعَلَنَا مُسْلِمِينَ",
            transliteration = "Alhamdu lillahil-lazi at'amana wa saqana wa ja'alana muslimin",
            translation = "All praise is due to Allah Who has given us food and drink and made us Muslims.",
            category = DuaCategory.DAILY_LIFE
        ),
        Dua(
            id = 12,
            title = "Dua Entering Home",
            arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ خَيْرَ الْمَوْلِجِ وَخَيْرَ الْمَخْرَجِ، بِسْمِ اللَّهِ وَلَجْنَا، وَبِسْمِ اللَّهِ خَرَجْنَا، وَعَلَى اللَّهِ رَبِّنَا تَوَكَّلْنَا",
            transliteration = "Allahumma inni as'aluka khayral-mawliji wa khayral-makhraji, bismillahi walajna wa bismillahi kharajna wa 'alallahi rabbina tawakkalna",
            translation = "O Allah, I ask You for the blessing of entering and the blessing of going out. In the Name of Allah we enter and in the Name of Allah we leave, and upon our Lord Allah we place our trust.",
            category = DuaCategory.DAILY_LIFE
        ),
        Dua(
            id = 13,
            title = "Dua Leaving Home",
            arabic = "بِسْمِ اللَّهِ تَوَكَّلْتُ عَلَى اللَّهِ، لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
            transliteration = "Bismillahi, tawakkaltu 'alallah, la hawla wa la quwwata illa billah",
            translation = "In the name of Allah, I place my trust in Allah, and there is no might nor power except with Allah.",
            category = DuaCategory.DAILY_LIFE
        ),
        Dua(
            id = 14,
            title = "Dua Before Sleeping",
            arabic = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
            transliteration = "Bismika Allahumma amutu wa ahya",
            translation = "In Your name, O Allah, I die and I live.",
            category = DuaCategory.DAILY_LIFE
        ),
        Dua(
            id = 15,
            title = "Dua in Distress",
            arabic = "لَا إِلَٰهَ إِلَّا أَنتَ سُبْحَانَكَ إِنِّي كُنتُ مِنَ الظَّالِمِينَ",
            transliteration = "La ilaha illa anta subhanaka inni kuntu minaz-zalimin",
            translation = "None has the right to be worshipped but You. Glorified be You. Truly I have been one of the wrongdoers.",
            category = DuaCategory.DAILY_LIFE
        ),
        Dua(
            id = 16,
            title = "Sayyidul Istighfar",
            arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَٰهَ إِلَّا أَنتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنتَ",
            transliteration = "Allahumma anta rabbi la ilaha illa anta, khalaqtani wa ana 'abduka...",
            translation = "O Allah, You are my Lord. There is no god but You. You created me and I am Your servant. I will abide by Your covenant and Your promise as best as I can. I seek refuge in You from the evil that I have done.",
            category = DuaCategory.DAILY_LIFE
        ),
        Dua(
            id = 17,
            title = "Dua for Parents",
            arabic = "رَبِّ ارْحَمْهُمَا كَمَا رَبَّيَانِي صَغِيرًا",
            transliteration = "Rabbir hamhuma kama rabbayani saghira",
            translation = "My Lord, have mercy upon them as they raised me when I was small.",
            category = DuaCategory.DAILY_LIFE
        ),

        // ── TRAVEL ────────────────────────────────────────────────────────────────
        Dua(
            id = 18,
            title = "Dua Before Journey",
            arabic = "اللَّهُمَّ إِنَّا نَسْأَلُكَ فِي سَفَرِنَا هَٰذَا الْبِرَّ وَالتَّقْوَى، وَمِنَ الْعَمَلِ مَا تَرْضَى",
            transliteration = "Allahumma inna nas'aluka fi safarina hazal-birra wat-taqwa, wa minal-'amali ma tarda",
            translation = "O Allah, we ask You on this journey for goodness and piety, and of deeds what pleases You.",
            category = DuaCategory.TRAVEL
        ),
        Dua(
            id = 19,
            title = "Dua Entering Vehicle",
            arabic = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَٰذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَىٰ رَبِّنَا لَمُنقَلِبُونَ",
            transliteration = "Subhanal-lazi sakhkhara lana haza wa ma kunna lahu muqrinin, wa inna ila rabbina lamunqalibun",
            translation = "Glory be to the One who has subjected this for us, though we would not have been capable of it, and surely to our Lord we are returning.",
            category = DuaCategory.TRAVEL
        )
    )
}

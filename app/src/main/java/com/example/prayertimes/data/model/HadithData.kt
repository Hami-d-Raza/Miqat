package com.example.prayertimes.data.model

data class Hadith(val text: String, val source: String)

object HadithData {
    val hadiths = listOf(
        Hadith("Actions are judged by intentions.", "Bukhari"),
        Hadith("The best of you are those who learn the Quran and teach it.", "Bukhari"),
        Hadith("Cleanliness is half of faith.", "Muslim"),
        Hadith("None of you truly believes until he loves for his brother what he loves for himself.", "Bukhari"),
        Hadith("The strong person is not the one who can wrestle someone else down. The strong person is the one who can control himself when he is angry.", "Bukhari"),
        Hadith("Make things easy, do not make them difficult.", "Bukhari"),
        Hadith("The most beloved of deeds to Allah are those that are most consistent, even if they are small.", "Bukhari"),
        Hadith("Whoever believes in Allah and the Last Day should speak good or remain silent.", "Bukhari"),
        Hadith("Do not waste water even if you are on a flowing river.", "Ibn Majah"),
        Hadith("Feed the hungry, visit the sick, and free the captive.", "Bukhari"),
        Hadith("Smiling at your brother is an act of charity.", "Tirmidhi"),
        Hadith("The best house among Muslims is the house in which orphans are well treated.", "Ibn Majah"),
        Hadith("He who does not thank people does not thank Allah.", "Abu Dawud"),
        Hadith("Whoever removes a worldly grief from a believer, Allah will remove one of his griefs on the Day of Judgment.", "Muslim"),
        Hadith("The world is a prison for the believer and a paradise for the unbeliever.", "Muslim"),
        Hadith("A good word is charity.", "Bukhari"),
        Hadith("Pay the worker his wages before his sweat dries.", "Ibn Majah"),
        Hadith("The merciful are shown mercy by the Most Merciful. Be merciful to those on earth and the One in the heavens will be merciful to you.", "Tirmidhi"),
        Hadith("Whoever is not grateful to people is not grateful to Allah.", "Abu Dawud"),
        Hadith("Make use of five before five: your youth before old age, your health before illness, your wealth before poverty, your free time before preoccupation, and your life before death.", "Hakim"),
        Hadith("The best of charity is that which is given when you are healthy and have little, fearing poverty and hoping for wealth.", "Abu Dawud"),
        Hadith("Indeed Allah does not look at your faces or your wealth but He looks at your hearts and your deeds.", "Muslim"),
        Hadith("The most perfect of the believers in faith are those with the best character.", "Tirmidhi"),
        Hadith("Seek knowledge from the cradle to the grave.", "Ibn Abdul Barr"),
        Hadith("Whoever follows a path in pursuit of knowledge, Allah will make a path to paradise easy for him.", "Muslim"),
        Hadith("The son of Adam does not fill any vessel worse than his stomach.", "Tirmidhi"),
        Hadith("Be in the world as if you were a stranger or a traveler.", "Bukhari"),
        Hadith("Verily Allah is gentle and loves gentleness in all matters.", "Bukhari"),
        Hadith("Take care of your health and your body, for it is a trust from Allah.", "Tabarani"),
        Hadith("Every act of kindness is charity.", "Bukhari")
    )
}

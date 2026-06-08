package com.example.prayertimes.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Surah(
    val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val ayahCount: Int,
    val ayahs: List<Ayah>
)

@Serializable
data class Ayah(
    val number: Int,
    val arabic: String,
    val translationEn: String,
    val translationUr: String
)

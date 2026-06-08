package com.example.prayertimes.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SavedLocation(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val cityName: String
)

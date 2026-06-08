package com.example.prayertimes.data.model

data class CompassData(
    val azimuth: Float = 0f,
    val qiblaBearing: Double = 0.0,
    val sensorAccuracy: SensorAccuracyLevel = SensorAccuracyLevel.UNKNOWN
)

enum class SensorAccuracyLevel(val displayName: String) {
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low — Please calibrate"),
    UNRELIABLE("Unreliable — Calibrate now"),
    UNKNOWN("Unknown")
}

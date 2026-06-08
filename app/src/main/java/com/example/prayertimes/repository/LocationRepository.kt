package com.example.prayertimes.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Provides the result of a location request.
 */
data class LocationResult(
    val latitude: Double,
    val longitude: Double,
    val cityName: String
)

/**
 * Repository for obtaining device location using FusedLocationProviderClient.
 */
class LocationRepository(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    /**
     * Gets the current location using GPS.
     * Returns null if location cannot be obtained.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationResult? {
        return try {
            val location = suspendCancellableCoroutine { continuation ->
                val cancellationToken = CancellationTokenSource()
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationToken.token
                ).addOnSuccessListener { loc ->
                    if (loc != null) {
                        continuation.resume(loc)
                    } else {
                        // Fall back to last known location
                        getLastKnownLocation(continuation)
                    }
                }.addOnFailureListener {
                    getLastKnownLocation(continuation)
                }

                continuation.invokeOnCancellation {
                    cancellationToken.cancel()
                }
            }

            val cityName = getCityName(location.latitude, location.longitude)
            LocationResult(
                latitude = location.latitude,
                longitude = location.longitude,
                cityName = cityName
            )
        } catch (e: Exception) {
            null
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation(
        continuation: kotlinx.coroutines.CancellableContinuation<android.location.Location>
    ) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    continuation.resume(loc)
                } else {
                    continuation.resume(
                        android.location.Location("default").apply {
                            latitude = 21.4225
                            longitude = 39.8262
                        }
                    )
                }
            }
            .addOnFailureListener {
                continuation.resume(
                    android.location.Location("default").apply {
                        latitude = 21.4225
                        longitude = 39.8262
                    }
                )
            }
    }

    /**
     * Reverse-geocodes coordinates to a city name.
     */
    @Suppress("DEPRECATION")
    private fun getCityName(latitude: Double, longitude: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                buildString {
                    address.locality?.let { append(it) }
                    address.countryName?.let {
                        if (isNotEmpty()) append(", ")
                        append(it)
                    }
                }.ifEmpty { "Unknown Location" }
            } else {
                "Unknown Location"
            }
        } catch (e: Exception) {
            "Unknown Location"
        }
    }

    /**
     * Geocodes a city name to coordinates.
     */
    @Suppress("DEPRECATION")
    suspend fun getCoordinatesForCity(cityName: String): LocationResult? {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocationName(cityName, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    LocationResult(
                        latitude = address.latitude,
                        longitude = address.longitude,
                        cityName = buildString {
                            address.locality?.let { append(it) }
                            address.countryName?.let {
                                if (isNotEmpty()) append(", ")
                                append(it)
                            }
                        }.ifEmpty { cityName }
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }
}

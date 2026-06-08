package com.example.prayertimes.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.prayertimes.data.model.CompassData
import com.example.prayertimes.data.model.SensorAccuracyLevel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Compass sensor using ONLY TYPE_ROTATION_VECTOR.
 * Alpha=0.03 (very smooth), dead zone=0.8°, SENSOR_DELAY_UI.
 */
class CompassSensor(context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    companion object {
        private const val ALPHA = 0.03f            // Very aggressive smoothing
        private const val DEAD_ZONE = 0.8f         // Min change to emit update
    }

    fun observeCompass(qiblaBearing: Double): Flow<CompassData> = callbackFlow {
        val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        if (rotationVectorSensor == null) {
            trySend(CompassData(
                azimuth = 0f,
                qiblaBearing = qiblaBearing,
                sensorAccuracy = SensorAccuracyLevel.UNRELIABLE
            ))
            awaitClose {}
            return@callbackFlow
        }

        val rotationMatrix   = FloatArray(9)
        val orientationAngles = FloatArray(3)
        var filteredAzimuth  = Float.NaN
        var lastEmitted      = Float.NaN
        var currentAccuracy  = SensorAccuracyLevel.HIGH

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return

                // Step 1 — get azimuth directly (no remapCoordinateSystem needed for portrait)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                var azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                azimuth = (azimuth + 360f) % 360f

                // Step 2 — low-pass filter with wraparound handling
                filteredAzimuth = if (filteredAzimuth.isNaN()) {
                    azimuth
                } else {
                    val delta = azimuth - filteredAzimuth
                    val wrapped = when {
                        delta > 180f  -> delta - 360f
                        delta < -180f -> delta + 360f
                        else          -> delta
                    }
                    ((filteredAzimuth + 0.03f * wrapped) + 360f) % 360f
                }

                // Step 3 — dead zone: only emit if change > 0.8°
                if (lastEmitted.isNaN()) {
                    lastEmitted = filteredAzimuth
                    trySend(CompassData(filteredAzimuth, qiblaBearing, currentAccuracy))
                } else {
                    var diff = filteredAzimuth - lastEmitted
                    if (diff > 180f) diff -= 360f
                    if (diff < -180f) diff += 360f
                    if (kotlin.math.abs(diff) >= DEAD_ZONE) {
                        lastEmitted = filteredAzimuth
                        trySend(CompassData(filteredAzimuth, qiblaBearing, currentAccuracy))
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                currentAccuracy = when (accuracy) {
                    SensorManager.SENSOR_STATUS_ACCURACY_HIGH   -> SensorAccuracyLevel.HIGH
                    SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> SensorAccuracyLevel.MEDIUM
                    SensorManager.SENSOR_STATUS_ACCURACY_LOW    -> SensorAccuracyLevel.LOW
                    SensorManager.SENSOR_STATUS_UNRELIABLE      -> SensorAccuracyLevel.UNRELIABLE
                    else -> SensorAccuracyLevel.UNKNOWN
                }
            }
        }

        // SENSOR_DELAY_UI — smooth, not jittery
        sensorManager.registerListener(listener, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}

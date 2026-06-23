package com.example.prayertimes

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.common.api.ResolvableApiException
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.view.View
import androidx.core.animation.doOnEnd
import com.example.prayertimes.data.datastore.SettingsDataStore
import com.example.prayertimes.theme.PrayerTimesTheme
import com.example.prayertimes.ui.navigation.AppNavigation
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

val LocalFontScale = compositionLocalOf { 1.0f }

class MainActivity : ComponentActivity() {

    private val settingsDataStore by lazy { SettingsDataStore(this) }

    private val gpsResolutionLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        // GPS prompt handled
    }

    @android.annotation.SuppressLint("InvalidFragmentVersionForActivityResult")
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            checkAndPromptLocationSettings()
        }
    }

    private fun checkAndPromptLocationSettings() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        
        client.checkLocationSettings(builder.build())
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        gpsResolutionLauncher.launch(
                            IntentSenderRequest.Builder(exception.resolution).build()
                        )
                    } catch (sendEx: Exception) {
                        // Ignore error
                    }
                }
            }
    }

    @android.annotation.SuppressLint("InvalidFragmentVersionForActivityResult")
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        // Notification permission handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("MIQAT_FATAL", "=== FATAL CRASH ===")
            android.util.Log.e("MIQAT_FATAL", "Thread: ${thread.name}")
            android.util.Log.e("MIQAT_FATAL", "Error: ${throwable.message}")
            android.util.Log.e("MIQAT_FATAL", "Cause: ${throwable.cause}")
            throwable.printStackTrace()
            defaultHandler?.uncaughtException(thread, throwable)
        }

        setTheme(R.style.Theme_App_Starting)
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Animation removed to prevent notification ANRs
        // Request permissions
        requestLocationPermissions()
        requestNotificationPermission()

        enableEdgeToEdge()
        setContent {
            val settings by settingsDataStore.settingsFlow.collectAsState(
                initial = com.example.prayertimes.data.model.AppSettings()
            )

            val isSystemDark = isSystemInDarkTheme()
            val darkTheme = when (settings.themeMode) {
                com.example.prayertimes.data.model.ThemeMode.DARK -> true
                com.example.prayertimes.data.model.ThemeMode.LIGHT -> false
                com.example.prayertimes.data.model.ThemeMode.SYSTEM -> isSystemDark
            }

            PrayerTimesTheme(darkTheme = darkTheme) {
                val currentDensity = androidx.compose.ui.platform.LocalDensity.current
                val customDensity = androidx.compose.ui.unit.Density(
                    density = currentDensity.density,
                    fontScale = currentDensity.fontScale * settings.fontSizeMultiplier
                )
                CompositionLocalProvider(
                    LocalFontScale provides settings.fontSizeMultiplier,
                    androidx.compose.ui.platform.LocalDensity provides customDensity
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation(startDestination = if (settings.onboardingComplete) "home" else "onboarding")
                    }
                }
            }
        }
    }

    private fun requestLocationPermissions() {
        val fineLocation = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocation = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (fineLocation != PackageManager.PERMISSION_GRANTED ||
            coarseLocation != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notifPermission = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            )
            if (notifPermission != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

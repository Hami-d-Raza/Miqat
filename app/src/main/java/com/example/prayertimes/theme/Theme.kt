package com.example.prayertimes.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Teal400,
    onPrimary = DarkSurface,
    primaryContainer = Teal800,
    onPrimaryContainer = Teal50,
    secondary = Gold500,
    onSecondary = DarkSurface,
    secondaryContainer = Gold700,
    onSecondaryContainer = Gold100,
    tertiary = NextPrayerGlow,
    background = DarkSurface,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainer = DarkCard,
    error = ErrorRed,
    outline = DarkOnSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = Teal800,
    onPrimary = LightSurface,
    primaryContainer = Teal200,
    onPrimaryContainer = Teal800,
    secondary = Gold700,
    onSecondary = LightSurface,
    secondaryContainer = Gold100,
    onSecondaryContainer = Gold700,
    tertiary = Teal400,
    background = LightSurface,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceContainer = LightCard,
    error = ErrorRed,
    outline = LightOnSurfaceVariant
)

@Composable
fun PrayerTimesTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PrayerTimesTypography,
        content = content
    )
}

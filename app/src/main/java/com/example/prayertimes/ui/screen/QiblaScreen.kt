package com.example.prayertimes.ui.screen

import com.example.prayertimes.R
import androidx.compose.ui.res.stringResource
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prayertimes.data.model.SensorAccuracyLevel
import com.example.prayertimes.theme.Gold500
import com.example.prayertimes.theme.Teal400
import com.example.prayertimes.ui.components.CompassView
import com.example.prayertimes.viewmodel.QiblaViewModel
import kotlinx.coroutines.delay
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.atan2
import androidx.compose.material.icons.automirrored.rounded.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen(viewModel: QiblaViewModel, onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    val compassData    by viewModel.compassData.collectAsStateWithLifecycle()
    val qiblaAngle     by viewModel.qiblaAngle.collectAsStateWithLifecycle()
    val needsCalibration by viewModel.needsCalibration.collectAsStateWithLifecycle()
    val isLocationSet  by viewModel.isLocationSet.collectAsStateWithLifecycle()
    val latitude       by viewModel.latitude.collectAsStateWithLifecycle()
    val longitude      by viewModel.longitude.collectAsStateWithLifecycle()
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); visible = true }

    val qiblaRelative  = ((qiblaAngle - compassData.azimuth + 360) % 360).toFloat()
    val isPointingToQibla = qiblaRelative < 5f || qiblaRelative > 355f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.qibla_direction)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = modifier.fillMaxSize().padding(paddingValues)) {
        if (!isLocationSet) {
            Column(
                Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Rounded.LocationOn, "Location needed", Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(16.dp))
                Text("Set Your Location", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Go to Settings to set your location\nfor Qibla direction.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))

                // Title
                AnimatedVisibility(visible, enter = fadeIn() + slideInVertically(initialOffsetY = { -40 }, animationSpec = spring(stiffness = Spring.StiffnessLow))) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Explore, "Qibla", Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        Text(stringResource(R.string.qibla_direction), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Calibration warning
                AnimatedVisibility(needsCalibration) {
                    Card(
                        Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Gold500.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.Warning, null, tint = Gold500, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.height(4.dp))
                            Text("Compass Calibration Needed", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Gold500)
                            Text(
                                "Move your phone in a figure-8 pattern\nto improve accuracy",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gold500.copy(0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Compass
                AnimatedVisibility(visible, enter = fadeIn(spring(stiffness = Spring.StiffnessVeryLow))) {
                    CompassView(
                        azimuth = compassData.azimuth,
                        qiblaBearing = qiblaAngle,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    )
                }

                // Heading + facing status
                Spacer(Modifier.height(8.dp))
                Text(
                    "Heading: ${"%.1f".format(compassData.azimuth)}°",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                androidx.compose.animation.AnimatedContent(
                    targetState = isPointingToQibla,
                    transitionSpec = { androidx.compose.animation.fadeIn(tween(500)) togetherWith androidx.compose.animation.fadeOut(tween(500)) },
                    label = "qiblaStatusCrossfade"
                ) { pointing ->
                    if (pointing) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Teal400.copy(0.15f)
                        ) {
                            Text(
                                "✓ Facing Qibla",
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Teal400
                            )
                        }
                    } else {
                        Text(
                            "Point phone toward Qibla direction",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Info card
                AnimatedVisibility(visible, enter = fadeIn() + slideInVertically(initialOffsetY = { 40 }, animationSpec = spring(stiffness = Spring.StiffnessLow))) {
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Box(
                            Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Brush.verticalGradient(listOf(Teal400.copy(0.05f), MaterialTheme.colorScheme.surfaceContainer)))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(stringResource(R.string.qibla_bearing), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "${"%.1f".format(qiblaAngle)}°",
                                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 48.sp, fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(stringResource(R.string.from_north), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                val accuracyColor = when (compassData.sensorAccuracy) {
                                    SensorAccuracyLevel.HIGH   -> Teal400
                                    SensorAccuracyLevel.MEDIUM -> Gold500
                                    else -> MaterialTheme.colorScheme.error
                                }
                                Text(
                                    "Sensor: ${compassData.sensorAccuracy.displayName}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = accuracyColor
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Distance to Makkah Card
                AnimatedVisibility(visible, enter = fadeIn() + slideInVertically(initialOffsetY = { 50 }, animationSpec = spring(stiffness = Spring.StiffnessLow))) {
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        val distance = calculateDistance(latitude, longitude, 21.4225, 39.8262)
                        val formattedDistance = if (distance > 1000) {
                            val format = java.text.NumberFormat.getNumberInstance(java.util.Locale.US)
                            format.maximumFractionDigits = 0
                            "${format.format(distance)} km"
                        } else {
                            "${distance.toInt()} km"
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = Teal400, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Distance to Makkah: $formattedDistance",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Calibration instructions card
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "📍 Tips for Accuracy",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "• Move phone in a figure-8 pattern to calibrate\n" +
                            "• Keep away from metal objects and magnets\n" +
                            "• Hold phone flat and level for best results",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(100.dp))
            }
            }
        }
    }
}

fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2).pow(2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2).pow(2)
    return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
}

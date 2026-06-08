package com.example.prayertimes.ui.screen

import android.content.Intent
import android.net.Uri
import com.example.prayertimes.R
import androidx.compose.ui.res.stringResource
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import java.time.LocalDate
import com.example.prayertimes.data.model.HadithData
import com.example.prayertimes.data.model.IslamicEventsData
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prayertimes.theme.Gold500
import com.example.prayertimes.theme.Teal400
import com.example.prayertimes.theme.Teal700
import com.example.prayertimes.ui.components.CountdownTimer
import com.example.prayertimes.ui.components.PrayerTimeCard
import com.example.prayertimes.ui.components.ShimmerPlaceholder
import com.example.prayertimes.ui.components.bounceClick
import com.example.prayertimes.viewmodel.HomeViewModel
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToStats: () -> Unit,
    onNavigateToTimetable: () -> Unit,
    onNavigateToAsmaulHusna: () -> Unit,
    onNavigateToQibla: () -> Unit,
    onNavigateToPrayerGuide: () -> Unit,
    onNavigateToHijriCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val prayerTimes by viewModel.prayerTimes.collectAsState()
    val countdownText by viewModel.countdownText.collectAsState()
    val nextEventName by viewModel.nextEventName.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var visible by remember { mutableStateOf(false) }
    var expandedPrayerIndex by remember { mutableStateOf<Int?>(null) }
    
    var isBatteryOptimized by remember { mutableStateOf(false) }
    
    var isRefreshing by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        visible = true
        isBatteryOptimized = !viewModel.isBatteryOptimizationIgnored()
    }
    
    LaunchedEffect(settings.isLocationSet) {
        if (settings.isLocationSet) {
            viewModel.checkAutoLocationUpdate()
        }
    }
    
    val showBatteryWarning = isBatteryOptimized && !settings.batteryWarningDismissed
    val locationUpdatePrompt by viewModel.locationUpdatePrompt.collectAsState()

    // Pulsing animation for the next prayer text
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator(color = Teal400)
            }
        } else if (!settings.isLocationSet) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Rounded.LocationOn, "Location needed", Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(16.dp))
                Text("Set Your Location", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                Text("Go to Settings to detect your location\nor enter coordinates manually.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        } else {
            prayerTimes?.let { daily ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        AnimatedVisibility(visible, enter = fadeIn() + slideInVertically(initialOffsetY = { -40 }, animationSpec = spring(stiffness = Spring.StiffnessLow))) {
                            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                if (settings.cityName.isNotEmpty()) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Rounded.LocationOn, stringResource(R.string.location), Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                        Text(settings.cityName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                }
                                Text(daily.date, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Rounded.NightsStay, "Hijri date", Modifier.size(16.dp), tint = Gold500)
                                    Text(daily.hijriDate, style = MaterialTheme.typography.bodyLarge, color = Gold500, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    item {
                        if (showBatteryWarning) {
                            val context = LocalContext.current
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("⚠️ Battery optimization may prevent Azan", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                                    Spacer(Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                        TextButton(onClick = { viewModel.dismissBatteryWarning() }) { Text("Dismiss") }
                                        Spacer(Modifier.width(8.dp))
                                        Button(onClick = { 
                                            val intent = Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${context.packageName}"))
                                            context.startActivity(intent)
                                            viewModel.dismissBatteryWarning()
                                        }) { Text("Fix Now") }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                        )
                    }

                    item {
                        AnimatedVisibility(visible, enter = fadeIn() + slideInVertically(initialOffsetY = { 60 }, animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy))) {
                            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(Brush.verticalGradient(listOf(Teal700.copy(alpha = 0.15f), Teal400.copy(alpha = 0.05f))))
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.compose.animation.AnimatedContent(
                                        targetState = nextEventName,
                                        transitionSpec = { androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(500)) togetherWith androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(500)) },
                                        label = "nextPrayerCrossfade"
                                    ) { eventName ->
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(if (settings.ramadanMode) "Next Ramadan Event" else stringResource(R.string.next_prayer), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                            Text(
                                                eventName, 
                                                style = MaterialTheme.typography.headlineMedium, 
                                                fontWeight = FontWeight.Bold, 
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.alpha(pulseAlpha)
                                            )
                                            CountdownTimer(countdownText)
                                            Text(stringResource(R.string.remaining), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    item {
                        Spacer(Modifier.height(4.dp))
                        Text(stringResource(R.string.prayer_times), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
                    }
                    
                    itemsIndexed(daily.prayers, key = { _, item -> item.prayer.name }) { index, prayerInfo ->
                        AnimatedVisibility(
                            visible = visible, 
                            enter = fadeIn(animationSpec = tween(300, delayMillis = index * 50)) + 
                                    slideInVertically(
                                        initialOffsetY = { 50 }, 
                                        animationSpec = tween(400, delayMillis = index * 50, easing = androidx.compose.animation.core.EaseOutQuint)
                                    )
                        ) {
                            val isExpanded = expandedPrayerIndex == index
                            val nextPrayerIndex = daily.prayers.indexOfFirst { it.isNext }
                            val isActive = if (nextPrayerIndex > 0) index == nextPrayerIndex - 1 else false
                            val endTime = if (index < daily.prayers.size - 1) daily.prayers[index + 1].formattedTime else ""
                            
                            PrayerTimeCard(
                                prayerTimeInfo = prayerInfo,
                                isExpanded = isExpanded,
                                countdownText = if (isActive) countdownText else "",
                                isActive = isActive,
                                endTime = endTime,
                                onClick = {
                                    if (prayerInfo.prayer != com.example.prayertimes.data.model.Prayer.SUNRISE) {
                                        expandedPrayerIndex = if (isExpanded) null else index
                                    }
                                },
                                onToggle = { isOffered ->
                                    viewModel.togglePrayerOffered(prayerInfo.prayer, isOffered)
                                }
                            )
                        }
                    }

                    item { Spacer(Modifier.height(8.dp)) }

                    item {
                        NearbyMosquesCard(
                            lat = settings.latitude,
                            lng = settings.longitude,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FeatureCard(
                                title = "Statistics",
                                icon = Icons.Rounded.BarChart,
                                onClick = onNavigateToStats,
                                modifier = Modifier.weight(1f).bounceClick(onClick = onNavigateToStats)
                            )
                            FeatureCard(
                                title = "Timetable",
                                icon = Icons.Rounded.CalendarMonth,
                                onClick = onNavigateToTimetable,
                                modifier = Modifier.weight(1f).bounceClick(onClick = onNavigateToTimetable)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FeatureCard(
                                title = "Qibla",
                                icon = Icons.Rounded.Explore,
                                onClick = onNavigateToQibla,
                                modifier = Modifier.weight(1f).bounceClick(onClick = onNavigateToQibla)
                            )
                            FeatureCard(
                                title = "Prayer Guide",
                                icon = Icons.Rounded.MenuBook,
                                onClick = onNavigateToPrayerGuide,
                                modifier = Modifier.weight(1f).bounceClick(onClick = onNavigateToPrayerGuide)
                            )
                        }
                    }

                    // Islamic Events Countdown
                    item {
                        val nextEvent = remember { IslamicEventsData.getNextEvent() }
                        
                        if (nextEvent != null) {
                            Spacer(Modifier.height(4.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Teal400.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Rounded.Event, contentDescription = null, tint = Teal700)
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(nextEvent.event.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text(nextEvent.event.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("Hijri: ${nextEvent.event.hijriDay}/${nextEvent.event.hijriMonth}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                    }
                                    Text(
                                        if (nextEvent.daysUntil == 0L) "Today" else "in ${nextEvent.daysUntil} days",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Teal700
                                    )
                                }
                            }
                        }
                    }
                    
                    // 99 Names Card
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FeatureCard(
                                title = "99 Names of Allah",
                                icon = Icons.Rounded.MenuBook,
                                onClick = onNavigateToAsmaulHusna,
                                modifier = Modifier.weight(1f).bounceClick(onClick = onNavigateToAsmaulHusna)
                            )
                            FeatureCard(
                                title = "Hijri Calendar",
                                icon = Icons.Rounded.DateRange,
                                onClick = onNavigateToHijriCalendar,
                                modifier = Modifier.weight(1f).bounceClick(onClick = onNavigateToHijriCalendar)
                            )
                        }
                    }

                    // Daily Hadith Card
                    item {
                        var hadithOffset by remember { mutableIntStateOf(0) }
                        var showHadithDialog by remember { mutableStateOf(false) }
                        
                        val dayIndex = (LocalDate.now().dayOfYear + hadithOffset) % HadithData.hadiths.size
                        val hadithIndex = if (dayIndex < 0) dayIndex + HadithData.hadiths.size else dayIndex
                        val hadith = HadithData.hadiths[hadithIndex]

                        Spacer(Modifier.height(4.dp))
                        Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).bounceClick(onClick = { showHadithDialog = true }),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(Icons.Rounded.FormatQuote, contentDescription = null, tint = Teal400, modifier = Modifier.size(24.dp))
                                    IconButton(
                                        onClick = { hadithOffset++ },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Rounded.Refresh, contentDescription = "Next Hadith", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    hadith.text,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface),
                                    maxLines = 3,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("— ${hadith.source}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                    Text("Tap to read more", style = MaterialTheme.typography.labelSmall, color = Teal400)
                                }
                            }
                        }

                        if (showHadithDialog) {
                            AlertDialog(
                                onDismissRequest = { showHadithDialog = false },
                                title = { Text("Hadith of the Day") },
                                text = { 
                                    Column(Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                                        Text(hadith.text, style = MaterialTheme.typography.bodyLarge)
                                        Spacer(Modifier.height(16.dp))
                                        Text("Source: ${hadith.source}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { showHadithDialog = false }) { Text("Close") }
                                }
                            )
                        }
                    }

                    item { Spacer(Modifier.height(100.dp)) }
                }
            }
        }
        
        if (locationUpdatePrompt != null) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissLocationUpdate() },
                title = { Text("Update Location?") },
                text = { Text("You seem to be in ${locationUpdatePrompt?.cityName}. Update prayer times?") },
                confirmButton = { TextButton(onClick = { viewModel.acceptLocationUpdate(locationUpdatePrompt!!) }) { Text("Yes") } },
                dismissButton = { TextButton(onClick = { viewModel.dismissLocationUpdate() }) { Text("No") } }
            )
        }
    }
}

@Composable
private fun NearbyMosquesCard(lat: Double, lng: Double, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                // Section 4 Fix: Correct intent format for finding mosques
                val mapsUri = Uri.parse("geo:0,0?q=masjid+near+$lat,$lng")
                val mapsIntent = Intent(Intent.ACTION_VIEW, mapsUri).apply {
                    setPackage("com.google.android.apps.maps")
                }
                if (mapsIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(mapsIntent)
                } else {
                    // Fallback to browser
                    val browserUri = Uri.parse("https://www.google.com/maps/search/masjid/@$lat,$lng,15z")
                    context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
                }
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(Gold500.copy(alpha = 0.08f), MaterialTheme.colorScheme.surfaceContainer)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Gold500.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Mosque, contentDescription = null, tint = Gold500, modifier = Modifier.size(24.dp))
            }
            Column {
                Text(stringResource(R.string.find_nearby_mosques), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("Opens in Google Maps", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FeatureCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = title, tint = Teal400, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
    }
}


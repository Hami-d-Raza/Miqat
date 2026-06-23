package com.example.prayertimes.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em
import com.example.prayertimes.R
import com.example.prayertimes.data.model.CalculationMethodOption
import com.example.prayertimes.data.model.MadhabOption
import com.example.prayertimes.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.media.MediaPlayer
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import java.util.Calendar
import java.util.Locale
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import com.example.prayertimes.data.model.SavedLocation
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    homeViewModel: com.example.prayertimes.viewmodel.HomeViewModel,
    quranViewModel: com.example.prayertimes.viewmodel.QuranViewModel,
    audioViewModel: com.example.prayertimes.viewmodel.QuranAudioViewModel,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val audioState by audioViewModel.audioState.collectAsState()
    val prayerTimes by homeViewModel.prayerTimes.collectAsState()
    val isDetectingLocation by viewModel.isDetectingLocation.collectAsState()
    val locationError by viewModel.locationError.collectAsState()
    val citySuggestions by viewModel.citySuggestions.collectAsState()
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); visible = true }

    val getTimeForPrayer = { prayer: com.example.prayertimes.data.model.Prayer -> 
        prayerTimes?.prayers?.find { it.prayer == prayer }?.formattedTime ?: ""
    }

    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp)
    ) {
        item {
            Column(Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(12.dp))
        AnimatedVisibility(visible, enter = fadeIn() + slideInVertically(initialOffsetY = { -30 }, animationSpec = spring(stiffness = Spring.StiffnessLow))) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                Icon(Icons.Rounded.Settings, "Settings", Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
                Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        // Location Section
        AnimatedVisibility(visible, enter = fadeIn() + slideInVertically(initialOffsetY = { 40 }, animationSpec = spring(stiffness = Spring.StiffnessLow))) {
            SettingsSection(stringResource(R.string.location), Icons.Rounded.LocationOn) {
                if (settings.isLocationSet) {
                    Text(settings.cityName.ifEmpty { "Custom Location" }, style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                    Text(String.format("%.4f°, %.4f°", settings.latitude, settings.longitude), style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    Spacer(Modifier.height(12.dp))
                }
                Button(onClick = { viewModel.detectLocation() }, Modifier.fillMaxWidth(), enabled = !isDetectingLocation, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    if (isDetectingLocation) { CircularProgressIndicator(Modifier.size(20.dp), MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)) }
                    else { Icon(Icons.Rounded.MyLocation, "Detect", Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)) }
                    Text(if (isDetectingLocation) "Detecting..." else "Detect My Location")
                }
                locationError?.let { Spacer(Modifier.height(4.dp)); Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
                Spacer(Modifier.height(12.dp)); HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant); Spacer(Modifier.height(12.dp))
                
                Text("Search City", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                var cityQuery by remember { mutableStateOf("") }
                var isCityDropdownExpanded by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = cityQuery,
                        onValueChange = { 
                            cityQuery = it
                            if (it.length >= 3) {
                                viewModel.fetchCitySuggestions(it)
                                isCityDropdownExpanded = true
                            } else {
                                isCityDropdownExpanded = false
                            }
                        },
                        placeholder = { Text("Search city name...", style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    DropdownMenu(
                        expanded = isCityDropdownExpanded && (citySuggestions.isNotEmpty() || cityQuery.length >= 3),
                        onDismissRequest = { isCityDropdownExpanded = false },
                        properties = androidx.compose.ui.window.PopupProperties(focusable = false),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (cityQuery.length >= 3 && citySuggestions.isEmpty() && isCityDropdownExpanded) {
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Searching...")
                                    }
                                },
                                onClick = {}
                            )
                        } else {
                            citySuggestions.forEach { suggestion ->
                                DropdownMenuItem(
                                    text = { Text(suggestion) },
                                    onClick = {
                                        cityQuery = suggestion
                                        isCityDropdownExpanded = false
                                        viewModel.searchCity(suggestion)
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Saved Locations
                val savedLocationsStr = settings.savedLocations
                val savedLocations = remember(savedLocationsStr) {
                    try {
                        Json.decodeFromString<List<SavedLocation>>(savedLocationsStr)
                    } catch (e: Throwable) {
                        emptyList()
                    }
                }
                
                if (savedLocations.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text("Saved Locations", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
                    androidx.compose.foundation.lazy.LazyRow(
                        state = lazyListState,
                        flingBehavior = androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior(lazyListState = lazyListState),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(savedLocations.size) { index ->
                            val loc = savedLocations[index]
                            val isCurrent = settings.latitude == loc.latitude && settings.longitude == loc.longitude
                            androidx.compose.material3.FilterChip(
                                selected = isCurrent,
                                onClick = { viewModel.selectSavedLocation(loc) },
                                label = { Text(loc.name, fontWeight = FontWeight.Bold) },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { viewModel.deleteSavedLocation(loc.id) },
                                        modifier = Modifier.size(16.dp)
                                    ) {
                                        Icon(Icons.Rounded.Close, "Delete", modifier = Modifier.size(14.dp))
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                var showSaveDialog by remember { mutableStateOf(false) }
                if (showSaveDialog) {
                    var locName by remember { mutableStateOf("") }
                    AlertDialog(
                        onDismissRequest = { showSaveDialog = false },
                        title = { Text("Save Location") },
                        text = {
                            OutlinedTextField(
                                value = locName,
                                onValueChange = { locName = it },
                                label = { Text("Location Name (e.g. Home, Work)") },
                                singleLine = true
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                if (locName.isNotBlank()) {
                                    viewModel.saveCurrentLocation(locName)
                                    showSaveDialog = false
                                }
                            }) { Text("Save") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showSaveDialog = false }) { Text("Cancel") }
                        }
                    )
                }
                if (settings.isLocationSet && savedLocations.size < 3) {
                    OutlinedButton(
                        onClick = { showSaveDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.BookmarkAdd, "Save", Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Save Current Location")
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        // Calculation Section
        AnimatedVisibility(visible, enter = fadeIn() + slideInVertically(initialOffsetY = { 60 }, animationSpec = spring(stiffness = Spring.StiffnessLow))) {
            SettingsSection("Calculation", Icons.Rounded.Tune) {
                DropdownSetting(stringResource(R.string.calculation_method), settings.calculationMethod.displayName, CalculationMethodOption.values().map { it.displayName }) { viewModel.updateCalculationMethod(CalculationMethodOption.values()[it]) }
                Spacer(Modifier.height(12.dp))
                DropdownSetting("Madhab (Namaz calculation)", settings.madhab.displayName, MadhabOption.values().map { it.displayName }) { viewModel.updateMadhab(MadhabOption.values()[it]) }
                
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                Text("Prayer Time Adjustments (Minutes)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                
                OffsetRow("Fajr", settings.fajrOffset) { viewModel.updatePrayerOffset("fajr", it) }
                OffsetRow("Dhuhr", settings.dhuhrOffset) { viewModel.updatePrayerOffset("dhuhr", it) }
                OffsetRow("Asr", settings.asrOffset) { viewModel.updatePrayerOffset("asr", it) }
                OffsetRow("Maghrib", settings.maghribOffset) { viewModel.updatePrayerOffset("maghrib", it) }
                OffsetRow("Isha", settings.ishaOffset) { viewModel.updatePrayerOffset("isha", it) }
            }
        }
        Spacer(Modifier.height(12.dp))
        // Notifications Section
        AnimatedVisibility(visible, enter = fadeIn() + slideInVertically(initialOffsetY = { 80 }, animationSpec = spring(stiffness = Spring.StiffnessLow))) {
            SettingsSection(stringResource(R.string.notifications), Icons.Rounded.Notifications) {
                
                val context = androidx.compose.ui.platform.LocalContext.current
                val testNotification = { prayerName: String ->
                    val intent = Intent(context, com.example.prayertimes.service.AzanService::class.java).apply {
                        putExtra(com.example.prayertimes.service.AzanService.EXTRA_PRAYER_NAME, "$prayerName (Test)")
                        val assignedSound = when (prayerName) {
                            "Fajr" -> settings.fajrSound
                            "Dhuhr" -> settings.dhuhrSound
                            "Asr" -> settings.asrSound
                            "Maghrib" -> settings.maghribSound
                            "Isha" -> settings.ishaSound
                            else -> settings.azanSound
                        }
                        putExtra(com.example.prayertimes.service.AzanService.EXTRA_AZAN_SOUND, assignedSound)
                    }
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                }

                PrayerNotificationRow(stringResource(R.string.fajr), getTimeForPrayer(com.example.prayertimes.data.model.Prayer.FAJR), settings.fajrNotification, { viewModel.updatePrayerNotification("fajr", it) }, { testNotification("Fajr") })
                PrayerNotificationRow(stringResource(R.string.dhuhr), getTimeForPrayer(com.example.prayertimes.data.model.Prayer.DHUHR), settings.dhuhrNotification, { viewModel.updatePrayerNotification("dhuhr", it) }, { testNotification("Dhuhr") })
                PrayerNotificationRow(stringResource(R.string.asr), getTimeForPrayer(com.example.prayertimes.data.model.Prayer.ASR), settings.asrNotification, { viewModel.updatePrayerNotification("asr", it) }, { testNotification("Asr") })
                PrayerNotificationRow(stringResource(R.string.maghrib), getTimeForPrayer(com.example.prayertimes.data.model.Prayer.MAGHRIB), settings.maghribNotification, { viewModel.updatePrayerNotification("maghrib", it) }, { testNotification("Maghrib") })
                PrayerNotificationRow(stringResource(R.string.isha), getTimeForPrayer(com.example.prayertimes.data.model.Prayer.ISHA), settings.ishaNotification, { viewModel.updatePrayerNotification("isha", it) }, { testNotification("Isha") })
                
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                
                var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
                var playingPrayer by remember { mutableStateOf<String?>(null) }
                val scope = rememberCoroutineScope()
                var showSoundsSheet by remember { mutableStateOf(false) }
                
                DisposableEffect(Unit) {
                    onDispose {
                        mediaPlayer?.release()
                        mediaPlayer = null
                    }
                }

                val playSound: (String, String) -> Unit = { prayer, sound ->
                    mediaPlayer?.stop()
                    mediaPlayer?.release()
                    val resId = when (sound) {
                        "Makkah Azan" -> com.example.prayertimes.R.raw.azan_asr_makkah
                        "Makkah" -> com.example.prayertimes.R.raw.azan_makkah
                        "Madinah" -> com.example.prayertimes.R.raw.azan_madinah
                        "Short Beep" -> com.example.prayertimes.R.raw.short_beep
                        "Fajr Special" -> com.example.prayertimes.R.raw.azan_makkah // Fallback
                        else -> 0
                    }
                    if (resId != 0) {
                        try {
                            mediaPlayer = MediaPlayer.create(context, resId)
                            mediaPlayer?.start()
                            playingPrayer = prayer
                            mediaPlayer?.setOnCompletionListener { mp ->
                                if (playingPrayer == prayer) {
                                    mp.release()
                                    if (mediaPlayer == mp) {
                                        mediaPlayer = null
                                        playingPrayer = null
                                    }
                                }
                            }
                        } catch (e: Throwable) {}
                    }
                }

                val stopSound: () -> Unit = {
                    mediaPlayer?.stop()
                    mediaPlayer?.release()
                    mediaPlayer = null
                    playingPrayer = null
                }

                val updateSound: (String, String) -> Unit = { prayer, sound ->
                    viewModel.updatePrayerSound(prayer, sound)
                    if (playingPrayer == prayer) stopSound()
                }

                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showSoundsSheet = true }.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(8.dp))
                        Text("Prayer Sounds", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                if (showSoundsSheet) {
                    ModalBottomSheet(onDismissRequest = { showSoundsSheet = false }) {
                        Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
                            Text("Prayer Sounds", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                            PrayerSoundDropdownRow("Fajr", settings.fajrSound, playingPrayer, playSound, stopSound, updateSound)
                            PrayerSoundDropdownRow("Dhuhr", settings.dhuhrSound, playingPrayer, playSound, stopSound, updateSound)
                            PrayerSoundDropdownRow("Asr", settings.asrSound, playingPrayer, playSound, stopSound, updateSound)
                            PrayerSoundDropdownRow("Maghrib", settings.maghribSound, playingPrayer, playSound, stopSound, updateSound)
                            PrayerSoundDropdownRow("Isha", settings.ishaSound, playingPrayer, playSound, stopSound, updateSound)
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                
                // Batch 2: Early Reminder
                val earlyOptions = listOf("Off", "5 minutes", "10 minutes", "15 minutes", "30 minutes")
                val earlyValues = listOf(0, 5, 10, 15, 30)
                val currentEarlyValue = settings.earlyReminderMins
                val currentEarlyIndex = earlyValues.indexOf(currentEarlyValue).takeIf { it >= 0 } ?: 0
                val currentEarlyText = if (currentEarlyValue == 0) "Off" else "$currentEarlyValue minutes before prayer"
                Column {
                    Text("Early Reminder", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    Text(currentEarlyText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(8.dp))
                val earlyListState = androidx.compose.foundation.lazy.rememberLazyListState()
                androidx.compose.foundation.lazy.LazyRow(
                    state = earlyListState,
                    flingBehavior = androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior(lazyListState = earlyListState),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(earlyOptions.size) { index ->
                        val isSelected = currentEarlyIndex == index
                        androidx.compose.material3.FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateEarlyReminderMins(earlyValues[index]) },
                            label = { Text(earlyOptions[index]) },
                            shape = RoundedCornerShape(16.dp),
                            colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
                
                Spacer(Modifier.height(12.dp))
                SwitchSetting("Jummah Reminder", settings.jummahReminder, "Friday reminder 60 mins before Dhuhr") { 
                    viewModel.updateJummahReminder(it)
                }
                
                SwitchSetting("Surah Al-Kahf Reminder", settings.kahfReminder, "Thursday night and Friday morning") { 
                    viewModel.updateKahfReminder(it)
                }
                
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                
                var isBatteryOptimized by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    try {
                        val pm = context.getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
                        isBatteryOptimized = !pm.isIgnoringBatteryOptimizations(context.packageName)
                    } catch (e: Throwable) {
                        isBatteryOptimized = false
                    }
                }
                
                if (isBatteryOptimized) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFF1A1200)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .drawBehind {
                                    drawLine(
                                        color = androidx.compose.ui.graphics.Color(0xFFFF8F00),
                                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                        end = androidx.compose.ui.geometry.Offset(0f, size.height),
                                        strokeWidth = 3.dp.toPx() * 2 // Stroke expands from center, so double width for border
                                    )
                                }
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Warning, contentDescription = "Warning", tint = androidx.compose.ui.graphics.Color(0xFFFFC107))
                                Spacer(Modifier.width(8.dp))
                                Text("Battery Optimization Active", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = androidx.compose.ui.graphics.Color(0xFFFFC107))
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("Your Azan notifications may not play on time or at all.", style = MaterialTheme.typography.bodyMedium, color = androidx.compose.ui.graphics.Color(0xFFE0E0E0))
                            Spacer(Modifier.height(4.dp))
                            Text("To fix: Go to Settings → Apps → Miqat → Battery → Select 'Unrestricted'", style = MaterialTheme.typography.bodySmall, color = androidx.compose.ui.graphics.Color(0xFFE0E0E0))
                            Spacer(Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = androidx.compose.ui.graphics.Color(0xFFFFC107)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFFFC107)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Open Battery Settings")
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFF0A1A0A)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .drawBehind {
                                    drawLine(
                                        color = androidx.compose.ui.graphics.Color(0xFF00897B),
                                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                        end = androidx.compose.ui.geometry.Offset(0f, size.height),
                                        strokeWidth = 3.dp.toPx() * 2
                                    )
                                }
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = "OK", tint = androidx.compose.ui.graphics.Color(0xFF00897B))
                                Spacer(Modifier.width(8.dp))
                                Text("Battery Optimization Disabled ✓", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = androidx.compose.ui.graphics.Color(0xFF00897B))
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("Azan notifications will play reliably", style = MaterialTheme.typography.bodyMedium, color = androidx.compose.ui.graphics.Color(0xFFE0E0E0))
                        }
                    }
                }
                
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Daily Hadith Notification", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Time: ${settings.hadithNotificationTime}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(8.dp))
                            TextButton(
                                onClick = {
                                    val parts = settings.hadithNotificationTime.split(":")
                                    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 7
                                    val min = parts.getOrNull(1)?.toIntOrNull() ?: 0
                                    try {
                                        TimePickerDialog(context, { _, h, m ->
                                            viewModel.updateHadithNotificationTime(String.format(Locale.US, "%02d:%02d", h, m))
                                        }, hour, min, false).show()
                                    } catch (e: Throwable) {}
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("Change", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    Switch(settings.hadithNotificationEnabled, { viewModel.updateHadithNotificationEnabled(it) }, colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.primaryContainer))
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        // Ramadan Section
        AnimatedVisibility(visible, enter = fadeIn() + slideInVertically(initialOffsetY = { 90 }, animationSpec = spring(stiffness = Spring.StiffnessLow))) {
            SettingsSection("Ramadan", Icons.Rounded.NightsStay) {
                SwitchSetting(stringResource(R.string.ramadan_mode), settings.ramadanMode, "Highlights Sehri and Iftar times on the Home screen") { viewModel.updateRamadanMode(it) }
            }
        }
        Spacer(Modifier.height(12.dp))
        // Appearance & Language Section
        AnimatedVisibility(visible, enter = fadeIn() + slideInVertically(initialOffsetY = { 100 }, animationSpec = spring(stiffness = Spring.StiffnessLow))) {
            SettingsSection(stringResource(R.string.appearance), Icons.Rounded.DarkMode) {
                
                val themeOptions = com.example.prayertimes.data.model.ThemeMode.values().map { it.displayName }
                DropdownSetting("Theme Mode", settings.themeMode.displayName, themeOptions) {
                    viewModel.updateThemeMode(com.example.prayertimes.data.model.ThemeMode.values()[it])
                }
                
                SwitchSetting("24-hour time", settings.use24hrFormat, "Use 24-hour format instead of AM/PM") { viewModel.updateUse24hrFormat(it) }
                
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                
                Text("Font Size", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    val fontOptions = listOf("Small" to 0.85f, "Medium" to 1.0f, "Large" to 1.15f)
                    fontOptions.forEach { (label, value) ->
                        val isSelected = settings.fontSizeMultiplier == value
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                                .clickable { viewModel.updateFontSizeMultiplier(value) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Aa", fontSize = (16 * value).sp, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(4.dp))
                                Text(label, style = MaterialTheme.typography.labelSmall, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                
            }
        }
        Spacer(Modifier.height(12.dp))
        // Quran Section
        AnimatedVisibility(visible, enter = fadeIn() + slideInVertically(initialOffsetY = { 110 }, animationSpec = spring(stiffness = Spring.StiffnessLow))) {
            SettingsSection("Quran", Icons.Rounded.MenuBook) {
                val quranLang by quranViewModel.translationLang.collectAsState()
                val quranLangs = listOf("en" to "English", "ur" to "Urdu")
                val currentQuranLangName = quranLangs.find { it.first == quranLang }?.second ?: "English"
                DropdownSetting("Translation Language", currentQuranLangName, quranLangs.map { it.second }) { index ->
                    quranViewModel.setTranslationLang(quranLangs[index].first)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                
                var showReciterDialog by remember { mutableStateOf(false) }
                
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showReciterDialog = true }.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Default Reciter", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        Text(audioState.selectedReciter.displayName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(audioState.selectedReciter.arabicName, style = MaterialTheme.typography.bodyMedium, color = com.example.prayertimes.theme.Teal400)
                }
                
                if (showReciterDialog) {
                    com.example.prayertimes.ui.screen.ReciterSelectionDialog(
                        audioState = audioState,
                        onDismiss = { showReciterDialog = false },
                        onReciterSelect = { audioViewModel.selectReciter(it); showReciterDialog = false }
                    )
                }
                
                val context = androidx.compose.ui.platform.LocalContext.current
                var cacheSize by remember { mutableStateOf(0f) }
                val audioRepo = remember { com.example.prayertimes.data.repository.QuranAudioRepository(context) }
                var showClearCacheDialog by remember { mutableStateOf(false) }
                
                LaunchedEffect(Unit) {
                    cacheSize = audioRepo.getCacheSizeMB()
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Audio Cache: ${String.format("%.1f", cacheSize)} MB", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    TextButton(onClick = { showClearCacheDialog = true }) {
                        Text("Clear Cache", color = MaterialTheme.colorScheme.error)
                    }
                }
                
                if (showClearCacheDialog) {
                    AlertDialog(
                        onDismissRequest = { showClearCacheDialog = false },
                        title = { Text("Clear Audio Cache") },
                        text = { Text("Are you sure you want to delete all downloaded Quran audio files? They will need to be re-downloaded to listen offline.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    audioRepo.clearCache()
                                    cacheSize = audioRepo.getCacheSizeMB()
                                    showClearCacheDialog = false
                                }
                            ) { Text("Clear", color = MaterialTheme.colorScheme.error) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showClearCacheDialog = false }) { Text("Cancel") }
                        }
                    )
                }
                
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                
                val qualityOptions = listOf("64", "128", "192")
                val qualityDisplayNames = listOf("Low (64kbps)", "Standard (128kbps)", "High (192kbps)")
                val currentQualityIndex = qualityOptions.indexOf(settings.audioQuality).coerceAtLeast(0)
                
                DropdownSetting("Audio Quality", qualityDisplayNames[currentQualityIndex], qualityDisplayNames) { index ->
                    viewModel.updateAudioQuality(qualityOptions[index])
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        // About Section
        AnimatedVisibility(visible, enter = fadeIn() + slideInVertically(initialOffsetY = { 120 }, animationSpec = spring(stiffness = Spring.StiffnessLow))) {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onNavigateToAbout() }, 
                shape = RoundedCornerShape(20.dp), 
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Rounded.Info, stringResource(R.string.about), Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                            Text(stringResource(R.string.about), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(id = R.string.app_name), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(4.dp))
                    Text("Tap to view more details, developer info, and contact.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), border = androidx.compose.foundation.BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                    Icon(icon, title, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(title.uppercase(), style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp, letterSpacing = 0.1.em), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                content()
            }
            Box(modifier = Modifier.matchParentSize()) {
                Box(modifier = Modifier.fillMaxHeight().width(3.dp).background(MaterialTheme.colorScheme.primary))
            }
        }
    }
}

@Composable
private fun PrayerNotificationRow(label: String, time: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, onTestClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(80.dp))
                if (time.isNotEmpty()) {
                    Text(time, style = MaterialTheme.typography.bodyMedium, color = com.example.prayertimes.theme.Teal400, fontWeight = FontWeight.Medium)
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = onTestClick, modifier = Modifier.height(32.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)) {
                Text("Test", fontSize = 12.sp)
            }
            Spacer(Modifier.width(12.dp))
            Switch(checked, onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.primaryContainer))
        }
    }
}

@Composable
private fun SwitchSetting(label: String, checked: Boolean, description: String? = null, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            description?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        Switch(checked, onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.primaryContainer))
    }
}

@Composable
private fun DropdownSetting(label: String, selectedValue: String, options: List<String>, button: @Composable (() -> Unit)? = null, onOptionSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = selectedValue,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { expanded = !expanded }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    options.forEachIndexed { index, option -> 
                        DropdownMenuItem(
                            text = { Text(option) }, 
                            onClick = { 
                                onOptionSelected(index)
                                expanded = false 
                            }
                        ) 
                    }
                }
            }
            if (button != null) {
                button()
            }
        }
    }
}

@Composable
fun OffsetRow(prayerName: String, currentOffset: Int, onOffsetChange: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(prayerName, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            if (currentOffset != 0) {
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { onOffsetChange(0) }, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Rounded.Close, contentDescription = "Reset", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }
        }
        
        val valueColor = when {
            currentOffset > 0 -> com.example.prayertimes.theme.Teal400
            currentOffset < 0 -> androidx.compose.ui.graphics.Color(0xFFFF9800)
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(50)).padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            IconButton(onClick = { if (currentOffset > -30) onOffsetChange(currentOffset - 1) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Rounded.Remove, contentDescription = "Decrease", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val sign = if (currentOffset > 0) "+" else ""
            Text("$sign$currentOffset min", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.width(64.dp), textAlign = TextAlign.Center, color = valueColor)
            IconButton(onClick = { if (currentOffset < 30) onOffsetChange(currentOffset + 1) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Rounded.Add, contentDescription = "Increase", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}


@Composable
fun PrayerSoundDropdownRow(
    prayerName: String,
    currentSound: String,
    playingPrayer: String?,
    onPlay: (String, String) -> Unit,
    onStop: () -> Unit,
    onUpdateSound: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Makkah Azan", "Makkah", "Madinah", "Short Beep", "Fajr Special", "Silent")

    Column {
        Text(prayerName, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = currentSound,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { expanded = !expanded }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    options.forEachIndexed { index, option -> 
                        DropdownMenuItem(
                            text = { Text(option) }, 
                            onClick = { 
                                onUpdateSound(prayerName, option)
                                expanded = false 
                            }
                        ) 
                    }
                }
            }
            if (currentSound != "Silent") {
                val isPlayingThis = playingPrayer == prayerName
                IconButton(
                    onClick = {
                        if (isPlayingThis) onStop() else onPlay(prayerName, currentSound)
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        if (isPlayingThis) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                        contentDescription = "Preview Azan",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

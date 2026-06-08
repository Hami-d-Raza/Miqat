package com.example.prayertimes.ui.screen

import com.example.prayertimes.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.prayertimes.data.model.AppSettings
import com.example.prayertimes.data.model.DailyPrayerTimes
import com.example.prayertimes.repository.PrayerRepository
import com.example.prayertimes.viewmodel.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(homeViewModel: HomeViewModel = viewModel(), onBackClick: () -> Unit = {}) {
    val settings by homeViewModel.settings.collectAsState()
    var currentViewMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var monthlyTimes by remember { mutableStateOf<List<Pair<Date, DailyPrayerTimes>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(settings.isLocationSet, currentViewMonth) {
        if (settings.isLocationSet) {
            isLoading = true
            withContext(Dispatchers.IO) {
                val repository = PrayerRepository()
                val times = generateMonthlyTimetable(settings, repository, currentViewMonth)
                monthlyTimes = times
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.monthly_timetable)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        if (isLoading || !settings.isLocationSet) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                if (!settings.isLocationSet) {
                    Text("Please set your location in Settings first.")
                } else {
                    CircularProgressIndicator()
                }
            }
        } else {
            Column(Modifier.padding(padding).fillMaxSize()) {
                MonthNavigation(
                    currentMonth = currentViewMonth,
                    onPrevious = { 
                        val newCal = currentViewMonth.clone() as Calendar
                        newCal.add(Calendar.MONTH, -1)
                        currentViewMonth = newCal
                    },
                    onNext = {
                        val newCal = currentViewMonth.clone() as Calendar
                        newCal.add(Calendar.MONTH, 1)
                        currentViewMonth = newCal
                    }
                )
                TableHeader()
                LazyColumn(Modifier.fillMaxSize()) {
                    items(monthlyTimes) { (date, dailyTimes) ->
                        TableRow(date = date, dailyTimes = dailyTimes)
                    }
                }
            }
        }
    }
}

@Composable
fun MonthNavigation(currentMonth: Calendar, onPrevious: () -> Unit, onNext: () -> Unit) {
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(androidx.compose.material.icons.Icons.Rounded.ChevronLeft, contentDescription = "Previous Month")
        }
        Text(
            text = monthFormat.format(currentMonth.time),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onNext) {
            Icon(androidx.compose.material.icons.Icons.Rounded.ChevronRight, contentDescription = "Next Month")
        }
    }
}

@Composable
fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        HeaderCell("Date", weight = 1.2f)
        HeaderCell(stringResource(R.string.fajr), weight = 1f)
        HeaderCell(stringResource(R.string.dhuhr), weight = 1f)
        HeaderCell(stringResource(R.string.asr), weight = 1f)
        HeaderCell(stringResource(R.string.maghrib), weight = 1.2f)
        HeaderCell(stringResource(R.string.isha), weight = 1f)
    }
}

@Composable
fun RowScope.HeaderCell(text: String, weight: Float) {
    Text(
        text = text,
        modifier = Modifier.weight(weight),
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.labelMedium
    )
}

@Composable
fun TableRow(date: Date, dailyTimes: DailyPrayerTimes) {
    val isToday = isSameDay(date, Date())
    val backgroundColor = if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        DataCell(dateFormat.format(date), weight = 1.2f, isBold = isToday)

        val fajr = dailyTimes.prayers.find { it.prayer.name == "FAJR" }?.formattedTime ?: "--"
        val dhuhr = dailyTimes.prayers.find { it.prayer.name == "DHUHR" }?.formattedTime ?: "--"
        val asr = dailyTimes.prayers.find { it.prayer.name == "ASR" }?.formattedTime ?: "--"
        val maghrib = dailyTimes.prayers.find { it.prayer.name == "MAGHRIB" }?.formattedTime ?: "--"
        val isha = dailyTimes.prayers.find { it.prayer.name == "ISHA" }?.formattedTime ?: "--"

        DataCell(fajr, weight = 1f, isBold = isToday)
        DataCell(dhuhr, weight = 1f, isBold = isToday)
        DataCell(asr, weight = 1f, isBold = isToday)
        DataCell(maghrib, weight = 1.2f, isBold = isToday)
        DataCell(isha, weight = 1f, isBold = isToday)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
}

@Composable
fun RowScope.DataCell(text: String, weight: Float, isBold: Boolean) {
    Text(
        text = text,
        modifier = Modifier.weight(weight),
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodySmall
    )
}

private fun generateMonthlyTimetable(settings: AppSettings, repository: PrayerRepository, targetMonth: Calendar): List<Pair<Date, DailyPrayerTimes>> {
    val calendar = targetMonth.clone() as Calendar
    calendar.set(Calendar.DAY_OF_MONTH, 1) // Start of month
    val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    val timesList = mutableListOf<Pair<Date, DailyPrayerTimes>>()
    
    for (i in 1..maxDays) {
        calendar.set(Calendar.DAY_OF_MONTH, i)
        val date = calendar.time
        val dailyTimes = repository.getDailyPrayerTimes(settings, date)
        timesList.add(Pair(date, dailyTimes))
    }
    return timesList
}

private fun isSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

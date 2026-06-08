package com.example.prayertimes.ui.screen

import android.icu.util.Calendar
import android.icu.util.IslamicCalendar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prayertimes.data.model.HijriCalendarData
import com.example.prayertimes.data.model.IslamicEvent
import com.example.prayertimes.theme.Gold500
import com.example.prayertimes.theme.Teal400
import com.example.prayertimes.viewmodel.HijriCalendarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HijriCalendarScreen(
    viewModel: HijriCalendarViewModel,
    onNavigateBack: () -> Unit
) {
    val currentMonth by viewModel.currentMonth.collectAsState()
    val currentYear by viewModel.currentYear.collectAsState()
    
    var selectedEvent by remember { mutableStateOf<IslamicEvent?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hijri Calendar", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.previousMonth() }) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Previous Month")
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${HijriCalendarData.monthNames[currentMonth]} $currentYear",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Teal400
                    )
                }
                
                IconButton(onClick = { viewModel.nextMonth() }) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next Month")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Weekdays Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                    Text(
                        text = day,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            androidx.compose.animation.AnimatedContent(
                targetState = currentYear * 12 + currentMonth,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally(tween(300)) { width -> width } + fadeIn(tween(300)) togetherWith
                        slideOutHorizontally(tween(300)) { width -> -width } + fadeOut(tween(300))
                    } else {
                        slideInHorizontally(tween(300)) { width -> -width } + fadeIn(tween(300)) togetherWith
                        slideOutHorizontally(tween(300)) { width -> width } + fadeOut(tween(300))
                    }
                },
                label = "calendarTransition"
            ) { targetState ->
                val year = targetState / 12
                val month = targetState % 12
                val calendar = IslamicCalendar()
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                
                val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                val emptyCells = (firstDayOfWeek - 1)
                
                Column {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(emptyCells) { Box(modifier = Modifier.height(60.dp)) }
                        
                        items(daysInMonth) { dayIndex ->
                            val day = dayIndex + 1
                            val isToday = viewModel.isToday(year, month, day)
                            val events = viewModel.getEventsForDate(month, day)
                            
                            Box(
                                modifier = Modifier
                                    .height(60.dp)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isToday) Teal400 else 
                                        if (month == 8) Teal400.copy(alpha = 0.1f) // Ramadan highlight
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable {
                                        if (events.isNotEmpty()) {
                                            selectedEvent = events.first()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = day.toString(),
                                        fontWeight = FontWeight.Bold,
                                        color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (events.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (isToday) Color.White else Gold500)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    val monthEvents = HijriCalendarData.events.filter { it.month == month }
                    if (monthEvents.isNotEmpty()) {
                        Text("Events this month", fontWeight = FontWeight.Bold, color = Teal400)
                        Spacer(modifier = Modifier.height(8.dp))
                        monthEvents.forEach { event ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selectedEvent = event },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(event.iconEmoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(event.name, fontWeight = FontWeight.Bold)
                                    val dayText = if (event.dayStart == event.dayEnd) "Day ${event.dayStart}" else "Days ${event.dayStart}-${event.dayEnd}"
                                    Text(dayText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
            
            // Events list is now inside the AnimatedContent
        }
    }
    
    if (selectedEvent != null) {
        AlertDialog(
            onDismissRequest = { selectedEvent = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(selectedEvent!!.iconEmoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(selectedEvent!!.name)
                }
            },
            text = { Text(selectedEvent!!.description) },
            confirmButton = {
                TextButton(onClick = { selectedEvent = null }) {
                    Text("Close")
                }
            }
        )
    }
}

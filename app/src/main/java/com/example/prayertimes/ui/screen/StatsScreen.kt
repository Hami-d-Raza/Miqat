package com.example.prayertimes.ui.screen

import com.example.prayertimes.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.ui.draw.alpha
import com.example.prayertimes.ui.components.ShimmerPlaceholder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.prayertimes.theme.Teal400
import com.example.prayertimes.theme.Gold500
import com.example.prayertimes.viewmodel.StatsViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import com.example.prayertimes.data.model.Prayer
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path

import com.example.prayertimes.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: StatsViewModel = viewModel(), homeViewModel: HomeViewModel? = null, onBackClick: () -> Unit = {}) {
    val allRecords by viewModel.allRecords.collectAsState()
    val scrollState = rememberScrollState()

    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val todayOffered = viewModel.getOfferedCountForDate(todayDate)
    val todayRecords = allRecords.filter { it.date == todayDate }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prayer Stats", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            var isStatsLoading by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) { delay(500); isStatsLoading = false }
            
            // 1. PRAYER CONSISTENCY SCORE
            val score = viewModel.calculateConsistencyScore()
            val grade = viewModel.getGrade(score)
            
            if (isStatsLoading) {
                ShimmerPlaceholder(Modifier.fillMaxWidth().height(160.dp), cornerRadius = 20.dp)
            } else {
            var startAnim by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { startAnim = true }
            val animatedScore by animateFloatAsState(
                targetValue = if (startAnim) score else 0f,
                animationSpec = tween(1000, easing = EaseOutCubic),
                label = "consistencyScore"
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Overall Consistency", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text(grade, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.size(80.dp),
                            color = Teal400.copy(alpha = 0.2f),
                            strokeWidth = 8.dp,
                            trackColor = Color.Transparent,
                        )
                        CircularProgressIndicator(
                            progress = { animatedScore / 100f },
                            modifier = Modifier.size(80.dp),
                            color = Teal400,
                            strokeWidth = 8.dp,
                            trackColor = Color.Transparent,
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            "${animatedScore.toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Teal400
                        )
                    }
                }
            }
            } // End of isStatsLoading else block

            val streaks = viewModel.getStreaks()
            if (isStatsLoading) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ShimmerPlaceholder(Modifier.weight(1f).height(100.dp), cornerRadius = 16.dp)
                    ShimmerPlaceholder(Modifier.weight(1f).height(100.dp), cornerRadius = 16.dp)
                }
            } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.LocalFireDepartment, contentDescription = "Current Streak", tint = Color(0xFFFF9800), modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Current Streak", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val streakText = if (streaks.first == 1) "1 day" else "${streaks.first} days"
                        Text(streakText, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.EmojiEvents, contentDescription = "Best Streak", tint = Teal400, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Best Streak", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val bestStreakText = if (streaks.second == 1) "1 day" else "${streaks.second} days"
                        Text(bestStreakText, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
            } // End of isStatsLoading else block

            // Today's Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Today's Prayers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    
                    val prayerTimesState by (homeViewModel?.prayerTimes ?: kotlinx.coroutines.flow.MutableStateFlow<com.example.prayertimes.data.model.DailyPrayerTimes?>(null)).collectAsState()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val prayers = listOf(Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA)
                        prayers.forEach { prayer ->
                            val isOffered = todayRecords.find { it.prayerName == prayer.name }?.isOffered == true
                            val prayerInfo = prayerTimesState?.prayers?.find { it.prayer == prayer }
                            val isPassed = prayerInfo?.isPassed == true

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val shortName = when (prayer) {
                                    Prayer.FAJR -> stringResource(R.string.fajr)
                                    Prayer.DHUHR -> "Zuhr"
                                    Prayer.ASR -> stringResource(R.string.asr)
                                    Prayer.MAGHRIB -> "Mghrb"
                                    Prayer.ISHA -> stringResource(R.string.isha)
                                    else -> ""
                                }
                                Text(shortName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(4.dp))
                                if (isOffered) {
                                    Icon(Icons.Rounded.CheckCircle, contentDescription = "Offered", tint = Color(0xFF4CAF50), modifier = Modifier.size(28.dp))
                                } else if (isPassed) {
                                    Icon(Icons.Rounded.Cancel, contentDescription = "Missed", tint = Color(0xFFF44336), modifier = Modifier.size(28.dp))
                                } else {
                                    Icon(Icons.Rounded.RadioButtonUnchecked, contentDescription = "Not Yet", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(28.dp))
                                }
                            }
                        }
                    }
                }
            }

            // 2. BEST/WORST PRAYER ANALYSIS
            val prayerConsistency = viewModel.getPrayerConsistency()
            if (prayerConsistency.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Prayer Analysis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        
                        val maxScore = prayerConsistency.values.maxOrNull() ?: 0f
                        val minScore = prayerConsistency.values.minOrNull() ?: 0f
                        
                        // We map exactly in order: Fajr, Dhuhr, Asr, Maghrib, Isha
                        val orderedPrayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
                        
                        orderedPrayers.forEach { prayer ->
                            val pScore = prayerConsistency[prayer] ?: 0f
                            val isBest = pScore == maxScore && pScore > 0f
                            val isWorst = pScore == minScore && pScore < 100f && maxScore > minScore
                            
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(prayer, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(60.dp))
                                        if (isBest) {
                                            Spacer(Modifier.width(8.dp))
                                            Text("Most consistent ⭐", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50))
                                        } else if (isWorst) {
                                            Spacer(Modifier.width(8.dp))
                                            Text("Needs attention ⚠️", style = MaterialTheme.typography.labelSmall, color = Color(0xFFF44336))
                                        }
                                    }
                                    Text("${pScore.toInt()}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { pScore / 100f },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                    color = if (isBest) Color(0xFF4CAF50) else if (isWorst) Color(0xFFF44336) else Teal400,
                                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                )
                            }
                        }
                    }
                }
            }

            // 3. WEEKLY COMPARISON
            val weeklyComparison = viewModel.getWeeklyComparison()
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("This Week vs Last Week", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("This week: ${weeklyComparison.first} prayers", style = MaterialTheme.typography.bodyLarge)
                            Text("Last week: ${weeklyComparison.second} prayers", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        
                        val diff = weeklyComparison.first - weeklyComparison.second
                        val percentChange = if (weeklyComparison.second == 0) {
                            if (weeklyComparison.first > 0) 100 else 0
                        } else {
                            ((diff.toFloat() / weeklyComparison.second) * 100).toInt()
                        }
                        
                        if (diff > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("↑", style = MaterialTheme.typography.titleLarge, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(4.dp))
                                Text("+${percentChange}% better", style = MaterialTheme.typography.labelMedium, color = Color(0xFF4CAF50))
                            }
                        } else if (diff < 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("↓", style = MaterialTheme.typography.titleLarge, color = Color(0xFFF44336), fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(4.dp))
                                Text("${percentChange}% worse", style = MaterialTheme.typography.labelMedium, color = Color(0xFFF44336))
                            }
                        } else {
                            Text("No change", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // 4. ALL TIME STATS
            val allTimeStats = viewModel.getAllTimeStats()
            Text("All Time Stats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Card 1
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Teal400, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("${allTimeStats.first}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Teal400)
                        Text("Total Offered", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                // Card 2
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Cancel, contentDescription = null, tint = Color(0xFFF44336), modifier = Modifier.size(24.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("${allTimeStats.second}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Teal400)
                        Text("Total Missed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                // Card 3
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Explore, contentDescription = null, tint = Gold500, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("${allTimeStats.third}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Teal400)
                        Text("Days Active", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // 5. MONTHLY COMPARISON CHART
            Text("Last 6 Months", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            val monthlyStats = viewModel.getMonthlyStats()
            MonthlyLineChart(data = monthlyStats)

            // Weekly Chart (Original)
            Text("Last 7 Days", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            val weeklyData = viewModel.getDailyCounts(7)
            if (isStatsLoading) {
                ShimmerPlaceholder(Modifier.fillMaxWidth().height(200.dp), cornerRadius = 16.dp)
            } else {
                WeeklyBarChart(data = weeklyData)
            }

            // Monthly View
            Text("Last 30 Days", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            val monthlyData = viewModel.getDailyCounts(30)
            MonthlyCalendarView(data = monthlyData)
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = Color(0xFF424242), label = "No data")
                LegendItem(color = Color(0xFFF44336), label = "0")
                LegendItem(color = Color(0xFFFF9800), label = "1-2")
                LegendItem(color = Color(0xFFFFC107), label = "3-4")
                LegendItem(color = Color(0xFF4CAF50), label = "5")
            }
            
            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun WeeklyBarChart(data: List<Pair<String, Int?>>) {
    val primaryColor = Teal400
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface

    val animatedHeights = data.mapIndexed { index, pair ->
        val count = pair.second ?: 0
        var startAnim by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { startAnim = true }
        animateFloatAsState(
            targetValue = if (startAnim) count.toFloat() else 0f,
            animationSpec = tween(500, delayMillis = index * 100, easing = EaseOutCubic),
            label = "barHeight_$index"
        ).value
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .padding(bottom = 24.dp) // extra padding for labels
    ) {
        val barWidth = size.width / (data.size * 2)
        val maxBars = 5f
        
        data.forEachIndexed { index, pair ->
            val count = pair.second
            
            // X position for each bar, centered in its segment
            val xPos = (size.width / data.size) * index + (size.width / data.size) / 2f - barWidth / 2f
            
            // Draw background track
            drawRoundRect(
                color = onSurface.copy(alpha = 0.1f),
                topLeft = Offset(xPos, 0f),
                size = Size(barWidth, size.height),
                cornerRadius = CornerRadius(barWidth / 2)
            )
            
            if (count != null) {
                val maxBarHeightDp = size.height
                val barHeight = (animatedHeights[index] / 5f) * maxBarHeightDp
                val yPos = size.height - barHeight
                
                val finalBarColor = when(count) {
                    5 -> Color(0xFF4CAF50) // Green
                    3, 4 -> Color(0xFFFFC107) // Yellow
                    1, 2 -> Color(0xFFFF9800) // Orange
                    0 -> Color(0xFFF44336) // Red
                    else -> Color(0xFF424242)
                }

                // Draw filled bar
                drawRoundRect(
                    color = finalBarColor,
                    topLeft = Offset(xPos, yPos),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2)
                )
            } else {
                // If no data, outline
                drawRoundRect(
                    color = onSurface.copy(alpha = 0.3f),
                    topLeft = Offset(xPos, size.height - barWidth), // Just a small circle/bar at bottom
                    size = Size(barWidth, barWidth),
                    cornerRadius = CornerRadius(barWidth / 2),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                )
            }

            // Draw X-axis label
            val dayFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outFormat = java.text.SimpleDateFormat("EEE", Locale.getDefault())
            try {
                val d = dayFormat.parse(pair.first)
                if (d != null) {
                    val label = outFormat.format(d)
                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        xPos + barWidth / 2f,
                        size.height + 24.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = onSurface.copy(alpha = 0.6f).hashCode()
                            textSize = 12.dp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                    )
                }
            } catch (e: Exception) {}
        }
    }
}

@Composable
fun MonthlyCalendarView(data: List<Pair<String, Int?>>) {
    val cols = 7
    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val rows = data.chunked(cols)
        var startAnim by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { startAnim = true }
        
        val rowAlphas = rows.mapIndexed { index, _ ->
            animateFloatAsState(
                targetValue = if (startAnim) 1f else 0f,
                animationSpec = tween(400, delayMillis = index * 50),
                label = "rowAlpha_$index"
            ).value
        }

        for ((rowIndex, row) in rows.withIndex()) {
            Row(
                modifier = Modifier.fillMaxWidth().alpha(rowAlphas[rowIndex]),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                for (item in row) {
                    val count = item.second
                    val dateStr = item.first
                    val isToday = dateStr == todayDate
                    
                    val color = when (count) {
                        null -> Color(0xFF424242) // grey for no data
                        5 -> Color(0xFF4CAF50) // Green
                        0 -> Color(0xFFF44336) // Red
                        1, 2 -> Color(0xFFFF9800) // Orange
                        else -> Color(0xFFFFC107) // Yellow (3,4)
                    }
                    
                    var dayNum = ""
                    try {
                        val d = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
                        if (d != null) {
                            val cal = Calendar.getInstance()
                            cal.time = d
                            dayNum = cal.get(Calendar.DAY_OF_MONTH).toString()
                        }
                    } catch (e: Exception) {}

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .then(
                                if (isToday) Modifier.background(color, RoundedCornerShape(4.dp)).background(Color.White.copy(alpha=0.3f), RoundedCornerShape(4.dp))
                                else Modifier.background(color, RoundedCornerShape(4.dp))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayNum,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
                // Fill empty spaces if last row is incomplete
                val emptySlots = cols - row.size
                for (i in 0 until emptySlots) {
                    Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                }
            }
        }
    }
}

@Composable
fun MonthlyLineChart(data: List<Pair<String, Int>>) {
    val lineColor = Teal400
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .padding(bottom = 24.dp, start = 8.dp, end = 8.dp) // extra padding for labels and sides
    ) {
        if (data.isEmpty()) return@Canvas
        
        val maxVal = 150f
        val points = mutableListOf<Offset>()
        
        val widthPerPoint = size.width / (data.size - 1).coerceAtLeast(1)
        
        data.forEachIndexed { index, pair ->
            val count = pair.second.coerceAtMost(150)
            
            val xPos = index * widthPerPoint
            val yPos = size.height - ((count.toFloat() / maxVal) * size.height)
            
            points.add(Offset(xPos, yPos))
            
            // Draw X-axis label
            drawContext.canvas.nativeCanvas.drawText(
                pair.first,
                xPos,
                size.height + 24.dp.toPx(),
                android.graphics.Paint().apply {
                    color = onSurface.copy(alpha = 0.6f).hashCode()
                    textSize = 12.dp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
            )
        }
        
        if (points.size > 1) {
            val path = Path()
            path.moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                path.lineTo(points[i].x, points[i].y)
            }
            
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(
                    width = 4.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
        
        // Draw dots at each data point
        points.forEach { point ->
            drawCircle(
                color = backgroundColor,
                radius = 6.dp.toPx(),
                center = point
            )
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = point
            )
        }
    }
}

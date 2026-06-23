package com.example.prayertimes.ui.screen

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prayertimes.data.model.Surah
import com.example.prayertimes.theme.Teal400
import com.example.prayertimes.theme.Gold500
import com.example.prayertimes.theme.arabicTextColor
import com.example.prayertimes.ui.components.ShimmerPlaceholder
import com.example.prayertimes.viewmodel.QuranViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranListScreen(
    viewModel: QuranViewModel,
    audioViewModel: com.example.prayertimes.viewmodel.QuranAudioViewModel,
    onNavigateBack: () -> Unit,
    onSurahClick: (Int) -> Unit
) {
    val surahs by viewModel.surahs.collectAsState()
    val downloadState by audioViewModel.downloadState.collectAsState()
    val lastReadSurah by viewModel.lastReadSurah.collectAsState()
    val lastReadAyah by viewModel.lastReadAyah.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredSurahs = remember(searchQuery, surahs) {
        if (searchQuery.isBlank()) surahs
        else surahs.filter { 
            it.nameEnglish.contains(searchQuery, ignoreCase = true) || 
            it.nameArabic.contains(searchQuery, ignoreCase = true) 
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quran", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search Surahs...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Teal400,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                singleLine = true
            )
            
            // Last Read Card
            if (lastReadSurah != null && lastReadAyah != null && searchQuery.isEmpty()) {
                val surah = surahs.find { it.number == lastReadSurah }
                if (surah != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable { onSurahClick(surah.number) },
                        colors = CardDefaults.cardColors(containerColor = Teal400),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.MenuBook, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Continue Reading", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                                Text("${surah.nameEnglish} • Ayah $lastReadAyah", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            androidx.compose.animation.AnimatedContent(
                targetState = surahs.isEmpty(),
                transitionSpec = { androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(400)) togetherWith androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(400)) },
                label = "quranListCrossfade",
                modifier = Modifier.weight(1f)
            ) { loading ->
                if (loading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        androidx.compose.material3.CircularProgressIndicator(color = Teal400)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
                    ) {
                        items(filteredSurahs, key = { it.number }) { surah ->
                            val isDownloaded = downloadState.downloadedSurahs.contains(surah.number)
                            SurahItem(surah = surah, isDownloaded = isDownloaded, onClick = { onSurahClick(surah.number) })
                        }
                    }
                }
            }
        }
    }
}

val surahStartingJuz = intArrayOf(
    0, // 1-indexed
    1, 1, 3, 4, 6, 7, 8, 9, 10, 11, // 1-10
    11, 12, 13, 13, 14, 14, 15, 15, 16, 16, // 11-20
    17, 17, 18, 18, 18, 19, 19, 20, 20, 21, // 21-30
    21, 21, 21, 22, 22, 22, 23, 23, 23, 24, // 31-40
    24, 25, 25, 25, 25, 26, 26, 26, 26, 26, // 41-50
    26, 27, 27, 27, 27, 27, 27, 28, 28, 28, // 51-60
    28, 28, 28, 28, 28, 28, 29, 29, 29, 29, // 61-70
    29, 29, 29, 29, 29, 29, 29, 30, 30, 30, // 71-80
    30, 30, 30, 30, 30, 30, 30, 30, 30, 30, // 81-90
    30, 30, 30, 30, 30, 30, 30, 30, 30, 30, // 91-100
    30, 30, 30, 30, 30, 30, 30, 30, 30, 30, // 101-110
    30, 30, 30, 30 // 111-114
)

@Composable
fun SurahItem(surah: Surah, isDownloaded: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = surah.number.toString(),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = surah.nameEnglish,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "${surah.ayahCount} Ayahs",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = surah.nameArabic,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.arabicTextColor,
                fontFamily = com.example.prayertimes.theme.ArabicFontFamily
            )
            val juzNumber = if (surah.number in 1..114) surahStartingJuz[surah.number] else 1
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isDownloaded) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = "Downloaded",
                        tint = Teal400,
                        modifier = Modifier.size(12.dp).padding(end = 2.dp)
                    )
                }
                Text(
                    text = "Juz $juzNumber",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}


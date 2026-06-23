package com.example.prayertimes.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material.icons.rounded.ZoomOut
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Check
import androidx.compose.animation.core.EaseInOutCubic
import kotlinx.coroutines.delay
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.scale
import com.example.prayertimes.data.model.Reciter
import com.example.prayertimes.viewmodel.QuranAudioViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prayertimes.data.model.Ayah
import com.example.prayertimes.data.model.BookmarkedAyah
import com.example.prayertimes.theme.Gold500
import com.example.prayertimes.theme.arabicTextColor
import com.example.prayertimes.theme.Teal400
import com.example.prayertimes.theme.ArabicFontFamily
import com.example.prayertimes.theme.UrduFontFamily
import com.example.prayertimes.viewmodel.QuranViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranReaderScreen(
    surahNumber: Int,
    viewModel: QuranViewModel,
    audioViewModel: QuranAudioViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        audioViewModel.initPlayer(context)
    }

    val loadState by viewModel.surahLoadingState.collectAsState()
    val currentPlayingAyah by audioViewModel.currentPlayingAyah.collectAsState()
    var hasScrolled by remember { mutableStateOf(false) }
    var isMushafMode by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    LaunchedEffect(surahNumber) {
        hasScrolled = false
        val scrollTo = if (surahNumber == viewModel.lastReadSurah.value) {
            viewModel.lastReadAyah.value ?: 0
        } else 0
        viewModel.loadSurah(surahNumber, scrollTo)
    }

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            viewModel.resetSurahState()
        }
    }

    val surah by viewModel.currentSurah.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val fontSizeMultiplier by viewModel.fontSizeMultiplier.collectAsState()
    val translationLang by viewModel.translationLang.collectAsState()
    val lastReadAyah by viewModel.lastReadAyah.collectAsState()

    val audioState by audioViewModel.audioState.collectAsState()
    val downloadState by audioViewModel.downloadState.collectAsState()
    var showReciterDialog by remember { mutableStateOf(false) }
    var showDownloadDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(surah?.nameEnglish ?: "Loading...", fontWeight = FontWeight.Bold)
                        if (surah != null) {
                            Text("${surah?.ayahCount} Ayahs", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isMushafMode = !isMushafMode }) {
                        Icon(
                            imageVector = if (isMushafMode) Icons.AutoMirrored.Rounded.List else Icons.AutoMirrored.Rounded.MenuBook, 
                            contentDescription = "Toggle Mushaf Mode"
                        )
                    }
                    IconButton(onClick = { showDownloadDialog = true }) {
                        Icon(Icons.Outlined.DownloadForOffline, contentDescription = "Download Surah")
                    }
                    IconButton(onClick = { viewModel.setFontSizeMultiplier((fontSizeMultiplier - 0.1f).coerceAtLeast(0.7f)) }) {
                        Icon(Icons.Rounded.ZoomOut, contentDescription = "Decrease Font")
                    }
                    IconButton(onClick = { viewModel.setFontSizeMultiplier((fontSizeMultiplier + 0.1f).coerceAtMost(2.0f)) }) {
                        Icon(Icons.Rounded.ZoomIn, contentDescription = "Increase Font")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        when (val state = loadState) {
            is com.example.prayertimes.viewmodel.SurahLoadState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.CircularProgressIndicator(color = Color(0xFF4DB6AC))
                }
            }
            is com.example.prayertimes.viewmodel.SurahLoadState.Ready -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (isMushafMode) {
                        MushafView(
                            state = state,
                            surahNumber = surahNumber,
                            fontSizeMultiplier = fontSizeMultiplier,
                            paddingValues = paddingValues
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) {
                        if (surahNumber != 1 && surahNumber != 9) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                        fontSize = (24 * fontSizeMultiplier).sp,
                                        fontFamily = ArabicFontFamily,
                                        color = Teal400,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        items(state.ayahs, key = { it.number }) { ayah ->
                            val isBookmarked = bookmarks.any { it.surahNumber == surahNumber && it.ayahNumber == ayah.number }
                            
                            LaunchedEffect(ayah.number) {
                                viewModel.saveLastRead(surahNumber, ayah.number)
                            }
                            
                            val isThisAyahPlaying = currentPlayingAyah == ayah.number && audioState.currentSurah == surahNumber

                            AyahCard(
                                ayah = ayah,
                                fontSizeMultiplier = fontSizeMultiplier,
                                translationLang = translationLang,
                                isBookmarked = isBookmarked,
                                isThisAyahPlaying = isThisAyahPlaying,
                                onBookmarkClick = { viewModel.toggleBookmark(surahNumber, ayah.number) },
                                onPlayClick = {
                                    if(isThisAyahPlaying) audioViewModel.pauseResume()
                                    else audioViewModel.playAyah(context, surahNumber, ayah.number)
                                }
                            )
                        }
                        
                        val showAudioBar = audioState.currentAyah > 0 || audioState.isPlaying || audioState.isLoading
                        item { Spacer(Modifier.height(if (showAudioBar) 140.dp else 16.dp)) }
                    }

                    LaunchedEffect(key1 = state.ayahs.isNotEmpty()) {
                        if(state.ayahs.isNotEmpty() && !hasScrolled) {
                            hasScrolled = true
                            if(state.scrollToAyah > 1) {
                                delay(200)
                                val headerOffset = if (surahNumber != 1 && surahNumber != 9) 1 else 0
                                listState.scrollToItem(
                                    index = (state.scrollToAyah - 1) + headerOffset,
                                    scrollOffset = 0
                                )
                            } else {
                                listState.scrollToItem(index = 0, scrollOffset = 0)
                            }
                        }
                    }

                    LaunchedEffect(key1 = currentPlayingAyah, key2 = audioState.currentSurah) {
                        if(currentPlayingAyah > 0 && audioState.currentSurah == surahNumber && !isMushafMode) {
                            val headerOffset = if (surahNumber != 1 && surahNumber != 9) 1 else 0
                            val targetIndex = (currentPlayingAyah - 1) + headerOffset
                            
                            val visibleItems = listState.layoutInfo.visibleItemsInfo
                            val viewportEnd = listState.layoutInfo.viewportEndOffset
                            val playerHeightPx = with(density) { 160.dp.toPx() } // Approx height of the player bar and padding
                            
                            val targetItem = visibleItems.find { it.index == targetIndex }
                            
                            if (targetItem != null) {
                                val itemBottom = targetItem.offset + targetItem.size
                                val safeBottom = viewportEnd - playerHeightPx
                                if (itemBottom > safeBottom) {
                                    val overlap = itemBottom - safeBottom
                                    listState.animateScrollBy(overlap)
                                } else if (targetItem.offset < 0) {
                                    // Hidden at the top
                                    listState.animateScrollBy(targetItem.offset.toFloat())
                                }
                            } else {
                                listState.animateScrollToItem(
                                    index = targetIndex,
                                    scrollOffset = 100
                                )
                            }
                        }
                    }

                    } // End of else block for LazyColumn vs MushafView

                    val showAudioBar = audioState.currentAyah > 0 || audioState.isPlaying || audioState.isLoading
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showAudioBar,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = paddingValues.calculateBottomPadding()),
                        enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }) + androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }) + androidx.compose.animation.fadeOut()
                    ) {
                        AudioPlayerBar(
                            audioState = audioState,
                            onPlayPause = { audioViewModel.pauseResume() },
                            onStop = { audioViewModel.stop() },
                            onPrevAyah = { audioViewModel.playPrevAyah(context) },
                            onNextAyah = { audioViewModel.playNextAyah(context) },
                            onReciterSelect = { showReciterDialog = true },
                            onToggleAutoPlay = { audioViewModel.toggleAutoPlay() }
                        )
                    }
                }

                if (showReciterDialog) {
                    ReciterSelectionDialog(
                        audioState = audioState,
                        onDismiss = { showReciterDialog = false },
                        onReciterSelect = { audioViewModel.selectReciter(it); showReciterDialog = false }
                    )
                }
                
                if (showDownloadDialog || downloadState.isDownloading) {
                    DownloadSurahDialog(
                        surahNumber = surahNumber,
                        downloadState = downloadState,
                        onStartDownload = { 
                            audioViewModel.downloadSurah(context, surahNumber) 
                            showDownloadDialog = false
                        },
                        onCancelDownload = { 
                            audioViewModel.cancelDownload() 
                            showDownloadDialog = false
                        },
                        onDismiss = { showDownloadDialog = false }
                    )
                }
            }
            is com.example.prayertimes.viewmodel.SurahLoadState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            else -> {}
        }
    }
}

@Composable
fun DownloadSurahDialog(
    surahNumber: Int,
    downloadState: com.example.prayertimes.viewmodel.DownloadState,
    onStartDownload: () -> Unit,
    onCancelDownload: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!downloadState.isDownloading) onDismiss() },
        title = { Text(if (downloadState.isDownloading) "Downloading Surah" else "Download Surah") },
        text = {
            Column {
                if (downloadState.isDownloading && downloadState.surahNumber == surahNumber) {
                    Text("Downloading Ayah ${downloadState.downloadedAyahs} of ${downloadState.totalAyahs}")
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { downloadState.progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = Teal400
                    )
                } else {
                    Text("Download audio for offline listening? This will download all ayahs of this surah for the current reciter.")
                }
            }
        },
        confirmButton = {
            if (downloadState.isDownloading && downloadState.surahNumber == surahNumber) {
                TextButton(onClick = onCancelDownload) {
                    Text("Cancel", color = MaterialTheme.colorScheme.error)
                }
            } else {
                TextButton(onClick = onStartDownload) {
                    Text("Download", color = Teal400)
                }
            }
        },
        dismissButton = {
            if (!downloadState.isDownloading) {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

@Composable
fun AyahCard(
    ayah: Ayah,
    fontSizeMultiplier: Float,
    translationLang: String,
    isBookmarked: Boolean,
    isThisAyahPlaying: Boolean,
    onBookmarkClick: () -> Unit,
    onPlayClick: () -> Unit
) {
    val defaultCardColor = MaterialTheme.colorScheme.surfaceVariant
    val playingCardColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    val cardBackground by androidx.compose.animation.animateColorAsState(
        targetValue = if (isThisAyahPlaying) playingCardColor else defaultCardColor,
        animationSpec = androidx.compose.animation.core.tween(500),
        label = "ayahBackground"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        shape = RoundedCornerShape(16.dp),
        border = if (isThisAyahPlaying) BorderStroke(1.dp, Color(0xFF4DB6AC)) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Teal400),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ayah.number.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                IconButton(
                    onClick = onPlayClick,
                    modifier = Modifier.size(32.dp).padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = if(isThisAyahPlaying) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Play ayah",
                        tint = if(isThisAyahPlaying) Color(0xFF4DB6AC) else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                IconButton(onClick = onBookmarkClick) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) Gold500 else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Text(
                    text = ayah.arabic,
                    fontSize = (24 * fontSizeMultiplier).sp,
                    fontFamily = ArabicFontFamily,
                    color = MaterialTheme.arabicTextColor,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    lineHeight = (36 * fontSizeMultiplier).sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            androidx.compose.material3.HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val translation = if (translationLang == "ur") ayah.translationUr else ayah.translationEn
            val isUrdu = translationLang == "ur"
            
            if (isUrdu) {
                Text(
                    text = translation,
                    fontSize = (16 * fontSizeMultiplier).sp,
                    fontFamily = UrduFontFamily,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right,
                    lineHeight = (24 * fontSizeMultiplier).sp,
                    style = androidx.compose.ui.text.TextStyle(textDirection = androidx.compose.ui.text.style.TextDirection.Rtl)
                )
            } else {
                Text(
                    text = translation,
                    fontSize = (16 * fontSizeMultiplier).sp,
                    fontFamily = com.example.prayertimes.theme.EnglishTranslationFontFamily,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    lineHeight = (24 * fontSizeMultiplier).sp
                )
            }
        }
    }
}

@Composable
fun AudioPlayerBar(
    modifier: Modifier = Modifier,
    audioState: com.example.prayertimes.viewmodel.AudioState,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onPrevAyah: () -> Unit,
    onNextAyah: () -> Unit,
    onReciterSelect: () -> Unit,
    onToggleAutoPlay: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.GraphicEq, contentDescription = "Reciter", tint = Color(0xFF4DB6AC), modifier = Modifier.size(16.dp))
                Text(
                    text = audioState.selectedReciter.displayName,
                    color = Color(0xFF4DB6AC),
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Auto", color = Color.Gray, fontSize = 11.sp)
                    Switch(
                        checked = audioState.isAutoPlay,
                        onCheckedChange = { onToggleAutoPlay() },
                        modifier = Modifier.scale(0.7f)
                    )
                }
            }
            
            if(audioState.currentAyah > 0) {
                Text(
                    text = "Ayah ${audioState.currentAyah}",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevAyah) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(28.dp))
                }
                
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { onPlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    if(audioState.isLoading) {
                        androidx.compose.material3.CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = if(audioState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                
                IconButton(onClick = onNextAyah) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(28.dp))
                }
                
                IconButton(onClick = onStop) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color(0xFFEF9A9A), modifier = Modifier.size(28.dp))
                }
                
                IconButton(onClick = onReciterSelect) {
                    Icon(Icons.Default.Person, contentDescription = "Reciter", tint = Color(0xFF4DB6AC), modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReciterSelectionDialog(
    audioState: com.example.prayertimes.viewmodel.AudioState,
    onDismiss: () -> Unit,
    onReciterSelect: (com.example.prayertimes.data.model.Reciter) -> Unit
) {
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = false)
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
            Text("Select Reciter", color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(
                "Choose your preferred Quran reciter",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp, top = 4.dp)
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(com.example.prayertimes.data.model.Reciter.values()) { reciter ->
                    val isSelected = audioState.selectedReciter == reciter
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onReciterSelect(reciter) },
                        colors = CardDefaults.cardColors(
                            containerColor = if(isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if(isSelected) BorderStroke(1.dp, Color(0xFF4DB6AC)) else null,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .border(2.dp, if(isSelected) Color(0xFF4DB6AC) else Color.Gray, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if(isSelected) {
                                    Box(modifier = Modifier.size(10.dp).background(Color(0xFF4DB6AC), CircleShape))
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    reciter.displayName,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    reciter.arabicName,
                                    fontFamily = com.example.prayertimes.theme.ArabicFontFamily,
                                    color = com.example.prayertimes.theme.Teal400,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    reciter.description,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                            
                            if(isSelected) {
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = Color(0xFF4DB6AC), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

fun Int.toArabicNumerals(): String {
    val arabicNumerals = arrayOf("٠", "١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩")
    return this.toString().map { char ->
        if (char.isDigit()) arabicNumerals[char.toString().toInt()] else char
    }.joinToString("")
}

@Composable
fun MushafView(
    state: com.example.prayertimes.viewmodel.SurahLoadState.Ready,
    surahNumber: Int,
    fontSizeMultiplier: Float,
    paddingValues: PaddingValues
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        if (surahNumber != 1 && surahNumber != 9) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    fontSize = (24 * fontSizeMultiplier).sp,
                    fontFamily = ArabicFontFamily,
                    color = Teal400,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        val mushafText = buildAnnotatedString {
            state.ayahs.forEach { ayah ->
                append(ayah.arabic)
                withStyle(style = SpanStyle(color = Teal400)) {
                    append(" ﴿")
                    append(ayah.number.toArabicNumerals())
                    append("﴾ ")
                }
            }
        }
        
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Text(
                text = mushafText,
                fontSize = (28 * fontSizeMultiplier).sp,
                fontFamily = ArabicFontFamily,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Justify,
                lineHeight = (46 * fontSizeMultiplier).sp
            )
        }
        Spacer(Modifier.height(140.dp)) // padding for audio bar if visible
    }
}

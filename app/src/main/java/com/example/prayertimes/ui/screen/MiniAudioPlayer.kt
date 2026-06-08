package com.example.prayertimes.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prayertimes.viewmodel.QuranAudioViewModel
import com.example.prayertimes.viewmodel.QuranViewModel

@Composable
fun MiniAudioPlayer(
    audioViewModel: QuranAudioViewModel,
    quranViewModel: QuranViewModel,
    modifier: Modifier = Modifier,
    onNavigateToQuran: (Int) -> Unit
) {
    val audioState by audioViewModel.audioState.collectAsState()
    val surahs by quranViewModel.surahs.collectAsState()
    
    val isVisible = audioState.currentSurah > 0 && (audioState.isPlaying || audioState.currentAyah > 0)
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }, animationSpec = spring()),
        exit = slideOutVertically(targetOffsetY = { it }, animationSpec = spring()),
        modifier = modifier
    ) {
        val currentSurahName = surahs.find { it.number == audioState.currentSurah }?.nameEnglish ?: "Surah ${audioState.currentSurah}"
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .clickable { onNavigateToQuran(audioState.currentSurah) },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A3A4A)),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentSurahName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Ayah ${audioState.currentAyah} • ${audioState.selectedReciter.displayName}",
                        color = Color(0xFF4DB6AC),
                        fontSize = 12.sp
                    )
                }
                
                // Controls
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00897B))
                            .clickable { audioViewModel.pauseResume() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (audioState.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (audioState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    IconButton(onClick = { audioViewModel.stop() }, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Stop",
                            tint = Color(0xFFEF9A9A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

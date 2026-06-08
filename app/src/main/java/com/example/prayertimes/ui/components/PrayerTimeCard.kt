package com.example.prayertimes.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.example.prayertimes.data.model.Prayer
import com.example.prayertimes.data.model.PrayerTimeInfo
import com.example.prayertimes.theme.Gold500
import com.example.prayertimes.theme.Teal400

@Composable
fun PrayerTimeCard(
    prayerTimeInfo: PrayerTimeInfo,
    isExpanded: Boolean = false,
    countdownText: String = "",
    isActive: Boolean = false,
    endTime: String? = null,
    onClick: () -> Unit = {},
    onToggle: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isNext = prayerTimeInfo.isNext
    val isPassed = prayerTimeInfo.isPassed
    val isSunrise = prayerTimeInfo.prayer == Prayer.SUNRISE

    val isOffered = prayerTimeInfo.isOffered

    val scale by animateFloatAsState(
        targetValue = if (isNext) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isNext) MaterialTheme.colorScheme.primaryContainer
        else if (isSunrise) Color.Transparent
        else MaterialTheme.colorScheme.surfaceContainer,
        label = "bgColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (isNext) MaterialTheme.colorScheme.onPrimaryContainer
        else if (isSunrise) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        else if (isPassed && !isOffered) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        else MaterialTheme.colorScheme.onSurface,
        label = "textColor"
    )
    val iconColor = if (isNext) Gold500 
        else if (isSunrise || (isPassed && !isOffered)) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        else MaterialTheme.colorScheme.onSurfaceVariant
        
    val iconBgColor = if (isNext) Gold500.copy(alpha = 0.2f)
        else if (isSunrise) Color.Transparent
        else MaterialTheme.colorScheme.surfaceVariant

    val borderColor = if (isNext) Color(0xFFFF9800) // Orange
        else if (isOffered) Color(0xFF4CAF50) // Green
        else if (isPassed) Color(0xFFF44336) // Red
        else Color.Gray // Grey

    val cardModifier = if (isSunrise) {
        modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 6.dp)
    } else {
        modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(backgroundColor, backgroundColor.copy(alpha = if (isNext) 0.85f else 1f))))
            .clickable(enabled = !isSunrise, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp) // Slightly less horizontal padding to accommodate border
            .animateContentSize()
    }

    if (isSunrise) {
        androidx.compose.material3.HorizontalDivider(
            modifier = Modifier.padding(horizontal = 32.dp).padding(top = 8.dp, bottom = 4.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    }

    Column(
        modifier = cardModifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                // Left border accent
                if (!isSunrise) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(40.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(borderColor)
                    )
                }
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getPrayerIcon(prayerTimeInfo.prayer),
                        contentDescription = prayerTimeInfo.name,
                        modifier = Modifier.size(20.dp),
                        tint = iconColor
                    )
                }
                Text(
                    text = prayerTimeInfo.name,
                    style = if (isSunrise) MaterialTheme.typography.bodySmall else MaterialTheme.typography.titleMedium,
                    fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
                    color = textColor
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = prayerTimeInfo.formattedTime,
                    style = if (isSunrise) MaterialTheme.typography.bodySmall else MaterialTheme.typography.titleMedium,
                    fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                    color = textColor
                )
                if (isNext && isSunrise) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Teal400))
                } else if (!isSunrise && isPassed && onToggle != null) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        var triggerScale by remember { mutableStateOf(false) }
                        val checkScale by animateFloatAsState(
                            targetValue = if (triggerScale) 1.3f else 1.0f,
                            animationSpec = tween(100),
                            finishedListener = { if (it == 1.3f) triggerScale = false },
                            label = "checkScale"
                        )
                        
                        val iconTint by animateColorAsState(
                            targetValue = if (isOffered) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            animationSpec = tween(200),
                            label = "checkColor"
                        )
                        
                        IconButton(
                            onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                triggerScale = true
                                onToggle(!isOffered) 
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isOffered) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                contentDescription = if (isOffered) "Offered" else "Not Offered",
                                tint = iconTint,
                                modifier = Modifier.size(28.dp).scale(checkScale)
                            )
                        }
                    }
                }
            }
        }
        
        androidx.compose.animation.AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f), modifier = Modifier.padding(bottom = 12.dp))
                
                val fardhText = getFardhText(prayerTimeInfo.prayer)
                val sunnahText = getSunnahText(prayerTimeInfo.prayer)
                
                if (fardhText != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Teal400))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(fardhText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                if (sunnahText != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(sunnahText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                if (!endTime.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Ends at: $endTime", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
    
    if (isSunrise) {
        androidx.compose.material3.HorizontalDivider(
            modifier = Modifier.padding(horizontal = 32.dp).padding(top = 4.dp, bottom = 8.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    }
}

private fun getPrayerIcon(prayer: Prayer): ImageVector = when (prayer) {
    Prayer.FAJR -> Icons.Rounded.WbTwilight
    Prayer.SUNRISE -> Icons.Rounded.WbSunny
    Prayer.DHUHR -> Icons.Rounded.WbSunny
    Prayer.ASR -> Icons.Rounded.WbSunny
    Prayer.MAGHRIB -> Icons.Rounded.WbTwilight
    Prayer.ISHA -> Icons.Rounded.WbTwilight
}

private fun getFardhText(prayer: Prayer): String? = when (prayer) {
    Prayer.FAJR -> "2 Fardh"
    Prayer.DHUHR -> "4 Fardh"
    Prayer.ASR -> "4 Fardh"
    Prayer.MAGHRIB -> "3 Fardh"
    Prayer.ISHA -> "4 Fardh"
    Prayer.SUNRISE -> null
}

private fun getSunnahText(prayer: Prayer): String? = when (prayer) {
    Prayer.FAJR -> "2 Sunnah Muakkadah (before)"
    Prayer.DHUHR -> "4 Sunnah Muakkadah (before) | 2 Sunnah Muakkadah (after)"
    Prayer.ASR -> "4 Sunnah Ghair Muakkadah (before)"
    Prayer.MAGHRIB -> "2 Sunnah Muakkadah (after)"
    Prayer.ISHA -> "2 Sunnah Muakkadah (after) | 3 Witr (Wajib)"
    Prayer.SUNRISE -> null
}

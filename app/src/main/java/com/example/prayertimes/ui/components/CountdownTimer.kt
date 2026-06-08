package com.example.prayertimes.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.togetherWith
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun CountdownTimer(countdownText: String, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val colonAlpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(500, easing = LinearEasing), RepeatMode.Reverse),
        label = "colonBlink"
    )

    if (countdownText.isNotEmpty()) {
        val parts = countdownText.split(":")
        if (parts.size == 3) {
            Row(modifier = modifier, horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                TimeDigit(parts[0])
                Text(":", style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Light, fontSize = 42.sp
                ), color = MaterialTheme.colorScheme.primary, modifier = Modifier.alpha(colonAlpha))
                TimeDigit(parts[1])
                Text(":", style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Light, fontSize = 42.sp
                ), color = MaterialTheme.colorScheme.primary, modifier = Modifier.alpha(colonAlpha))
                TimeDigit(parts[2])
            }
        }
    }
}

@Composable
private fun TimeDigit(value: String) {
    androidx.compose.animation.AnimatedContent(
        targetState = value,
        transitionSpec = {
            (androidx.compose.animation.slideInVertically { height -> height } + androidx.compose.animation.fadeIn()) togetherWith
            (androidx.compose.animation.slideOutVertically { height -> -height } + androidx.compose.animation.fadeOut())
        },
        label = "timeDigit"
    ) { digit ->
        Text(digit, style = MaterialTheme.typography.displayMedium.copy(
            fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold, fontSize = 42.sp, letterSpacing = 2.sp
        ), color = MaterialTheme.colorScheme.onSurface)
    }
}

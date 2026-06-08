package com.example.prayertimes.ui.screen

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.prayertimes.R
import androidx.compose.ui.res.stringResource
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.prayertimes.theme.Gold500
import com.example.prayertimes.theme.Teal400
import com.example.prayertimes.viewmodel.TasbihViewModel
import kotlinx.coroutines.delay

@Composable
fun TasbihScreen(
    viewModel: TasbihViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val count by viewModel.currentCount.collectAsState()
    val target by viewModel.target.collectAsState()
    val selectedPreset by viewModel.selectedPreset.collectAsState()
    val todayTotal by viewModel.todayTotal.collectAsState()
    val goalReached by viewModel.goalReached.collectAsState()

    // Vibrator helper
    fun vibrate(pattern: LongArray = longArrayOf(0, 60)) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            @Suppress("DEPRECATION")
            v.vibrate(pattern, -1)
        }
    }

    // Goal completion flash
    val goalFlashColor by animateColorAsState(
        targetValue = if (goalReached) Color(0xFF4CAF50).copy(alpha = 0.25f) else Color.Transparent,
        animationSpec = tween(300),
        label = "goalFlash"
    )
    LaunchedEffect(goalReached) {
        if (goalReached) {
            vibrate(longArrayOf(0, 100, 80, 100, 80, 100))
            android.widget.Toast.makeText(context, "MashaAllah! ✓", android.widget.Toast.LENGTH_SHORT).show()
            delay(1500)
            viewModel.dismissGoal()
        }
    }

    // Counter scale bounce on tap
    var tapTrigger by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (tapTrigger) 1.03f else 1f,
        animationSpec = tween(75),
        label = "tapScale",
        finishedListener = { if (it == 1.03f) tapTrigger = false }
    )
    
    LaunchedEffect(goalReached) {
        if (goalReached) {
            vibrate(longArrayOf(0, 100, 80, 100, 80, 100))
            android.widget.Toast.makeText(context, "MashaAllah! ✓", android.widget.Toast.LENGTH_SHORT).show()
            delay(3000L)
            viewModel.dismissGoal()
        }
    }

    // Custom target dialog
    var showTargetDialog by remember { mutableStateOf(false) }
    var customTarget by remember { mutableStateOf("") }

    if (showTargetDialog) {
        AlertDialog(
            onDismissRequest = { showTargetDialog = false },
            title = { Text("Custom Target") },
            text = {
                OutlinedTextField(
                    value = customTarget,
                    onValueChange = { customTarget = it.filter { c -> c.isDigit() } },
                    label = { Text("Enter target number") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    customTarget.toIntOrNull()?.let { if (it > 0) viewModel.setTarget(it) }
                    showTargetDialog = false
                }) { Text("Set") }
            },
            dismissButton = {
                TextButton(onClick = { showTargetDialog = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(goalFlashColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                viewModel.increment()
                vibrate()
                tapTrigger = true
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        
        Spacer(modifier = Modifier.weight(1f))

        Text(
            selectedPreset.arabic,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Gold500,
            style = MaterialTheme.typography.headlineLarge.copy(textDirection = TextDirection.Rtl)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            selectedPreset.label,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .scale(animatedScale)
                .fillMaxWidth()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$count",
                    fontSize = 120.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Target: $target",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        AnimatedVisibility(visible = goalReached) {
            Text(
                "MashaAllah! Goal Reached ✓",
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.weight(1.5f))

        // Actions row (Presets, Target, Reset)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Preset Button
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clickable {
                    val presets = listOf(
                        com.example.prayertimes.viewmodel.TasbihViewModel.DhikrPreset.SUBHANALLAH,
                        com.example.prayertimes.viewmodel.TasbihViewModel.DhikrPreset.ALHAMDULILLAH,
                        com.example.prayertimes.viewmodel.TasbihViewModel.DhikrPreset.ALLAHU_AKBAR
                    )
                    val nextIdx = (presets.indexOf(selectedPreset) + 1) % presets.size
                    viewModel.selectPreset(presets[nextIdx])
                }
            ) {
                Icon(
                    Icons.Rounded.SwapHoriz, 
                    contentDescription = "Change Dhikr", 
                    modifier = Modifier.padding(16.dp).size(24.dp), 
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Target Button
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clickable {
                    val targets = listOf(33, 99, 100)
                    val nextIdx = if (targets.contains(target)) (targets.indexOf(target) + 1) % targets.size else 0
                    viewModel.setTarget(targets[nextIdx])
                }
            ) {
                Icon(
                    Icons.Rounded.TrackChanges, 
                    contentDescription = "Change Target", 
                    modifier = Modifier.padding(16.dp).size(24.dp), 
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Reset button
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clickable { viewModel.reset() }
            ) {
                Icon(
                    Icons.Rounded.RestartAlt, 
                    contentDescription = stringResource(R.string.reset), 
                    modifier = Modifier.padding(16.dp).size(24.dp), 
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

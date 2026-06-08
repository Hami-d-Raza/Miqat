package com.example.prayertimes.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prayertimes.data.model.GuideStep
import com.example.prayertimes.data.model.PrayerGuideData
import com.example.prayertimes.data.model.WuduGuideData
import com.example.prayertimes.theme.Gold500
import com.example.prayertimes.theme.Teal400
import com.example.prayertimes.viewmodel.PrayerGuideViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerGuideScreen(
    viewModel: PrayerGuideViewModel,
    onNavigateBack: () -> Unit
) {
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()
    val prayerStepIndex by viewModel.prayerStepIndex.collectAsState()
    val wuduStepIndex by viewModel.wuduStepIndex.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prayer Guide", fontWeight = FontWeight.Bold) },
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
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = { Text("How to Pray") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = { Text("Wudu Guide") }
                )
            }

            AnimatedContent(
                targetState = selectedTabIndex,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "tabContent"
            ) { targetIndex ->
                if (targetIndex == 0) {
                    GuideStepper(
                        steps = PrayerGuideData.prayerSteps,
                        currentIndex = prayerStepIndex,
                        onStepChange = { viewModel.setPrayerStep(it) }
                    )
                } else {
                    GuideStepper(
                        steps = WuduGuideData.wuduSteps,
                        currentIndex = wuduStepIndex,
                        onStepChange = { viewModel.setWuduStep(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun GuideStepper(
    steps: List<GuideStep>,
    currentIndex: Int,
    onStepChange: (Int) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Step Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, _ ->
                    val isSelected = index == currentIndex
                    val dotSize by androidx.compose.animation.core.animateDpAsState(
                        targetValue = if (isSelected) 12.dp else 8.dp,
                        animationSpec = androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy)
                    )
                    val dotColor by androidx.compose.animation.animateColorAsState(
                        targetValue = if (isSelected) Teal400 else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        animationSpec = androidx.compose.animation.core.tween(300)
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(dotSize)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }

            val pagerState = androidx.compose.foundation.pager.rememberPagerState(
                initialPage = currentIndex,
                pageCount = { steps.size }
            )
            
            androidx.compose.runtime.LaunchedEffect(currentIndex) {
                if (pagerState.currentPage != currentIndex) {
                    pagerState.animateScrollToPage(currentIndex)
                }
            }
            androidx.compose.runtime.LaunchedEffect(pagerState.currentPage) {
                if (pagerState.currentPage != currentIndex) {
                    onStepChange(pagerState.currentPage)
                }
            }

            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val step = steps[page]
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Step ${step.stepNumber}",
                        color = Teal400,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = step.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (step.arabicText.isNotEmpty()) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            Text(
                                text = step.arabicText,
                                fontSize = 28.sp,
                                color = Gold500,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    if (step.transliteration.isNotEmpty()) {
                        Text(
                            text = step.transliteration,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    
                    Text(
                        text = step.description,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }
            }
        }

        // Navigation Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { onStepChange(currentIndex - 1) },
                enabled = currentIndex > 0,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            ) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Previous")
                Spacer(Modifier.width(4.dp))
                Text("Prev")
            }

            Button(
                onClick = { onStepChange(currentIndex + 1) },
                enabled = currentIndex < steps.size - 1,
                colors = ButtonDefaults.buttonColors(containerColor = Teal400, contentColor = Color.White)
            ) {
                Text("Next")
                Spacer(Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next")
            }
        }
    }
}

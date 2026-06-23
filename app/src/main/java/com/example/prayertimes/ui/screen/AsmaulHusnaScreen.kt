package com.example.prayertimes.ui.screen
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prayertimes.data.model.AsmaulHusnaData
import com.example.prayertimes.data.model.DivineName
import com.example.prayertimes.data.model.NameCategory
import com.example.prayertimes.theme.Gold500
import com.example.prayertimes.theme.Teal400
import com.example.prayertimes.theme.Teal700
import com.example.prayertimes.ui.components.ShimmerPlaceholder
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsmaulHusnaScreen(
    onBackClick: () -> Unit,
    onNameClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) { delay(400); isLoading = false }
    
    val filteredNames = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            AsmaulHusnaData.names
        } else {
            val query = searchQuery.lowercase()
            AsmaulHusnaData.names.filter {
                it.transliteration.lowercase().contains(query) ||
                it.meaning.lowercase().contains(query) ||
                it.arabic.contains(query) ||
                it.number.toString() == query
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("99 Names of Allah", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(modifier = modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search by name or meaning...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Teal400,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            androidx.compose.animation.AnimatedContent(
                targetState = isLoading,
                transitionSpec = { androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(400)) togetherWith androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(400)) },
                label = "asmaulHusnaGrid"
            ) { loading ->
                if (loading) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(8) {
                            DivineNameShimmerCard()
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredNames, key = { it.number }) { divineName ->
                            DivineNameCard(divineName = divineName, onClick = { onNameClick(divineName.number) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DivineNameShimmerCard() {
    Card(
        modifier = Modifier.fillMaxWidth().height(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                ShimmerPlaceholder(Modifier.size(24.dp), cornerRadius = 8.dp)
            }
            Spacer(Modifier.height(8.dp))
            ShimmerPlaceholder(Modifier.height(30.dp).width(60.dp))
            Spacer(Modifier.height(8.dp))
            ShimmerPlaceholder(Modifier.height(20.dp).width(80.dp))
            Spacer(Modifier.height(8.dp))
            ShimmerPlaceholder(Modifier.height(14.dp).width(100.dp))
        }
    }
}

@Composable
fun DivineNameCard(divineName: DivineName, onClick: () -> Unit) {
    val categoryColor = when (divineName.category) {
        NameCategory.BEAUTY -> Gold500
        NameCategory.MAJESTY -> Teal700
        NameCategory.PERFECTION -> Color(0xFF9C27B0) // Purple
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(categoryColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = divineName.number.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = categoryColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = divineName.arabic,
                style = MaterialTheme.typography.headlineMedium,
                color = categoryColor,
                fontFamily = com.example.prayertimes.theme.ArabicFontFamily,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = divineName.transliteration,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(4.dp))
            
            Text(
                text = divineName.meaning,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

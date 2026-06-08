package com.example.prayertimes.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import com.example.prayertimes.data.model.Dua
import com.example.prayertimes.data.model.DuaCategory
import com.example.prayertimes.data.model.DuasData
import com.example.prayertimes.theme.Gold500
import com.example.prayertimes.theme.Teal400
import com.example.prayertimes.viewmodel.DuasViewModel
import androidx.compose.ui.graphics.vector.ImageVector

val DuaCategory.icon: ImageVector
    get() = when (this) {
        DuaCategory.AFTER_PRAYER -> Icons.Rounded.Mosque
        DuaCategory.MORNING -> Icons.Rounded.WbSunny
        DuaCategory.EVENING -> Icons.Rounded.NightsStay
        DuaCategory.DAILY_LIFE -> Icons.Rounded.Favorite
        DuaCategory.TRAVEL -> Icons.Rounded.Flight
    }

@Composable
fun DuasScreen(modifier: Modifier = Modifier, viewModel: DuasViewModel = viewModel()) {
    var selectedCategory by remember { mutableStateOf<DuaCategory?>(null) }
    var showingFavorites by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val favorites by viewModel.favorites.collectAsState()
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(50); visible = true }

    Column(modifier = modifier.fillMaxSize()) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it
                if (it.isNotEmpty()) {
                    selectedCategory = null
                    showingFavorites = false
                }
            },
            placeholder = { Text("Search duas...") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Teal400,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        AnimatedContent(
            targetState = when {
                searchQuery.isNotEmpty() -> "search"
                showingFavorites -> "favorites"
                selectedCategory != null -> "category"
                else -> "home"
            },
            transitionSpec = {
                (fadeIn() + slideInHorizontally { it / 4 }) togetherWith
                        (fadeIn().let { androidx.compose.animation.ExitTransition.None } + slideOutHorizontally { -it / 4 })
            },
            label = "duasContent"
        ) { state ->
            when (state) {
                "search" -> {
                    val results = DuasData.duas.filter {
                        it.title.contains(searchQuery, true) ||
                        it.arabic.contains(searchQuery, true) ||
                        it.translation.contains(searchQuery, true) ||
                        it.transliteration.contains(searchQuery, true)
                    }
                    DuasList(
                        duas = results,
                        showCategory = true,
                        favorites = favorites,
                        onToggleFavorite = { viewModel.toggleFavorite(it) }
                    )
                }
                "favorites" -> {
                    val duas = DuasData.duas.filter { it.id.toString() in favorites }
                    Column {
                        TextButton(
                            onClick = { showingFavorites = false },
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Back")
                        }
                        Text(
                            "⭐ Favorites",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                        DuasList(
                            duas = duas,
                            favorites = favorites,
                            onToggleFavorite = { viewModel.toggleFavorite(it) }
                        )
                    }
                }
                "category" -> {
                    val cat = selectedCategory
                    if (cat != null) {
                        val duas = DuasData.duas.filter { it.category == cat }
                        Column {
                            TextButton(
                                onClick = { selectedCategory = null },
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Back")
                            }
                            Text(
                                " ${cat.displayName}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                            DuasList(
                                duas = duas,
                                favorites = favorites,
                                onToggleFavorite = { viewModel.toggleFavorite(it) }
                            )
                        }
                    }
                }
                else -> {
                    // Category grid
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp) // Increased spacing
                    ) {
                        item {
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(animationSpec = tween(300)) + slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(400))
                            ) {
                            // Favorites Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp) // Increased height
                                    .clickable { showingFavorites = true },
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.horizontalGradient(listOf(Gold500.copy(alpha = 0.1f), MaterialTheme.colorScheme.surfaceContainer)))
                                        .padding(horizontal = 20.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                        Box(
                                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Gold500.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) { 
                                            Icon(Icons.Rounded.Star, contentDescription = null, tint = Gold500, modifier = Modifier.size(28.dp))
                                        }
                                        Column {
                                            Text("Favorites", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Text("${favorites.size} saved duas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            }
                        }

                        val categories = DuaCategory.values()
                        items(categories.size) { index ->
                            val category = categories[index]
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(animationSpec = tween(300, delayMillis = (index + 1) * 50)) + 
                                        slideInVertically(
                                            initialOffsetY = { 50 }, 
                                            animationSpec = tween(400, delayMillis = (index + 1) * 50)
                                        )
                            ) {
                                DuaCategoryCard(
                                    category = category,
                                    count = DuasData.duas.count { it.category == category },
                                    onClick = { selectedCategory = category }
                                )
                            }
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun DuaCategoryCard(category: DuaCategory, count: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp) // Increased height
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(listOf(Teal400.copy(alpha = 0.08f), MaterialTheme.colorScheme.surfaceContainer)))
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(
                        when (category) {
                            DuaCategory.MORNING -> Gold500.copy(alpha = 0.15f)
                            DuaCategory.EVENING -> Color(0xFF673AB7).copy(alpha = 0.15f)
                            DuaCategory.TRAVEL -> Color(0xFF2196F3).copy(alpha = 0.15f)
                            else -> Teal400.copy(alpha = 0.15f)
                        }
                    ),
                    contentAlignment = Alignment.Center
                ) { 
                    Icon(
                        category.icon, 
                        contentDescription = null, 
                        tint = when (category) {
                            DuaCategory.MORNING -> Gold500
                            DuaCategory.EVENING -> Color(0xFF673AB7)
                            DuaCategory.TRAVEL -> Color(0xFF2196F3)
                            else -> Teal400
                        },
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column {
                    Text(category.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("$count duas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DuasList(
    duas: List<Dua>, 
    showCategory: Boolean = false, 
    favorites: Set<String>, 
    onToggleFavorite: (String) -> Unit
) {
    val expandedStates = remember { androidx.compose.runtime.mutableStateMapOf<Int, Boolean>() }
    if (duas.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.SearchOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f))
                Spacer(Modifier.height(12.dp))
                Text("No duas available", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(duas, key = { it.id }) { dua ->
            DuaCard(
                dua = dua, 
                showCategory = showCategory, 
                isFavorite = dua.id.toString() in favorites,
                onToggleFavorite = { onToggleFavorite(dua.id.toString()) },
                expanded = expandedStates[dua.id] == true,
                onToggleExpanded = { expandedStates[dua.id] = !(expandedStates[dua.id] ?: false) }
            )
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun DuaCard(
    dua: Dua, 
    showCategory: Boolean = false, 
    isFavorite: Boolean, 
    onToggleFavorite: () -> Unit,
    expanded: Boolean,
    onToggleExpanded: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpanded() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
                // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dua.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (showCategory) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(dua.category.icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = Teal400)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = dua.category.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = Teal400
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            if (isFavorite) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Gold500 else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    // Copy button
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val textToCopy = "${dua.arabic}\n\n${dua.translation}"
                            clipboard.setPrimaryClip(ClipData.newPlainText("Dua", textToCopy))
                            Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            Icons.Rounded.ContentCopy,
                            contentDescription = "Copy",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Arabic text
            Box(Modifier.fillMaxWidth()) {
                androidx.compose.foundation.text.BasicText(
                    text = dua.arabic,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 24.sp,
                        textDirection = TextDirection.Rtl,
                        lineHeight = 42.sp,
                        color = Gold500,
                        textAlign = TextAlign.End
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Expandable transliteration + translation
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.08f))
                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = dua.transliteration,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontStyle = FontStyle.Italic,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = dua.translation,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 16.sp,
                            lineHeight = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Icon(
                        if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
        }
    }
}

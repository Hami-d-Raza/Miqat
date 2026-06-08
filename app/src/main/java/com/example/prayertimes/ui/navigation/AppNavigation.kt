package com.example.prayertimes.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BlurCircular
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.prayertimes.ui.screen.AboutScreen
import com.example.prayertimes.ui.screen.DuasScreen
import com.example.prayertimes.ui.screen.HomeScreen
import com.example.prayertimes.ui.screen.QiblaScreen
import com.example.prayertimes.ui.screen.SettingsScreen
import com.example.prayertimes.ui.screen.StatsScreen
import com.example.prayertimes.ui.screen.TasbihScreen
import com.example.prayertimes.ui.screen.TimetableScreen
import com.example.prayertimes.viewmodel.HomeViewModel
import com.example.prayertimes.viewmodel.QiblaViewModel
import com.example.prayertimes.viewmodel.SettingsViewModel
import com.example.prayertimes.viewmodel.TasbihViewModel
import com.example.prayertimes.viewmodel.StatsViewModel
import com.example.prayertimes.R

sealed class Screen(val route: String, val titleRes: Int, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    data object Onboarding : Screen("onboarding", R.string.app_name, Icons.Filled.Home, Icons.Outlined.Home)
    data object Home     : Screen("home",     R.string.nav_home,     Icons.Filled.Home,     Icons.Outlined.Home)
    data object Qibla    : Screen("qibla",    R.string.nav_qibla,    Icons.Filled.Explore,  Icons.Outlined.Explore)
    data object Tasbih   : Screen("tasbih",   R.string.nav_tasbih,   Icons.Rounded.BlurCircular, Icons.Rounded.BlurCircular)
    data object Duas     : Screen("duas",     R.string.nav_duas,     Icons.Rounded.AutoAwesome, Icons.Rounded.AutoAwesome)
    data object Stats    : Screen("stats",    R.string.prayer_statistics,    Icons.Filled.BarChart, Icons.Outlined.BarChart)
    data object Settings : Screen("settings", R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings)
    data object Timetable: Screen("timetable",R.string.monthly_timetable,Icons.Filled.Home,    Icons.Outlined.Home)
    data object About    : Screen("about",    R.string.about,    Icons.Rounded.Info,   Icons.Rounded.Info)
    data object AsmaulHusna : Screen("asmaulhusna", R.string.app_name, Icons.Rounded.MenuBook, Icons.Rounded.MenuBook)
    data object AsmaulHusnaDetail : Screen("asmaulhusna_detail/{number}", R.string.app_name, Icons.Rounded.MenuBook, Icons.Rounded.MenuBook) {
        fun createRoute(number: Int) = "asmaulhusna_detail/$number"
    }
    data object QuranList : Screen("quran_list", R.string.nav_quran, Icons.Rounded.MenuBook, Icons.Rounded.MenuBook)
    data object QuranReader : Screen("quran_reader/{surah}", R.string.app_name, Icons.Rounded.MenuBook, Icons.Rounded.MenuBook) {
        fun createRoute(surah: Int) = "quran_reader/$surah"
    }
    data object PrayerGuide : Screen("prayer_guide", R.string.app_name, Icons.Rounded.MenuBook, Icons.Rounded.MenuBook)
    data object HijriCalendar : Screen("hijri_calendar", R.string.app_name, Icons.Rounded.DateRange, Icons.Rounded.DateRange)
}

// Quran is now in position 2 of the nav bar (swapped with Qibla)
// Qibla is now accessible from the Home screen tiles
private val navTabs = listOf(Screen.Home, Screen.QuranList, Screen.Tasbih, Screen.Duas, Screen.Settings)

@Composable
fun AppNavigation(startDestination: String) {
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = viewModel()
    val qiblaViewModel: QiblaViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()
    val tasbihViewModel: TasbihViewModel = viewModel()
    val statsViewModel: StatsViewModel = viewModel()
    val quranViewModel: com.example.prayertimes.viewmodel.QuranViewModel = viewModel()
    val quranAudioViewModel: com.example.prayertimes.viewmodel.QuranAudioViewModel = viewModel()
    val prayerGuideViewModel: com.example.prayertimes.viewmodel.PrayerGuideViewModel = viewModel()
    val hijriCalendarViewModel: com.example.prayertimes.viewmodel.HijriCalendarViewModel = viewModel()

    Scaffold(
        bottomBar = {
            // Only show bottom bar for main tabs
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            
            androidx.compose.foundation.layout.Column {
                if (currentRoute?.startsWith("quran_reader") != true) {
                    com.example.prayertimes.ui.screen.MiniAudioPlayer(
                        audioViewModel = quranAudioViewModel,
                        quranViewModel = quranViewModel,
                        onNavigateToQuran = { surah -> 
                            val route = Screen.QuranReader.createRoute(surah)
                            if (currentRoute != route) {
                                navController.navigate(route)
                            }
                        }
                    )
                }
                
                if (currentRoute in navTabs.map { it.route }) {
                    NavigationBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        tonalElevation = 8.dp
                    ) {
                        val currentDestination = navBackStackEntry?.destination
                        navTabs.forEach { screen ->
                            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                            val scale by androidx.compose.animation.core.animateFloatAsState(
                                targetValue = if (selected) 1.1f else 1.0f,
                                animationSpec = androidx.compose.animation.core.spring(
                                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                                ), label = "nav_scale"
                            )
                            NavigationBarItem(
                                icon = { Icon(if (selected) screen.selectedIcon else screen.unselectedIcon, stringResource(screen.titleRes), modifier = Modifier.scale(scale)) },
                                label = { Text(stringResource(screen.titleRes), fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                                selected = selected,
                                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                    selectedIconColor = com.example.prayertimes.theme.Teal400,
                                    selectedTextColor = com.example.prayertimes.theme.Teal400,
                                    unselectedIconColor = androidx.compose.ui.graphics.Color.Gray,
                                    unselectedTextColor = androidx.compose.ui.graphics.Color.Gray,
                                    indicatorColor = com.example.prayertimes.theme.Teal400.copy(alpha = 0.1f)
                                ),
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true; restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination,
            Modifier.padding(innerPadding),
            enterTransition = {
                val isBottomNavSwitch = initialState.destination.route in navTabs.map { it.route } && targetState.destination.route in navTabs.map { it.route }
                if (isBottomNavSwitch) {
                    androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(200))
                } else {
                    androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(300)) + 
                    androidx.compose.animation.slideInHorizontally(androidx.compose.animation.core.tween(300, easing = androidx.compose.animation.core.EaseInOutCubic)) { it }
                }
            },
            exitTransition = {
                val isBottomNavSwitch = initialState.destination.route in navTabs.map { it.route } && targetState.destination.route in navTabs.map { it.route }
                if (isBottomNavSwitch) {
                    androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(200))
                } else {
                    androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(300)) + 
                    androidx.compose.animation.slideOutHorizontally(androidx.compose.animation.core.tween(300, easing = androidx.compose.animation.core.EaseInOutCubic)) { -it }
                }
            },
            popEnterTransition = {
                val isBottomNavSwitch = initialState.destination.route in navTabs.map { it.route } && targetState.destination.route in navTabs.map { it.route }
                if (isBottomNavSwitch) {
                    androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(200))
                } else {
                    androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(300)) + 
                    androidx.compose.animation.slideInHorizontally(androidx.compose.animation.core.tween(300, easing = androidx.compose.animation.core.EaseInOutCubic)) { -it }
                }
            },
            popExitTransition = {
                val isBottomNavSwitch = initialState.destination.route in navTabs.map { it.route } && targetState.destination.route in navTabs.map { it.route }
                if (isBottomNavSwitch) {
                    androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(200))
                } else {
                    androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(300)) + 
                    androidx.compose.animation.slideOutHorizontally(androidx.compose.animation.core.tween(300, easing = androidx.compose.animation.core.EaseInOutCubic)) { it }
                }
            }
        ) {
            composable(Screen.Onboarding.route) {
                com.example.prayertimes.ui.screen.OnboardingScreen(
                    onComplete = {
                        settingsViewModel.updateOnboardingComplete(true)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Home.route) { 
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                    onNavigateToTimetable = { navController.navigate(Screen.Timetable.route) },
                    onNavigateToAsmaulHusna = { navController.navigate(Screen.AsmaulHusna.route) },
                    onNavigateToQibla = { navController.navigate(Screen.Qibla.route) },
                    onNavigateToPrayerGuide = { navController.navigate(Screen.PrayerGuide.route) },
                    onNavigateToHijriCalendar = { navController.navigate(Screen.HijriCalendar.route) }
                ) 
            }
            composable(Screen.Qibla.route)     { QiblaScreen(viewModel = qiblaViewModel, onBackClick = { navController.popBackStack() }) }
            composable(Screen.Tasbih.route)    { TasbihScreen(viewModel = tasbihViewModel) }
            composable(Screen.Duas.route)      { DuasScreen() }
            composable(Screen.Stats.route)     { 
                StatsScreen(viewModel = statsViewModel, onBackClick = { navController.popBackStack() }) 
            }
            composable(Screen.Timetable.route) { 
                TimetableScreen(homeViewModel = homeViewModel, onBackClick = { navController.popBackStack() }) 
            }
            composable(
                Screen.Settings.route,
                enterTransition = {
                    androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(250)) +
                    androidx.compose.animation.slideInVertically(androidx.compose.animation.core.tween(250)) { 30 }
                }
            )  { 
                SettingsScreen(
                    viewModel = settingsViewModel,
                    homeViewModel = homeViewModel,
                    quranViewModel = quranViewModel,
                    audioViewModel = quranAudioViewModel,
                    onNavigateToAbout = { navController.navigate(Screen.About.route) }
                ) 
            }
            composable(Screen.About.route)     { AboutScreen(onBackClick = { navController.popBackStack() }) }
            composable(Screen.AsmaulHusna.route) { 
                com.example.prayertimes.ui.screen.AsmaulHusnaScreen(
                    onBackClick = { navController.popBackStack() },
                    onNameClick = { number -> navController.navigate(Screen.AsmaulHusnaDetail.createRoute(number)) }
                )
            }
            composable(
                route = Screen.AsmaulHusnaDetail.route,
                arguments = listOf(androidx.navigation.navArgument("number") { type = androidx.navigation.NavType.IntType })
            ) { backStackEntry ->
                val number = backStackEntry.arguments?.getInt("number") ?: 1
                com.example.prayertimes.ui.screen.AsmaulHusnaDetailScreen(
                    number = number,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.QuranList.route) {
                com.example.prayertimes.ui.screen.QuranListScreen(
                    viewModel = quranViewModel,
                    audioViewModel = quranAudioViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onSurahClick = { surah -> navController.navigate(Screen.QuranReader.createRoute(surah)) }
                )
            }
            composable(
                route = Screen.QuranReader.route,
                arguments = listOf(androidx.navigation.navArgument("surah") { type = androidx.navigation.NavType.IntType })
            ) { backStackEntry ->
                val surah = backStackEntry.arguments?.getInt("surah") ?: 1
                com.example.prayertimes.ui.screen.QuranReaderScreen(
                    surahNumber = surah,
                    viewModel = quranViewModel,
                    audioViewModel = quranAudioViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.PrayerGuide.route) {
                com.example.prayertimes.ui.screen.PrayerGuideScreen(
                    viewModel = prayerGuideViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.HijriCalendar.route) {
                com.example.prayertimes.ui.screen.HijriCalendarScreen(
                    viewModel = hijriCalendarViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

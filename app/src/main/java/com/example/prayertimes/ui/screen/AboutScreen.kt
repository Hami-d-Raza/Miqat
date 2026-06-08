package com.example.prayertimes.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prayertimes.BuildConfig
import com.example.prayertimes.R
import com.example.prayertimes.theme.Gold500
import com.example.prayertimes.theme.Teal400

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_mosque),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(120.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Quran Audio Edition",
                style = MaterialTheme.typography.labelSmall,
                color = Teal400,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your Complete Islamic Companion",
                style = MaterialTheme.typography.bodyLarge,
                color = Gold500,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Prayer • Quran • Guidance",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Developer Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Developed by",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Muhammad Hamid Raza",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Teal400
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(onClick = { 
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:hamidraza9182@gmail.com")
                            }
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Rounded.Email, contentDescription = "Email", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Info Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "How It Works",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    InfoRow("Prayer times calculated using the Adhan library for accurate worldwide calculations.")
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow("Quran audio streams natively via Islamic Network CDN (cdn.islamic.network).")
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow("Offline audio caching handled seamlessly via ExoPlayer Media3.")
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow("Qibla direction calculated using great circle bearing to the Holy Kaaba, Makkah.")
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow("All core features work completely offline.")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Features List
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Features",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    FeatureItem("🕌 Accurate prayer times for any location worldwide")
                    FeatureItem("🧭 Qibla compass with real-time direction")
                    FeatureItem("📅 Monthly prayer timetable with navigation")
                    FeatureItem("📊 Prayer tracking and statistics with streaks")
                    FeatureItem("📿 Digital Tasbih counter with vibration")
                    FeatureItem("🤲 Offline duas collection with favorites & search")
                    FeatureItem("🕌 Nearby mosque finder via Google Maps")
                    FeatureItem("📖 Complete offline Quran — 114 Surahs")
                    FeatureItem("🔊 Quran audio recitation — 2 authentic reciters")
                    FeatureItem("🎧 Online streaming + offline audio caching")
                    FeatureItem("🌟 99 Names of Allah (Asma ul Husna)")
                    FeatureItem("🗓️ Hijri calendar with Islamic events")
                    FeatureItem("📚 Step-by-step prayer and wudu guide")
                    FeatureItem("🌙 Islamic events countdown")
                    FeatureItem("🔔 Smart azan notifications with real audio")
                    FeatureItem("⏰ Pre-prayer early reminders")
                    FeatureItem("📢 Friday Jummah & Surah Al-Kahf reminders")
                    FeatureItem("📍 Distance to Makkah calculator")
                    FeatureItem("🌅 Daily hadith on home screen")
                    FeatureItem("🌍 English & Urdu language support")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Quran Reciters Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Quran Reciters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Authentic recitations from Masjid Al-Haram",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ReciterRow("Mishary Rashid Alafasy", "مشاري راشد العفاسي")
                    Spacer(modifier = Modifier.height(8.dp))
                    ReciterRow("Maher Al-Muaiqly", "ماهر المعيقلي")
                    Spacer(modifier = Modifier.height(8.dp))
                    ReciterRow("Alafasy + Urdu Translation", "العفاسي + ترجمة أردية")
                }
            }
            


            // Footer
            Text(
                text = "Made with ❤️ for the Muslim Ummah",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "© 2026 Muhammad Hamid Raza",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun InfoRow(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            Icons.Rounded.Info,
            contentDescription = null,
            tint = Teal400,
            modifier = Modifier.size(18.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun FeatureItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        Icon(Icons.Rounded.CheckCircle, contentDescription = "Check", tint = Teal400, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ReciterRow(nameEn: String, nameAr: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Mic, contentDescription = null, tint = Teal400, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = nameEn,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = nameAr,
            style = MaterialTheme.typography.bodyMedium,
            color = Gold500
        )
    }
}

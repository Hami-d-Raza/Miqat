import os

strings = {
    '"Next Prayer"': 'stringResource(R.string.next_prayer)',
    '"remaining"': 'stringResource(R.string.remaining)',
    '"Prayer Times"': 'stringResource(R.string.prayer_times)',
    '"Fajr"': 'stringResource(R.string.fajr)',
    '"Sunrise"': 'stringResource(R.string.sunrise)',
    '"Dhuhr"': 'stringResource(R.string.dhuhr)',
    '"Asr"': 'stringResource(R.string.asr)',
    '"Maghrib"': 'stringResource(R.string.maghrib)',
    '"Isha"': 'stringResource(R.string.isha)',
    '"Find Nearby Mosques"': 'stringResource(R.string.find_nearby_mosques)',
    '"Prayer Statistics"': 'stringResource(R.string.prayer_statistics)',
    '"Monthly Timetable"': 'stringResource(R.string.monthly_timetable)',
    '"Qibla Direction"': 'stringResource(R.string.qibla_direction)',
    '"Point phone toward Qibla"': 'stringResource(R.string.point_toward_qibla)',
    '"Facing Qibla ✓"': 'stringResource(R.string.facing_qibla)',
    '"Qibla Bearing"': 'stringResource(R.string.qibla_bearing)',
    '"from North"': 'stringResource(R.string.from_north)',
    '"Tasbih Counter"': 'stringResource(R.string.tasbih_counter)',
    '"Tap circle to count"': 'stringResource(R.string.tap_to_count)',
    '"Reset"': 'stringResource(R.string.reset)',
    '"Target:"': 'stringResource(R.string.target)',
    '"Custom"': 'stringResource(R.string.custom)',
    '"Today total"': 'stringResource(R.string.today_total)',
    '"Location"': 'stringResource(R.string.location)',
    '"Detect my location"': 'stringResource(R.string.detect_location)',
    '"Search city"': 'stringResource(R.string.search_city)',
    '"Calculation Method"': 'stringResource(R.string.calculation_method)',
    '"Notifications"': 'stringResource(R.string.notifications)',
    '"Azan Sound"': 'stringResource(R.string.azan_sound)',
    '"Ramadan Mode"': 'stringResource(R.string.ramadan_mode)',
    '"Appearance & Language"': 'stringResource(R.string.appearance)',
    '"Theme"': 'stringResource(R.string.theme)',
    '"About"': 'stringResource(R.string.about)',
    '"Dark"': 'stringResource(R.string.dark)',
    '"Light"': 'stringResource(R.string.light)'
}

directory = r"d:\Andriod app\PrayerTimes\app\src\main\java\com\example\prayertimes\ui"

for root, _, files in os.walk(directory):
    for file in files:
        if file.endswith(".kt"):
            path = os.path.join(root, file)
            with open(path, "r", encoding="utf-8") as f:
                content = f.read()
                
            orig = content
            for k, v in strings.items():
                content = content.replace(k, v)
                
            if content != orig:
                if "import androidx.compose.ui.res.stringResource" not in content and "stringResource(" in content:
                    content = content.replace("import androidx.compose.", "import androidx.compose.ui.res.stringResource\nimport androidx.compose.", 1)
                if "import com.example.prayertimes.R" not in content and "R.string" in content:
                    content = content.replace("import androidx.compose.", "import com.example.prayertimes.R\nimport androidx.compose.", 1)
                with open(path, "w", encoding="utf-8") as f:
                    f.write(content)
print("Done")

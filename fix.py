with open('app/src/main/java/com/example/prayertimes/ui/screen/SettingsScreen.kt', 'r', encoding='utf-8', errors='ignore') as f:
    lines = f.readlines()

lines = lines[:687]

content = ''.join(lines) + '''
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerSoundDropdownRow(
    prayerName: String,
    currentSound: String,
    playingPrayer: String?,
    onPlay: (String, String) -> Unit,
    onStop: () -> Unit,
    onUpdateSound: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Makkah", "Madinah", "Short Beep", "Fajr Special", "Silent")

    Column {
        Text(prayerName, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExposedDropdownMenuBox(expanded, { expanded = it }, modifier = Modifier.weight(1f)) {
                OutlinedTextField(currentSound, {}, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable), shape = RoundedCornerShape(12.dp), singleLine = true)
                ExposedDropdownMenu(expanded, { expanded = false }) {
                    options.forEachIndexed { index, option -> 
                        DropdownMenuItem(text = { Text(option) }, onClick = { onUpdateSound(prayerName, option); expanded = false }) 
                    }
                }
            }
            if (currentSound != "Silent") {
                val isPlayingThis = playingPrayer == prayerName
                IconButton(
                    onClick = {
                        if (isPlayingThis) onStop() else onPlay(prayerName, currentSound)
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        if (isPlayingThis) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                        contentDescription = "Preview Azan",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
'''
with open('app/src/main/java/com/example/prayertimes/ui/screen/SettingsScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)

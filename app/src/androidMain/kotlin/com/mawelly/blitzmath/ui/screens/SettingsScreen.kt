package com.mawelly.blitzmath.ui.screens

import android.content.Intent
import android.net.Uri

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mawelly.blitzmath.LanguageManager
import com.mawelly.blitzmath.data.GameDataStore
import com.mawelly.blitzmath.data.AppTheme
import com.mawelly.blitzmath.localization.Strings
import com.mawelly.blitzmath.localization.AppLanguage
import com.mawelly.blitzmath.ui.theme.LocalBlitzMathColors
import com.mawelly.blitzmath.ui.dialogs.SupportDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackToMenu: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = remember { GameDataStore(context) }
    val languageManager = remember { LanguageManager(context) }

    // Ses seviyeleri state
    var musicVolume by remember { mutableStateOf(0.5f) }
    var sfxVolume by remember { mutableStateOf(0.8f) }

    // Mevcut dil ve tema
    var currentLang by remember { mutableStateOf(Strings.currentLanguage) }
    val currentTheme by dataStore.theme.collectAsState(initial = AppTheme.DEEP_SPACE)
    var showSupportDialog by remember { mutableStateOf(false) }

    // DataStore'dan değerleri yükle
    LaunchedEffect(Unit) {
        dataStore.musicVolume.collect { musicVolume = it }
        dataStore.sfxVolume.collect { sfxVolume = it }
    }

    // Dil değiştirme fonksiyonu - restartApp YOK, sadece state güncelleme
    fun changeLanguage(newLang: AppLanguage) {
        if (currentLang != newLang) {
            languageManager.saveLanguage(newLang) // Strings.setLanguage çağrılır
            scope.launch {
                dataStore.saveLanguage(newLang)
            }
            currentLang = newLang
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeightVal = maxHeight.value
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding((maxHeight.value * 0.02f).dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Üst Bar - Geri Butonu ve Başlık
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToMenu,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = Strings.settings,
                    fontSize = (screenWidth.value * 0.07f).coerceIn(24f, 32f).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Müzik Sesi
            SettingsItem(
                icon = Icons.Default.MusicNote,
                title = Strings.music,
                volume = musicVolume,
                onVolumeChange = { newVolume ->
                    musicVolume = newVolume
                    scope.launch {
                        dataStore.saveMusicVolume(newVolume)
                    }
                },
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Ses Efektleri
            SettingsItem(
                icon = Icons.Default.VolumeUp,
                title = Strings.sound,
                volume = sfxVolume,
                onVolumeChange = { newVolume ->
                    sfxVolume = newVolume
                    scope.launch {
                        dataStore.saveSfxVolume(newVolume)
                    }
                },
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Titreşim Gücü
            val vibrationStrengthState by dataStore.vibrationStrength.collectAsState(initial = 1.0f)
            val hapticManager = remember { com.mawelly.blitzmath.utils.HapticManager(context) }
            var sliderValue by remember(vibrationStrengthState) { mutableFloatStateOf(vibrationStrengthState) }
            var lastVibrationTime by remember { mutableLongStateOf(0L) }
            
            SettingsItem(
                icon = Icons.Default.Vibration,
                title = Strings.vibrationStrength,
                volume = sliderValue,
                onVolumeChange = { newStrength ->
                    sliderValue = newStrength
                    val currentTime = System.currentTimeMillis()
                    
                    // Sürükleme sırasında her 150ms'de bir titreşim (önizleme için her zaman aktif)
                    if (currentTime - lastVibrationTime > 150L) {
                        hapticManager.vibrateTick(true, newStrength)
                        lastVibrationTime = currentTime
                    }
                },
                onValueChangeFinished = {
                    scope.launch {
                        dataStore.saveVibrationStrength(sliderValue)
                    }
                },
                color = MaterialTheme.colorScheme.tertiary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Auto Tema
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = Strings.autoTheme,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = Strings.autoThemeDesc,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }

                val isAutoThemeEnabled by dataStore.autoTheme.collectAsState(initial = false)
                Switch(
                    checked = isAutoThemeEnabled,
                    onCheckedChange = { scope.launch { dataStore.saveAutoTheme(it) } },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.secondary,
                        uncheckedThumbColor = Color.White.copy(alpha = 0.5f),
                        uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                    )
                )
            }

            // Sesli Geri Bildirim
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = Strings.voiceFeedback,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = Strings.voiceFeedbackDesc,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }

                val isVoiceEnabled by dataStore.voiceEnabled.collectAsState(initial = true)
                Switch(
                    checked = isVoiceEnabled,
                    onCheckedChange = { scope.launch { dataStore.saveVoiceEnabled(it) } },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = Color.White.copy(alpha = 0.5f),
                        uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tema Seçimi Başlığı
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Strings.theme,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = Strings.swipeHint,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val optionWidth = (screenWidth * 0.22f).coerceIn(80.dp, 100.dp)
                AppTheme.entries.forEach { theme ->
                    ThemeOption(
                        theme = theme,
                        isSelected = currentTheme == theme,
                        width = optionWidth,
                        onClick = {
                            scope.launch {
                                dataStore.saveTheme(theme)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = Strings.languageLabel,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val flagSize = (screenWidth * 0.15f).coerceIn(55.dp, 75.dp)
                
                LanguageFlag(
                    flag = "\uD83C\uDDF9\uD83C\uDDF7",
                    languageName = Strings.turkish,
                    isSelected = currentLang == AppLanguage.TURKISH,
                    size = flagSize,
                    onClick = { changeLanguage(AppLanguage.TURKISH) }
                )

                LanguageFlag(
                    flag = "\uD83C\uDDEC\uD83C\uDDE7",
                    languageName = Strings.english,
                    isSelected = currentLang == AppLanguage.ENGLISH,
                    size = flagSize,
                    onClick = { changeLanguage(AppLanguage.ENGLISH) }
                )

                LanguageFlag(
                    flag = "\uD83C\uDDEA\uD83C\uDDF8",
                    languageName = Strings.spanish,
                    isSelected = currentLang == AppLanguage.SPANISH,
                    size = flagSize,
                    onClick = { changeLanguage(AppLanguage.SPANISH) }
                )

                LanguageFlag(
                    flag = "\uD83C\uDDE9\uD83C\uDDEA",
                    languageName = Strings.german,
                    isSelected = currentLang == AppLanguage.GERMAN,
                    size = flagSize,
                    onClick = { changeLanguage(AppLanguage.GERMAN) }
                )

                LanguageFlag(
                    flag = "\uD83C\uDDEB\uD83C\uDDF7",
                    languageName = Strings.french,
                    isSelected = currentLang == AppLanguage.FRENCH,
                    size = flagSize,
                    onClick = { changeLanguage(AppLanguage.FRENCH) }
                )

                LanguageFlag(
                    flag = "\uD83C\uDDEE\uD83C\uDDF9",
                    languageName = Strings.italian,
                    isSelected = currentLang == AppLanguage.ITALIAN,
                    size = flagSize,
                    onClick = { changeLanguage(AppLanguage.ITALIAN) }
                )

                LanguageFlag(
                    flag = "\uD83C\uDDF5\uD83C\uDDF9",
                    languageName = Strings.portuguese,
                    isSelected = currentLang == AppLanguage.PORTUGUESE,
                    size = flagSize,
                    onClick = { changeLanguage(AppLanguage.PORTUGUESE) }
                )

                LanguageFlag(
                    flag = "\uD83C\uDDEE\uD83C\uDDF3",
                    languageName = Strings.hindi,
                    isSelected = currentLang == AppLanguage.HINDI,
                    size = flagSize,
                    onClick = { changeLanguage(AppLanguage.HINDI) }
                )

                LanguageFlag(
                    flag = "\uD83C\uDDE8\uD83C\uDDF3",
                    languageName = Strings.chinese,
                    isSelected = currentLang == AppLanguage.CHINESE,
                    size = flagSize,
                    onClick = { changeLanguage(AppLanguage.CHINESE) }
                )

                LanguageFlag(
                    flag = "🇷🇺",
                    languageName = Strings.russian,
                    isSelected = currentLang == AppLanguage.RUSSIAN,
                    size = flagSize,
                    onClick = { changeLanguage(AppLanguage.RUSSIAN) }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Gizlilik Politikası Butonu
            Text(
                text = Strings.privacyPolicy,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9C27B0), // Purple accent
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://mawellystudio.com/privacy"))
                        context.startActivity(intent)
                    }
                    .padding(vertical = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Text(
                text = "Mawelly Studio",
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                color = Color.White.copy(alpha = 0.4f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(80.dp))
        }

        // Destek Butonu (Sağ Alt)
        FloatingActionButton(
            onClick = { showSupportDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 8.dp),
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
            contentColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SupportAgent,
                contentDescription = "Support",
                modifier = Modifier.size(28.dp)
            )
        }

        // Destek Penceresi
        if (showSupportDialog) {
            SupportDialog(onDismiss = { showSupportDialog = false })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit = {},
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "${(volume * 100).toInt()}%",
                    fontSize = 14.sp,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Slider(
                value = volume,
                onValueChange = onVolumeChange,
                onValueChangeFinished = onValueChangeFinished,
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = color,
                    activeTrackColor = color,
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                )
            )
        }
    }
}

@Composable
private fun ThemeOption(
    theme: AppTheme,
    isSelected: Boolean,
    width: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    val (primaryColor, themeName) = when (theme) {
        AppTheme.DEEP_SPACE -> Color(0xFF00d9ff) to "Space"
        AppTheme.CYBERPUNK -> Color(0xFFFF00FF) to "Neon"
        AppTheme.FOREST -> Color(0xFF4CAF50) to "Forest"
        AppTheme.AQUA -> Color(0xFF7FDBFF) to "Aqua"
        AppTheme.MIDNIGHT -> Color(0xFFBB86FC) to "Midnight"
        AppTheme.SUNSET -> Color(0xFFFF9E80) to "Sunset"
        AppTheme.LAVENDER -> Color(0xFFE1BEE7) to "Lavender"
        AppTheme.FIRE -> Color(0xFFFF5252) to "Fire"
        AppTheme.GLACIER -> Color(0xFFB2EBF2) to "Glacier"
        AppTheme.GOLDEN -> Color(0xFFFFD700) to "Golden"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(width)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(height = width * 0.7f, width = width * 0.7f)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isSelected) primaryColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)
                )
                .border(
                    width = 2.dp,
                    color = if (isSelected) primaryColor else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(30.dp, 10.dp).background(primaryColor, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.size(30.dp, 10.dp).background(primaryColor.copy(alpha = 0.5f), RoundedCornerShape(2.dp)))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = themeName,
            fontSize = 12.sp,
            color = if (isSelected) primaryColor else Color.White.copy(alpha = 0.6f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun LanguageFlag(
    flag: String,
    languageName: String,
    isSelected: Boolean,
    size: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)
                )
                .border(
                    width = 2.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = flag,
                fontSize = (size.value * 0.5f).sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = languageName,
            fontSize = 14.sp,
            color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.White.copy(alpha = 0.6f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
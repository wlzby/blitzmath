package com.mawelly.blitzmath.ui.screens

import com.mawelly.blitzmath.core.LocalPlatformServices

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mawelly.blitzmath.localization.AppLanguage
import com.mawelly.blitzmath.localization.Strings
import blitzmath.app.generated.resources.Res
import blitzmath.app.generated.resources.blitzmath_logo
import org.jetbrains.compose.resources.painterResource
import kotlinx.coroutines.launch
import java.util.UUID

enum class SetupStep {
    LANGUAGE_SELECTION,
    NAME_ENTRY
}

@Composable
fun LanguageSelectionScreen(
    dataStore: com.mawelly.blitzmath.data.IGameDataStore,
    onLanguageSelected: () -> Unit
) {
    val platformServices = LocalPlatformServices.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var currentStep by remember { mutableStateOf(SetupStep.LANGUAGE_SELECTION) }
    var selectedLanguage by remember { mutableStateOf<AppLanguage?>(null) }
    var playerName by remember { mutableStateOf("") }
    var showNameError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        val screenHeight = maxHeight
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo - Sadece yuvarlak logo, çerçeve yok
            val logoSize = (screenHeight.value * 0.18f).coerceIn(100f, 150f).dp
            Image(
                painter = painterResource(Res.drawable.blitzmath_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(logoSize),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Blitz Math",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = Strings.challenge,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(60.dp))

            // STEP TRANSITIONS
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                },
                label = "setup_steps"
            ) { step ->
                when (step) {
                    SetupStep.LANGUAGE_SELECTION -> {
                        LanguageStep(
                            onSelect = { lang ->
                                selectedLanguage = lang
                                com.mawelly.blitzmath.localization.Strings.setLanguage(lang)
                                scope.launch { dataStore.saveLanguage(lang) }
                                currentStep = SetupStep.NAME_ENTRY
                            }
                        )
                    }
                    SetupStep.NAME_ENTRY -> {
                        NameEntryStep(
                            playerName = playerName,
                            onNameChange = { playerName = it },
                            onContinue = {
                                val trimmedName = playerName.trim()
                                when {
                                    trimmedName.isEmpty() -> {
                                        showNameError = true
                                        errorMessage = Strings.getNameRequired(selectedLanguage)
                                    }
                                    !Strings.isValidUsername(trimmedName) -> {
                                        showNameError = true
                                        errorMessage = if (Strings.isUsernameBanned(trimmedName)) {
                                            Strings.getBannedWordError()
                                        } else {
                                            Strings.getInvalidNameError()
                                        }
                                    }
                                    else -> {
                                        completeSetup(dataStore, scope, "regular", trimmedName, onLanguageSelected)
                                    }
                                }
                            },
                            onBack = { currentStep = SetupStep.LANGUAGE_SELECTION },
                            showError = showNameError,
                            errorText = errorMessage
                        )
                    }
                }
            }
        }
    }
}

private fun completeSetup(
    dataStore: com.mawelly.blitzmath.data.IGameDataStore,
    scope: kotlinx.coroutines.CoroutineScope,
    type: String,
    name: String,
    onComplete: () -> Unit
) {
    scope.launch { dataStore.saveLoginType(type) }
    scope.launch { dataStore.savePlayerName(name) }
    scope.launch { dataStore.setFirstLaunchCompleted() }
    onComplete()
}

@Composable
private fun LanguageStep(onSelect: (AppLanguage) -> Unit) {
    var activeLangIndex by remember { mutableIntStateOf(0) }
    val languages = AppLanguage.entries

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1200)
            activeLangIndex = (activeLangIndex + 1) % languages.size
        }
    }

    val animatedText = Strings.getSelectLanguage(languages[activeLangIndex])

    val deviceLanguage = java.util.Locale.getDefault().language
    val deviceCountry = java.util.Locale.getDefault().country

    val languageOptions = remember {
        listOf(
            LangDisplayInfo(AppLanguage.TURKISH, "🇹🇷", Strings.turkish, "Turkish", "tr", "TR"),
            LangDisplayInfo(AppLanguage.ENGLISH, "🇬🇧", Strings.english, "English", "en", "GB"),
            LangDisplayInfo(AppLanguage.SPANISH, "🇪🇸", Strings.spanish, "Español", "es", "ES"),
            LangDisplayInfo(AppLanguage.GERMAN, "🇩🇪", Strings.german, "Deutsch", "de", "DE"),
            LangDisplayInfo(AppLanguage.FRENCH, "🇫🇷", Strings.french, "Français", "fr", "FR"),
            LangDisplayInfo(AppLanguage.ITALIAN, "🇮🇹", Strings.italian, "Italiano", "it", "IT"),
            LangDisplayInfo(AppLanguage.PORTUGUESE, "🇵🇹", Strings.portuguese, "Português", "pt", "BR"),
            LangDisplayInfo(AppLanguage.HINDI, "🇮🇳", Strings.hindi, "हिन्दी", "hi", "IN"),
            LangDisplayInfo(AppLanguage.CHINESE, "🇨🇳", Strings.chinese, "简体中文", "zh", "CN"),
            LangDisplayInfo(AppLanguage.RUSSIAN, "🇷🇺", Strings.russian, "Русский", "ru", "RU")
        ).sortedByDescending { it.languageCode == deviceLanguage || it.countryCode == deviceCountry }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        androidx.compose.animation.AnimatedContent(
            targetState = animatedText,
            transitionSpec = {
                (androidx.compose.animation.slideInVertically(
                    animationSpec = androidx.compose.animation.core.tween(700, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                ) { height -> height } + androidx.compose.animation.fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(700)
                )).togetherWith(
                    androidx.compose.animation.slideOutVertically(
                        animationSpec = androidx.compose.animation.core.tween(700, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                    ) { height -> -height } + androidx.compose.animation.fadeOut(
                        animationSpec = androidx.compose.animation.core.tween(700)
                    )
                )
            },
            label = "TitleAnimation"
        ) { text ->
            Text(
                text = text,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(32.dp))

        languageOptions.forEach { option ->
            LanguageOption(
                emoji = option.emoji,
                title = option.title,
                subtitle = option.subtitle,
                onClick = { onSelect(option.lang) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

data class LangDisplayInfo(
    val lang: AppLanguage,
    val emoji: String,
    val title: String,
    val subtitle: String,
    val languageCode: String,
    val countryCode: String
)

@Composable
private fun NameEntryStep(
    playerName: String,
    onNameChange: (String) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    showError: Boolean,
    errorText: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = Strings.enterName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = playerName,
            onValueChange = onNameChange,
            placeholder = { Text(Strings.enterName, color = Color.White.copy(alpha = 0.3f)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = showError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFE94560),
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                unfocusedContainerColor = Color.Transparent
            )
        )
        if (showError) {
            Text(text = errorText, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE94560))
        ) {
            Text(text = Strings.continue_, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun LanguageOption(
    emoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 32.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = subtitle, fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "→", color = MaterialTheme.colorScheme.secondary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

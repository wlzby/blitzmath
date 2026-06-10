package com.mawelly.blitzmath.ui

import android.app.Activity
import android.util.Log
import com.mawelly.blitzmath.R
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mawelly.blitzmath.ui.theme.LocalBlitzMathColors
import com.mawelly.blitzmath.localization.AppLanguage
import com.mawelly.blitzmath.LanguageManager
import com.mawelly.blitzmath.ui.utils.ShareManager
import com.mawelly.blitzmath.analytics.AnalyticsManager
import com.mawelly.blitzmath.ads.IAdManager
import com.mawelly.blitzmath.audio.SoundManager
import com.mawelly.blitzmath.utils.HapticManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.ui.graphics.vector.ImageVector
import com.mawelly.blitzmath.game.CheckpointManager
import com.mawelly.blitzmath.game.GameMode
import com.mawelly.blitzmath.game.GameState
import com.mawelly.blitzmath.game.MathGenerator
import com.mawelly.blitzmath.game.OperationType
import com.mawelly.blitzmath.game.ScientistCard
import com.mawelly.blitzmath.game.ScientistCards
import com.mawelly.blitzmath.leaderboard.LeaderboardManager
import com.mawelly.blitzmath.localization.Strings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import com.mawelly.blitzmath.game.MathQuote
import com.mawelly.blitzmath.game.mathQuotes
import com.mawelly.blitzmath.ui.dialogs.ScientistCardUnlockDialog
import com.mawelly.blitzmath.ui.components.SuccessConfetti
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.blur

object QuoteManager {
    private var currentIndex = 0

    fun getNextQuote(): MathQuote {
        val quote = mathQuotes[currentIndex]
        currentIndex = (currentIndex + 1) % mathQuotes.size
        return quote
    }

    fun reset() {
        currentIndex = 0
    }
}

@Composable
fun GameScreen(
    mode: GameMode,
    startLevel: Int,
    soundManager: SoundManager,
    voiceManager: com.mawelly.blitzmath.audio.VoiceManager? = null,
    dataStore: com.mawelly.blitzmath.data.GameDataStore, // Added
    adMobManager: IAdManager? = null,
    onLevelComplete: (Int) -> Unit,
    onBackToMenu: () -> Unit,
    onShowRanking: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = remember(context) {
        var c = context
        while (c is android.content.ContextWrapper) {
            if (c is android.app.Activity) return@remember c
            c = c.baseContext
        }
        null
    }

    val currentLang by com.mawelly.blitzmath.localization.Strings.currentLanguageFlow.collectAsState(initial = com.mawelly.blitzmath.localization.Strings.currentLanguage)

    val languageManager = remember { LanguageManager(context) }
    val leaderboardManager = remember { LeaderboardManager() }
    val analyticsManager = remember { AnalyticsManager.getInstance(context) }
    val hapticManager = remember { HapticManager(context) }
    val unlockedCards by dataStore.unlockedCards.collectAsState(initial = null)
    val equippedCards by dataStore.equippedCards.collectAsState(initial = null)
    val isVoiceEnabled by dataStore.voiceEnabled.collectAsState(initial = true)
    val isVibrationEnabled by dataStore.vibrationEnabled.collectAsState(initial = true)
    val vibrationStrength by dataStore.vibrationStrength.collectAsState(initial = 1.0f)
    val savedCharges by dataStore.cardCharges.collectAsState(initial = null)
    val savedLastUseTimes by dataStore.cardLastUseTime.collectAsState(initial = null)
    val savedLives by dataStore.livesCount.collectAsState(initial = null)
    val savedLastLifeLossTime by dataStore.lastLifeLossTime.collectAsState(initial = null)
    val isReviewed by dataStore.isReviewed.collectAsState(initial = false)
    val scope = rememberCoroutineScope()

    // --- Pause State ---
    var isAppPaused by remember { mutableStateOf(false) }

    var newlyUnlockedCard by remember { mutableStateOf<com.mawelly.blitzmath.game.ScientistCard?>(null) }
    var showReviewInvitation by remember { mutableStateOf(false) }
    var showWelcomeGift by remember { mutableStateOf(false) }
    var welcomeCard by remember { mutableStateOf<ScientistCard?>(null) }

    // Wait for all essential data to load before creating GameState or showing UI
    val uCards = unlockedCards
    val eCards = equippedCards
    val sCharges = savedCharges
    val sTimes = savedLastUseTimes
    val sLives = savedLives
    val sLossTime = savedLastLifeLossTime

    if (uCards == null || eCards == null || sCharges == null || sTimes == null || sLives == null || sLossTime == null || activity == null) {
        Box(modifier = Modifier.fillMaxSize())
        return
    }

    val currentActivity = activity // Safe reference

    val gameState = remember(mode, startLevel) {
        GameState(
            soundManager = soundManager,
            voiceManager = voiceManager,
            isVoiceEnabled = isVoiceEnabled,
            mode = mode,
            startCheckpoint = startLevel,
            languageManager = languageManager,
            leaderboardManager = leaderboardManager,
            unlockedCards = uCards,
            equippedCards = eCards,
            hapticManager = hapticManager,
            vibrationEnabled = isVibrationEnabled,
            vibrationStrength = vibrationStrength,
            onCardUnlocked = { newlyUnlockedCard = it },
            startingLives = sLives,
            lastLossTime = sLossTime,
            scope = scope,
            dataStore = dataStore
        )
    }

    // Lifecycle Observer to catch Background/Foreground transitions
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, gameState) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    isAppPaused = true
                }
                Lifecycle.Event.ON_RESUME -> {
                    isAppPaused = false
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Sync charges and start times from DataStore only once or when saved data changes
    LaunchedEffect(sCharges, sTimes) {
        gameState.syncChargesWithTime(sCharges, sTimes)
    }

    // Persist charges and recharge times back to DataStore whenever they change
    // IMPORTANT: Only save if we have initialized from real DataStore values AND sync is complete
    LaunchedEffect(gameState.cardCharges.toMap(), gameState.cardLastUseTimes.toMap()) {
        if (gameState.isSynced) {
            dataStore.saveCardCharges(gameState.cardCharges.toMap())
            dataStore.saveCardLastUseTime(gameState.cardLastUseTimes.toMap())
        }
    }

    // Periodic sync (every 1s) to update UI countdown timers
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            gameState.tickRefill() // Auto-refill logic
            if (savedCharges != null && savedLastUseTimes != null) {
                gameState.syncChargesWithTime(
                    gameState.cardCharges.toMap(),
                    gameState.cardLastUseTimes.toMap()
                )
            }
        }
    }

    // Schedule notifications when a charge is used and it starts/continues recharging
    LaunchedEffect(gameState.cardLastUseTimes.toMap()) {
        try {
            val workManager = androidx.work.WorkManager.getInstance(context.applicationContext)
            // Iterating over a copy to prevent ConcurrentModificationException
            gameState.cardLastUseTimes.toMap().forEach { (cardId, lastUseTime) ->
            if (lastUseTime > 0L) {
                val card = ScientistCards.getCardById(cardId) ?: return@forEach
                val currentChargeCount = gameState.cardCharges[cardId] ?: 0
                val chargesToFull = card.maxCharges - currentChargeCount
                
                // We notify when it reaches MAX charges
                val delayMinutes = (chargesToFull * card.rechargeDurationMinutes).toLong()
                if (delayMinutes > 0) {
                    val workRequest = androidx.work.OneTimeWorkRequestBuilder<com.mawelly.blitzmath.notifications.RechargeWorker>()
                        .setInitialDelay(delayMinutes, java.util.concurrent.TimeUnit.MINUTES)
                        .setInputData(androidx.work.workDataOf("card_id" to cardId))
                        .addTag("recharge_$cardId")
                        .build()

                    workManager.enqueueUniqueWork(
                        "recharge_$cardId",
                        androidx.work.ExistingWorkPolicy.REPLACE,
                        workRequest
                    )
                }
            } else {
                workManager.cancelUniqueWork("recharge_$cardId")
            }
            }
        } catch (e: Exception) {
            Log.e("GameScreen", "Error scheduling notifications: ${e.message}")
        }
    }

    // Oyun döngüsü
    LaunchedEffect(gameState, gameState.isGameOver, gameState.isCheckpointComplete, isAppPaused) {
        if (!gameState.isGameOver && !gameState.isCheckpointComplete && !isAppPaused) {
            var lastTimeMs = System.currentTimeMillis()
            while (!gameState.isGameOver && !gameState.isCheckpointComplete && !isAppPaused) {
                kotlinx.coroutines.delay(16)
                val currentTimeMs = System.currentTimeMillis()
                val deltaTime = (currentTimeMs - lastTimeMs) / 1000f
                lastTimeMs = currentTimeMs
                gameState.updateTimer(deltaTime)
            }
        }
    }

    // Checkpoint tamamlandığında callback ve log
    LaunchedEffect(gameState.isCheckpointComplete) {
        if (gameState.isCheckpointComplete) {
            analyticsManager.logGameEnd(mode.name, gameState.score.toLong(), true)
            onLevelComplete(gameState.currentCheckpoint + 1)
        }
    }

    var showInitialLeaderboard by remember { mutableStateOf(false) }
    var hasShownLeaderboardOnce by remember { mutableStateOf(false) }

    // Game Over log and Stars
    LaunchedEffect(gameState.isGameOver) {
        if (gameState.isGameOver) {
            analyticsManager.logGameEnd(mode.name, gameState.score.toLong(), false)
            val earnedStars = (gameState.score / 10) + 5
            if (earnedStars > 0) {
                dataStore.addStars(earnedStars)
            }
            
            // Sıralamayı göster (Henüz gösterilmediyse)
            if (!hasShownLeaderboardOnce) {
                showInitialLeaderboard = true
            }

            // In-App Review & Unlock Reward Logic
            dataStore.incrementGamesPlayed()
            val totalPlayed = dataStore.gamesPlayed.first()
            
            if (totalPlayed == 1) {
                // İLK OYUN HEDİYESİ: PİSAGOR
                val pythagoras = ScientistCards.getCardById("pythagoras")
                if (pythagoras != null) {
                    dataStore.unlockCard(pythagoras.id)
                    welcomeCard = pythagoras
                    delay(800)
                    showWelcomeGift = true
                }
            } else if (totalPlayed > 0 && totalPlayed % 5 == 0 && !isReviewed) {
                // Bi saniye bekleyip diyalogu çıkarıyoruz ki Game Over ekranı otursun
                delay(1200)
                showReviewInvitation = true
            }
        } else {
            // Oyun bittiğinde tekrar gösterilebilmesi için sıfırla
            hasShownLeaderboardOnce = false
        }
    }

    val blitzColors = LocalBlitzMathColors.current
    val gradientBrush = Brush.verticalGradient(
        colors = blitzColors.backgroundGradient
    )

    // Ekran boyutunu tespit et (Daha global kullanım için)
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val isUltraSmall = screenHeight < 640.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .padding(if (isUltraSmall) 8.dp else 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        soundManager.stopBGM()
                        onBackToMenu()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    // Canları buraya taşıdık
                    val pLevel = languageManager.getPlayerLevel()
                    val pProgress = languageManager.getPlayerLevelProgress()
                    HeartsHUD(
                        livesRemaining = gameState.livesRemaining, 
                        refillTime = gameState.getFormattedRefillTime(),
                        isCompact = true,
                        playerLevel = pLevel,
                        playerProgress = pProgress
                    )
                }

                Text(
                    text = "BLITZ MATH",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }

            // --- Pause Overlay (Anti-Cheat) ---
            if (isAppPaused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.85f)) // Dark obscure
                        .clickable(enabled = false) {}, // Prevent clicks
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = Strings.paused,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            } else {
                // Normal UI content
                when {
                    gameState.isSaveMePending -> SaveMeScreen(
                        gameState = gameState,
                        adMobManager = adMobManager,
                        activity = currentActivity
                    )
                    gameState.isGameOver -> {
                        if (showInitialLeaderboard) {
                            com.mawelly.blitzmath.ui.screens.LeaderboardPopup(
                                mode = gameState.mode.name.lowercase(),
                                onDismiss = { 
                                    showInitialLeaderboard = false
                                    hasShownLeaderboardOnce = true
                                }
                            )
                        } else {
                            GameOverScreen(
                                gameState = gameState,
                                adMobManager = adMobManager,
                                activity = currentActivity,
                                onBackToMenu = onBackToMenu,
                                onShowRanking = onShowRanking,
                                currentLang = currentLang
                            )
                        }
                    }
                    gameState.isCheckpointComplete -> CheckpointCompleteScreen(
                        gameState = gameState,
                        onNextCheckpoint = { gameState.nextCheckpoint() }
                    )
                    else -> GamePlayScreen(gameState = gameState, adMobManager = adMobManager, activity = currentActivity)
                }
            }
        }

        if (showReviewInvitation) {
            AlertDialog(
                onDismissRequest = { showReviewInvitation = false },
                containerColor = Color(0xFF1a1a2e),
                shape = RoundedCornerShape(24.dp),
                title = {
                    Text(
                        text = Strings.reviewInvitationTitle,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Text(
                        text = Strings.reviewInvitationMessage,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showReviewInvitation = false
                            com.mawelly.blitzmath.utils.AppReviewManager.showReviewDialog(activity!!, dataStore) {
                                Log.d("GameScreen", "Review flow completed.")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = Strings.rateNow, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showReviewInvitation = false }) {
                        Text(text = Strings.noThanks, color = Color.White.copy(alpha = 0.5f))
                    }
                }
            )
        }

        // HOŞ GELDİN HEDİYESİ DİYALOGU
        if (showWelcomeGift && welcomeCard != null) {
            AlertDialog(
                onDismissRequest = { showWelcomeGift = false },
                containerColor = Color(0xFF1a1a2e),
                shape = RoundedCornerShape(24.dp),
                title = {
                    Text(
                        text = Strings.welcomeGiftTitle,
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        welcomeCard?.imageResId?.let { resId ->
                            Image(
                                painter = painterResource(id = resId),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(16.dp))
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        Text(
                            text = Strings.welcomeGiftMessage,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showWelcomeGift = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = Strings.ok, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

enum class AnswerFeedback { NONE, CORRECT, WRONG }

@Composable
private fun GamePlayScreen(gameState: GameState, adMobManager: IAdManager? = null, activity: android.app.Activity? = null) {
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var feedbackType by remember { mutableStateOf(AnswerFeedback.NONE) }
    val scope = rememberCoroutineScope()

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val isLandscape = screenWidth > screenHeight && screenHeight < 600.dp
        val isSmallScreen = screenHeight < 740.dp
        val isUltraSmallScreen = screenHeight < 640.dp
        
        // Dinamik boyutlandırma hesaplamaları (Daha Esnek ve Responsive)
        // Slot tabanlı responsive hesaplamalar (Daha Dolgun Görünüm)
        val circleSize = (screenHeight * 0.09f).coerceIn(52.dp, 85.dp)
        val mainCircleSize = (screenHeight * 0.11f).coerceIn(65.dp, 100.dp)
        
        val cardHeight = (screenHeight * 0.20f).coerceIn(110.dp, 180.dp)
        val buttonHeight = (screenHeight * 0.10f).coerceIn(55.dp, 95.dp)
        val buttonWidth = (screenWidth * 0.42f).coerceIn(110.dp, 180.dp)
        val spacing = (screenHeight * 0.015f).coerceIn(8.dp, 16.dp)

        if (isLandscape) {
            // --- LANDSCAPE LAYOUT ---
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: HUD & Question
                Column(
                    modifier = Modifier.weight(1.1f).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    ModernHUD(gameState = gameState, isSmall = true)
                    
                    TimerBar(
                        timeLeft = gameState.timeLeft,
                        maxTime = if (gameState.mode == com.mawelly.blitzmath.game.GameMode.CHALLENGE) 30f else 5f
                    )
                    
                    QuestionCard(
                        question = gameState.currentQuestion.displayText,
                        checkpoint = if (gameState.mode == com.mawelly.blitzmath.game.GameMode.CHALLENGE) -1 else gameState.currentCheckpoint,
                        height = cardHeight,
                        isSmall = true
                    )
                }

                // Right Column: Options & Jokers
                Column(
                    modifier = Modifier.weight(0.9f).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    OptionsGrid(
                        options = gameState.currentQuestion.options,
                        onOptionSelected = { index, option ->
                            if (selectedOptionIndex == null) {
                                selectedOptionIndex = index
                                feedbackType = if (option == gameState.currentQuestion.correctAnswer) 
                                    AnswerFeedback.CORRECT else AnswerFeedback.WRONG
                                
                                scope.launch {
                                    val isCorrect = feedbackType == AnswerFeedback.CORRECT
                                    val transitionDelay = if (gameState.mode == com.mawelly.blitzmath.game.GameMode.CHALLENGE) 0L else if (isCorrect) 150L else 450L
                                    delay(transitionDelay)
                                    gameState.checkAnswer(option)
                                    selectedOptionIndex = null
                                    feedbackType = AnswerFeedback.NONE
                                }
                            }
                        },
                        buttonWidth = buttonWidth,
                        buttonHeight = buttonHeight,
                        spacing = spacing,
                        zapEliminatedIndex = gameState.zapEliminatedOption,
                        feedbackOptionIndex = selectedOptionIndex,
                        feedbackType = feedbackType
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    JokerSkillBar(
                        gameState = gameState,
                        adMobManager = adMobManager,
                        activity = activity,
                        isSmall = true,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                    )
                }
            }
        } else {
            // --- PORTRAIT LAYOUT (Consolidated Game Area) ---
            Column(
                modifier = Modifier.fillMaxSize().padding(top = 8.dp), // Notches/Status bar protection
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 1. HUD SECTION (18% of screen height)
                Box(
                    modifier = Modifier.weight(0.18f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    ModernHUD(
                        gameState = gameState, 
                        isSmall = isSmallScreen,
                        isUltraSmall = isUltraSmallScreen,
                        circleSize = circleSize,
                        mainCircleSize = mainCircleSize
                    )
                }

                // 2. MAIN GAME AREA (65% of screen height)
                // Bu alan tüm oyun öğelerini (soru, süre, butonlar) birbirine yakın tutar
                Column(
                    modifier = Modifier.weight(0.65f).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Soru İlerleme & Mesaj
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        if (gameState.mode != com.mawelly.blitzmath.game.GameMode.CHALLENGE) {
                            QuestionProgressBar(
                                current = gameState.currentQuestionIndex,
                                total = gameState.questionsPerCheckpoint
                            )
                            Spacer(modifier = Modifier.height(spacing / 2))
                        }

                        val speedMsg = gameState.speedMessage
                        Box(
                            modifier = Modifier.height(if (isUltraSmallScreen) 22.dp else 28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (speedMsg.isNotEmpty()) {
                                LaunchedEffect(speedMsg) { delay(1500); gameState.clearSpeedMessage() }
                                Text(
                                    text = speedMsg,
                                    fontSize = if (isUltraSmallScreen) 16.sp else 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        speedMsg.startsWith("❌") -> Color(0xFFe94560)
                                        speedMsg.startsWith("⏰") -> Color(0xFFf9a825)
                                        else -> Color(0xFF00d9ff)
                                    },
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing))

                    // Süre Çubuğu
                    TimerBar(
                        timeLeft = gameState.timeLeft,
                        maxTime = if (gameState.mode == com.mawelly.blitzmath.game.GameMode.CHALLENGE) 30f else 5f
                    )

                    Spacer(modifier = Modifier.height(spacing))

                    // Soru Kartı
                    QuestionCard(
                        question = gameState.currentQuestion.displayText,
                        checkpoint = if (gameState.mode == com.mawelly.blitzmath.game.GameMode.CHALLENGE) -1 else gameState.currentCheckpoint,
                        height = cardHeight,
                        isSmall = isSmallScreen
                    )

                    Spacer(modifier = Modifier.height(spacing))

                    // Seçenekler (Cevap Butonları)
                    OptionsGrid(
                        options = gameState.currentQuestion.options,
                        onOptionSelected = { index, option ->
                            if (selectedOptionIndex == null) {
                                selectedOptionIndex = index
                                feedbackType = if (option == gameState.currentQuestion.correctAnswer) 
                                    AnswerFeedback.CORRECT else AnswerFeedback.WRONG
                                
                                scope.launch {
                                    val isCorrect = feedbackType == AnswerFeedback.CORRECT
                                    val transitionDelay = if (gameState.mode == com.mawelly.blitzmath.game.GameMode.CHALLENGE) 0L else if (isCorrect) 150L else 450L
                                    delay(transitionDelay)
                                    gameState.checkAnswer(option)
                                    selectedOptionIndex = null
                                    feedbackType = AnswerFeedback.NONE
                                }
                            }
                        },
                        buttonWidth = buttonWidth,
                        buttonHeight = buttonHeight,
                        spacing = spacing,
                        zapEliminatedIndex = gameState.zapEliminatedOption,
                        feedbackOptionIndex = selectedOptionIndex,
                        feedbackType = feedbackType
                    )
                }

                // 3. JOKER SECTION (15% of screen height)
                Box(
                    modifier = Modifier.weight(0.15f).fillMaxWidth().padding(bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    JokerSkillBar(
                        gameState = gameState,
                        adMobManager = adMobManager,
                        activity = activity,
                        isSmall = isSmallScreen,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernHUD(
    gameState: GameState, 
    isSmall: Boolean = false, 
    isUltraSmall: Boolean = false,
    circleSize: Dp = 75.dp,
    mainCircleSize: Dp = 88.dp
) {
    val baseSize = circleSize
    val centerSize = mainCircleSize
    
    val isChallenge = gameState.mode == com.mawelly.blitzmath.game.GameMode.CHALLENGE
    val challengeColor = Color(0xFFe94560)

    Row(
        modifier = Modifier
            .widthIn(max = 600.dp)
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = if (isSmall) 4.dp else 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sol: Checkpoint - 🎯
        CircularStat(
            label = if (isChallenge) Strings.statSpeed else Strings.statCheck,
            value = if (isChallenge) String.format("%.1f", gameState.timeLeft) else gameState.currentCheckpoint.toString(),
            color = if (isChallenge) Color(0xFFf9a825) else Color(0xFF00d9ff),
            icon = Icons.Default.Adjust,
            size = baseSize,
            fontSize = if (isSmall) 16.sp else 20.sp
        )

        // Orta: Score - ⭐
        CircularStat(
            label = Strings.statScore,
            value = gameState.score.toString(),
            color = challengeColor,
            icon = Icons.Default.Star,
            size = centerSize,
            fontSize = if (isSmall) 22.sp else 28.sp,
            labelFontSize = if (isSmall) 8.sp else 10.sp,
            isMain = true
        )

        // Sağ: Streak - 🔥
        CircularStat(
            label = Strings.statStreak,
            value = gameState.streak.toString(),
            color = if (isChallenge) Color(0xFFe94560) else Color(0xFFf9a825),
            icon = Icons.Default.Whatshot,
            size = baseSize,
            fontSize = if (isSmall) 18.sp else 22.sp
        )
    }
}

@Composable
private fun CircularStat(
    label: String,
    value: String,
    color: Color,
    icon: ImageVector,
    size: Dp,
    fontSize: TextUnit,
    labelFontSize: TextUnit = 10.sp,
    isMain: Boolean = false
) {
    val scale by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = if (isMain) 1.06f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .scale(if (isMain) scale else 1f),
            contentAlignment = Alignment.Center
        ) {
            // LAYER 1: Outer Glow (Shadow System)
            Box(
                modifier = Modifier
                    .size(size * 0.9f)
                    .shadow(
                        elevation = if (isMain) 20.dp else 12.dp,
                        shape = CircleShape,
                        spotColor = color.copy(alpha = 0.8f),
                        ambientColor = color.copy(alpha = 0.4f)
                    )
            )

            // LAYER 2: Crystal Glass Lens Base
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                color.copy(alpha = 0.5f),
                                color.copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.4f)
                            )
                        ),
                        shape = CircleShape
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.4f), Color.Transparent, color.copy(alpha = 0.3f))
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // LAYER 3: Glass Top Reflection (Specular Highlight)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.25f),
                                    Color.Transparent,
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // LAYER 4: Content (Icon + Label + Value)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = (size.value * 0.05f).dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                                 tint = color.copy(alpha = 0.9f),
                                 modifier = Modifier.size((size.value * 0.22f).dp)
                             )
                             
                             Spacer(modifier = Modifier.height(2.dp))
                             
                             Text(
                        text = label,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = labelFontSize,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    
                    Text(
                        text = value,
                        color = Color.White,
                        fontSize = fontSize,
                        fontWeight = FontWeight.Black,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                blurRadius = 4f
                            )
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionProgressBar(current: Int, total: Int) {
    val progress = current.toFloat() / total

    Column(
        modifier = Modifier.fillMaxWidth(0.9f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = Strings.question,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
            Text(
                text = "$current / $total",
                color = Color(0xFF00d9ff),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(if (current == 0) 6.dp else 10.dp) // Start with slightly thinner, then expand
                .clip(RoundedCornerShape(5.dp)),
            color = Color(0xFFf9a825),
            trackColor = Color.White.copy(alpha = 0.1f)
        )
    }
}

@Composable
private fun TimerBar(timeLeft: Float, maxTime: Float) {
    val progress = (timeLeft / maxTime).coerceIn(0f, 1f)
    val color = if (maxTime == 30f) {
        // Challenge modunda daha agresif renkler (Turuncu -> Kırmızı)
        when {
            progress > 0.4f -> Color(0xFFf9a825)
            else -> Color(0xFFe94560)
        }
    } else {
        when {
            progress > 0.6f -> Color(0xFF00d9ff)
            progress > 0.3f -> Color(0xFFf9a825)
            else -> Color(0xFFe94560)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(0.95f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = Strings.time,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = String.format("%.1fs", timeLeft),
                color = color,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp)),
            color = color,
            trackColor = Color.White.copy(alpha = 0.1f)
        )
    }
}

@Composable
private fun QuestionCard(question: String, checkpoint: Int, height: Dp = 180.dp, isSmall: Boolean = false) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(animation = tween(1000), repeatMode = RepeatMode.Reverse),
        label = "scale"
    )

    val isChallenge = checkpoint == -1
    
    val cardColor = when {
        isChallenge -> Color(0xFF6A11CB) // Base color for challenge (Purple)
        checkpoint <= 2 -> Color(0xFF4CAF50)
        checkpoint <= 4 -> Color(0xFF2196F3)
        checkpoint <= 6 -> Color(0xFF9C27B0)
        checkpoint <= 8 -> Color(0xFFf44336)
        else -> Color(0xFF00d9ff)
    }

    // Modern Challenge Gradient: Deep Purple to Neon Pink
    val challengeGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF6A11CB), Color(0xFF2575FC), Color(0xFFE94560))
    )

    Card(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .height(height)
            .scale(scale)
            .then(
                if (isChallenge) {
                    Modifier.border(
                        width = 2.dp,
                        brush = Brush.sweepGradient(listOf(Color(0xFF00d9ff), Color(0xFFe94560), Color(0xFF00d9ff))),
                        shape = RoundedCornerShape(if (isSmall) 20.dp else 28.dp)
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(if (isSmall) 20.dp else 28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isChallenge) Color.Transparent else cardColor.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isChallenge) Modifier.background(challengeGradient) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            // Arka planda hafif bir desen efekti (Challenge için)
            if (isChallenge) {
                Icon(
                    imageVector = Icons.Default.Whatshot,
                    contentDescription = null,
                    modifier = Modifier
                        .size(height * 0.8f)
                        .alpha(0.1f)
                        .rotate(-15f),
                    tint = Color.White
                )
            }

            val baseFontSize = if (question.length > 5) 44.sp else 56.sp
            val finalFontSize = if (isSmall) (baseFontSize.value * 0.8f).sp else baseFontSize
            Text(
                text = question,
                color = Color.White,
                fontSize = finalFontSize,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color.Black.copy(alpha = 0.3f),
                        blurRadius = 8f
                    )
                )
            )
        }
    }
}

@Composable
private fun OptionsGrid(
    options: List<Int>,
    onOptionSelected: (Int, Int) -> Unit,
    buttonWidth: Dp,
    buttonHeight: Dp,
    spacing: Dp,
    zapEliminatedIndex: Int? = null,
    feedbackOptionIndex: Int? = null,
    feedbackType: AnswerFeedback = AnswerFeedback.NONE
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(spacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
            OptionButton(
                number = options[0],
                onClick = { onOptionSelected(0, options[0]) },
                color = Color(0xFFe94560),
                width = buttonWidth,
                height = buttonHeight,
                isEliminated = zapEliminatedIndex == 0,
                feedback = if (feedbackOptionIndex == 0) feedbackType else AnswerFeedback.NONE
            )
            OptionButton(
                number = options[1],
                onClick = { onOptionSelected(1, options[1]) },
                color = Color(0xFF00d9ff),
                width = buttonWidth,
                height = buttonHeight,
                isEliminated = zapEliminatedIndex == 1,
                feedback = if (feedbackOptionIndex == 1) feedbackType else AnswerFeedback.NONE
            )
        }
        OptionButton(
            number = options[2],
            onClick = { onOptionSelected(2, options[2]) },
            color = Color(0xFFf9a825),
            width = buttonWidth * 1.1f,
            height = buttonHeight,
            isEliminated = zapEliminatedIndex == 2,
            feedback = if (feedbackOptionIndex == 2) feedbackType else AnswerFeedback.NONE
        )
    }
}

@Composable
private fun OptionButton(
    number: Int,
    onClick: () -> Unit,
    color: Color,
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
    isEliminated: Boolean = false,
    feedback: AnswerFeedback = AnswerFeedback.NONE
) {
    val feedbackColor by animateColorAsState(
        targetValue = when (feedback) {
            AnswerFeedback.CORRECT -> Color(0xFF4CAF50) // Yeşil
            AnswerFeedback.WRONG -> Color(0xFFE91E63)   // Kırmızı
            else -> if (isEliminated) Color.DarkGray.copy(alpha = 0.7f) else color.copy(alpha = 0.9f)
        },
        animationSpec = tween(200),
        label = "feedbackColor"
    )

    val scale by animateFloatAsState(
        targetValue = when (feedback) {
            AnswerFeedback.CORRECT -> 1.15f
            AnswerFeedback.WRONG -> 0.95f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = if (feedback == AnswerFeedback.CORRECT) Spring.DampingRatioMediumBouncy else Spring.DampingRatioNoBouncy,
            stiffness = if (feedback == AnswerFeedback.CORRECT) Spring.StiffnessMedium else Spring.StiffnessLow
        ),
        label = "scale"
    )

    val textString = if (isEliminated) "⚡" else number.toString()
    val fontSize = when {
        isEliminated -> 36.sp
        textString.length > 4 -> 28.sp
        textString.length > 2 -> 34.sp
        else -> 40.sp
    }
    val alpha = if (isEliminated) 0.3f else 1f

    Button(
        onClick = { if (!isEliminated) onClick() },
        modifier = modifier
            .size(width = width, height = height)
            .scale(scale)
            .alpha(alpha)
            .then(
                if (feedback == AnswerFeedback.CORRECT) {
                    Modifier.border(
                        width = 4.dp,
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White, Color(0xFF4CAF50), Color.Transparent),
                            radius = 150f
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = feedbackColor
        ),
        enabled = !isEliminated && feedback == AnswerFeedback.NONE,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isEliminated) 0.dp else 12.dp,
            pressedElevation = 4.dp
        ),
        contentPadding = PaddingValues(4.dp)
    ) {
        Text(
            text = textString,
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold,
            color = if (isEliminated) Color(0xFFFFAA00) else Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1,
            style = androidx.compose.ui.text.TextStyle(
                shadow = if (feedback == AnswerFeedback.CORRECT) {
                    androidx.compose.ui.graphics.Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        blurRadius = 8f
                    )
                } else null
            )
        )
    }
}

@Composable
private fun JokerSkillBar(
    gameState: GameState,
    adMobManager: IAdManager? = null,
    activity: android.app.Activity? = null,
    isSmall: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val equipped = gameState.equippedCards.toList().take(2)
    if (equipped.isEmpty()) return

    // Track which card is currently showing the ad-chain dialog
    var refillCardId by remember { mutableStateOf<String?>(null) }
    var adsWatched by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    // 1 Reklam = 1 Hak sistemi
    fun showRefillAd(cardId: String) {
        val analyticsManager = com.mawelly.blitzmath.analytics.AnalyticsManager.getInstance(context)
        
        if (activity == null || adMobManager == null) {
            gameState.refillCharges(cardId)
            return
        }

        refillCardId = cardId
        
        scope.launch {
            // Reklam hazır mı kontrol et, değilse 3 saniye bekle
            var retryCount = 0
            while (adMobManager.isAdReady(com.mawelly.blitzmath.ads.IAdManager.Placement.REFILL_CHARGES) == false && retryCount < 3) {
                delay(1000)
                retryCount++
            }

            analyticsManager.logAdClick("RefillAbility_Single_$cardId")
            adMobManager.showAd(activity, IAdManager.Placement.REFILL_CHARGES) {
                analyticsManager.logAdReward("RefillAbility_Single_$cardId")
                gameState.refillCharges(cardId)
                refillCardId = null
            }
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        equipped.forEach { cardId ->
            val card = ScientistCards.getCardById(cardId)
            val charges = gameState.cardCharges[cardId] ?: 0
            val hasCharges = charges > 0
            val canUse = hasCharges && !gameState.isGameOver && !gameState.isCheckpointComplete
            val isEmpty = charges <= 0
            val isRefilling = refillCardId == cardId

            // Pulsing animation for the + button when empty
            val infiniteTransition = rememberInfiniteTransition(label = "pulse_$cardId")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (isEmpty) 1.25f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse_scale_$cardId"
            )

            if (card != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = canUse && !isRefilling) { gameState.activateSkill(cardId) }
                ) {
                    // Card Avatar + state overlay
                    Box(contentAlignment = Alignment.BottomEnd) {
                        val circleSize = if (isSmall) 52.dp else 62.dp
                        Box(
                            modifier = Modifier
                                .size(circleSize)
                                .aspectRatio(1f) // Baskı gelse bile tam daire kalması için
                                .shadow(if (canUse) 10.dp else 2.dp, CircleShape)
                                .background(
                                    if (canUse) Brush.radialGradient(
                                        listOf(Color(0xFF00d9ff).copy(alpha = 0.3f), Color.Transparent)
                                    ) else Brush.radialGradient(listOf(Color.DarkGray, Color.Black)),
                                    CircleShape
                                )
                                .border(
                                    width = if (canUse) 2.dp else 0.dp,
                                    color = if (canUse) Color(0xFF00d9ff) else Color.Transparent,
                                    shape = CircleShape
                                )
                                .alpha(if (canUse) 1f else 0.45f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (card.imageResId != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp) // Yüzün kenarlara yapışmasını ve kesilmesini önlemek için boşluk
                                        .clip(CircleShape)
                                ) {
                                    Image(
                                        painter = painterResource(id = card.imageResId),
                                        contentDescription = card.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit // Fit kullanarak tüm yüzün görünmesini sağlıyoruz
                                    )
                                }
                                
                                // Neon Çerçeve (Ring)
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .border(
                                            width = if (isSmall) 1.8.dp else 2.5.dp,
                                            brush = Brush.sweepGradient(
                                                listOf(
                                                    Color(0xFF00d9ff), 
                                                    Color(0xFFe94560), 
                                                    Color(0xFF00d9ff)
                                                )
                                            ),
                                            shape = CircleShape
                                        )
                                )
                            } else {
                                Text(card.name.take(1), fontSize = if (isSmall) 18.sp else 22.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            // Dark overlay when empty
                            if (isEmpty) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                )
                            }

                            // Charge count overlay — improved for consistency across screen ratios
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (isSmall) 18.dp else 24.dp)
                                    .align(Alignment.BottomCenter)
                                    .background(
                                        color = when {
                                            isEmpty -> Color(0xFFFFD700).copy(alpha = 0.9f)
                                            charges == 1 -> Color(0xFFFF4444).copy(alpha = 0.85f)
                                            else -> Color.Black.copy(alpha = 0.65f)
                                        }
                                    )
                                    .padding(bottom = 1.dp), // Tiny lift to stay clear of the circle's bottom edge
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isEmpty) "0" else "$charges",
                                    fontSize = if (isSmall) 11.sp else 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.offset(y = (-1).dp) // Fine-tune text centering
                                )
                            }
                        }

                        // "+" Refill badge & Timer Pill – pulsing, top-right corner when empty
                        if (isEmpty && !isRefilling) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 8.dp, y = (-8).dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val remainingMs = gameState.getRemainingRechargeTime(cardId)
                                if (remainingMs > 0) {
                                    val seconds = (remainingMs / 1000) % 60
                                    val minutes = (remainingMs / (1000 * 60)) % 60
                                    val timerText = String.format("%02d:%02d", minutes, seconds)
                                    
                                    // Timer Pill
                                    Box(
                                        modifier = Modifier
                                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFF00d9ff).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = timerText,
                                            fontSize = 9.sp,
                                            color = Color(0xFF00d9ff),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(2.dp))
                                }

                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .scale(pulseScale)
                                        .background(Color(0xFFFFD700), CircleShape)
                                        .border(1.5.dp, Color.White, CircleShape)
                                        .clickable { showRefillAd(cardId) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+", fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }

                        // Loading spinner while ads are running
                        if (isRefilling) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .background(Color(0xFF00d9ff), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(if (isSmall) 3.dp else 5.dp))

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = card.name,
                        fontSize = if (isSmall) 10.sp else 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (canUse) Color.White else Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    if (isEmpty) {
                        Text(
                            text = "1 Reklam = 1 Hak",
                            fontSize = 9.sp,
                            color = Color(0xFFFFD700).copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckpointCompleteScreen(
    gameState: GameState,
    onNextCheckpoint: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "scale"
    )

    val nextCheckpoint = gameState.currentCheckpoint + 1
    val nextOp = CheckpointManager.getOperationForCheckpoint(nextCheckpoint)
    val nextOpName = when (nextOp) {
        OperationType.ADDITION -> Strings.addition
        OperationType.SUBTRACTION -> Strings.subtraction
        OperationType.MULTIPLICATION -> Strings.multiplication
        OperationType.DIVISION -> Strings.division
        OperationType.MIXED -> Strings.mixed
    }.uppercase()

    Box(modifier = Modifier.fillMaxSize()) {
        SuccessConfetti()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Text(text = "🎉", fontSize = 80.sp, modifier = Modifier.scale(scale))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = Strings.checkpointComplete,
            color = Color(0xFF00d9ff),
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${Strings.checkpointLabel} ${gameState.currentCheckpoint}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp
                )
                Text(
                    text = "✓ ${Strings.congratulations.uppercase()}",
                    color = Color(0xFF4CAF50),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${Strings.nextLevel}: ${Strings.checkpointLabel} $nextCheckpoint",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${Strings.addition}: $nextOpName",
                    color = when (nextOp) {
                        OperationType.ADDITION -> Color(0xFF4CAF50)
                        OperationType.SUBTRACTION -> Color(0xFFf44336)
                        OperationType.MULTIPLICATION -> Color(0xFF9C27B0)
                        OperationType.DIVISION -> Color(0xFF2196F3)
                        else -> Color(0xFFf9a825)
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNextCheckpoint,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(60.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00d9ff))
        ) {
            Text(
                text = "${Strings.nextLevel} ➔",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1a1a2e)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bilge Matematikçi Sözü Kartı (Senin Güzel Fikrin)
        val randomQuote = remember { mathQuotes.random() }
        val currentLang = Strings.currentLanguage
        
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.1f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "\"${randomQuote.getQuote(currentLang)}\"",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontStyle = FontStyle.Italic,
                        lineHeight = 20.sp
                    ),
                    color = Color.White.copy(alpha = 0.95f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = "— ${randomQuote.author}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color(0xFF00d9ff)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SaveMeScreen(
    gameState: GameState,
    adMobManager: IAdManager?,
    activity: android.app.Activity
) {
    val progress = (gameState.saveMeTimeLeft / 3.0f).coerceIn(0f, 1f)
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val screenHeight = maxHeight
        val screenWidth = maxWidth
        val isSmall = screenHeight < 650.dp

        // Premium Background Layer (Blurred)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(20.dp)
                .background(Color.Black.copy(alpha = 0.5f))
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = Strings.saveMeTitle,
                fontSize = (screenWidth.value * 0.08f).coerceIn(24f, 36f).sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(if (isSmall) 20.dp else 40.dp))

            // Countdown Ring (Neon)
            Box(contentAlignment = Alignment.Center) {
                val ringSize = (screenHeight.value * 0.2f).coerceIn(120f, 160f).dp
                CircularProgressIndicator(
                    progress = 1f,
                    modifier = Modifier.size(ringSize),
                    color = Color.White.copy(alpha = 0.1f),
                    strokeWidth = (ringSize.value * 0.075f).dp
                )
                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.size(ringSize),
                    color = Color(0xFFe94560),
                    strokeWidth = (ringSize.value * 0.075f).dp,
                    trackColor = Color.Transparent
                )
                
                // Shadow / Glow effect for the neon ring
                Box(
                    modifier = Modifier
                        .size(ringSize)
                        .border((ringSize.value * 0.075f).dp, Brush.radialGradient(listOf(Color(0xFFe94560).copy(alpha = 0.3f), Color.Transparent)), CircleShape)
                )

                Text(
                    text = "❤️",
                    fontSize = (ringSize.value * 0.4f).sp
                )
            }

            Spacer(modifier = Modifier.height(if (isSmall) 30.dp else 60.dp))

            // Watch Ad Button (Premium Gradient + Pulse)
            val context = androidx.compose.ui.platform.LocalContext.current
            val analyticsManager = remember { com.mawelly.blitzmath.analytics.AnalyticsManager.getInstance(context) }

            Button(
                onClick = {
                    analyticsManager.logRefillLivesClick("Gameplay")
                    analyticsManager.logAdClick("RevillLives_InGame")
                    adMobManager?.showAd(activity, IAdManager.Placement.SAVE_ME) {
                        analyticsManager.logAdReward("RevillLives_InGame")
                        gameState.resurrect()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(if (screenWidth > 600.dp) 0.6f else 0.85f)
                    .height(if (isSmall) 55.dp else 64.dp)
                    .scale(pulseScale),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFe94560), Color(0xFF9C27B0))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎥 ", fontSize = 20.sp)
                        Text(
                            text = Strings.watchAdToSave,
                            fontSize = (screenWidth.value * 0.045f).coerceIn(16f, 20f).sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // No Thanks Button (Glassmorphism)
            TextButton(
                onClick = { gameState.declineSaveMe() },
                modifier = Modifier
                    .fillMaxWidth(if (screenWidth > 600.dp) 0.4f else 0.6f)
                    .height(48.dp)
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            ) {
                Text(
                    text = Strings.noThanks,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun HeartsHUD(
    livesRemaining: Int, 
    refillTime: String = "", 
    isCompact: Boolean = false,
    playerLevel: Int = 1,
    playerProgress: Float = 0f
) {
    var isHeartsExpanded by remember { mutableStateOf(false) }
    
    // Auto collapse after 3.5 seconds
    LaunchedEffect(isHeartsExpanded) {
        if (isHeartsExpanded) {
            kotlinx.coroutines.delay(3500)
            isHeartsExpanded = false
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 12.dp),
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .animateContentSize(animationSpec = androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessLow))
    ) {
        // --- 1. CIRCULAR XP BAR ---
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(if (isCompact) 32.dp else 38.dp)
        ) {
            androidx.compose.material3.CircularProgressIndicator(
                progress = { 1f },
                color = Color.White.copy(alpha = 0.2f),
                strokeWidth = 3.dp,
                modifier = Modifier.fillMaxSize()
            )
            androidx.compose.material3.CircularProgressIndicator(
                progress = { playerProgress },
                color = Color(0xFF00E5FF),
                strokeWidth = 3.dp,
                modifier = Modifier.fillMaxSize()
            )
            Text(
                text = "$playerLevel",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = if (isCompact) 12.sp else 14.sp
            )
        }

        // --- 2. EXPANDABLE HEARTS ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) {
                isHeartsExpanded = !isHeartsExpanded
            }
        ) {
            if (isHeartsExpanded) {
                // Show all 5 hearts
                repeat(5) { index ->
                    val isFilled = index < livesRemaining
                    val tint = if (isFilled) Color(0xFFE94560) else Color.White.copy(alpha = 0.25f)
                    
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier
                            .size(if (isCompact) 18.dp else 22.dp)
                            .graphicsLayer {
                                if (isFilled && livesRemaining <= 2) {
                                    val scale = 1f + (0.12f * kotlin.math.sin(System.currentTimeMillis() / 350.0).toFloat())
                                    scaleX = scale
                                    scaleY = scale
                                }
                            }
                    )
                }
            } else {
                // Show single heart + number
                val tint = if (livesRemaining > 0) Color(0xFFE94560) else Color.White.copy(alpha = 0.25f)
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier
                        .size(if (isCompact) 20.dp else 24.dp)
                        .graphicsLayer {
                            if (livesRemaining in 1..2) {
                                val scale = 1f + (0.12f * kotlin.math.sin(System.currentTimeMillis() / 350.0).toFloat())
                                scaleX = scale
                                scaleY = scale
                            }
                        }
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$livesRemaining",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isCompact) 14.sp else 16.sp
                )
            }
        }

        // Timer Section (Horizontal)
        if (livesRemaining < 5 && refillTime.isNotEmpty()) {
            Surface(
                color = Color.Black.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF00d9ff).copy(alpha = 0.4f)),
                modifier = Modifier.padding(start = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "⏳",
                        fontSize = if (isCompact) 10.sp else 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = refillTime,
                        color = Color(0xFF00d9ff),
                        fontSize = if (isCompact) 11.sp else 13.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun GameOverScreen(
    gameState: GameState,
    adMobManager: IAdManager?,
    activity: Activity,
    onBackToMenu: () -> Unit,
    onShowRanking: (String) -> Unit,
    currentLang: AppLanguage
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val nextQuote = remember { QuoteManager.getNextQuote() }
    val adCalled = remember { mutableStateOf(false) }
    val hasUsedSecondChance = remember { mutableStateOf(false) }
    val scrollState = androidx.compose.foundation.rememberScrollState()
    val scope = rememberCoroutineScope()
    
    val leaderboardManager = remember { com.mawelly.blitzmath.leaderboard.LeaderboardManager() }
    var currentPlayerRank by remember { mutableStateOf<Int?>(null) }
    val playerId = gameState.languageManager?.getPlayerId() ?: ""

    // Skorları sunucuya gönder ve sıralamayı al
    LaunchedEffect(gameState.score) {
        if (gameState.score > 0) {
            val modeName = gameState.mode.name.lowercase()
            leaderboardManager.submitScore(
                playerId = playerId,
                playerName = gameState.languageManager?.getPlayerName() ?: "",
                score = gameState.score.toLong(),
                level = gameState.currentCheckpoint,
                country = java.util.Locale.getDefault().country,
                mode = modeName
            )
            
            // Sıralamayı al
            val rankResult = leaderboardManager.getPlayerRank(playerId, mode = modeName)
            rankResult.onSuccess { rank ->
                currentPlayerRank = rank
            }
        }
    }

    val earnedStars = remember { (gameState.score / 10) + 5 }
    var displayedStars by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        for (i in 1..earnedStars) {
            displayedStars = i
            kotlinx.coroutines.delay(50)
        }
    }

    LaunchedEffect(Unit) {
        if (!adCalled.value && gameState.showAdOnGameOver) {
            adCalled.value = true
            adMobManager?.onGameOver(activity) { gameState.onAdShown() }
        }
    }

    // Sıralama Kartı için manuel tetikleyici
    var showManualLeaderboard by remember { mutableStateOf(false) }

    if (showManualLeaderboard) {
        com.mawelly.blitzmath.ui.screens.LeaderboardPopup(
            mode = gameState.mode.name.lowercase(),
            onDismiss = { showManualLeaderboard = false }
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // Calculate adaptive sizes relative to screen height/width
        val screenH = maxHeight
        val screenW = maxWidth
        val isSmall = screenH < 650.dp
        val isMedium = screenH < 800.dp
        val titleSize = if (isSmall) 22.sp else if (isMedium) 26.sp else 32.sp
        val scoreSize = if (isSmall) 34.sp else if (isMedium) 40.sp else 46.sp
        val cpSize = if (isSmall) 30.sp else if (isMedium) 34.sp else 38.sp
        val bodySize = if (isSmall) 11.sp else 13.sp
        val btnFontSize = if (isSmall) 11.sp else if (isMedium) 13.sp else 14.sp
        val btnHeight = if (isSmall) 42.dp else 48.dp
        val cardPad = if (isSmall) 10.dp else 14.dp
        val vSpace = if (isSmall) 6.dp else 10.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = if (isSmall) 8.dp else 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val scale by animateFloatAsState(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
                label = "scale"
            )

            Spacer(modifier = Modifier.height(vSpace / 2))
            
            // Game Over title — auto-size to prevent overflow
            Text(
                text = Strings.gameOver,
                color = Color(0xFFe94560),
                fontSize = titleSize,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(vSpace / 2))

            // ⭐ Animated stars
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text("⭐", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "+$displayedStars",
                    color = Color(0xFFFFD700),
                    fontSize = if (isSmall) 15.sp else 17.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(vSpace))

            // Score card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(cardPad),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Checkpoint
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(Strings.checkpointLabel, color = Color.White.copy(alpha = 0.6f), fontSize = bodySize)
                        Text(
                            gameState.currentCheckpoint.toString(),
                            color = Color(0xFF00d9ff), fontSize = cpSize, fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Box(modifier = Modifier.width(1.dp).height(50.dp).background(Color.White.copy(alpha = 0.2f)))

                    // Score
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(Strings.finalScore, color = Color.White.copy(alpha = 0.6f), fontSize = bodySize)
                        Text(
                            gameState.score.toString(),
                            color = Color(0xFFf9a825), fontSize = scoreSize, fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                if (gameState.score == gameState.highScore && gameState.score > 0) {
                    Text(
                        "🏆 ${Strings.newRecord} 🏆",
                        color = Color(0xFFf9a825),
                        fontSize = if (isSmall) 14.sp else 17.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = cardPad / 2)
                    )
                }
            }

            // --- World Ranking Premium Section ---
            Spacer(modifier = Modifier.height(vSpace))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showManualLeaderboard = true }
                    .border(1.dp, Color(0xFF00d9ff).copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier.padding(cardPad),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(if (isSmall) 36.dp else 44.dp)
                                .background(Color(0xFF00d9ff).copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌍", fontSize = if (isSmall) 18.sp else 22.sp)
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = Strings.globalLeaderboard,
                                color = Color.White,
                                fontSize = if (isSmall) 14.sp else 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (currentPlayerRank != null && currentPlayerRank!! > 0) 
                                    "${Strings.yourRank}: #$currentPlayerRank" 
                                    else "Rank: ...",
                                color = Color(0xFF00d9ff),
                                fontSize = if (isSmall) 11.sp else 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    Button(
                        onClick = { showManualLeaderboard = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00d9ff).copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = "VIEW ➔",
                            color = Color(0xFF00d9ff),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Challenge Modu İstatistikleri (YENİ)
            if (gameState.mode == com.mawelly.blitzmath.game.GameMode.CHALLENGE) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = vSpace / 2),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(cardPad)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${Strings.totalPlayTime}:", color = Color.White.copy(alpha = 0.7f), fontSize = bodySize)
                            Text("${gameState.gameDurationSeconds.toInt()} sn", color = Color.White, fontWeight = FontWeight.Bold, fontSize = bodySize)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${Strings.totalTimeBonus}:", color = Color.White.copy(alpha = 0.7f), fontSize = bodySize)
                            Text("+${gameState.totalTimeBonusEarned.toInt()} sn", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = bodySize)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(vSpace))

            // 🎬 Second Chance button
            if (!hasUsedSecondChance.value) {
                Button(
                    onClick = {
                        hasUsedSecondChance.value = true
                        val analyticsManager = com.mawelly.blitzmath.analytics.AnalyticsManager.getInstance(context)
                        analyticsManager.logAdClick("Revive_GameOver")
                        adMobManager?.showAd(activity, IAdManager.Placement.SAVE_ME) {
                            analyticsManager.logAdReward("Revive_GameOver")
                            gameState.resurrect()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(btnHeight),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
                ) {
                    Text(
                        "🎬 ${Strings.watchAdContinue}",
                        fontSize = btnFontSize,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(vSpace / 2))
            }

            // Quote card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF9C27B0).copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(cardPad),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("💡", fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"${nextQuote.getQuote(currentLang)}\"",
                        color = Color.White, fontSize = bodySize, fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center, lineHeight = (bodySize.value * 1.4f).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "— ${nextQuote.author}",
                        color = Color(0xFF00d9ff), fontSize = (bodySize.value - 1).sp,
                        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(vSpace))

            // Retry / Back buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { 
                        if (gameState.livesRemaining > 0) {
                            gameState.restart() 
                        } else {
                            onBackToMenu()
                        }
                    },
                    modifier = Modifier.weight(1f).height(btnHeight),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00d9ff)),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text(
                        Strings.retry, fontSize = btnFontSize, fontWeight = FontWeight.Bold,
                        color = Color(0xFF1a1a2e), maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                Button(
                    onClick = {
                        onBackToMenu()
                    },
                    modifier = Modifier.weight(1f).height(btnHeight),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFf9a825)),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text(
                        Strings.backToMenu, fontSize = btnFontSize, fontWeight = FontWeight.Bold,
                        color = Color(0xFF1a1a2e), maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(vSpace / 2))

            // 📤 Share score
            OutlinedButton(
                onClick = {
                    ShareManager.shareScoreWithScreenshot(
                        activity = activity,
                        score = gameState.score,
                        checkpoint = gameState.currentCheckpoint
                    )
                },
                modifier = Modifier.fillMaxWidth().height(btnHeight - 6.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00d9ff))
            ) {
                Text(
                    "📤 ${Strings.shareScore}",
                    fontSize = btnFontSize, fontWeight = FontWeight.Bold,
                    color = Color(0xFF00d9ff), maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(vSpace))
        }
    }
}

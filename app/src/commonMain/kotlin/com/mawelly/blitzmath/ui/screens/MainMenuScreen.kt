package com.mawelly.blitzmath.ui.screens

import com.mawelly.blitzmath.core.LocalPlatformServices
import kotlin.math.pow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.material3.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.*
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.mawelly.blitzmath.ui.theme.LocalBlitzMathColors
import com.mawelly.blitzmath.localization.Strings
import com.mawelly.blitzmath.data.IGameDataStore
import com.mawelly.blitzmath.localization.AppLanguage
import com.mawelly.blitzmath.game.DailyRewardManager
import com.mawelly.blitzmath.game.DailyRewardStatus
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Close
import com.mawelly.blitzmath.game.DailyTasksManager
import com.mawelly.blitzmath.ui.components.FloatingSymbolsBackground
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.foundation.interaction.collectIsPressedAsState

@Composable
fun MainMenuScreen(
    dataStore: IGameDataStore,
    onPlayClick: () -> Unit,
    onMixedModeClick: () -> Unit,
    onChallengeClick: () -> Unit,
    onVsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    onCollectionClick: () -> Unit,
    onMoreGamesClick: (String) -> Unit,
    onExitClick: () -> Unit,
    onPromptVoice: ((String, Float, Boolean) -> Unit)? = null,
    platformServices: com.mawelly.blitzmath.core.PlatformServices,
    pLevel: Int = 1,
    pProgress: Float = 0f,
    currentXp: Int = 0
) {
    val analyticsManager = platformServices.analyticsManager
    
    val currentLang = Strings.currentLanguage
    var slogan by remember(currentLang) { mutableStateOf(Strings.slogan) }
    
    // Slogan rotasyonu (Sürekli değişmesi için)
    LaunchedEffect(currentLang) {
        while (true) {
            delay(6000) // 6 saniyede bir değiştir
            slogan = Strings.slogan
        }
    }
    
    val scope = rememberCoroutineScope()
    val streak by dataStore.streakCount.collectAsState(initial = 0)
    val lastClaimTime by dataStore.lastClaimTime.collectAsState(initial = 0L)
    val totalStars by dataStore.starCount.collectAsState(initial = 0)
    val savedLives by dataStore.livesCount.collectAsState(initial = 5)
    val savedLossTime by dataStore.lastLifeLossTime.collectAsState(initial = 0L)
    val unlockedCards by dataStore.unlockedCards.collectAsState(initial = emptySet())

    // Most played mode detection
    val gamesPlayed by dataStore.gamesPlayed.collectAsState(initial = 0)
    val isReviewed by dataStore.isReviewed.collectAsState(initial = false)
    var showSurveyDialog by remember { mutableStateOf(false) }

    val classicLevel by dataStore.classicLevel.collectAsState(initial = 1)
    val mixedLevel by dataStore.mixedLevel.collectAsState(initial = 1)
    val classicHighScore by dataStore.highScore.collectAsState(initial = 0)
    val mixedHighScore by dataStore.mixedHighScore.collectAsState(initial = 0)
    val challengeHighScore by dataStore.challengeHighScore.collectAsState(initial = 0)

    val mostPlayedMode = remember(classicLevel, mixedLevel, classicHighScore, mixedHighScore, challengeHighScore) {
        val scores = mapOf(
            "CLASSIC" to classicLevel * 10 + classicHighScore / 10,
            "MIXED" to mixedLevel * 10 + mixedHighScore / 10,
            "CHALLENGE" to challengeHighScore / 5
        )
        scores.maxByOrNull { it.value }?.key ?: "CLASSIC"
    }

    val canAffordCard = remember(totalStars, unlockedCards) {
        com.mawelly.blitzmath.game.ScientistCards.cards.any { 
            it.price <= totalStars && !unlockedCards.contains(it.id) 
        }
    }

    var currentLives by remember(savedLives) { mutableIntStateOf(savedLives ?: 5) }
    var lastLossTime by remember(savedLossTime) { mutableLongStateOf(savedLossTime ?: 0L) }
    var showNoLivesDialog by remember { mutableStateOf(false) }

    val xpForNextLevel = pLevel.toDouble().pow(2.0).toInt() * 100

    // Constant for refill time: 15 minutes
    val REFILL_TIME_MS = 15 * 60 * 1000L
    val MAX_LIVES = 5

    // Countdown timer state
    var timeLeftToRefill by remember { mutableStateOf("") }
    
    var showRewardDialog by remember { mutableStateOf(false) }
    var showDailyTasksDialog by remember { mutableStateOf(false) }
    
    // Check for daily reward on launch
    LaunchedEffect(Unit) {
        analyticsManager.logScreenView("MainMenu")
        val currentTime = platformServices.getCurrentTimeMillis()
        val rewardStatus = DailyRewardManager.getRewardStatus(lastClaimTime, currentTime)
        if (rewardStatus == DailyRewardStatus.AVAILABLE || 
            rewardStatus == DailyRewardStatus.STREAK_RESET) {
            showRewardDialog = false
        }
    }

    // Refill Logic Loop
    LaunchedEffect(currentLives, lastLossTime) {
        // Safety check: if missing lives but no timestamp, initialize it
        if (currentLives < MAX_LIVES && lastLossTime <= 0L) {
            val now = platformServices.getCurrentTimeMillis()
            lastLossTime = now
            scope.launch {
                dataStore.saveLastLifeLossTime(now)
            }
        }

        while (currentLives < MAX_LIVES && lastLossTime > 0) {
            val now = platformServices.getCurrentTimeMillis()
            val timePassed = now - lastLossTime
            val livesToRefill = (timePassed / REFILL_TIME_MS).toInt()

            if (livesToRefill > 0) {
                val newLives = (currentLives + livesToRefill).coerceAtMost(MAX_LIVES)
                val newLossTime = if (newLives == MAX_LIVES) 0L else lastLossTime + (livesToRefill * REFILL_TIME_MS)
                
                currentLives = newLives
                lastLossTime = newLossTime
                
                scope.launch {
                    dataStore.saveLives(newLives)
                    dataStore.saveLastLifeLossTime(newLossTime)
                }
            } else {
                // Update countdown string
                val remainingMs = REFILL_TIME_MS - (timePassed % REFILL_TIME_MS)
                val minutes = (remainingMs / 1000 / 60)
                val seconds = (remainingMs / 1000 % 60)
                timeLeftToRefill = minutes.toString().padStart(2, '0') + ":" + seconds.toString().padStart(2, '0')
            }
            delay(1000)
        }
        if (currentLives >= MAX_LIVES) {
            timeLeftToRefill = ""
            lastLossTime = 0L
        }
    }

    val challengePlaysToday by dataStore.challengePlaysToday.collectAsState(initial = 0)
    val lastChallengeDate by dataStore.lastChallengeDate.collectAsState(initial = "")
    
    val todayStr = remember { 
        platformServices.getCurrentDateString()
    }
    
    val actualPlaysToday = if (lastChallengeDate == todayStr) challengePlaysToday else 0
    val isChallengeAvailable = actualPlaysToday < 5
    var showChallengeLimitDialog by remember { mutableStateOf(false) }

    var newlyUnlockedCard by remember { mutableStateOf<com.mawelly.blitzmath.game.ScientistCard?>(null) }

    val blitzColors = LocalBlitzMathColors.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {

        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val screenHeightVal = screenHeight.value
        val screenWidthVal = screenWidth.value
        
        val buttonHeight = (screenHeightVal * 0.08f).coerceIn(55f, 75f).dp
        val spacing = (screenHeightVal * 0.015f).coerceIn(8f, 16f).dp
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- HUD Bar (Top) ---
            MainMenuTopHUD(
                currentLives = currentLives,
                timeLeftToRefill = timeLeftToRefill,
                totalStars = totalStars,
                playerLevel = pLevel,
                playerProgress = pProgress,
                currentXp = currentXp,
                xpForNextLevel = xpForNextLevel,
                canAffordCard = canAffordCard,
                onCollectionClick = onCollectionClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(10f)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(bottom = 80.dp)
            ) {
            // --- Title Section ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "BLITZ MATH",
                            fontSize = (screenWidthVal * 0.11f).coerceIn(36f, 54f).sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 4.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.graphicsLayer {
                                shadowElevation = 20f
                            }
                        )

                        if (gamesPlayed >= 2 && !isReviewed) {
                            Spacer(modifier = Modifier.width(10.dp))
                            val infiniteTransition = rememberInfiniteTransition(label = "giftBoxPulse")
                            val giftScale by infiniteTransition.animateFloat(
                                initialValue = 0.95f,
                                targetValue = 1.15f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(600, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "giftScale"
                            )

                            Box(
                                modifier = Modifier
                                    .scale(giftScale)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFFFFD700), Color(0xFFFF8C00))
                                        )
                                    )
                                    .border(2.dp, Color.White, RoundedCornerShape(14.dp))
                                    .clickable {
                                        showSurveyDialog = true
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "🎁",
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        text = "+1000",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .width(180.dp)
                            .height(2.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color.Transparent, blitzColors.accent, Color.Transparent)
                                )
                            )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AnimatedContent(
                        targetState = slogan,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(1000)) + scaleIn(initialScale = 0.9f))
                                .togetherWith(fadeOut(animationSpec = tween(1000)) + scaleOut(targetScale = 1.1f))
                        },
                        label = "sloganAnimation"
                    ) { targetSlogan ->
                        Text(
                            text = targetSlogan,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            letterSpacing = 2.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- Game Modes Grid (Modern Layout) ---
            val cardWidth = if (screenWidth > 600.dp) 450.dp else screenWidth * 0.9f

            // ═══ HERO CARD: En çok oynanan mod dinamik olarak gösterilir ═══
            val heroConfig = when (mostPlayedMode) {
                "CLASSIC" -> Triple(
                    Strings.menuClassic,
                    "Seviye $classicLevel • ${Strings.menuClassicSubtitle}",
                    Color(0xFF00C853)
                )
                "MIXED" -> Triple(
                    Strings.menuMixed,
                    "Seviye $mixedLevel • ${Strings.menuMixedSubtitle}",
                    Color(0xFFD500F9)
                )
                "CHALLENGE" -> Triple(
                    Strings.menuChallenge,
                    if (isChallengeAvailable) Strings.menuChallengeSubtitle else Strings.challengeAlreadyPlayed,
                    Color(0xFFFFAB00)
                )
                else -> Triple(
                    Strings.menuClassic,
                    Strings.menuClassicSubtitle,
                    Color(0xFF00C853)
                )
            }
            val heroOnClick: () -> Unit = when (mostPlayedMode) {
                "CLASSIC" -> ({
                    if (currentLives > 0) { analyticsManager.logModeSelection("Classic"); onPlayClick() } else showNoLivesDialog = true
                })
                "MIXED" -> ({
                    if (currentLives > 0) { analyticsManager.logModeSelection("Mixed"); onMixedModeClick() } else showNoLivesDialog = true
                })
                "CHALLENGE" -> ({
                    if (isChallengeAvailable) {
                        if (currentLives > 0) { analyticsManager.logModeSelection("Challenge"); scope.launch { dataStore.saveChallengePlayInfo(actualPlaysToday + 1, todayStr) }; onChallengeClick() } else showNoLivesDialog = true
                    } else showChallengeLimitDialog = true
                })
                else -> ({ if (currentLives > 0) { analyticsManager.logModeSelection("Classic"); onPlayClick() } else showNoLivesDialog = true })
            }

            // Hero Quick Play Card
            Card(
                modifier = Modifier
                    .width(cardWidth)
                    .height(130.dp)
                    .clickable(onClick = heroOnClick),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    heroConfig.third.copy(alpha = 0.35f),
                                    heroConfig.third.copy(alpha = 0.08f),
                                    Color(0xFF0A0A1A).copy(alpha = 0.9f)
                                )
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                listOf(heroConfig.third.copy(alpha = 0.8f), Color.White.copy(alpha = 0.1f), heroConfig.third.copy(alpha = 0.4f))
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(heroConfig.third.copy(alpha = 0.15f), CircleShape)
                                .border(1.5.dp, heroConfig.third.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            GameModeGraphic(mode = mostPlayedMode, color = heroConfig.third, modifier = Modifier.size(44.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .background(heroConfig.third.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("🏆 EN ÇOK", color = heroConfig.third, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = heroConfig.first,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = heroConfig.second,
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = heroConfig.third,
                            modifier = Modifier.size(28.dp).rotate(45f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ═══ 2x2 Grid ═══
            Column(
                modifier = Modifier.width(cardWidth),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Row 1: Classic & Mixed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModernGlassButton(
                        title = Strings.menuClassic,
                        icon = "CLASSIC",
                        mainColor = Color(0xFF00C853),
                        onClick = {
                            if (currentLives > 0) { analyticsManager.logModeSelection("Classic"); onPlayClick() } else showNoLivesDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        compact = true
                    )
                    ModernGlassButton(
                        title = Strings.menuMixed,
                        icon = "MIXED",
                        mainColor = Color(0xFFD500F9),
                        onClick = {
                            if (currentLives > 0) { analyticsManager.logModeSelection("Mixed"); onMixedModeClick() } else showNoLivesDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        compact = true
                    )
                }
                // Row 2: VS Duel & Challenge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModernGlassButton(
                        title = Strings.menuOnlineVsDuelTitle,
                        icon = "VS",
                        mainColor = Color(0xFF00E5FF),
                        onClick = {
                            if (currentLives > 0) { analyticsManager.logModeSelection("VS"); onVsClick() } else showNoLivesDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        compact = true
                    )
                    ModernGlassButton(
                        title = Strings.menuChallenge,
                        icon = "CHALLENGE",
                        mainColor = if (isChallengeAvailable) Color(0xFFFFAB00) else Color.Gray,
                        onClick = {
                            if (isChallengeAvailable) {
                                if (currentLives > 0) {
                                    analyticsManager.logModeSelection("Challenge")
                                    scope.launch { dataStore.saveChallengePlayInfo(actualPlaysToday + 1, todayStr) }
                                    onChallengeClick()
                                } else showNoLivesDialog = true
                            } else showChallengeLimitDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        compact = true,
                        isLocked = !isChallengeAvailable
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

        // ═══ FLOATING BOTTOM NAV BAR ═══
        FloatingBottomNavBar(
            onStoreClick = onCollectionClick,
            onTasksClick = { showDailyTasksDialog = true },
            onPlayClick = {
                // Başlat = Online Duel modu
                if (currentLives > 0) { analyticsManager.logModeSelection("VS"); onVsClick() } else showNoLivesDialog = true
            },
            onRankingClick = onLeaderboardClick,
            onSettingsClick = onSettingsClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(x = (-20).dp, y = 10.dp)
                .requiredWidth(maxWidth + 40.dp)
                .zIndex(20f)
        )
        
        if (showRewardDialog) {
            DailyRewardDialog(currentTime = platformServices.getCurrentTimeMillis(),
                streak = streak,
                lastClaimTime = lastClaimTime,
                onClaim = {
                    scope.launch {
                        val newStreak = DailyRewardManager.calculateNewStreak(streak, lastClaimTime, platformServices.getCurrentTimeMillis())
                        val rewardStars = DailyRewardManager.getStarReward(newStreak)
                        dataStore.saveDailyReward(newStreak, platformServices.getCurrentTimeMillis(), rewardStars)
                        showRewardDialog = false
                    }
                },
                onDismiss = { showRewardDialog = false }
            )
        }


        if (showSurveyDialog) {
            SurveyRewardDialog(
                onDismiss = { showSurveyDialog = false },
                onConfirm = {
                    scope.launch {
                        dataStore.saveIsReviewed(true)
                        dataStore.addStars(1000)
                        platformServices.soundManager.playSound("success")
                        showSurveyDialog = false
                    }
                }
            )
        }

        var showExitDialog by remember { mutableStateOf(false) }

        
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                containerColor = Color(0xFF1a1a2e),
                shape = RoundedCornerShape(24.dp),
                title = {
                    Text(
                        text = Strings.exitDialogTitle,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Text(
                        text = Strings.exitDialogMessage,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showExitDialog = false
                            onExitClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = blitzColors.accent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = Strings.exitConfirm,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showExitDialog = false
                        }
                    ) {
                        Text(
                            text = Strings.exitDismiss,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            )
        }
        
        if (showNoLivesDialog) {
            AlertDialog(
                onDismissRequest = { showNoLivesDialog = false },
                containerColor = Color(0xFF1a1a2e),
                shape = RoundedCornerShape(24.dp),
                title = {
                    Text(
                        text = Strings.outOfLivesTitle,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "❤️ ❤️ ❤️ ❤️ ❤️",
                            fontSize = 24.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Text(
                            text = Strings.outOfLivesMessage,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            analyticsManager.logRefillLivesClick("MainMenu")
                            analyticsManager.logAdClick("RefillLives_MainMenu")
                            platformServices.adController.showRewardedAd(
                                placement = com.mawelly.blitzmath.core.AdPlacement.REFILL_CHARGES,
                                onReward = {
                                    analyticsManager.logAdReward("RefillLives_MainMenu")
                                    scope.launch {
                                        dataStore.saveLives(5)
                                        dataStore.saveLastLifeLossTime(0L)
                                        currentLives = 5
                                        lastLossTime = 0L
                                        showNoLivesDialog = false
                                    }
                                },
                                onClosed = {}
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE94560)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎥 ", fontSize = 18.sp)
                            Text(
                                text = Strings.outOfLivesRefillAd,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNoLivesDialog = false }) {
                        Text(text = Strings.noThanks, color = Color.White.copy(alpha = 0.6f))
                    }
                }
            )
        }
        if (showChallengeLimitDialog) {
            AlertDialog(
                onDismissRequest = { showChallengeLimitDialog = false },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color(0xFF1a1a2e),
                title = {
                    Text(
                        text = Strings.challenge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                text = {
                    Column {
                        Text(
                            text = Strings.challengeAlreadyPlayed,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            analyticsManager.logAdClick("Challenge_Unlock")
                            platformServices.adController.showRewardedAd(
                                placement = com.mawelly.blitzmath.core.AdPlacement.DAILY_BONUS,
                                onReward = {
                                    analyticsManager.logAdReward("Challenge_Unlock")
                                    scope.launch {
                                        // Reset count to allow one more play
                                        dataStore.saveChallengePlayInfo(actualPlaysToday - 1, todayStr)
                                    }
                                    showChallengeLimitDialog = false
                                },
                                onClosed = {}
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("🎥 ${Strings.watchAdToPlayAgain}", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showChallengeLimitDialog = false }) {
                        Text(Strings.noThanks, color = Color.White.copy(alpha = 0.5f))
                    }
                }
            )
        }
        
        if (showDailyTasksDialog) {
            DailyTasksDialog(
                dataStore = dataStore,
                currentTime = platformServices.getCurrentTimeMillis(),
                onDismiss = { showDailyTasksDialog = false },
                platformServices = platformServices
            )
        }
    }
}

@Composable
fun DailyRewardDialog(
    currentTime: Long,
    streak: Int,
    lastClaimTime: Long,
    onClaim: () -> Unit,
    onDismiss: () -> Unit
) {
    val blitzColors = LocalBlitzMathColors.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1a1a2e),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 8.dp,
        modifier = Modifier.border(2.dp, blitzColors.accent, RoundedCornerShape(24.dp)),
        title = {
            Text(
                text = Strings.dailyReward,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🔥 ${Strings.streak}: $streak",
                    color = blitzColors.accent,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = Strings.dailyBonusDesc,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )

                val nextStreak = DailyRewardManager.calculateNewStreak(streak, lastClaimTime, currentTime)
                val rewardAmount = DailyRewardManager.getStarReward(nextStreak)

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "+$rewardAmount ${Strings.stars} ⭐",
                    color = Color(0xFFFFD700),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Day indicators
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val currentDayInWeek = (streak % 7).let { if (it == 0 && streak > 0) 7 else it }
                    for (i in 1..7) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (i <= currentDayInWeek) blitzColors.accent else Color.White.copy(alpha = 0.1f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (i <= currentDayInWeek) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = "$i",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onClaim,
                colors = ButtonDefaults.buttonColors(containerColor = blitzColors.accent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = Strings.claim,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
private fun GameModeGraphic(
    mode: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(48.dp)) {
        val width = size.width
        val height = size.height
        val center = androidx.compose.ui.geometry.Offset(width / 2f, height / 2f)

        when (mode) {
            "CLASSIC" -> {
                // Draw notebook page with math equations
                val rectWidth = width * 0.7f
                val rectHeight = height * 0.8f
                val left = (width - rectWidth) / 2f
                val top = (height - rectHeight) / 2f
                drawRoundRect(
                    color = color.copy(alpha = 0.2f),
                    topLeft = androidx.compose.ui.geometry.Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(rectWidth, rectHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
                drawRoundRect(
                    color = color,
                    topLeft = androidx.compose.ui.geometry.Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(rectWidth, rectHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                )
                val lineSpacing = rectHeight / 5f
                for (i in 1..3) {
                    drawLine(
                        color = color.copy(alpha = 0.5f),
                        start = androidx.compose.ui.geometry.Offset(left + 6.dp.toPx(), top + i * lineSpacing),
                        end = androidx.compose.ui.geometry.Offset(left + rectWidth - 6.dp.toPx(), top + i * lineSpacing),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }
                drawCircle(
                    color = color,
                    center = androidx.compose.ui.geometry.Offset(left + rectWidth * 0.3f, top + 4 * lineSpacing),
                    radius = 3.dp.toPx()
                )
            }
            "MIXED" -> {
                // Draw operator circle shield with +, -, *, /
                drawCircle(
                    color = color.copy(alpha = 0.15f),
                    center = center,
                    radius = width / 2.2f
                )
                drawCircle(
                    color = color,
                    center = center,
                    radius = width / 2.2f,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                )
                drawLine(
                    color = color.copy(alpha = 0.3f),
                    start = androidx.compose.ui.geometry.Offset(center.x - width / 2.2f, center.y),
                    end = androidx.compose.ui.geometry.Offset(center.x + width / 2.2f, center.y),
                    strokeWidth = 1.5.dp.toPx()
                )
                drawLine(
                    color = color.copy(alpha = 0.3f),
                    start = androidx.compose.ui.geometry.Offset(center.x, center.y - height / 2.2f),
                    end = androidx.compose.ui.geometry.Offset(center.x, center.y + height / 2.2f),
                    strokeWidth = 1.5.dp.toPx()
                )
                val offsetVal = width * 0.22f
                // Top-Left: "+"
                drawLine(color = color, start = androidx.compose.ui.geometry.Offset(center.x - offsetVal - 4.dp.toPx(), center.y - offsetVal), end = androidx.compose.ui.geometry.Offset(center.x - offsetVal + 4.dp.toPx(), center.y - offsetVal), strokeWidth = 2.dp.toPx())
                drawLine(color = color, start = androidx.compose.ui.geometry.Offset(center.x - offsetVal, center.y - offsetVal - 4.dp.toPx()), end = androidx.compose.ui.geometry.Offset(center.x - offsetVal, center.y - offsetVal + 4.dp.toPx()), strokeWidth = 2.dp.toPx())
                // Top-Right: "-"
                drawLine(color = color, start = androidx.compose.ui.geometry.Offset(center.x + offsetVal - 4.dp.toPx(), center.y - offsetVal), end = androidx.compose.ui.geometry.Offset(center.x + offsetVal + 4.dp.toPx(), center.y - offsetVal), strokeWidth = 2.dp.toPx())
                // Bottom-Left: "*"
                drawLine(color = color, start = androidx.compose.ui.geometry.Offset(center.x - offsetVal - 3.dp.toPx(), center.y + offsetVal - 3.dp.toPx()), end = androidx.compose.ui.geometry.Offset(center.x - offsetVal + 3.dp.toPx(), center.y + offsetVal + 3.dp.toPx()), strokeWidth = 2.dp.toPx())
                drawLine(color = color, start = androidx.compose.ui.geometry.Offset(center.x - offsetVal + 3.dp.toPx(), center.y + offsetVal - 3.dp.toPx()), end = androidx.compose.ui.geometry.Offset(center.x - offsetVal - 3.dp.toPx(), center.y + offsetVal + 3.dp.toPx()), strokeWidth = 2.dp.toPx())
                // Bottom-Right: "/"
                drawLine(color = color, start = androidx.compose.ui.geometry.Offset(center.x + offsetVal - 4.dp.toPx(), center.y + offsetVal), end = androidx.compose.ui.geometry.Offset(center.x + offsetVal + 4.dp.toPx(), center.y + offsetVal), strokeWidth = 2.dp.toPx())
                drawCircle(color = color, center = androidx.compose.ui.geometry.Offset(center.x + offsetVal, center.y + offsetVal - 4.dp.toPx()), radius = 1.5.dp.toPx())
                drawCircle(color = color, center = androidx.compose.ui.geometry.Offset(center.x + offsetVal, center.y + offsetVal + 4.dp.toPx()), radius = 1.5.dp.toPx())
            }
            "CHALLENGE" -> {
                // Draw stopwatch + lightning
                val topSpace = height * 0.12f
                drawCircle(
                    color = color.copy(alpha = 0.2f),
                    center = center,
                    radius = width / 2.3f
                )
                drawCircle(
                    color = color,
                    center = center,
                    radius = width / 2.3f,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                )
                drawLine(
                    color = color,
                    start = androidx.compose.ui.geometry.Offset(center.x, 0f),
                    end = androidx.compose.ui.geometry.Offset(center.x, topSpace + 2.dp.toPx()),
                    strokeWidth = 3.dp.toPx()
                )
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(center.x + 3.dp.toPx(), height * 0.22f)
                    lineTo(center.x - 7.dp.toPx(), height * 0.55f)
                    lineTo(center.x + 2.dp.toPx(), height * 0.55f)
                    lineTo(center.x - 3.dp.toPx(), height * 0.78f)
                    lineTo(center.x + 7.dp.toPx(), height * 0.45f)
                    lineTo(center.x - 2.dp.toPx(), height * 0.45f)
                    close()
                }
                drawPath(path = path, color = color)
            }
            "VS" -> {
                // === İki Oyuncu Karşılıklı Rekabet ===
                val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 2.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                val strokeThick = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 2.5.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )

                // --- Sol oyuncu (sola bakan) ---
                val leftCx = width * 0.22f
                val headR = width * 0.09f
                // Kafa
                drawCircle(color = color, center = androidx.compose.ui.geometry.Offset(leftCx, height * 0.25f), radius = headR, style = stroke)
                // Vücut
                val bodyPath1 = androidx.compose.ui.graphics.Path().apply {
                    moveTo(leftCx, height * 0.35f)
                    lineTo(leftCx - width * 0.07f, height * 0.58f)
                    lineTo(leftCx + width * 0.07f, height * 0.58f)
                    close()
                }
                drawPath(path = bodyPath1, color = color.copy(alpha = 0.25f))
                drawPath(path = bodyPath1, color = color, style = stroke)
                // Sol kol (sağa, rakibe doğru uzanıyor)
                drawLine(
                    color = color,
                    start = androidx.compose.ui.geometry.Offset(leftCx, height * 0.42f),
                    end = androidx.compose.ui.geometry.Offset(leftCx + width * 0.13f, height * 0.48f),
                    strokeWidth = 2.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                // Sol bacaklar
                drawLine(color = color, start = androidx.compose.ui.geometry.Offset(leftCx - width * 0.04f, height * 0.58f), end = androidx.compose.ui.geometry.Offset(leftCx - width * 0.06f, height * 0.76f), strokeWidth = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                drawLine(color = color, start = androidx.compose.ui.geometry.Offset(leftCx + width * 0.03f, height * 0.58f), end = androidx.compose.ui.geometry.Offset(leftCx + width * 0.04f, height * 0.76f), strokeWidth = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)

                // --- Sağ oyuncu (sağa bakan, ayna görüntüsü) ---
                val rightCx = width * 0.78f
                // Kafa
                drawCircle(color = color, center = androidx.compose.ui.geometry.Offset(rightCx, height * 0.25f), radius = headR, style = stroke)
                // Vücut
                val bodyPath2 = androidx.compose.ui.graphics.Path().apply {
                    moveTo(rightCx, height * 0.35f)
                    lineTo(rightCx - width * 0.07f, height * 0.58f)
                    lineTo(rightCx + width * 0.07f, height * 0.58f)
                    close()
                }
                drawPath(path = bodyPath2, color = color.copy(alpha = 0.25f))
                drawPath(path = bodyPath2, color = color, style = stroke)
                // Sağ kol (sola, rakibe doğru uzanıyor)
                drawLine(
                    color = color,
                    start = androidx.compose.ui.geometry.Offset(rightCx, height * 0.42f),
                    end = androidx.compose.ui.geometry.Offset(rightCx - width * 0.13f, height * 0.48f),
                    strokeWidth = 2.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                // Sağ bacaklar
                drawLine(color = color, start = androidx.compose.ui.geometry.Offset(rightCx - width * 0.03f, height * 0.58f), end = androidx.compose.ui.geometry.Offset(rightCx - width * 0.04f, height * 0.76f), strokeWidth = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                drawLine(color = color, start = androidx.compose.ui.geometry.Offset(rightCx + width * 0.04f, height * 0.58f), end = androidx.compose.ui.geometry.Offset(rightCx + width * 0.06f, height * 0.76f), strokeWidth = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)

                // --- Orta: Çarpışma / Şimşek ---
                // Parlak çarpışma halkası
                drawCircle(
                    color = color.copy(alpha = 0.18f),
                    center = center,
                    radius = width * 0.14f
                )
                drawCircle(
                    color = color.copy(alpha = 0.5f),
                    center = center,
                    radius = width * 0.14f,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                )
                // Şimşek bolt (orta)
                val boltPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(center.x + 2.5.dp.toPx(), height * 0.30f)
                    lineTo(center.x - 5.dp.toPx(), height * 0.52f)
                    lineTo(center.x + 1.5.dp.toPx(), height * 0.52f)
                    lineTo(center.x - 2.5.dp.toPx(), height * 0.70f)
                    lineTo(center.x + 5.dp.toPx(), height * 0.48f)
                    lineTo(center.x - 1.5.dp.toPx(), height * 0.48f)
                    close()
                }
                drawPath(path = boltPath, color = color)
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ModernGlassButton(
    title: String,
    subtitle: String = "",
    icon: String,
    mainColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    isLocked: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "buttonScale")
    val glowAlpha by animateFloatAsState(if (isPressed) 0.8f else 0.3f, label = "glowAlpha")

    val blitzColors = LocalBlitzMathColors.current

    Card(
        modifier = modifier
            .scale(scale)
            .heightIn(min = if (compact) 90.dp else 110.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.04f)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            mainColor.copy(alpha = 0.6f),
                            Color.White.copy(alpha = 0.1f),
                            mainColor.copy(alpha = 0.6f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(if (compact) 12.dp else 20.dp)
        ) {
            // Glow Effect
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 10.dp, y = (-10).dp)
                    .background(Brush.radialGradient(listOf(mainColor.copy(alpha = glowAlpha), Color.Transparent)))
            )

            if (compact) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isLocked) {
                        Text(text = "🔒", fontSize = 28.sp)
                    } else {
                        GameModeGraphic(mode = icon, color = mainColor, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    AutoSizingText(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon Orb
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(mainColor.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, mainColor.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLocked) {
                            Text(text = "🔒", fontSize = 32.sp)
                        } else {
                            GameModeGraphic(mode = icon, color = mainColor, modifier = Modifier.size(40.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        AutoSizingText(
                            text = title,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            letterSpacing = 1.sp,
                            maxLines = 1
                        )
                        if (subtitle.isNotEmpty()) {
                            Text(
                                text = subtitle,
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                modifier = Modifier.basicMarquee()
                            )
                        }
                    }
                    
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Add,
                        contentDescription = null,
                        tint = mainColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp).rotate(45f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SmallIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.9f else 1f, label = "iconScale")

    Column(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Color.White.copy(alpha = 0.08f), CircleShape)
                .border(1.dp, color.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        AutoSizingText(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun FloatingBottomNavBar(
    onStoreClick: () -> Unit,
    onTasksClick: () -> Unit,
    onPlayClick: () -> Unit,
    onRankingClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "navBarPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "centerButtonPulse"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "centerRingAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(82.dp)
            .drawBehind {
                val barPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, 0f)
                    lineTo(0f, size.height)
                    lineTo(size.width, size.height)
                    lineTo(size.width, 0f)
                    close()
                }
                drawPath(
                    path = barPath,
                    color = androidx.compose.ui.graphics.Color(0xFF0D0D20).copy(alpha = 0.97f)
                )
                // Top shimmer line
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(
                            androidx.compose.ui.graphics.Color.Transparent,
                            androidx.compose.ui.graphics.Color(0xFF00E5FF).copy(alpha = 0.5f),
                            androidx.compose.ui.graphics.Color(0xFF7C3AED).copy(alpha = 0.5f),
                            androidx.compose.ui.graphics.Color.Transparent
                        )
                    ),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                    strokeWidth = 1.5.dp.toPx()
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .align(Alignment.TopCenter)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Store
            NavBarItem(icon = Icons.Default.Store, label = Strings.menuStore, onClick = onStoreClick)

            // Tasks / Görevler
            NavBarItem(icon = Icons.Default.EmojiEvents, label = Strings.menuTasks, onClick = onTasksClick)

            // Center Play Button (Başlat = VS / Online Duel)
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                // Animated outer ring
                Box(
                    modifier = Modifier
                        .size((80 * pulseScale).dp)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFF7C3AED).copy(alpha = pulseAlpha),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )
                // Main button
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(12.dp, CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF7C3AED), Color(0xFF00E5FF))
                            ),
                            CircleShape
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onPlayClick
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    GameModeGraphic(
                        mode = "VS",
                        color = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Ranking / Sıralama
            NavBarItem(icon = Icons.Default.Leaderboard, label = Strings.menuRanking, onClick = onRankingClick)

            // Settings / Ayarlar
            NavBarItem(icon = Icons.Default.Settings, label = Strings.menuSettings, onClick = onSettingsClick)
        }
    }
}

@Composable
private fun NavBarItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.88f else 1f, label = "navItemScale")

    Column(
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun MainMenuTopHUD(
    currentLives: Int,
    timeLeftToRefill: String,
    totalStars: Int,
    playerLevel: Int,
    playerProgress: Float,
    currentXp: Int,
    xpForNextLevel: Int,
    canAffordCard: Boolean = false,
    onCollectionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isHeartsExpanded by remember { mutableStateOf(false) }
    var isXpExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(isHeartsExpanded) {
        if (isHeartsExpanded) { delay(3500); isHeartsExpanded = false }
    }
    LaunchedEffect(isXpExpanded) {
        if (isXpExpanded) { delay(3500); isXpExpanded = false }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- LEFT HUD (XP + HEARTS) ---
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. XP WIDGET
            Row(
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) { isXpExpanded = !isXpExpanded }
                    .animateContentSize(animationSpec = androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessLow))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Bar
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(22.dp)) {
                    androidx.compose.material3.CircularProgressIndicator(
                        progress = { 1f },
                        color = Color.White.copy(alpha = 0.2f),
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                    androidx.compose.material3.CircularProgressIndicator(
                        progress = { playerProgress },
                        color = Color(0xFF00E5FF),
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                    Text(
                        text = "$playerLevel",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                }
                
                if (isXpExpanded) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$currentXp / $xpForNextLevel XP",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // 2. HEARTS WIDGET
            Row(
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) { isHeartsExpanded = !isHeartsExpanded }
                    .animateContentSize(animationSpec = androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessLow))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (isHeartsExpanded) {
                    repeat(5) { index ->
                        val isFilled = index < currentLives
                        val tint = if (isFilled) Color(0xFFE94560) else Color.White.copy(alpha = 0.25f)
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    val tint = if (currentLives > 0) Color(0xFFE94560) else Color.White.copy(alpha = 0.25f)
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$currentLives",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                if (currentLives < 5 && timeLeftToRefill.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refill Time",
                        tint = Color(0xFF00d9ff),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = timeLeftToRefill,
                        color = Color(0xFF00d9ff),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // --- STARS WIDGET (Top Right) ---
        val infiniteTransition = rememberInfiniteTransition(label = "starsPulse")
        val starsScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = if (canAffordCard) 1.15f else 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "starsScaleAnimation"
        )

        Row(
            modifier = Modifier
                .scale(starsScale)
                .shadow(4.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable { onCollectionClick() }
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Stars",
                tint = Color(0xFFFFD700), // Gold Yellow
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$totalStars",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun AutoSizingText(
    text: String,
    color: Color,
    fontWeight: FontWeight,
    fontSize: androidx.compose.ui.unit.TextUnit,
    textAlign: TextAlign = TextAlign.Start,
    letterSpacing: androidx.compose.ui.unit.TextUnit = 0.sp,
    maxLines: Int = 1,
    modifier: Modifier = Modifier
) {
    var scaledFontSize by remember(text) { mutableStateOf(fontSize) }
    var readyToDraw by remember(text) { mutableStateOf(false) }

    Text(
        text = text,
        color = color,
        fontWeight = fontWeight,
        fontSize = scaledFontSize,
        textAlign = textAlign,
        letterSpacing = letterSpacing,
        maxLines = maxLines,
        softWrap = false,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow) {
                if (scaledFontSize.value > 8f) {
                    scaledFontSize = (scaledFontSize.value * 0.9f).sp
                } else {
                    readyToDraw = true
                }
            } else {
                readyToDraw = true
            }
        },
        modifier = modifier.alpha(if (readyToDraw) 1f else 0f)
    )
}

@Composable
fun DailyTasksDialog(
    dataStore: IGameDataStore,
    currentTime: Long,
    onDismiss: () -> Unit,
    platformServices: com.mawelly.blitzmath.core.PlatformServices
) {
    val scope = rememberCoroutineScope()
    val stateStr by dataStore.dailyTasksClaimed.collectAsState(initial = "")
    val starCount by dataStore.starCount.collectAsState(initial = 0)
    
    val state = remember(stateStr, currentTime) {
        DailyTasksManager.parseState(stateStr, currentTime)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF131325),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.border(2.dp, Color(0xFF7C3AED).copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (Strings.currentLanguage == AppLanguage.TURKISH) "GÜNLÜK GÖREVLER" else "DAILY TASKS",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (Strings.currentLanguage == AppLanguage.TURKISH) 
                        "Görevleri tamamla, yıldızları topla! Görevler her gün sıfırlanır." 
                        else "Complete tasks to earn stars! Tasks reset daily.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )

                DailyTasksManager.tasks.forEach { task ->
                    val progress = DailyTasksManager.getTaskProgress(task, state)
                    val isCompleted = DailyTasksManager.isTaskCompleted(task, state)
                    val isClaimed = state.claimedTasks.contains(task.id)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E1E38), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (Strings.currentLanguage == AppLanguage.TURKISH) task.titleTr else task.titleEn,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (Strings.currentLanguage == AppLanguage.TURKISH) task.descTr else task.descEn,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            // Progress bar
                            LinearProgressIndicator(
                                progress = { progress.toFloat() / task.target.toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF00E5FF),
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )
                            
                            Spacer(modifier = Modifier.height(2.dp))
                            
                            Text(
                                text = "$progress / ${task.target}",
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Star",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "+${task.reward}",
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))

                            when {
                                isClaimed -> {
                                    Box(
                                        modifier = Modifier
                                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (Strings.currentLanguage == AppLanguage.TURKISH) "ALINDI" else "CLAIMED",
                                            color = Color.White.copy(alpha = 0.4f),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                isCompleted -> {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                // Claim reward
                                                val newClaimed = state.claimedTasks + task.id
                                                val newState = state.copy(claimedTasks = newClaimed)
                                                dataStore.saveDailyTasksClaimed(DailyTasksManager.serializeState(newState))
                                                dataStore.addStars(task.reward)
                                                platformServices.soundManager.playSuccess()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text(
                                            text = if (Strings.currentLanguage == AppLanguage.TURKISH) "AL" else "CLAIM",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                else -> {
                                    Box(
                                        modifier = Modifier
                                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (Strings.currentLanguage == AppLanguage.TURKISH) "YAPILIYOR" else "IN PROGRESS",
                                            color = Color.White.copy(alpha = 0.4f),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}

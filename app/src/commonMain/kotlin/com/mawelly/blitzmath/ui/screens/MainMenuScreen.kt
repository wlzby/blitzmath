package com.mawelly.blitzmath.ui.screens

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
import androidx.compose.ui.res.painterResource
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

    val canAffordCard = remember(totalStars, unlockedCards) {
        com.mawelly.blitzmath.game.ScientistCards.cards.any { 
            it.price <= totalStars && !unlockedCards.contains(it.id) 
        }
    }

    var currentLives by remember(savedLives) { mutableIntStateOf(savedLives ?: 5) }
    var lastLossTime by remember(savedLossTime) { mutableLongStateOf(savedLossTime ?: 0L) }
    var showNoLivesDialog by remember { mutableStateOf(false) }

    val xpForNextLevel = Math.pow(pLevel.toDouble(), 2.0).toInt() * 100

    // Constant for refill time: 15 minutes
    val REFILL_TIME_MS = 15 * 60 * 1000L
    val MAX_LIVES = 5

    // Countdown timer state
    var timeLeftToRefill by remember { mutableStateOf("") }
    
    var showRewardDialog by remember { mutableStateOf(false) }
    
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
            val now = System.currentTimeMillis()
            lastLossTime = now
            scope.launch {
                dataStore.saveLastLifeLossTime(now)
            }
        }

        while (currentLives < MAX_LIVES && lastLossTime > 0) {
            val now = System.currentTimeMillis()
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
                timeLeftToRefill = String.format("%02d:%02d", minutes, seconds)
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
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {

        val screenWidth = maxWidth
        val screenHeight = maxHeight
        
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
                .align(Alignment.TopCenter)
                .zIndex(10f) // En üst katmanda durması için Z-Index eklendi (tıklamalar çalışacak)
        )

        // Dinamik boyutlandırma hesaplamalarını güvenli hale getirelim
        val screenHeightVal = screenHeight.value
        val screenWidthVal = screenWidth.value
        
        val buttonHeight = (screenHeightVal * 0.08f).coerceIn(55f, 75f).dp
        val spacing = (screenHeightVal * 0.015f).coerceIn(8f, 16f).dp

        val scrollState = rememberScrollState()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = 100.dp, bottom = 120.dp)
        ) {
            // --- Title Section ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

            Spacer(modifier = Modifier.height(30.dp))

            // --- Game Modes Grid (Modern Layout) ---
            val cardWidth = if (screenWidth > 600.dp) 450.dp else screenWidth * 0.85f
            
            // 1. CHALLENGE MODE (Hero Button - Swapped with Classic)
            ModernGlassButton(
                title = Strings.menuChallenge,
                subtitle = if (isChallengeAvailable) Strings.menuChallengeSubtitle else Strings.challengeAlreadyPlayed,
                icon = "⚡",
                mainColor = if (isChallengeAvailable) Color(0xFFFFAB00) else Color.Gray,
                onClick = {
                    if (isChallengeAvailable) {
                        if (currentLives > 0) {
                            analyticsManager.logModeSelection("Challenge")
                            scope.launch {
                                dataStore.saveChallengePlayInfo(actualPlaysToday + 1, todayStr)
                            }
                            onChallengeClick()
                        } else {
                            showNoLivesDialog = true
                        }
                    } else {
                        showChallengeLimitDialog = true
                    }
                },
                modifier = Modifier.width(cardWidth),
                isLocked = !isChallengeAvailable
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 1.5. ÇEVRİMİÇİ DÜELLO (Online VS Mode Hero Button)
            ModernGlassButton(
                title = if (currentLang == AppLanguage.TURKISH) "ÇEVRİMİÇİ DÜELLO" else "ONLINE VS DUEL",
                subtitle = if (currentLang == AppLanguage.TURKISH) "Canlı VS - 2 Dakika Hız Yarışı!" else "Live VS - 2 Minute Speed Race!",
                icon = "⚔️",
                mainColor = Color(0xFF00E5FF),
                onClick = {
                    if (currentLives > 0) {
                        analyticsManager.logModeSelection("VS")
                        onVsClick()
                    } else {
                        showNoLivesDialog = true
                    }
                },
                modifier = Modifier.width(cardWidth)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. MIXED & CLASSIC (Row)
            Row(
                modifier = Modifier.width(cardWidth),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ModernGlassButton(
                    title = Strings.menuMixed,
                    icon = "🎰",
                    mainColor = Color(0xFFD500F9),
                    onClick = {
                        if (currentLives > 0) {
                            analyticsManager.logModeSelection("Mixed")
                            onMixedModeClick()
                        } else {
                            showNoLivesDialog = true
                        }
                    },
                    modifier = Modifier.weight(1f),
                    compact = true
                )

                ModernGlassButton(
                    title = Strings.menuClassic,
                    icon = "📖",
                    mainColor = Color(0xFF00C853),
                    onClick = {
                        if (currentLives > 0) {
                            analyticsManager.logModeSelection("Classic")
                            onPlayClick()
                        } else {
                            showNoLivesDialog = true
                        }
                    },
                    modifier = Modifier.weight(1f),
                    compact = true
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
            
            // --- Collection & Leaderboard ---
            Row(
                modifier = Modifier.width(cardWidth),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SmallIconButton(
                    icon = Icons.Default.EmojiEvents,
                    label = Strings.collection,
                    color = Color(0xFF00E5FF),
                    onClick = onCollectionClick,
                    modifier = Modifier.weight(1f)
                )
                
                SmallIconButton(
                    icon = Icons.Default.Leaderboard,
                    label = Strings.menuLeaderboard,
                    color = Color(0xFFFFD600),
                    onClick = onLeaderboardClick,
                    modifier = Modifier.weight(1f)
                )
                
                SmallIconButton(
                    icon = Icons.Default.Settings,
                    label = Strings.menuSettings,
                    color = Color(0xFFBDBDBD),
                    onClick = onSettingsClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Diğer Oyunlarımız (Sol Alt)
        var isExpanded by remember { mutableStateOf(false) }
        val rotation by animateFloatAsState(
            targetValue = if (isExpanded) 45f else 0f,
            animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing),
            label = "plusRotation"
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 20.dp, start = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = if (isExpanded) 
                                listOf(Color(0xFFE94560), Color(0xFFC0392B)) 
                            else 
                                listOf(Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.05f))
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isExpanded = !isExpanded },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = Strings.otherGames,
                    tint = Color.White,
                    modifier = Modifier
                        .size(30.dp)
                        .rotate(rotation)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(300)) + expandHorizontally(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300)) + shrinkHorizontally(animationSpec = tween(300))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    
                    val infiniteTransition = rememberInfiniteTransition(label = "gamesPulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.08f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulseScale"
                    )

                    // One Top Tower
                    Box(
                        modifier = Modifier
                            .scale(pulseScale)
                            .size(64.dp)
                            .shadow(12.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .clickable { onMoreGamesClick("com.mawelly.onetoptower") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tower",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Oyna İste
                    Box(
                        modifier = Modifier
                            .scale(pulseScale)
                            .size(64.dp)
                            .shadow(12.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .clickable { onMoreGamesClick("com.oyna.iste") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Oyna İste",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
        }
        
        if (showRewardDialog) {
            DailyRewardDialog(currentTime = platformServices.getCurrentTimeMillis(),
                streak = streak,
                lastClaimTime = lastClaimTime,
                onClaim = {
                    scope.launch {
                        val newStreak = DailyRewardManager.calculateNewStreak(streak, lastClaimTime, platformServices.getCurrentTimeMillis())
                        val rewardStars = DailyRewardManager.getStarReward(newStreak)
                        dataStore.saveDailyReward(newStreak, System.currentTimeMillis(), rewardStars)
                        showRewardDialog = false
                    }
                },
                onDismiss = { showRewardDialog = false }
            )
        }

        var showExitDialog by remember { mutableStateOf(false) }

        // Çıkış (Sağ Alt)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp, end = 20.dp)
                .size(56.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.05f))
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                .clickable {
                    showExitDialog = true
                    onPromptVoice?.invoke(Strings.randomExitVoicePrompt, 1.15f, true)
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Exit",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(30.dp)
            )
        }
        
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
                            isExpanded = true // Aç diğer oyunları
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
                            onExitClick()
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
            .height(if (compact) 100.dp else 120.dp)
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
                    Text(text = if (isLocked) "🔒" else icon, fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
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
                        Text(text = if (isLocked) "🔒" else icon, fontSize = 32.sp)
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
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
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
    ) {
        // --- LEFT HUD (XP + HEARTS) ---
        Row(
            modifier = Modifier.align(Alignment.TopStart),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                .align(Alignment.TopEnd)
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

package com.mawelly.blitzmath

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mawelly.blitzmath.core.LocalPlatformServices
import com.mawelly.blitzmath.data.IGameDataStore
import com.mawelly.blitzmath.data.AppTheme
import com.mawelly.blitzmath.localization.Strings
import com.mawelly.blitzmath.localization.AppLanguage
import com.mawelly.blitzmath.ui.screens.*
import com.mawelly.blitzmath.ui.theme.BlitzMathTheme
import com.mawelly.blitzmath.ui.theme.LocalBlitzMathColors
import com.mawelly.blitzmath.ui.components.FloatingSymbolsBackground
import org.jetbrains.compose.resources.painterResource
import blitzmath.app.generated.resources.Res
import blitzmath.app.generated.resources.blitzmath_logo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

enum class AppScreen {
    SPLASH,
    LANGUAGE_SELECTION,
    MAIN_MENU,
    GAME_CLASSIC,
    GAME_MIXED,
    SETTINGS,
    GLOBAL_LEADERBOARD,
    COLLECTION,
    GAME_CHALLENGE,
    VS_SCREEN
}

@Composable
fun App(dataStore: IGameDataStore) {
    val scope = rememberCoroutineScope()
    val platformServices = LocalPlatformServices.current
    
    val savedTheme by dataStore.theme.collectAsState(initial = AppTheme.MIDNIGHT)
    val starCount by dataStore.starCount.collectAsState(initial = 100)
    
    var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }
    var currentLevel by remember { mutableStateOf(1) }
    
    var leaderboardInitialMode by remember { mutableStateOf("classic") }
    var leaderboardScrollToId by remember { mutableStateOf<String?>(null) }
    
    BlitzMathTheme(themeType = savedTheme) {
        val blitzColors = LocalBlitzMathColors.current
        
        Box(modifier = Modifier.fillMaxSize()) {
            // Background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(blitzColors.backgroundGradient))
            )
            
            if (currentScreen == AppScreen.SPLASH || 
                currentScreen == AppScreen.MAIN_MENU || 
                currentScreen == AppScreen.LANGUAGE_SELECTION ||
                currentScreen == AppScreen.SETTINGS ||
                currentScreen == AppScreen.GLOBAL_LEADERBOARD ||
                currentScreen == AppScreen.COLLECTION) {
                
                FloatingSymbolsBackground(symbolCount = if (currentScreen == AppScreen.SPLASH) 15 else 30)
            }
            
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(600, easing = LinearOutSlowInEasing)) + 
                     scaleIn(initialScale = 0.92f, animationSpec = tween(600, easing = LinearOutSlowInEasing)))
                        .togetherWith(fadeOut(animationSpec = tween(500, easing = FastOutLinearInEasing)) +
                                     scaleOut(targetScale = 1.08f, animationSpec = tween(500, easing = FastOutLinearInEasing)))
                },
                modifier = Modifier.fillMaxSize(),
                label = "screen_transition"
            ) { targetScreen ->
                when (targetScreen) {
                    AppScreen.SPLASH -> {
                        IosSplashContent(onFinish = {
                            currentScreen = AppScreen.MAIN_MENU
                        })
                    }
                    AppScreen.LANGUAGE_SELECTION -> {
                        LanguageSelectionScreen(
                            dataStore = dataStore,
                            onLanguageSelected = {
                                currentScreen = AppScreen.MAIN_MENU
                            }
                        )
                    }
                    AppScreen.MAIN_MENU -> {
                        val unlockedCards by dataStore.unlockedCards.collectAsState(initial = emptySet())
                        val equippedCards by dataStore.equippedCards.collectAsState(initial = emptySet())
                        val playerXp by dataStore.playerXp.collectAsState(initial = 0)
                        
                        // Calculate Level
                        val calculatedLevel = Math.floor(Math.sqrt(playerXp / 100.0)).toInt() + 1
                        val level = calculatedLevel.coerceIn(1, 100)
                        
                        // Calculate Progress
                        val xpForCurrentLevel = Math.pow((level - 1).toDouble(), 2.0).toInt() * 100
                        val xpForNextLevel = Math.pow(level.toDouble(), 2.0).toInt() * 100
                        val progress = if (level >= 100) 1f else ((playerXp - xpForCurrentLevel).toFloat() / (xpForNextLevel - xpForCurrentLevel).toFloat()).coerceIn(0f, 1f)
                        
                        MainMenuScreen(
                            dataStore = dataStore,
                            onPlayClick = {
                                currentLevel = 1
                                currentScreen = AppScreen.GAME_CLASSIC
                            },
                            onMixedModeClick = {
                                currentLevel = 1
                                currentScreen = AppScreen.GAME_MIXED
                            },
                            onChallengeClick = {
                                currentScreen = AppScreen.GAME_CHALLENGE
                            },
                            onVsClick = {
                                currentScreen = AppScreen.VS_SCREEN
                            },
                            onSettingsClick = { currentScreen = AppScreen.SETTINGS },
                            onLeaderboardClick = {
                                leaderboardInitialMode = "classic"
                                leaderboardScrollToId = null
                                currentScreen = AppScreen.GLOBAL_LEADERBOARD
                            },
                            onCollectionClick = { currentScreen = AppScreen.COLLECTION },
                            onMoreGamesClick = {
                                platformServices.openUrl("https://play.google.com/store")
                            },
                            onExitClick = {
                                // No-op on iOS
                            },
                            onPromptVoice = { _, _, _ ->
                                // Optional voice synthesis
                            },
                            platformServices = platformServices,
                            pLevel = level,
                            pProgress = progress,
                            currentXp = playerXp
                        )
                    }
                    AppScreen.GAME_CLASSIC, AppScreen.GAME_MIXED, AppScreen.GAME_CHALLENGE -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                                Text(
                                    text = "Game Mode is coming soon to iOS!",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { currentScreen = AppScreen.MAIN_MENU },
                                    colors = ButtonDefaults.buttonColors(containerColor = LocalBlitzMathColors.current.accent)
                                ) {
                                    Text("Back to Menu", color = Color.White)
                                }
                            }
                        }
                    }
                    AppScreen.SETTINGS -> {
                        SettingsScreen(
                            dataStore = dataStore,
                            onBackToMenu = {
                                currentScreen = AppScreen.MAIN_MENU
                            }
                        )
                    }
                    AppScreen.GLOBAL_LEADERBOARD -> {
                        GlobalLeaderboardScreen(
                            initialMode = leaderboardInitialMode,
                            scrollToPlayerId = leaderboardScrollToId,
                            onBackToMenu = { currentScreen = AppScreen.MAIN_MENU }
                        )
                    }
                    AppScreen.COLLECTION -> {
                        val unlockedCards by dataStore.unlockedCards.collectAsState(initial = emptySet())
                        val equippedCards by dataStore.equippedCards.collectAsState(initial = emptySet())
                        val cardCharges by dataStore.cardCharges.collectAsState(initial = emptyMap())
                        val cardLastUseTimes by dataStore.cardLastUseTime.collectAsState(initial = emptyMap())
                        
                        CollectionScreen(
                            unlockedCardIds = unlockedCards,
                            equippedCardIds = equippedCards,
                            starCount = starCount,
                            cardCharges = cardCharges,
                            cardLastUseTimes = cardLastUseTimes,
                            onBuyCard = { cardId, price ->
                                scope.launch {
                                    if (dataStore.spendStars(price)) {
                                        dataStore.unlockCard(cardId)
                                    }
                                }
                            },
                            onToggleEquip = { cardId ->
                                scope.launch {
                                    dataStore.toggleEquipCard(cardId)
                                }
                            },
                            onBack = { currentScreen = AppScreen.MAIN_MENU }
                        )
                    }
                    AppScreen.VS_SCREEN -> {
                        VsScreen(
                            onBackToMenu = { currentScreen = AppScreen.MAIN_MENU }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IosSplashContent(onFinish: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(3000, easing = LinearEasing)),
        label = "rotation"
    )

    var visible by remember { mutableStateOf(false) }
    
    val growthScale by animateFloatAsState(
        targetValue = if (visible) 1.30f else 1.0f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "growth"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(animation = tween(1500), repeatMode = RepeatMode.Reverse),
        label = "pulse"
    )
    
    LaunchedEffect(Unit) {
        visible = true
        delay(1800)
        onFinish()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1200, delayMillis = 300))
            ) {
                Box(modifier = Modifier.size(250.dp), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .graphicsLayer {
                                scaleX = pulseScale
                                scaleY = pulseScale
                            }
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFF00d9ff).copy(alpha = 0.15f), Color.Transparent)
                                ),
                                CircleShape
                            )
                    )

                    Box(
                        modifier = Modifier
                            .size(175.dp)
                            .graphicsLayer {
                                scaleX = growthScale
                                scaleY = growthScale
                                rotationZ = rotation
                                alpha = (2f - pulseScale).coerceIn(0f, 1f)
                            }
                            .border(
                                width = 3.dp,
                                brush = Brush.linearGradient(
                                    listOf(Color(0xFF00d9ff), Color(0xFFe94560))
                                ),
                                shape = CircleShape
                            )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(108.dp)
                    .graphicsLayer {
                        scaleX = growthScale
                        scaleY = growthScale
                    }
                    .shadow(15.dp, CircleShape)
                    .background(Color(0xFF1a1a2e), CircleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(Res.drawable.blitzmath_logo),
                    contentDescription = "Blitz Math Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1500, delayMillis = 800)),
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "BLITZ YOUR BRAIN",
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 4.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        modifier = Modifier
                            .width(100.dp)
                            .height(2.dp)
                            .clip(CircleShape),
                        color = Color(0xFF00d9ff),
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}

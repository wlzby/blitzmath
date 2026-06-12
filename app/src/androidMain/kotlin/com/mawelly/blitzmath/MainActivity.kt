package com.mawelly.blitzmath
import com.mawelly.blitzmath.core.LocalPlatformServices
import com.mawelly.blitzmath.leaderboard.ILeaderboardManager
import androidx.compose.runtime.CompositionLocalProvider

import android.os.Bundle
import android.content.Intent
import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.mawelly.blitzmath.ads.IAdManager
import com.mawelly.blitzmath.ads.AdsProvider
import com.mawelly.blitzmath.ads.ConsentManager
import com.mawelly.blitzmath.audio.SoundManager
import com.mawelly.blitzmath.audio.VoiceManager
import com.mawelly.blitzmath.data.GameDataStore
import com.mawelly.blitzmath.game.GameMode
import com.mawelly.blitzmath.localization.Strings
import com.mawelly.blitzmath.localization.AppLanguage
import com.mawelly.blitzmath.ui.GameScreen
import com.mawelly.blitzmath.ui.screens.GlobalLeaderboardScreen
import com.mawelly.blitzmath.ui.screens.LanguageSelectionScreen
import com.mawelly.blitzmath.ui.screens.MainMenuScreen
import com.mawelly.blitzmath.ui.screens.SettingsScreen
import com.mawelly.blitzmath.ui.theme.BlitzMathTheme
import com.mawelly.blitzmath.analytics.AnalyticsManager
import com.mawelly.blitzmath.core.AndroidPlatformServices
import com.mawelly.blitzmath.ui.dialogs.UpdateDialog
import com.google.firebase.firestore.FirebaseFirestore
import android.net.Uri
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.work.*
import com.mawelly.blitzmath.data.AppTheme
import com.mawelly.blitzmath.notifications.EngagementWorker
import java.util.concurrent.TimeUnit
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.Text
import androidx.compose.material3.LinearProgressIndicator
import com.mawelly.blitzmath.ui.components.FloatingSymbolsBackground
import com.mawelly.blitzmath.ui.theme.LocalBlitzMathColors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Sistem Splash ekranını yükle (Logosuz, sadece arka plan)
        val splashScreen = try {
            installSplashScreen()
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
        
        try {
            super.onCreate(savedInstanceState)

            // GDPR Rıza Formu Kontrolü - Güvenli hale getirildi
            try {
                ConsentManager(this).gatherConsent(object : ConsentManager.ConsentCallback {
                    override fun onConsentFinished() {
                        // Rıza işlemi tamamlandı
                    }
                })
            } catch (t: Throwable) {
                t.printStackTrace()
            }
            
            // 2. Sistem ekranının kapanma anını kontrol et
            splashScreen?.setOnExitAnimationListener { splashProvider ->
                splashProvider.remove()
            }

            // TAM EKRAN MODU
            try {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val controller = WindowInsetsControllerCompat(window, window.decorView)
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } catch (t: Throwable) {
                t.printStackTrace()
            }

            setContent {
                BlitzMathApp()
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            // Kritik hata durumunda bile uygulamayı kapatma
            // En azından siyah ekran yerine bir hata mesajı veya boş tema gösterilebilir
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    override fun onResume() {
        super.onResume()
        // Uygulama açıldığında bekleyen bildirimi iptal et
        WorkManager.getInstance(this).cancelUniqueWork("engagement_work")
    }

    override fun onStop() {
        super.onStop()
        
        val workManager = WorkManager.getInstance(this)
        
        // İlk bildirim: 12 saat sonra
        val workRequest12h = OneTimeWorkRequestBuilder<EngagementWorker>()
            .setInitialDelay(12, TimeUnit.HOURS)
            .build()
            
        workManager.enqueueUniqueWork(
            "engagement_work_12h",
            ExistingWorkPolicy.REPLACE,
            workRequest12h
        )
        
        // İkinci bildirim: 24 saat sonra
        val workRequest24h = OneTimeWorkRequestBuilder<EngagementWorker>()
            .setInitialDelay(24, TimeUnit.HOURS)
            .build()
            
        workManager.enqueueUniqueWork(
            "engagement_work_24h",
            ExistingWorkPolicy.REPLACE,
            workRequest24h
        )

        // Sıralama Kontrolü: Her 1 saatte bir
        val rankCheckRequest = PeriodicWorkRequestBuilder<com.mawelly.blitzmath.notifications.RankCheckWorker>(
            1, TimeUnit.HOURS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "rank_check_work",
            ExistingPeriodicWorkPolicy.KEEP, // Mevcut olanı bozma, devam etsin
            rankCheckRequest
        )
    }
}

enum class Screen {
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
fun BlitzMathApp() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Ses Yönetimi: Uygulama arka plana atıldığında müziği duraklat/devam ettir
    val dataStore = remember { GameDataStore(context) }
    val soundManager = remember { SoundManager(context) }
    val voiceManager = remember { VoiceManager(context) }
    val leaderboardManager = remember { com.mawelly.blitzmath.leaderboard.LeaderboardManager() }
    
    // Ses Ayarları ve Seslendirme Dili Senkronizasyonu
    LaunchedEffect(Strings.currentLanguage) {
        voiceManager.setLanguage(Strings.currentLanguage)
    }
    
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    soundManager.pauseBGM()
                    voiceManager.stop()
                }
                Lifecycle.Event.ON_RESUME -> soundManager.resumeBGM()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            soundManager.release()
            voiceManager.release()
        }
    }
    
    // Bildirim izni iste (Android 13+)
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val activity = context as? ComponentActivity
            if (activity != null && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    fun getAutoTheme(): AppTheme {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 6..9 -> AppTheme.GLACIER
            in 10..17 -> AppTheme.FOREST
            in 18..20 -> AppTheme.SUNSET
            else -> AppTheme.MIDNIGHT
        }
    }

    val languageManager = remember { LanguageManager(context) }
    val adMobManager = remember { AdsProvider.getInstance(context) }
    val scope = rememberCoroutineScope()
    val isAutoThemeEnabled by dataStore.autoTheme.collectAsState(initial = false)
    val savedTheme by dataStore.theme.collectAsState(initial = AppTheme.MIDNIGHT)
    
    val currentTheme = if (isAutoThemeEnabled) getAutoTheme() else savedTheme

    var currentLang by remember { mutableStateOf(Strings.currentLanguage) }
    val isFirstLaunch = remember { languageManager.isFirstLaunch() }

    var analyticsManager by remember { mutableStateOf<AnalyticsManager?>(null) }
    
    // Initialize PlatformServices
    val platformServices = remember(adMobManager, analyticsManager, soundManager) {
        AndroidPlatformServices(
            activity = context as android.app.Activity,
            context = context,
            adManager = adMobManager,
            analyticsManager = analyticsManager ?: AnalyticsManager.getInstance(context),
            soundManager = soundManager
        )
    }

    // YENİ: Reklamı önceden yükle - Güvenli hale getirildi
    LaunchedEffect(Unit) {
        try {
            delay(2500) // Biraz daha bekle
            adMobManager.preloadAll()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    LaunchedEffect(Unit) {
        analyticsManager = AnalyticsManager.getInstance(context)
        if (!isFirstLaunch) {
            languageManager.loadSavedLanguage()
            currentLang = Strings.currentLanguage
        }
    }

    var currentScreen by remember {
        mutableStateOf(Screen.SPLASH)
    }

    var leaderboardInitialMode by remember { mutableStateOf("classic") }
    var leaderboardScrollToId by remember { mutableStateOf<String?>(null) }

    var currentLevel by remember { mutableIntStateOf(1) }

    LaunchedEffect(Unit) {
        // FCM Token Kaydı - Sadece GMS varsa
        try {
            if (com.mawelly.blitzmath.utils.ServiceChecker.isGmsAvailable(context)) {
                com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                    if (token != null) {
                        val playerId = languageManager.getPlayerId()
                        if (playerId.isNotEmpty()) {
                            scope.launch {
                                try {
                                    val lbm = com.mawelly.blitzmath.leaderboard.LeaderboardManager()
                                    lbm.updateFcmToken(playerId, token)
                                } catch (e: Exception) { e.printStackTrace() }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showRanking(mode: String, scrollToPlayer: Boolean = false) {
        leaderboardInitialMode = mode.lowercase()
        leaderboardScrollToId = if (scrollToPlayer) languageManager.getPlayerId() else null
        currentScreen = Screen.GLOBAL_LEADERBOARD
    }

    // --- FORCED UPDATE CHECK ---
    var showUpdateDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        try {
            if (!com.mawelly.blitzmath.utils.ServiceChecker.isGmsAvailable(context)) {
                android.util.Log.d("UpdateCheck", "Skipping update check (GMS not available)")
                return@LaunchedEffect
            }
            
            android.util.Log.d("UpdateCheck", "Starting Firestore check...")
            val db = FirebaseFirestore.getInstance()
            db.collection("config").document("app_version")
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        android.util.Log.d("UpdateCheck", "Document data: ${document.data}")
                        // Daha esnek veri çekme (hem sayı hem string destekler)
                        val minVersion = when (val v = document.get("min_version")) {
                            is Long -> v
                            is Int -> v.toLong()
                            is String -> v.toLongOrNull() ?: 0L
                            is Double -> v.toLong()
                            else -> {
                                android.util.Log.d("UpdateCheck", "Unknown type for min_version: ${v?.javaClass}")
                                0L
                            }
                        }
                        
                        val currentVersion = com.mawelly.blitzmath.BuildConfig.VERSION_CODE.toLong()
                        
                        android.util.Log.d("UpdateCheck", "Final Comparison -> min_version: $minVersion, App currentVersion: $currentVersion")
                        
                        if (currentVersion < minVersion) {
                            android.util.Log.d("UpdateCheck", "SHOWING DIALOG")
                            showUpdateDialog = true
                        }
                    }
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("UpdateCheck", "Firestore check failed: ${e.message}")
                }
        } catch (e: Exception) {
            android.util.Log.e("UpdateCheck", "Exception in LaunchedEffect: ${e.message}")
        }
    }

    // Ekran Takibi (Analytics)
    LaunchedEffect(currentScreen, analyticsManager) {
        analyticsManager?.logScreenView(currentScreen.name)
    }

    LaunchedEffect(currentScreen) {
        if (currentScreen == Screen.MAIN_MENU) {
            // Dil ayarlarını arka planda kontrol et, UI'ı bloke etme
            try {
                languageManager.loadSavedLanguage()
                currentLang = Strings.currentLanguage
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    CompositionLocalProvider(LocalPlatformServices provides platformServices) {
        BlitzMathTheme(themeType = currentTheme) {
        val blitzColors = LocalBlitzMathColors.current
        
        Box(modifier = Modifier.fillMaxSize()) {
            // GLOBAL BACKGROUND (Static across menu transitions)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(blitzColors.backgroundGradient))
            )
            
            // Only show floating symbols for specific screens
            if (currentScreen == Screen.SPLASH || 
                currentScreen == Screen.MAIN_MENU || 
                currentScreen == Screen.LANGUAGE_SELECTION ||
                currentScreen == Screen.SETTINGS ||
                currentScreen == Screen.GLOBAL_LEADERBOARD ||
                currentScreen == Screen.COLLECTION) {
                
                FloatingSymbolsBackground(symbolCount = if (currentScreen == Screen.SPLASH) 15 else 30)
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
            ) { targetScreen: Screen ->
            when (targetScreen) {
                Screen.SPLASH -> {
                    SplashContent(onFinish = {
                        currentScreen = if (isFirstLaunch) Screen.LANGUAGE_SELECTION else Screen.MAIN_MENU
                    })
                }
                Screen.LANGUAGE_SELECTION -> {
                    LanguageSelectionScreen(dataStore = dataStore,
                        onLanguageSelected = {
                            scope.launch {
                                dataStore.saveLanguage(Strings.currentLanguage)
                            }
                            currentScreen = Screen.MAIN_MENU
                        }
                    )
                }
                Screen.MAIN_MENU -> {
                    MainMenuScreen(
                        dataStore = dataStore,
                        onPlayClick = {
                            analyticsManager?.logModeSelection("CLASSIC")
                            currentLevel = 1
                            currentScreen = Screen.GAME_CLASSIC
                        },
                        onMixedModeClick = {
                            analyticsManager?.logModeSelection("MIXED")
                            currentLevel = 1
                            currentScreen = Screen.GAME_MIXED
                        },
                        onChallengeClick = {
                            analyticsManager?.logModeSelection("CHALLENGE")
                            scope.launch {
                                dataStore.saveLastChallengePlayTime(System.currentTimeMillis())
                            }
                            currentScreen = Screen.GAME_CHALLENGE
                        },
                        onVsClick = {
                            currentScreen = Screen.VS_SCREEN
                        },
                        onSettingsClick = { currentScreen = Screen.SETTINGS },
                        onLeaderboardClick = { showRanking("classic", false) },
                        onCollectionClick = { currentScreen = Screen.COLLECTION },
                        onMoreGamesClick = { packageId ->
                            analyticsManager?.logMoreGamesClick(packageId)
                            try {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("market://details?id=$packageId")
                                    setPackage("com.android.vending")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageId&hl=tr"))
                                context.startActivity(browserIntent)
                            }
                        },
                        onExitClick = {
                            (context as? ComponentActivity)?.finish()
                        },
                        onPromptVoice = { message, multiplier, isProfessional ->
                            voiceManager.speak(message, multiplier, isProfessional)
                        },
                        platformServices = platformServices,
                        pLevel = languageManager.getPlayerLevel(),
                        pProgress = languageManager.getPlayerLevelProgress(),
                        currentXp = languageManager.getPlayerXP()
                    )
                }
                Screen.GAME_CLASSIC -> {
                    GameScreen(
                        mode = GameMode.CLASSIC,
                        startLevel = currentLevel,
                        dataStore = dataStore,
                        voiceManager = voiceManager,
                        leaderboardManager = leaderboardManager,
                        onLevelComplete = { nextLevel ->
                            currentLevel = nextLevel
                        },
                        onBackToMenu = {
                            soundManager.stopBGM()
                            currentScreen = Screen.MAIN_MENU
                        },
                        onShowRanking = { mode -> showRanking(mode, true) }
                    )
                }
                Screen.GAME_MIXED -> {
                    GameScreen(
                        mode = GameMode.MIXED,
                        startLevel = currentLevel,
                        dataStore = dataStore,
                        voiceManager = voiceManager,
                        leaderboardManager = leaderboardManager,
                        onLevelComplete = { nextLevel ->
                            currentLevel = nextLevel
                        },
                        onBackToMenu = {
                            soundManager.stopBGM()
                            currentScreen = Screen.MAIN_MENU
                        },
                        onShowRanking = { mode -> showRanking(mode, true) }
                    )
                }
                Screen.GAME_CHALLENGE -> {
                    GameScreen(
                        mode = GameMode.CHALLENGE,
                        startLevel = 1,
                        dataStore = dataStore,
                        voiceManager = voiceManager,
                        leaderboardManager = leaderboardManager,
                        onLevelComplete = { },
                        onBackToMenu = {
                            soundManager.stopBGM()
                            currentScreen = Screen.MAIN_MENU
                        },
                        onShowRanking = { mode -> showRanking(mode, true) }
                    )
                }
                Screen.SETTINGS -> {
                    SettingsScreen(dataStore = dataStore,
                        onBackToMenu = {
                            scope.launch {
                                val savedLang = dataStore.language.first()
                                if (savedLang != Strings.currentLanguage) {
                                    Strings.setLanguage(savedLang)
                                    currentLang = savedLang
                                }
                            }
                            currentScreen = Screen.MAIN_MENU
                        }
                    )
                }
                Screen.GLOBAL_LEADERBOARD -> {
                    GlobalLeaderboardScreen(
                        dataStore = dataStore,
                        initialMode = leaderboardInitialMode,
                        scrollToPlayerId = leaderboardScrollToId,
                        onBackToMenu = { currentScreen = Screen.MAIN_MENU }
                    )
                }
                Screen.COLLECTION -> {
                    val unlockedCards by dataStore.unlockedCards.collectAsState(initial = emptySet())
                    val equippedCards by dataStore.equippedCards.collectAsState(initial = emptySet())
                    val starCount by dataStore.starCount.collectAsState(initial = 0)
                    val cardCharges by dataStore.cardCharges.collectAsState(initial = emptyMap())
                    val cardLastUseTimes by dataStore.cardLastUseTime.collectAsState(initial = emptyMap())
                    
                    com.mawelly.blitzmath.ui.screens.CollectionScreen(
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
                        onBack = { currentScreen = Screen.MAIN_MENU }
                    )
                }
                Screen.VS_SCREEN -> {
                    com.mawelly.blitzmath.ui.screens.VsScreen(
                        
                        
                        onBackToMenu = { currentScreen = Screen.MAIN_MENU }
                    )
                }
            }
        }
    }

    // Show forced update dialog if needed - ALWAYS ON THE TOP OF EVERYTHING
        if (showUpdateDialog) {
            UpdateDialog()
        }
        }
    }
}

@Composable
fun SplashContent(onFinish: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    
    // Dönüş animasyonu
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(3000, easing = LinearEasing)),
        label = "rotation"
    )

    // Başlangıç tetikleyici
    var visible by remember { mutableStateOf(false) }
    
    // Açılış Büyüme (Growth) Animasyonu: %30 Büyüme sağlar
    val growthScale by animateFloatAsState(
        targetValue = if (visible) 1.30f else 1.0f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "growth"
    )

    // Nefes alma (Pulse) animasyonu
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(animation = tween(1500), repeatMode = RepeatMode.Reverse),
        label = "pulse"
    )
    LaunchedEffect(Unit) {
        visible = true
        delay(1800) // Splash ekranı süresi (Daha seri)
        onFinish()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // MERKEZİ ÜST ÜSTE BİNDİRİLMİŞ KATMANLAR
        Box(contentAlignment = Alignment.Center) {
            // HALKALAR VE PARILTI: Uygulama açılınca sonradan belirir (Fade-in)
            androidx.compose.animation.AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1200, delayMillis = 300))
            ) {
                Box(modifier = Modifier.size(250.dp), contentAlignment = Alignment.Center) {
                    // Arkadaki Neon Parıltı (Glow)
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

                    // Neon Halka (Pürüzsüz ve Tam Yuvarlak)
                    Box(
                        modifier = Modifier
                            .size(175.dp)
                            .graphicsLayer {
                                scaleX = growthScale
                                scaleY = growthScale
                                rotationZ = rotation
                                alpha = (2f - pulseScale).coerceIn(0f, 1f) // Hafif parlama efekti, çökme koruması
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

            // LOGO: Sistemden devralınır (108dp) ve büyüme efektine katılır
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
                Image(
                    painter = painterResource(id = R.drawable.blitzmath_logo),
                    contentDescription = "Blitz Math Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // SLOGAN VE ALT METİN (Logonun merkezini bozmamak için Column dışında, ayrı konumlandırıldı)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            androidx.compose.animation.AnimatedVisibility(
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

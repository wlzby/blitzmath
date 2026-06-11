package com.mawelly.blitzmath.ui.screens

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.mawelly.blitzmath.LanguageManager
import com.mawelly.blitzmath.R
import com.mawelly.blitzmath.audio.SoundManager
import com.mawelly.blitzmath.game.CheckpointConfig
import com.mawelly.blitzmath.game.DifficultyLevel
import com.mawelly.blitzmath.game.OperationType
import com.mawelly.blitzmath.game.Question
import com.mawelly.blitzmath.localization.AppLanguage
import com.mawelly.blitzmath.localization.Strings
import com.mawelly.blitzmath.ui.components.FloatingSymbolsBackground
import com.mawelly.blitzmath.ui.components.SuccessConfetti
import com.mawelly.blitzmath.ui.theme.LocalBlitzMathColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.random.Random

enum class VsState {
    ENTER_NAME,
    MATCHMAKING,
    MATCHED,
    PLAYING,
    GAME_OVER
}

enum class RematchState {
    NONE,
    REQUESTED_BY_ME,
    REQUESTED_BY_OPPONENT,
    ACCEPTED
}

fun getFlagEmojiForCountryCode(countryCode: String): String {
    if (countryCode.length != 2) return "❓"
    val firstLetter = Character.codePointAt(countryCode.uppercase(java.util.Locale.ROOT), 0) - 0x41 + 0x1F1E6
    val secondLetter = Character.codePointAt(countryCode.uppercase(java.util.Locale.ROOT), 1) - 0x41 + 0x1F1E6
    return try { String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter)) } catch (e: Exception) { "❓" }
}

@Composable
actual fun VsScreen(onBackToMenu: () -> Unit) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    
    val soundManager = androidx.compose.runtime.remember { com.mawelly.blitzmath.audio.SoundManager(context) }
    val languageManager = androidx.compose.runtime.remember { com.mawelly.blitzmath.LanguageManager(context) }
    val scope = rememberCoroutineScope()
    
    var currentState by remember { mutableStateOf(VsState.ENTER_NAME) }
    var rematchState by remember { mutableStateOf(RematchState.NONE) }
    var playerName by remember { mutableStateOf(languageManager.getPlayerName()) }
    var inputName by remember { mutableStateOf(playerName) }
    var myPlayerId by remember { mutableStateOf(languageManager.getPlayerId()) }
    var myLevel by remember { mutableIntStateOf(languageManager.getPlayerLevel()) }
    
    var activeLobbyId by remember { mutableStateOf("") }
    var myRole by remember { mutableIntStateOf(1) } // 1: Player 1, 2: Player 2
    
    var myCountryCode by remember { mutableStateOf(java.util.Locale.getDefault().country.takeIf { it.isNotBlank() } ?: "US") }
    var opponentCountryCode by remember { mutableStateOf("") }
    
    var opponentName by remember { mutableStateOf("") }
    var opponentLevel by remember { mutableIntStateOf(1) }
    var matchLevel by remember { mutableIntStateOf(1) }
    var opponentScore by remember { mutableIntStateOf(0) }
    var myScore by remember { mutableIntStateOf(0) }
    
    var currentQuestionLocalIndex by remember { mutableIntStateOf(0) }
    var consecutiveMistakes by remember { mutableIntStateOf(0) }
    var currentQuestion by remember { mutableStateOf<Question?>(null) }
    
    var gameSeed by remember { mutableLongStateOf(0L) }
    var gameStartTimestamp by remember { mutableLongStateOf(0L) }
    var gameDurationSeconds by remember { mutableLongStateOf(120L) }
    var secondsLeft by remember { mutableLongStateOf(120L) }
    
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var feedbackType by remember { mutableStateOf(AnswerFeedbackType.NONE) }
    var buttonLockedUntil by remember { mutableLongStateOf(0L) }
    var showOpponentFeedback by remember { mutableStateOf(false) }
    
    var listenerRegistration by remember { mutableStateOf<ListenerRegistration?>(null) }
    
    // NEW: Bot Mode variables
    var isBotMode by remember { mutableStateOf(false) }
    
    // NEW: Quick Chat (Emote) variables
    var myActiveEmote by remember { mutableStateOf<String?>(null) }
    var opponentActiveEmote by remember { mutableStateOf<String?>(null) }
    
    // Auto-hide emotes after 3.5 seconds
    LaunchedEffect(myActiveEmote) {
        if (myActiveEmote != null) {
            delay(3500)
            myActiveEmote = null
        }
    }
    LaunchedEffect(opponentActiveEmote) {
        if (opponentActiveEmote != null) {
            delay(3500)
            opponentActiveEmote = null
        }
    }
    
    // XP Reward on Game Over
    LaunchedEffect(currentState) {
        if (currentState == VsState.GAME_OVER) {
            languageManager.addPlayerXP(myScore)
        }
    }
    
    val blitzColors = LocalBlitzMathColors.current
    val currentLang = Strings.currentLanguage
    
    // Add bot wrong tracker to skip question if both player and bot answer incorrectly
    var botWrongIndex by remember { mutableIntStateOf(-1) }
    
    // Per-question timer state
    var questionStartTime by remember { mutableLongStateOf(0L) }
    var questionTimeLeftMs by remember { mutableLongStateOf(5000L) }
    
    // Auto-enter matchmaking if name already exists
    LaunchedEffect(Unit) {
        if (playerName.isNotEmpty()) {
            currentState = VsState.MATCHMAKING
            startMatchmaking(
                db = db,
                myPlayerId = myPlayerId,
                myPlayerName = playerName,
                myLevel = myLevel,
                myCountryCode = myCountryCode,
                onMatched = { lobbyId, role, seed, startTime, oppName, oppLevel, oppCountry ->
                    activeLobbyId = lobbyId
                    myRole = role
                    gameSeed = seed
                    gameStartTimestamp = startTime
                    opponentName = oppName
                    opponentLevel = oppLevel
                    opponentCountryCode = oppCountry
                    matchLevel = ((myLevel + oppLevel) / 2).coerceAtLeast(1)
                    currentState = VsState.MATCHED
                },
                onLobbyIdAssigned = { activeLobbyId = it }
            )
        }
    }

    // 15-second Matchmaking Timeout to trigger Bot Mode
    LaunchedEffect(currentState) {
        if (currentState == VsState.MATCHMAKING) {
            isBotMode = false
            delay(15000)
            if (currentState == VsState.MATCHMAKING) {
                // 15 seconds have passed without finding a human player!
                isBotMode = true
                
                // Stop Firestore listner and delete our waiting lobby
                listenerRegistration?.remove()
                if (activeLobbyId.isNotEmpty()) {
                    db.collection("vs_lobbies").document(activeLobbyId).delete()
                }
                activeLobbyId = "bot_lobby"
                
                // Pick a realistic human-looking Turkish/Universal name
                val fakeNames = listOf(
                    "Kerem_92", "Alex_Math", "Sakura", "JohnDoe", "Mert_06", 
                    "Elena_K", "BurakY", "SpeedyMath", "David_M", "NinjaBrain",
                    "MathGenius", "Elif_Math", "Chen_Wei", "Oliver99", "Zehra_T",
                    "Sophie_M", "Brainiac", "Luiz_F", "Yuki_99", "FastFingers"
                )
                val fakeCountries = listOf("US", "GB", "DE", "FR", "TR", "JP", "BR", "CA", "AU", "KR", "ES", "IT", "RU", "AZ")
                opponentCountryCode = fakeCountries.random()
                opponentName = fakeNames.random()
                opponentLevel = (myLevel + Random.nextInt(-3, 4)).coerceAtLeast(1)
                matchLevel = ((myLevel + opponentLevel) / 2).coerceAtLeast(1)
                gameSeed = Random.nextLong()
                gameStartTimestamp = System.currentTimeMillis() + 3500L
                currentState = VsState.MATCHED
            }
        }
    }

    // BOT BEHAVIOR SIMULATOR (Local Coroutine representing a human-like opponent)
    if (isBotMode) {
        // Bot responds to player's emotes!
        LaunchedEffect(myActiveEmote) {
            if (myActiveEmote != null) {
                delay(Random.nextLong(1200L, 2500L))
                
                val goodLuckStr = Strings.vsEmoteGoodLuck
                val fastStr = Strings.vsEmoteTooFast
                val oopsStr = Strings.vsEmoteOops
                val hurryStr = Strings.vsEmoteHurryUp
                val thanksStr = Strings.vsEmoteThanks
                val goodGameStr = Strings.vsEmoteGoodGame
                
                when (myActiveEmote) {
                    goodLuckStr -> opponentActiveEmote = thanksStr
                    fastStr -> opponentActiveEmote = listOf(thanksStr, goodGameStr).random()
                    oopsStr -> opponentActiveEmote = fastStr
                    hurryStr -> opponentActiveEmote = oopsStr
                    thanksStr -> opponentActiveEmote = goodGameStr
                    goodGameStr -> opponentActiveEmote = goodGameStr
                }
            }
        }
    }

    LaunchedEffect(currentState, isBotMode, currentQuestionLocalIndex) {
        if (isBotMode) {
            val goodLuckStr = Strings.vsEmoteGoodLuck
            val fastStr = Strings.vsEmoteTooFast
            val oopsStr = Strings.vsEmoteOops
            val hurryStr = Strings.vsEmoteHurryUp
            val thanksStr = Strings.vsEmoteThanks
            val goodGameStr = Strings.vsEmoteGoodGame

            if (currentState == VsState.MATCHED && currentQuestionLocalIndex == 0) {
                // Bot greets before game starts
                delay(1200)
                if (Random.nextFloat() < 0.75f) { // 75% chance to greet
                    opponentActiveEmote = goodLuckStr 
                }
            }
            else if (currentState == VsState.PLAYING && currentQuestion != null) {
                val question = currentQuestion ?: return@LaunchedEffect
                
                // Calculate a human-like solve delay based on math operation difficulty
                val opText = question.displayText
                val isHard = opText.contains("×") || opText.contains("÷")
                
                // Bot gets stressed/rushes if losing, slowing down if winning
                val scoreDiff = opponentScore - myScore
                
                var baseTimeMs = if (isHard) 3200L else 1800L
                if (scoreDiff < -20) baseTimeMs -= 400L // Rushing when losing
                if (scoreDiff > 20) baseTimeMs += 300L  // Relaxing when winning
                
                // Add randomness to represent human speed variability
                val speedFactor = (currentQuestionLocalIndex * 30L).coerceAtMost(600L)
                val randomOffset = Random.nextLong(-400L, 1000L) - speedFactor
                
                val finalDelay = (baseTimeMs + randomOffset).coerceIn(900L, 4800L)
                
                delay(finalDelay)
                
                // Check that the user hasn't answered yet!
                if (currentState == VsState.PLAYING && selectedOptionIndex == null) {
                    
                    // Dynamic Accuracy: Struggles on hard questions and when panicking
                    var accuracyChance = 85
                    if (isHard) accuracyChance -= 15
                    if (scoreDiff < -20) accuracyChance -= 12 // Panic lowers accuracy!
                    if (scoreDiff > 20) accuracyChance += 5   // Confidence improves accuracy
                    
                    val isCorrect = Random.nextInt(0, 100) < accuracyChance
                    
                    if (isCorrect) {
                        opponentScore += 10
                        soundManager.playWrong() // Inform player that rival was faster!
                        showOpponentFeedback = true
                        
                        // Small chance to taunt if winning
                        if (scoreDiff > 10 && Random.nextFloat() < 0.18f) {
                            opponentActiveEmote = listOf(hurryStr, fastStr, goodGameStr).random()
                        }
                        
                        scope.launch {
                            delay(1200)
                            showOpponentFeedback = false
                        }
                        
                        // Advance locally to the next question
                        currentQuestionLocalIndex += 1
                        currentQuestion = generateDeterministicQuestion(currentQuestionLocalIndex, gameSeed, matchLevel)
                        selectedOptionIndex = null
                        feedbackType = AnswerFeedbackType.NONE
                        buttonLockedUntil = System.currentTimeMillis() + 500L
                        botWrongIndex = -1 // Reset
                    } else {
                        // Bot answered wrong!
                        if (Random.nextFloat() < 0.40f) { // 40% chance to emote when making a mistake
                            opponentActiveEmote = listOf(oopsStr, hurryStr).random()
                        }
                        
                        botWrongIndex = currentQuestionLocalIndex
                        
                        if (buttonLockedUntil == Long.MAX_VALUE) {
                            // Both human and bot answered wrong! Skip!
                            scope.launch {
                                delay(600) // Small delay to let user see both failed
                                currentQuestionLocalIndex += 1
                                currentQuestion = generateDeterministicQuestion(currentQuestionLocalIndex, gameSeed, matchLevel, consecutiveMistakes)
                                selectedOptionIndex = null
                                feedbackType = AnswerFeedbackType.NONE
                                buttonLockedUntil = System.currentTimeMillis() + 500L
                                botWrongIndex = -1
                            }
                        }
                    }
                }
            }
        }
    }

    // Realtime game session observer (Only runs in multiplayer mode)
    LaunchedEffect(activeLobbyId, currentState) {
        if (!isBotMode && activeLobbyId.isNotEmpty() && activeLobbyId != "bot_lobby" && (currentState == VsState.MATCHED || currentState == VsState.PLAYING)) {
            val lobbyRef = db.collection("vs_lobbies").document(activeLobbyId)
            
            listenerRegistration?.remove()
            val registration = lobbyRef.addSnapshotListener { snapshot, error ->
                if (snapshot != null && snapshot.exists()) {
                    val status = snapshot.getString("status") ?: "waiting"
                    val p1Score = snapshot.getLong("player1Score") ?: 0L
                    val p2Score = snapshot.getLong("player2Score") ?: 0L
                    val dbIndex = snapshot.getLong("currentQuestionIndex") ?: 0L
                    val lastAnswerer = snapshot.getString("lastAnswererId") ?: ""
                    
                    val p1Name = snapshot.getString("player1Name") ?: ""
                    val p2Name = snapshot.getString("player2Name") ?: ""
                    
                    val p1EmoteRaw = snapshot.getString("p1Emote")
                    val p2EmoteRaw = snapshot.getString("p2Emote")
                    
                    fun parseEmote(raw: String?, setEmote: (String?) -> Unit) {
                        raw?.split("|")?.let { parts ->
                            if (parts.size == 2) {
                                val timestamp = parts[1].toLongOrNull() ?: 0L
                                if (System.currentTimeMillis() - timestamp < 4000L) {
                                    setEmote(parts[0])
                                }
                            }
                        }
                    }
                    
                    if (myRole == 1) {
                        opponentName = p2Name
                        opponentScore = p2Score.toInt()
                        myScore = p1Score.toInt()
                        parseEmote(p2EmoteRaw) { opponentActiveEmote = it }
                    } else {
                        opponentName = p1Name
                        opponentScore = p1Score.toInt()
                        myScore = p2Score.toInt()
                        parseEmote(p1EmoteRaw) { opponentActiveEmote = it }
                    }
                    
                    if (status == "finished") {
                        currentState = VsState.GAME_OVER
                    }
                    
                    // Question index changed (claimed by someone!)
                    if (dbIndex.toInt() > currentQuestionLocalIndex) {
                        // Play SFX & show flash if opponent answered first
                        if (lastAnswerer.isNotEmpty() && lastAnswerer != myPlayerId) {
                            soundManager.playWrong()
                            showOpponentFeedback = true
                            scope.launch {
                                delay(1000)
                                showOpponentFeedback = false
                            }
                        } else if (lastAnswerer == myPlayerId) {
                            soundManager.playCorrect()
                        }
                        
                        currentQuestionLocalIndex = dbIndex.toInt()
                        currentQuestion = generateDeterministicQuestion(currentQuestionLocalIndex, gameSeed, matchLevel, consecutiveMistakes)
                        selectedOptionIndex = null
                        feedbackType = AnswerFeedbackType.NONE
                        buttonLockedUntil = System.currentTimeMillis() + 500L
                    }
                }
            }
            listenerRegistration = registration
        }
    }
    
    // Countdown and Timer logic
    LaunchedEffect(currentState, gameStartTimestamp) {
        if (currentState == VsState.MATCHED && gameStartTimestamp > 0L) {
            val waitTime = gameStartTimestamp - System.currentTimeMillis()
            if (waitTime > 0) {
                delay(waitTime)
            }
            soundManager.playLevelUp()
            currentState = VsState.PLAYING
            currentQuestionLocalIndex = 0
            consecutiveMistakes = 0
            currentQuestion = generateDeterministicQuestion(0, gameSeed, matchLevel, consecutiveMistakes)
            myScore = 0
            opponentScore = 0
        }
        
        if (currentState == VsState.PLAYING) {
            while (currentState == VsState.PLAYING) {
                val elapsed = (System.currentTimeMillis() - gameStartTimestamp) / 1000L
                val left = (120L - elapsed).coerceAtLeast(0L)
                secondsLeft = left
                
                // KNOCKOUT CHECK (Tug of War)
                val scoreDiff = myScore - opponentScore
                if (scoreDiff >= 100 || scoreDiff <= -100) {
                    currentState = VsState.GAME_OVER
                    if (!isBotMode && activeLobbyId.isNotEmpty() && activeLobbyId != "bot_lobby") {
                        db.collection("vs_lobbies").document(activeLobbyId).update("status", "finished")
                    }
                    soundManager.playGameOver()
                    break
                }
                
                if (left <= 0L) {
                    currentState = VsState.GAME_OVER
                    if (!isBotMode && activeLobbyId.isNotEmpty() && activeLobbyId != "bot_lobby") {
                        db.collection("vs_lobbies").document(activeLobbyId).update("status", "finished")
                    }
                    soundManager.playGameOver()
                    break
                }
                delay(200)
            }
        }
    }
    
    // 5-Second Per-Question Timeout logic
    LaunchedEffect(currentQuestionLocalIndex, currentState) {
        if (currentState == VsState.PLAYING) {
            questionStartTime = System.currentTimeMillis()
            while (true) {
                val elapsed = System.currentTimeMillis() - questionStartTime
                val left = (5000L - elapsed).coerceAtLeast(0L)
                questionTimeLeftMs = left
                
                if (left <= 0L) {
                    // Timeout! Skip question automatically.
                    soundManager.playWrong() // Optional timeout feedback
                    
                    if (isBotMode) {
                        currentQuestionLocalIndex += 1
                        currentQuestion = generateDeterministicQuestion(currentQuestionLocalIndex, gameSeed, matchLevel)
                        selectedOptionIndex = null
                        feedbackType = AnswerFeedbackType.NONE
                        buttonLockedUntil = System.currentTimeMillis() + 500L
                        botWrongIndex = -1
                        questionStartTime = System.currentTimeMillis()
                    } else {
                        // In multiplayer, both clients can attempt to advance safely via transaction
                        val lobbyRef = db.collection("vs_lobbies").document(activeLobbyId)
                        db.runTransaction { transaction ->
                            val snap = transaction.get(lobbyRef)
                            val dbIndex = snap.getLong("currentQuestionIndex") ?: 0L
                            if (dbIndex.toInt() == currentQuestionLocalIndex) {
                                transaction.update(lobbyRef, "currentQuestionIndex", dbIndex + 1)
                            }
                            null
                        }
                    }
                    break
                }
                delay(30)
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            listenerRegistration?.remove()
            if (activeLobbyId.isNotEmpty()) {
                db.collection("vs_lobbies").document(activeLobbyId).get()
                    .addOnSuccessListener { snap ->
                        if (snap.exists() && snap.getString("status") == "waiting") {
                            db.collection("vs_lobbies").document(activeLobbyId).delete()
                        }
                    }
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(blitzColors.backgroundGradient))
    ) {
        FloatingSymbolsBackground(symbolCount = 20)
        
        when (currentState) {
            VsState.ENTER_NAME -> {
                EnterNameView(
                    inputName = inputName,
                    onNameChange = { inputName = it },
                    onSubmit = {
                        if (inputName.trim().isNotEmpty()) {
                            languageManager.savePlayerName(inputName.trim())
                            playerName = languageManager.getPlayerName()
                            myPlayerId = languageManager.getPlayerId()
                            currentState = VsState.MATCHMAKING
                            startMatchmaking(
                                db = db,
                                myPlayerId = myPlayerId,
                                myPlayerName = playerName,
                                myLevel = myLevel,
                                myCountryCode = myCountryCode,
                                onMatched = { lobbyId, role, seed, startTime, oppName, oppLevel, oppCountry ->
                                    activeLobbyId = lobbyId
                                    myRole = role
                                    gameSeed = seed
                                    gameStartTimestamp = startTime
                                    opponentName = oppName
                                    opponentLevel = oppLevel
                                    opponentCountryCode = oppCountry
                                    currentState = VsState.MATCHED
                                },
                                onLobbyIdAssigned = { activeLobbyId = it }
                            )
                        }
                    },
                    onBack = onBackToMenu,
                    currentLang = currentLang
                )
            }
            VsState.MATCHMAKING -> {
                MatchmakingRadarView(
                    playerName = playerName,
                    onCancel = {
                        listenerRegistration?.remove()
                        if (activeLobbyId.isNotEmpty()) {
                            db.collection("vs_lobbies").document(activeLobbyId).delete()
                        }
                        onBackToMenu()
                    },
                    currentLang = currentLang
                )
            }
            VsState.MATCHED -> {
                MatchedBannerView(
                    playerName = playerName,
                    opponentName = opponentName,
                    myCountryCode = myCountryCode,
                    opponentCountryCode = opponentCountryCode,
                    gameStartTimestamp = gameStartTimestamp,
                    currentLang = currentLang
                )
            }
            VsState.PLAYING -> {
                currentQuestion?.let { question ->
                    VsGameplayView(
                        question = question,
                        localIndex = currentQuestionLocalIndex,
                        myScore = myScore,
                        opponentScore = opponentScore,
                        opponentName = opponentName,
                        myLevel = myLevel,
                        opponentLevel = opponentLevel,
                        myCountryCode = myCountryCode,
                        opponentCountryCode = opponentCountryCode,
                        secondsLeftProvider = { secondsLeft },
                        questionTimeLeftMsProvider = { questionTimeLeftMs },
                        selectedOptionIndex = selectedOptionIndex,
                        feedbackType = feedbackType,
                        showOpponentFeedback = showOpponentFeedback,
                        buttonLockedUntil = buttonLockedUntil,
                        myActiveEmote = myActiveEmote,
                        opponentActiveEmote = opponentActiveEmote,
                        onSendEmote = { emoteText ->
                            val payload = "$emoteText|${System.currentTimeMillis()}"
                            myActiveEmote = emoteText // Locally show immediately
                            if (!isBotMode && activeLobbyId.isNotEmpty() && activeLobbyId != "bot_lobby") {
                                val field = if (myRole == 1) "p1Emote" else "p2Emote"
                                db.collection("vs_lobbies").document(activeLobbyId).update(field, payload)
                            }
                        },
                        onOptionSelected = { index, option ->
                            val now = System.currentTimeMillis()
                            if (selectedOptionIndex == null && now > buttonLockedUntil) {
                                selectedOptionIndex = index
                                if (option == question.correctAnswer) {
                                    feedbackType = AnswerFeedbackType.CORRECT
                                    
                                    if (isBotMode) {
                                        myScore += 10
                                        soundManager.playCorrect()
                                        scope.launch {
                                            delay(150)
                                            currentQuestionLocalIndex += 1
                                            currentQuestion = generateDeterministicQuestion(currentQuestionLocalIndex, gameSeed, matchLevel, consecutiveMistakes)
                                            selectedOptionIndex = null
                                            feedbackType = AnswerFeedbackType.NONE
                                            buttonLockedUntil = System.currentTimeMillis() + 500L
                                        }
                                    } else {
                                        // Claim point in Firestore!
                                        val lobbyRef = db.collection("vs_lobbies").document(activeLobbyId)
                                        db.runTransaction { transaction ->
                                            val snap = transaction.get(lobbyRef)
                                            val dbIndex = snap.getLong("currentQuestionIndex") ?: 0L
                                            if (dbIndex.toInt() == currentQuestionLocalIndex) {
                                                val scoreField = if (myRole == 1) "player1Score" else "player2Score"
                                                val currentScore = snap.getLong(scoreField) ?: 0L
                                                
                                                transaction.update(
                                                    lobbyRef, mapOf(
                                                        "currentQuestionIndex" to dbIndex + 1,
                                                        scoreField to currentScore + 10,
                                                        "lastAnswererId" to myPlayerId
                                                    )
                                                )
                                                true
                                            } else {
                                                false // Opponent already got it!
                                            }
                                        }
                                    }
                                } else {
                                    feedbackType = AnswerFeedbackType.WRONG
                                    soundManager.playWrong()
                                    buttonLockedUntil = Long.MAX_VALUE // 1-chance lock!
                                    
                                    if (isBotMode) {
                                        if (botWrongIndex == currentQuestionLocalIndex) {
                                            // Both bot and human got it wrong! Skip!
                                            scope.launch {
                                                delay(450)
                                                currentQuestionLocalIndex += 1
                                                currentQuestion = generateDeterministicQuestion(currentQuestionLocalIndex, gameSeed, matchLevel)
                                                selectedOptionIndex = null
                                                feedbackType = AnswerFeedbackType.NONE
                                                buttonLockedUntil = System.currentTimeMillis() + 500L
                                                botWrongIndex = -1
                                            }
                                        }
                                    } else {
                                        val lobbyRef = db.collection("vs_lobbies").document(activeLobbyId)
                                        db.runTransaction { transaction ->
                                            val snap = transaction.get(lobbyRef)
                                            val dbIndex = snap.getLong("currentQuestionIndex") ?: 0L
                                            if (dbIndex.toInt() == currentQuestionLocalIndex) {
                                                val myWrongField = if (myRole == 1) "player1WrongIndex" else "player2WrongIndex"
                                                val oppWrongField = if (myRole == 1) "player2WrongIndex" else "player1WrongIndex"
                                                
                                                val oppWrongIndex = snap.getLong(oppWrongField) ?: -1L
                                                
                                                if (oppWrongIndex == dbIndex) {
                                                    // Both got it wrong! Skip question!
                                                    transaction.update(
                                                        lobbyRef, mapOf(
                                                            myWrongField to dbIndex,
                                                            "currentQuestionIndex" to dbIndex + 1
                                                        )
                                                    )
                                                } else {
                                                    // Only I got it wrong so far
                                                    transaction.update(lobbyRef, myWrongField, dbIndex)
                                                }
                                            }
                                            null
                                        }
                                    }
                                }
                            }
                        },
                        onQuit = {
                            if (!isBotMode && activeLobbyId.isNotEmpty() && activeLobbyId != "bot_lobby") {
                                db.collection("vs_lobbies").document(activeLobbyId).update("status", "finished")
                            }
                            currentState = VsState.GAME_OVER
                        },
                        currentLang = currentLang
                    )
                }
            }
            VsState.GAME_OVER -> {
                LaunchedEffect(Unit) {
                    val xpGained = myScore
                    languageManager.addPlayerXP(xpGained)
                }
                
                LaunchedEffect(activeLobbyId) {
                    if (isBotMode || activeLobbyId.isEmpty()) return@LaunchedEffect
                    
                    val listener = db.collection("vs_lobbies").document(activeLobbyId)
                        .addSnapshotListener { snapshot, e ->
                            if (e != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                            val p1Rematch = snapshot.getBoolean("player1Rematch") ?: false
                            val p2Rematch = snapshot.getBoolean("player2Rematch") ?: false
                            val newStatus = snapshot.getString("status") ?: ""
                            
                            val myRematch = if (myRole == 1) p1Rematch else p2Rematch
                            val oppRematch = if (myRole == 1) p2Rematch else p1Rematch
                            
                            if (myRematch && oppRematch && newStatus == "active") {
                                rematchState = RematchState.ACCEPTED
                                myScore = 0
                                opponentScore = 0
                                currentQuestionLocalIndex = 0
                                currentQuestion = null
                                gameStartTimestamp = snapshot.getLong("gameStartTimestamp") ?: (System.currentTimeMillis() + 3500)
                                currentState = VsState.MATCHED
                                rematchState = RematchState.NONE
                            } else if (myRematch && !oppRematch) {
                                rematchState = RematchState.REQUESTED_BY_ME
                            } else if (!myRematch && oppRematch) {
                                rematchState = RematchState.REQUESTED_BY_OPPONENT
                            }
                        }
                }

                VsGameOverView(
                    myScore = myScore,
                    opponentScore = opponentScore,
                    opponentName = opponentName,
                    rematchState = rematchState,
                    onRematchRequested = {
                        if (isBotMode) {
                            myScore = 0
                            opponentScore = 0
                            currentQuestionLocalIndex = 0
                            currentQuestion = null
                            gameStartTimestamp = System.currentTimeMillis() + 3500
                            currentState = VsState.MATCHED
                            rematchState = RematchState.NONE
                        } else if (activeLobbyId.isNotEmpty()) {
                            val updateField = if (myRole == 1) "player1Rematch" else "player2Rematch"
                            db.collection("vs_lobbies").document(activeLobbyId).update(updateField, true)
                        }
                    },
                    onRematchAccepted = {
                        if (activeLobbyId.isNotEmpty()) {
                            val updateField = if (myRole == 1) "player1Rematch" else "player2Rematch"
                            db.runTransaction { transaction ->
                                val docRef = db.collection("vs_lobbies").document(activeLobbyId)
                                val snapshot = transaction.get(docRef)
                                val p1Rematch = if (myRole == 1) true else snapshot.getBoolean("player1Rematch") ?: false
                                val p2Rematch = if (myRole == 2) true else snapshot.getBoolean("player2Rematch") ?: false
                                
                                transaction.update(docRef, updateField, true)
                                
                                if (p1Rematch && p2Rematch) {
                                    transaction.update(docRef, "status", "active")
                                    transaction.update(docRef, "player1Score", 0)
                                    transaction.update(docRef, "player2Score", 0)
                                    transaction.update(docRef, "gameStartTimestamp", System.currentTimeMillis() + 3500)
                                }
                            }
                        }
                    },
                    onPlayAgain = {
                        currentState = VsState.MATCHMAKING
                        myScore = 0
                        opponentScore = 0
                        currentQuestionLocalIndex = 0
                        activeLobbyId = ""
                        currentQuestion = null
                        isBotMode = false
                        botWrongIndex = -1
                        rematchState = RematchState.NONE
                        startMatchmaking(
                            db = db,
                            myPlayerId = myPlayerId,
                            myPlayerName = playerName,
                            myLevel = myLevel,
                            myCountryCode = myCountryCode,
                            onMatched = { lobbyId, role, seed, startTime, oppName, oppLevel, oppCountry ->
                                activeLobbyId = lobbyId
                                myRole = role
                                gameSeed = seed
                                gameStartTimestamp = startTime
                                opponentName = oppName
                                opponentLevel = oppLevel
                                opponentCountryCode = oppCountry
                                currentState = VsState.MATCHED
                            },
                            onLobbyIdAssigned = { activeLobbyId = it }
                        )
                    },
                    onMenu = onBackToMenu,
                    currentLang = currentLang
                )
            }
        }
    }
}

// MATCHMAKING LOUNGE ORCHESTRATOR
private fun startMatchmaking(
    db: FirebaseFirestore,
    myPlayerId: String,
    myPlayerName: String,
    myLevel: Int,
    myCountryCode: String,
    onMatched: (lobbyId: String, role: Int, seed: Long, startTime: Long, opponentName: String, opponentLevel: Int, opponentCountry: String) -> Unit,
    onLobbyIdAssigned: (String) -> Unit
) {
    db.collection("vs_lobbies")
        .whereEqualTo("status", "waiting")
        .limit(15) // Fetch up to 15 waiting lobbies to find a level match
        .get()
        .addOnSuccessListener { snapshot ->
            if (!snapshot.isEmpty) {
                // Find a lobby within +/- 10 levels
                val suitableDoc = snapshot.documents.firstOrNull { doc ->
                    val p1Level = doc.getLong("player1Level")?.toInt() ?: 1
                    Math.abs(p1Level - myLevel) <= 10
                }

                if (suitableDoc != null) {
                    val lobbyId = suitableDoc.id
                    val lobbyRef = db.collection("vs_lobbies").document(lobbyId)
                    
                    db.runTransaction { transaction ->
                        val freshSnap = transaction.get(lobbyRef)
                        val status = freshSnap.getString("status") ?: "waiting"
                        
                        if (status == "waiting") {
                            val seed = freshSnap.getLong("seed") ?: 0L
                            val p1Name = freshSnap.getString("player1Name") ?: ""
                            val p1Level = freshSnap.getLong("player1Level")?.toInt() ?: 1
                            val p1Country = freshSnap.getString("player1Country") ?: "US"
                            val startTime = System.currentTimeMillis() + 3500L
                            
                            transaction.update(
                                lobbyRef, mapOf(
                                    "player2Id" to myPlayerId,
                                    "player2Name" to myPlayerName,
                                    "player2Level" to myLevel,
                                    "player2Country" to myCountryCode,
                                    "status" to "active",
                                    "gameStartTimestamp" to startTime
                                )
                            )
                            
                            // Return bundle to main block
                            mapOf(
                                "lobbyId" to lobbyId,
                                "role" to 2,
                                "seed" to seed,
                                "startTime" to startTime,
                                "opponentName" to p1Name,
                                "opponentLevel" to p1Level,
                                "opponentCountry" to p1Country
                            )
                        } else {
                            throw Exception("Lobby closed")
                        }
                    }.addOnSuccessListener { result ->
                        @Suppress("UNCHECKED_CAST")
                        val bundle = result as Map<String, Any>
                        onLobbyIdAssigned(bundle["lobbyId"] as String)
                        onMatched(
                            bundle["lobbyId"] as String,
                            bundle["role"] as Int,
                            bundle["seed"] as Long,
                            bundle["startTime"] as Long,
                            bundle["opponentName"] as String,
                            bundle["opponentLevel"] as Int,
                            bundle["opponentCountry"] as String
                        )
                    }.addOnFailureListener {
                        // Try to create own lobby if match failed
                        createLobby(db, myPlayerId, myPlayerName, myLevel, myCountryCode, onMatched, onLobbyIdAssigned)
                    }
                } else {
                    // No suitable level match found
                    createLobby(db, myPlayerId, myPlayerName, myLevel, myCountryCode, onMatched, onLobbyIdAssigned)
                }
            } else {
                createLobby(db, myPlayerId, myPlayerName, myLevel, myCountryCode, onMatched, onLobbyIdAssigned)
            }
        }
        .addOnFailureListener {
            createLobby(db, myPlayerId, myPlayerName, myLevel, myCountryCode, onMatched, onLobbyIdAssigned)
        }
}

private fun createLobby(
    db: FirebaseFirestore,
    myPlayerId: String,
    myPlayerName: String,
    myLevel: Int,
    myCountryCode: String,
    onMatched: (lobbyId: String, role: Int, seed: Long, startTime: Long, opponentName: String, opponentLevel: Int, opponentCountry: String) -> Unit,
    onLobbyIdAssigned: (String) -> Unit
) {
    val lobbyId = UUID.randomUUID().toString().take(8)
    val seed = Random.nextLong()
    onLobbyIdAssigned(lobbyId)
    
    val newLobby = hashMapOf(
        "lobbyId" to lobbyId,
        "player1Id" to myPlayerId,
        "player1Name" to myPlayerName,
        "player1Level" to myLevel,
        "player1Country" to myCountryCode,
        "player1Score" to 0L,
        "player2Id" to "",
        "player2Name" to "",
        "player2Level" to 0,
        "player2Country" to "",
        "player2Score" to 0L,
        "status" to "waiting",
        "seed" to seed,
        "currentQuestionIndex" to 0L,
        "lastAnswererId" to "",
        "gameStartTimestamp" to 0L,
        "createdAt" to java.util.Date()
    )
    
    db.collection("vs_lobbies").document(lobbyId)
        .set(newLobby)
        .addOnSuccessListener {
            // Listen for opponent to join
            val lobbyRef = db.collection("vs_lobbies").document(lobbyId)
            var registration: ListenerRegistration? = null
            registration = lobbyRef.addSnapshotListener { snapshot, error ->
                if (snapshot != null && snapshot.exists()) {
                    val status = snapshot.getString("status") ?: "waiting"
                    val p2Name = snapshot.getString("player2Name") ?: ""
                    val p2Country = snapshot.getString("player2Country") ?: "US"
                    val startTime = snapshot.getLong("gameStartTimestamp") ?: 0L
                    
                    if (status == "active" && p2Name.isNotEmpty() && startTime > 0) {
                        val p2Level = snapshot.getLong("player2Level")?.toInt() ?: 1
                        registration?.remove()
                        onMatched(lobbyId, 1, seed, startTime, p2Name, p2Level, p2Country)
                    }
                }
            }
        }
}

// DETERMINISTIC SYNCED MATH ENGINE
private fun generateDeterministicQuestion(index: Int, seed: Long, baseLevel: Int, mistakes: Int = 0): Question {
    // Yanlış yaptıkça zorluğu düşüren akıllı sistem (Her 2 yanlışta -15 level, min 1)
    val penalty = (mistakes / 2) * 15
    val level = (baseLevel - penalty).coerceIn(1, 100)

    val op = when ((index + level) % 4) {
        0 -> OperationType.ADDITION
        1 -> OperationType.SUBTRACTION
        2 -> OperationType.MULTIPLICATION
        3 -> OperationType.DIVISION
        else -> OperationType.ADDITION
    }
    
    // Akıllı ve pürüzsüz zorluk eğrisi (Smooth Scaling)
    val addMin: Int
    val addMax: Int
    val multMin: Int
    val multMax: Int

    when {
        level <= 10 -> { // Çaylak
            addMin = 1
            addMax = 15
            multMin = 1
            multMax = 5
        }
        level <= 30 -> { // Gelişmekte
            addMin = 15
            addMax = 50
            multMin = 3
            multMax = 9
        }
        level <= 70 -> { // Deneyimli
            addMin = 50
            addMax = 150
            multMin = 6
            multMax = 15
        }
        else -> { // Usta (Level 71-100)
            addMin = 100
            addMax = 500
            multMin = 10
            multMax = 25
        }
    }
    
    // Index, oyun uzadıkça sayılara minik tatlı bir ekleme yapar ancak seviyeyi bozmaz
    val range = when (op) {
        OperationType.ADDITION -> (addMin + index / 2)..(addMax + index)
        OperationType.SUBTRACTION -> (addMin + index / 2)..(addMax + index)
        OperationType.MULTIPLICATION -> multMin..multMax
        OperationType.DIVISION -> multMin.coerceAtLeast(2)..multMax
        else -> addMin..addMax
    }
    
    val config = CheckpointConfig(
        checkpointNumber = (index / 10) + 1,
        questionInCheckpoint = (index % 10) + 1,
        operationType = op,
        difficulty = DifficultyLevel.NORMAL,
        timeLimit = 5f,
        numberRange = range
    )
    
    val rand = Random(seed + index + level)
    
    return when (op) {
        OperationType.ADDITION -> {
            val a = rand.nextInt(range.first, range.last + 1)
            val b = rand.nextInt(range.first, range.last + 1)
            createVsQuestion(text = "$a + $b", correct = a + b, config = config, rand = rand)
        }
        OperationType.SUBTRACTION -> {
            val a = rand.nextInt(range.first, range.last + 1)
            val b = rand.nextInt(range.first, a.coerceAtLeast(range.first + 1))
            createVsQuestion(text = "$a - $b", correct = a - b, config = config, rand = rand)
        }
        OperationType.MULTIPLICATION -> {
            val a = rand.nextInt(range.first, range.last + 1)
            val b = rand.nextInt(range.first, range.last + 1)
            createVsQuestion(text = "$a × $b", correct = a * b, config = config, rand = rand)
        }
        OperationType.DIVISION -> {
            val b = rand.nextInt(range.first.coerceAtLeast(2), range.last + 1)
            val correct = rand.nextInt(range.first.coerceAtLeast(2), range.last + 1)
            val a = b * correct
            createVsQuestion(text = "$a ÷ $b", correct = correct, config = config, rand = rand)
        }
        else -> Question("1 + 1", 2, listOf(1, 2, 3, 4), "")
    }
}

private fun createVsQuestion(
    text: String,
    correct: Int,
    config: CheckpointConfig,
    rand: Random
): Question {
    val options = mutableListOf(correct)
    
    val (minOffset, maxOffset) = when {
        correct <= 20 -> 2 to 8
        correct <= 50 -> 5 to 15
        else -> 10 to 30
    }
    
    var attempts = 0
    while (options.size < 3 && attempts < 100) {
        val offset = if (rand.nextBoolean()) rand.nextInt(minOffset, maxOffset + 1) else -rand.nextInt(minOffset, maxOffset + 1)
        val wrong = correct + offset
        if (wrong > 0 && wrong != correct && wrong !in options) {
            options.add(wrong)
        }
        attempts++
    }
    
    while (options.size < 3) {
        val backup = correct + rand.nextInt(minOffset, maxOffset * 2)
        if (backup !in options) options.add(backup)
    }
    
    // Synced deterministic shuffle!
    val shuffled = options.toList().sortedBy { rand.nextDouble() }
    
    return Question(
        displayText = text,
        correctAnswer = correct,
        options = shuffled,
        hint = ""
    )
}

enum class AnswerFeedbackType { NONE, CORRECT, WRONG }

// --- UI SCREENS AND VIEWS ---

@Composable
fun EnterNameView(
    inputName: String,
    onNameChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    currentLang: AppLanguage
) {
    val titleText = Strings.vsTitleStartDuel
    val labelText = Strings.vsLabelPlayerName
    val placeholderText = Strings.vsPlaceholderUsername
    val buttonText = Strings.vsBtnConnect
    
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier
                .width(340.dp)
                .padding(16.dp)
                .border(2.dp, Color(0xFF00E5FF), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16162a).copy(alpha = 0.9f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = titleText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = labelText,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = inputName,
                    onValueChange = onNameChange,
                    placeholder = { Text(placeholderText, color = Color.White.copy(alpha = 0.4f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        cursorColor = Color(0xFF00E5FF)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(28.dp))
                
                Button(
                    onClick = onSubmit,
                    enabled = inputName.trim().isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E5FF),
                        disabledContainerColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text(
                        text = buttonText,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                TextButton(onClick = onBack) {
                    Text(
                        text = if (currentLang == AppLanguage.TURKISH) "VAZGEÇ" else "CANCEL",
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun MatchmakingRadarView(
    playerName: String,
    onCancel: () -> Unit,
    currentLang: AppLanguage
) {
    val radarTransition = rememberInfiniteTransition(label = "radarScale")
    val pulse1 by radarTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "pulse1"
    )
    val pulse2 by radarTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 1000, easing = LinearEasing)
        ),
        label = "pulse2"
    )
    val rotation by radarTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing)
        ),
        label = "rotation"
    )
    
    val scanningText = Strings.vsMatchmakingTitle
    val scanningDesc = Strings.vsMatchmakingDesc
    
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Radar Visualiser
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                // Expanding rings
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(pulse1)
                        .graphicsLayer { alpha = 1.0f - pulse1 }
                        .border(2.dp, Color(0xFF00E5FF), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(pulse2)
                        .graphicsLayer { alpha = 1.0f - pulse2 }
                        .border(2.dp, Color(0xFFD500F9), CircleShape)
                )
                
                // Rotating radar sweep
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(rotation)
                ) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(Color.Transparent, Color(0xFF00E5FF).copy(alpha = 0.3f), Color.Transparent)
                        ),
                        startAngle = 0f,
                        sweepAngle = 120f,
                        useCenter = true
                    )
                }
                
                // Central Avatar Orb
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .shadow(12.dp, CircleShape, spotColor = Color(0xFF00E5FF))
                        .background(Color(0xFF1a1a2e), CircleShape)
                        .border(2.dp, Color(0xFF00E5FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                text = scanningText,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = scanningDesc,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 40.dp)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                modifier = Modifier.width(140.dp)
            ) {
                Icon(Icons.Default.Cancel, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (currentLang == AppLanguage.TURKISH) "İPTAL" else "CANCEL",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MatchedBannerView(
    playerName: String,
    opponentName: String,
    myCountryCode: String,
    opponentCountryCode: String,
    gameStartTimestamp: Long,
    currentLang: AppLanguage
) {
    var countdown by remember { mutableIntStateOf(3) }
    
    LaunchedEffect(gameStartTimestamp) {
        while (countdown > 0) {
            val left = ((gameStartTimestamp - System.currentTimeMillis()) / 1000L).coerceAtLeast(0L)
            countdown = left.toInt()
            delay(200)
        }
    }
    
    val matchedTitle = Strings.vsMatchedTitle
    val vsWord = "VS"
    val preparingText = Strings.vsPreparingText
    
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = matchedTitle,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFFAB00),
                letterSpacing = 4.sp
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                // My Orb (Left)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .shadow(16.dp, CircleShape, spotColor = Color(0xFF00E5FF))
                            .background(
                                Brush.verticalGradient(listOf(Color(0xFF00d9ff), Color(0xFF00838f))),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = getFlagEmojiForCountryCode(myCountryCode), fontSize = 40.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = playerName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                }
                
                // VS Text (Center)
                Text(
                    text = "⚔️",
                    fontSize = 36.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                
                // Opponent Orb (Right)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .shadow(16.dp, CircleShape, spotColor = Color(0xFFD500F9))
                            .background(
                                Brush.verticalGradient(listOf(Color(0xFFD500F9), Color(0xFF7B1FA2))),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = getFlagEmojiForCountryCode(opponentCountryCode), fontSize = 40.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = opponentName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // Giant Countdown
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
                    .border(2.dp, Color(0xFFFFAB00), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (countdown <= 0) "GO!" else countdown.toString(),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFAB00)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = preparingText,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun VsGameplayView(
    question: Question,
    localIndex: Int,
    myScore: Int,
    opponentScore: Int,
    opponentName: String,
    myLevel: Int,
    opponentLevel: Int,
    myCountryCode: String,
    opponentCountryCode: String,
    secondsLeftProvider: () -> Long,
    questionTimeLeftMsProvider: () -> Long,
    selectedOptionIndex: Int?,
    feedbackType: AnswerFeedbackType,
    showOpponentFeedback: Boolean,
    buttonLockedUntil: Long,
    myActiveEmote: String?,
    opponentActiveEmote: String?,
    onSendEmote: (String) -> Unit,
    onOptionSelected: (Int, Int) -> Unit,
    onQuit: () -> Unit,
    currentLang: AppLanguage
) {
    var isEmoteMenuOpen by remember { mutableStateOf(false) }
    var isOpponentMuted by remember { mutableStateOf(false) }
    var lastEmoteTime by remember { mutableLongStateOf(0L) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 28.dp), // Added extra top padding for camera notch
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        // TOP SCOREBOARD HUD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player Score (Left)
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = getFlagEmojiForCountryCode(myCountryCode), fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .shadow(4.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.3f))
                            .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "${Strings.vsYou} (Lv.$myLevel)",
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = myScore.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    }
                }
                
                // Emote Bubble
                androidx.compose.animation.AnimatedVisibility(
                    visible = myActiveEmote != null,
                    enter = scaleIn(initialScale = 0.5f) + fadeIn(),
                    exit = scaleOut(targetScale = 0.5f) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 16.dp, y = 24.dp)
                ) {
                    myActiveEmote?.let { text ->
                        SpeechBubble(text = text, isRightSide = false)
                    }
                }
            }
            
            // TIMER HUD (Center)
            VsTimerHud(secondsLeftProvider)
            
            // Opponent Score (Right)
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .shadow(4.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.3f))
                            .border(1.dp, Color(0xFFD500F9).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "$opponentName (Lv.$opponentLevel)".uppercase(),
                            color = Color(0xFFD500F9),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = opponentScore.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .border(1.dp, Color(0xFFD500F9).copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = getFlagEmojiForCountryCode(opponentCountryCode), fontSize = 20.sp)
                    }
                }
                
                // Emote Bubble
                androidx.compose.animation.AnimatedVisibility(
                    visible = opponentActiveEmote != null && !isOpponentMuted,
                    enter = scaleIn(initialScale = 0.5f) + fadeIn(),
                    exit = scaleOut(targetScale = 0.5f) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = (-16).dp, y = 24.dp)
                ) {
                    opponentActiveEmote?.let { text ->
                        SpeechBubble(text = text, isRightSide = true)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // TUG OF WAR BAR
        TugOfWarBar(myScore = myScore, opponentScore = opponentScore)
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Opponent got point splash feedback
        Box(
            modifier = Modifier.height(30.dp),
            contentAlignment = Alignment.Center
        ) {
            if (showOpponentFeedback) {
                Text(
                    text = if (currentLang == AppLanguage.TURKISH) "⚡ RAKİP DAHA HIZLI DAVRANDI! ⚡" else "⚡ RIVAL CLAIMED THE POINT! ⚡",
                    color = Color(0xFFD500F9),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            } else if (System.currentTimeMillis() < buttonLockedUntil) {
                Text(
                    text = if (currentLang == AppLanguage.TURKISH) "❌ HATALI CEVAP! KİLİTLENDİ" else "❌ WRONG ANSWER! LOCKED",
                    color = Color(0xFFE94560),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // CHALLENGE MODE INSPIRED QUESTION CARD
        val infinitePulse = rememberInfiniteTransition(label = "pulse")
        val scalePulse by infinitePulse.animateFloat(
            initialValue = 1f,
            targetValue = 1.02f,
            animationSpec = infiniteRepeatable(animation = tween(1000), repeatMode = RepeatMode.Reverse),
            label = "scale"
        )
        val challengeGradient = Brush.linearGradient(
            colors = listOf(Color(0xFF6A11CB), Color(0xFF2575FC), Color(0xFFE94560))
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(180.dp)
                .scale(scalePulse)
                .border(
                    width = 2.dp,
                    brush = Brush.sweepGradient(listOf(Color(0xFF00d9ff), Color(0xFFe94560), Color(0xFF00d9ff))),
                    shape = RoundedCornerShape(28.dp)
                ),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(challengeGradient),
                contentAlignment = Alignment.Center
            ) {
                // Background icon overlay
                Icon(
                    imageVector = Icons.Default.Whatshot,
                    contentDescription = null,
                    modifier = Modifier
                        .size(140.dp)
                        .alpha(0.1f)
                        .rotate(-15f),
                    tint = Color.White
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${Strings.question} ${localIndex + 1}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = question.displayText,
                        color = Color.White,
                        fontSize = if (question.displayText.length > 5) 44.sp else 56.sp,
                        fontWeight = FontWeight.ExtraBold,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.3f),
                                blurRadius = 8f
                            )
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 5-Second per-question progress bar (Isolated for performance)
                    QuestionTimerBar(questionTimeLeftMsProvider)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(28.dp))
        
        // CHALLENGE MODE INSPIRED OPTIONS GRID
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            val options = question.options
            
            // Define vibrant option colors
            val baseColors = listOf(Color(0xFFe94560), Color(0xFF00d9ff), Color(0xFFf9a825), Color(0xFF9C27B0))
            
            for (rowIndex in 0 until (options.size + 1) / 2) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (colIndex in 0..1) {
                        val index = rowIndex * 2 + colIndex
                        if (index < options.size) {
                            val option = options[index]
                            val isSelected = selectedOptionIndex == index
                            val isLastOdd = (options.size % 2 != 0) && (index == options.size - 1)
                            
                            val isCorrect = isSelected && feedbackType == AnswerFeedbackType.CORRECT
                            val isWrong = isSelected && feedbackType == AnswerFeedbackType.WRONG
                            val baseColor = baseColors[index % baseColors.size]
                            
                            val bgColor by animateColorAsState(
                                targetValue = when {
                                    isCorrect -> Color(0xFF4CAF50)
                                    isWrong -> Color(0xFFE91E63)
                                    else -> Color.White.copy(alpha = 0.08f) // Glassmorphism translucent base
                                },
                                animationSpec = tween(200),
                                label = "bgColor"
                            )
                            
                            val borderColor by animateColorAsState(
                                targetValue = when {
                                    isCorrect -> Color(0xFF81C784)
                                    isWrong -> Color(0xFFF48FB1)
                                    else -> baseColor.copy(alpha = 0.6f) // Neon rim
                                },
                                animationSpec = tween(200),
                                label = "borderColor"
                            )
                            
                            val btnScale by animateFloatAsState(
                                targetValue = when {
                                    isCorrect -> 1.15f
                                    isWrong -> 0.95f
                                    else -> 1f
                                },
                                animationSpec = spring(
                                    dampingRatio = if (isCorrect) Spring.DampingRatioMediumBouncy else Spring.DampingRatioNoBouncy,
                                    stiffness = if (isCorrect) Spring.StiffnessMedium else Spring.StiffnessLow
                                ),
                                label = "scale"
                            )

                            Button(
                                onClick = { 
                                    if (selectedOptionIndex == null && System.currentTimeMillis() > buttonLockedUntil) {
                                        onOptionSelected(index, option)
                                    }
                                },
                                modifier = Modifier
                                    .weight(if (isLastOdd) 2f else 1f) // Make full width if last odd item
                                    .height(85.dp)
                                    .scale(btnScale)
                                    .border(
                                        width = 2.dp,
                                        color = borderColor,
                                        shape = RoundedCornerShape(24.dp)
                                    ),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = bgColor),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 0.dp, 
                                    pressedElevation = 0.dp,
                                    disabledElevation = 0.dp,
                                    hoveredElevation = 0.dp,
                                    focusedElevation = 0.dp
                                ),
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                Text(
                                    text = option.toString(),
                                    fontSize = if (option.toString().length > 4) 30.sp else 38.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isCorrect || isWrong) Color.White else baseColor,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    style = androidx.compose.ui.text.TextStyle(
                                        shadow = if (isCorrect || isWrong) {
                                            androidx.compose.ui.graphics.Shadow(color = Color.Black.copy(alpha = 0.5f), blurRadius = 8f)
                                        } else null
                                    )
                                )
                            }
                        } else {
                            if (options.size % 2 == 0) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Spacer(modifier = Modifier.weight(1f))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onQuit) {
                Text(
                    text = Strings.vsBtnForfeit,
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Emote Button
            IconButton(
                onClick = { isEmoteMenuOpen = true },
                modifier = Modifier
                    .size(54.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
                    .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape)
            ) {
                Text("💬", fontSize = 24.sp)
            }
        }
    } // End of Box
    
    // Modern Bottom Sheet Emote Overlay
    if (isEmoteMenuOpen) {
        // Dim Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable { isEmoteMenuOpen = false }
        )
    }
    
    androidx.compose.animation.AnimatedVisibility(
        visible = isEmoteMenuOpen,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = Modifier.align(Alignment.BottomCenter)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFF131326).copy(alpha = 0.98f),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color(0xFF00E5FF).copy(alpha = 0.3f),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
                .clickable(enabled = false) {}
                .padding(bottom = 36.dp, top = 16.dp, start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Drag handle indicator
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(4.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                val textEmotes = listOf(Strings.vsEmoteGoodLuck, Strings.vsEmoteTooFast, Strings.vsEmoteOops, Strings.vsEmoteHurryUp, Strings.vsEmoteThanks, Strings.vsEmoteGoodGame)

                // Render 2 rows of 3 text emotes
                for (row in 0..1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (col in 0..2) {
                            val text = textEmotes[row * 3 + col]
                            EmoteTextButton(
                                text = text,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    val now = System.currentTimeMillis()
                                    if (now - lastEmoteTime > 2000L) {
                                        onSendEmote(text)
                                        lastEmoteTime = now
                                    }
                                    isEmoteMenuOpen = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Emojis + Mute Row
                val emojis = listOf("😡", "😢", "😂", "👍")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    emojis.forEach { emoji ->
                        EmoteEmojiButton(
                            emoji = emoji,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val now = System.currentTimeMillis()
                                if (now - lastEmoteTime > 2000L) {
                                    onSendEmote(emoji)
                                    lastEmoteTime = now
                                }
                                isEmoteMenuOpen = false
                            }
                        )
                    }
                    
                    // Mute Button (Modern)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.4f)
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .clickable {
                                isOpponentMuted = !isOpponentMuted
                                isEmoteMenuOpen = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "💬", fontSize = 28.sp)
                        if (isOpponentMuted) {
                            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                drawLine(
                                    color = Color(0xFFE94560),
                                    start = androidx.compose.ui.geometry.Offset(size.width * 0.8f, size.height * 0.2f),
                                    end = androidx.compose.ui.geometry.Offset(size.width * 0.2f, size.height * 0.8f),
                                    strokeWidth = 4.dp.toPx()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
  } // End of Box
}

@Composable
fun QuestionTimerBar(timeLeftProvider: () -> Long) {
    val timeLeft = timeLeftProvider()
    LinearProgressIndicator(
        progress = { (timeLeft / 5000f).coerceIn(0f, 1f) },
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .height(6.dp)
            .clip(CircleShape),
        color = if (timeLeft < 1500L) Color(0xFFE94560) else Color(0xFF00E5FF),
        trackColor = Color.White.copy(alpha = 0.2f),
        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
    )
}

@Composable
fun VsTimerHud(secondsLeftProvider: () -> Long) {
    val secondsLeft = secondsLeftProvider()
    val durationText = String.format("%02d:%02d", secondsLeft / 60, secondsLeft % 60)
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularTimerIndicator(secondsLeftProvider)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = durationText,
            color = if (secondsLeft <= 15L) Color(0xFFE94560) else Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 15.sp
        )
    }
}

@Composable
fun CircularTimerIndicator(secondsLeftProvider: () -> Long) {
    val secondsLeft = secondsLeftProvider()
    val progress = (secondsLeft.toFloat() / 120f).coerceIn(0f, 1f)
    val color = when {
        secondsLeft <= 15L -> Color(0xFFE94560)
        secondsLeft <= 45L -> Color(0xFFFFD600)
        else -> Color(0xFF00E5FF)
    }
    
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(56.dp)) {
        CircularProgressIndicator(
            progress = { progress },
            color = color,
            strokeWidth = 4.dp,
            trackColor = Color.White.copy(alpha = 0.1f),
            modifier = Modifier.fillMaxSize()
        )
        Icon(
            imageVector = Icons.Default.OfflineBolt,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun TugOfWarBar(myScore: Int, opponentScore: Int) {
    val difference = myScore - opponentScore
    val tugRatio = (difference.toFloat() / 100f).coerceIn(-1f, 1f)
    val animatedTugRatio by animateFloatAsState(
        targetValue = tugRatio,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "tugRatio"
    )
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "HALAT ÇEKME (TUG OF WAR)",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .background(Color(0xFF131326))
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF00E5FF).copy(alpha = 0.3f)))
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFD500F9).copy(alpha = 0.3f)))
            }
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val centerPx = maxWidth.value / 2
                val offsetPx = centerPx * animatedTugRatio
                Box(
                    modifier = Modifier
                        .offset(x = (centerPx + offsetPx - 16).dp)
                        .size(32.dp)
                        .align(Alignment.CenterStart)
                        .shadow(8.dp, CircleShape)
                        .background(Color.White, CircleShape)
                        .border(2.dp, if(animatedTugRatio > 0) Color(0xFF00E5FF) else if(animatedTugRatio < 0) Color(0xFFD500F9) else Color.Gray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.OfflineBolt,
                        contentDescription = null,
                        tint = if(animatedTugRatio > 0) Color(0xFF00E5FF) else if(animatedTugRatio < 0) Color(0xFFD500F9) else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun VsGameOverView(
    myScore: Int,
    opponentScore: Int,
    opponentName: String,
    rematchState: RematchState,
    onRematchRequested: () -> Unit,
    onRematchAccepted: () -> Unit,
    onPlayAgain: () -> Unit,
    onMenu: () -> Unit,
    currentLang: AppLanguage
) {
    val hasWon = myScore > opponentScore
    val isDraw = myScore == opponentScore
    val isKnockout = Math.abs(myScore - opponentScore) >= 100
    
    val resultTitle = when {
        isKnockout && hasWon -> Strings.vsGameOverKnockoutWin
        isKnockout && !hasWon -> Strings.vsGameOverKnockoutLoss
        hasWon -> Strings.vsGameOverVictory
        isDraw -> Strings.vsGameOverDraw
        else -> Strings.vsGameOverDefeat
    }
    
    val resultColor = when {
        hasWon -> Color(0xFF00E5FF)
        isDraw -> Color(0xFFFFAB00)
        else -> Color(0xFFE94560)
    }

    // ANIMATION STATES
    val animatedMyScore = remember { androidx.compose.animation.core.Animatable(0f) }
    val animatedOppScore = remember { androidx.compose.animation.core.Animatable(0f) }
    val scaleMe = remember { androidx.compose.animation.core.Animatable(1f) }
    val scaleOpp = remember { androidx.compose.animation.core.Animatable(1f) }

    LaunchedEffect(myScore, opponentScore) {
        launch {
            animatedMyScore.animateTo(
                targetValue = myScore.toFloat(),
                animationSpec = androidx.compose.animation.core.tween(1800, easing = androidx.compose.animation.core.FastOutSlowInEasing)
            )
            // BÃ¼yÃ¼me efekti (Sadece kazandÄ±ysa)
            if (hasWon) {
                scaleMe.animateTo(1.35f, androidx.compose.animation.core.spring(dampingRatio = 0.45f, stiffness = 400f))
            }
        }
        launch {
            animatedOppScore.animateTo(
                targetValue = opponentScore.toFloat(),
                animationSpec = androidx.compose.animation.core.tween(1800, easing = androidx.compose.animation.core.FastOutSlowInEasing)
            )
            if (!hasWon && !isDraw) {
                scaleOpp.animateTo(1.35f, androidx.compose.animation.core.spring(dampingRatio = 0.45f, stiffness = 400f))
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (hasWon) {
            SuccessConfetti()
        }
        
        Card(
            modifier = Modifier
                .width(350.dp)
                .padding(16.dp)
                .border(2.dp, resultColor, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131326).copy(alpha = 0.95f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = resultTitle,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = resultColor,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(36.dp))
                
                // Final Score Breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.graphicsLayer {
                            scaleX = scaleMe.value
                            scaleY = scaleMe.value
                        }
                    ) {
                        Text(
                            text = Strings.vsYourScore,
                            fontSize = 11.sp,
                            color = if (hasWon) Color(0xFFFFD700) else Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = animatedMyScore.value.toInt().toString(),
                            fontSize = 32.sp,
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Black
                        )
                    }
                    
                    Text(text = "VS", fontSize = 18.sp, color = Color.White.copy(alpha = 0.2f), fontWeight = FontWeight.Bold)
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.graphicsLayer {
                            scaleX = scaleOpp.value
                            scaleY = scaleOpp.value
                        }
                    ) {
                        Text(
                            text = opponentName.uppercase(),
                            fontSize = 11.sp,
                            color = if (!hasWon && !isDraw) Color(0xFFFFD700) else Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.widthIn(max = 90.dp)
                        )
                        Text(
                            text = animatedOppScore.value.toInt().toString(),
                            fontSize = 32.sp,
                            color = Color(0xFFD500F9),
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(36.dp))
                
                // Rematch Section
                when (rematchState) {
                    RematchState.NONE -> {
                        Button(
                            onClick = onRematchRequested,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text(
                                text = Strings.vsRematchRequest,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        }
                    }
                    RematchState.REQUESTED_BY_ME -> {
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = false
                        ) {
                            Text(
                                text = Strings.vsRematchWaiting,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                    RematchState.REQUESTED_BY_OPPONENT -> {
                        Button(
                            onClick = onRematchAccepted,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text(
                                text = Strings.vsRematchAccept,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        }
                    }
                    RematchState.ACCEPTED -> {
                        // Will start immediately
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                
                Button(
                    onClick = onPlayAgain,
                    colors = ButtonDefaults.buttonColors(containerColor = resultColor),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = Strings.vsPlayAgain,
                        color = if (hasWon) Color.Black else Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(14.dp))
                
                TextButton(onClick = onMenu) {
                    Text(
                        text = Strings.vsMenu,
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SpeechBubble(text: String, isRightSide: Boolean) {
    val bubbleColor = if (isRightSide) Color(0xFFD500F9) else Color(0xFF00E5FF)
    val shape = if (isRightSide) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }
    
    Box(
        modifier = Modifier
            .shadow(6.dp, shape)
            .background(Color.White, shape)
            .border(2.dp, bubbleColor, shape)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = Color.Black,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp
        )
    }
}

@Composable
fun EmoteTextButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(52.dp)
            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
fun EmoteEmojiButton(emoji: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .aspectRatio(1.4f)
            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = 32.sp,
            textAlign = TextAlign.Center
        )
    }
}




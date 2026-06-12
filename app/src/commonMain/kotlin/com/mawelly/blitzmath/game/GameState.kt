package com.mawelly.blitzmath.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mawelly.blitzmath.localization.AppLanguage
import com.mawelly.blitzmath.audio.IVoiceManager
import com.mawelly.blitzmath.core.PlatformServices
import com.mawelly.blitzmath.core.ISoundManager
import com.mawelly.blitzmath.core.IHapticManager
import com.mawelly.blitzmath.leaderboard.ILeaderboardManager
import com.mawelly.blitzmath.data.IGameDataStore
import com.mawelly.blitzmath.localization.Strings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.math.sqrt
import kotlin.math.floor

class GameState(
    private val platformServices: PlatformServices,
    private val voiceManager: IVoiceManager? = null,
    val isVoiceEnabled: Boolean = true,
    val mode: GameMode,
    startCheckpoint: Int = 1,
    private val dataStore: IGameDataStore? = null,
    val unlockedCards: Set<String> = emptySet(),
    val equippedCards: Set<String> = emptySet(),
    private val vibrationEnabled: Boolean = true,
    private val vibrationStrength: Float = 1.0f,
    private val onCardUnlocked: (ScientistCard) -> Unit = {},
    startingLives: Int = 5,
    lastLossTime: Long = 0L,
    val playerXp: Int = 0,
    val playerId: String = "",
    val playerName: String = "",
    private val leaderboardManager: ILeaderboardManager? = null,
    private val scope: CoroutineScope? = null
) {
    private val soundManager: ISoundManager get() = platformServices.soundManager
    private val hapticManager: IHapticManager get() = platformServices.hapticManager

    var livesRemaining by mutableIntStateOf(startingLives)
        private set

    var lastLifeLossTime by mutableStateOf(lastLossTime)
        private set

    // Constant for refill time: 15 minutes
    private val REFILL_TIME_MS = 15 * 60 * 1000L
    private val MAX_LIVES = 5

    init {
        checkAndRefillLives()
    }

    private fun checkAndRefillLives() {
        if (livesRemaining >= MAX_LIVES) {
            lastLifeLossTime = 0L
            return
        }

        // Safety check: If we have missing lives but no start time, set it to now
        if (lastLifeLossTime <= 0L) {
            lastLifeLossTime = platformServices.getCurrentTimeMillis()
            saveLivesToDisk()
            return
        }

        val now = platformServices.getCurrentTimeMillis()
        val timePassed = now - lastLifeLossTime
        
        if (timePassed >= REFILL_TIME_MS) {
            val livesToRefill = (timePassed / REFILL_TIME_MS).toInt()
            if (livesToRefill > 0) {
                val oldLives = livesRemaining
                livesRemaining = (livesRemaining + livesToRefill).coerceAtMost(MAX_LIVES)
                
                if (livesRemaining == MAX_LIVES) {
                    lastLifeLossTime = 0L
                } else {
                    lastLifeLossTime += livesToRefill * REFILL_TIME_MS
                }
                
                if (livesRemaining != oldLives) {
                    saveLivesToDisk()
                }
            }
        }
    }

    private fun saveLivesToDisk() {
        scope?.launch {
            dataStore?.saveLives(livesRemaining)
            dataStore?.saveLastLifeLossTime(lastLifeLossTime)
        }
    }
    
    var currentCheckpoint by mutableIntStateOf(startCheckpoint)
        private set

    var currentQuestionIndex by mutableIntStateOf(0)
        private set

    val questionsPerCheckpoint = 10

    // Soru geçmişi: Aynı soruların üst üste çıkmasını engeller
    private val questionHistory = mutableListOf<String>()
    private val maxHistorySize = 5

    var score by mutableIntStateOf(0)
        private set

    var timeLeft by mutableStateOf(if (mode == GameMode.CHALLENGE) 30.0f else 5.0f)
        private set

    var isGameOver by mutableStateOf(false)
        private set

    var isCheckpointComplete by mutableStateOf(false)
        private set

    var highScore by mutableIntStateOf(0)
        private set

    var isSaveMePending by mutableStateOf(false)
        private set

    var saveMeTimeLeft by mutableStateOf(3.0f)
        private set

    var streak by mutableIntStateOf(0)
        private set

    var lastHighStreak by mutableIntStateOf(0)
        private set

    var consecutiveMistakes by mutableIntStateOf(0)
        private set

    var speedMessage by mutableStateOf("")
        private set

    var speedBonus by mutableIntStateOf(0)
        private set

    var showAdOnGameOver by mutableStateOf(false)
        private set

    // Active Skill: Charge system - how many uses left per card id
    val cardCharges = androidx.compose.runtime.mutableStateMapOf<String, Int>()
    val cardLastUseTimes = androidx.compose.runtime.mutableStateMapOf<String, Long>()

    // Active Skill: Tesla ZAP - the option index to visually eliminate (null = nothing eliminated)
    var zapEliminatedOption by mutableStateOf<Int?>(null)
        private set

    // Active Skill: FREEZE_TIME (Cahit Arf) - freeze countdown in seconds
    var timeFreezeLeft by mutableStateOf(0f)
        private set

    // Active Skill: SLOW_MOTION (Einstein) - makes timer run at 50%
    var slowMotionActive by mutableStateOf(false)
        private set

    var isSynced by mutableStateOf(false)
        private set

    // Active Skill: EXTRA_LIFE shield (Newton)
    var shieldsRemaining by mutableIntStateOf(0)
        private set

    var totalTimeBonusEarned by mutableStateOf(0f)
        private set
    var gameDurationSeconds by mutableStateOf(0f)
        private set
    private var gameStartTimeMillis: Long = platformServices.getCurrentTimeMillis()

    private var lastQuestionTimeLeft: Float = 30f

    var currentQuestion by mutableStateOf(generateQuestionForCheckpoint())
        private set

    init {
        resetTimer()
        soundManager.setEnabled(true) // Start sound system if needed
    }

    /**
     * Synchronizes local charges with saved data and calculates time-based refills.
     */
    fun syncChargesWithTime(savedCharges: Map<String, Int>, lastUseTimes: Map<String, Long>) {
        val currentTime = platformServices.getCurrentTimeMillis()
        
        // Ensure all equipped cards have an entry in use times to avoid potential NPEs
        equippedCards.forEach { id ->
            if (!cardLastUseTimes.containsKey(id)) {
                cardLastUseTimes[id] = lastUseTimes[id] ?: 0L
            }
        }

        equippedCards.forEach { id ->
            val card = ScientistCards.getCardById(id) ?: return@forEach
            val currentCount = savedCharges[id] ?: card.maxCharges
            val lastTime = lastUseTimes[id] ?: 0L
            
            if (currentCount < card.maxCharges && lastTime > 0L) {
                val elapsedMs = currentTime - lastTime
                val durationMs = card.rechargeDurationMinutes * 60 * 1000L
                val refilledCount = (elapsedMs / durationMs).toInt()
                
                if (refilledCount > 0) {
                    val newCount = (currentCount + refilledCount).coerceAtMost(card.maxCharges)
                    cardCharges[id] = newCount
                    // If still less than max, advance the timestamp by the refilled duration
                    cardLastUseTimes[id] = if (newCount < card.maxCharges) {
                        lastTime + (refilledCount * durationMs)
                    } else {
                        0L // Fully recharged
                    }
                } else {
                    cardCharges[id] = currentCount
                    cardLastUseTimes[id] = lastTime
                }
            } else {
                cardCharges[id] = currentCount
                cardLastUseTimes[id] = lastTime
            }
        }
        isSynced = true
    }

    fun getRemainingRechargeTime(cardId: String): Long {
        val card = ScientistCards.getCardById(cardId) ?: return 0L
        val lastTime = cardLastUseTimes[cardId] ?: return 0L
        if (lastTime == 0L || (cardCharges[cardId] ?: 0) >= card.maxCharges) return 0L
        
        val durationMs = card.rechargeDurationMinutes * 60 * 1000L
        val targetTime = lastTime + durationMs
        return (targetTime - platformServices.getCurrentTimeMillis()).coerceAtLeast(0L)
    }

    private fun getOperationForMixedMode(): OperationType {
        return listOf(OperationType.ADDITION, OperationType.SUBTRACTION, OperationType.MULTIPLICATION, OperationType.DIVISION).random()
    }

    val checkpointConfig: CheckpointConfig
        get() {
            val operation = when (mode) {
                GameMode.CLASSIC -> CheckpointManager.getOperationForCheckpoint(currentCheckpoint)
                GameMode.MIXED -> getOperationForMixedMode()
                GameMode.ENDLESS -> CheckpointManager.getOperationForCheckpoint(currentCheckpoint)
                GameMode.CHALLENGE -> getOperationForMixedMode() // Challenge is mixed
            }
            
            // Dinamik Zorluk Ayarı: Kullanıcı üst üste hata yaparsa zorluğu geçici olarak düşür
            var effectiveLevel = playerXp
            if (consecutiveMistakes >= 2) {
                // Her 2 ardışık hatada zorluk %25 düşer. Maksimum %75 düşebilir.
                val reduction = ((consecutiveMistakes / 2) * 0.25).coerceAtMost(0.75)
                effectiveLevel = (playerXp * (1.0 - reduction)).toInt().coerceAtLeast(1)
            }
            
            val calculatedLevel = floor(sqrt(effectiveLevel / 100.0)).toInt() + 1
            val pLevel = calculatedLevel.coerceIn(1, 100)
            
            // Base range for Addition/Subtraction. Multiplication scales this down inside MathGenerator.
            val levelMultiplier = sqrt(pLevel.toDouble())
            val minVal = (levelMultiplier * 2).toInt().coerceAtLeast(1)
            val maxVal = 10 + (levelMultiplier * 10).toInt()
            val strictLevelRange = minVal..maxVal
            
            return CheckpointConfig(
                checkpointNumber = currentCheckpoint,
                questionInCheckpoint = currentQuestionIndex + 1,
                operationType = operation,
                difficulty = if (mode == GameMode.CHALLENGE) DifficultyLevel.NORMAL else CheckpointManager.getDifficulty(currentCheckpoint),
                timeLimit = if (mode == GameMode.CHALLENGE) 30f else 5f,
                numberRange = strictLevelRange
            )
        }

    private fun generateQuestionForCheckpoint(): Question {
        var newQuestion: Question
        var attempts = 0
        
        try {
            // Önceki 5 soruyla aynı olmaması için kontrol et (max 10 deneme)
            do {
                newQuestion = MathGenerator.generateQuestion(checkpointConfig)
                attempts++
            } while (questionHistory.contains(newQuestion.displayText) && attempts < 10)
        } catch (e: Exception) {
            // Fallback question if generator fails
            newQuestion = Question("1 + 1", 2, listOf(1, 2, 3, 4), "Simple Addition")
        }

        // Geçmişe ekle ve boyutu koru
        questionHistory.add(newQuestion.displayText)
        if (questionHistory.size > maxHistorySize) {
            questionHistory.removeAt(0)
        }
        
        // Update start time for next question's speed calculation
        lastQuestionTimeLeft = timeLeft
        
        return newQuestion
    }

    fun checkAnswer(selectedOption: Int): Boolean {
        if (isGameOver || isCheckpointComplete) return false

        return if (selectedOption == currentQuestion.correctAnswer) {
            val timeSpent = if (mode == com.mawelly.blitzmath.game.GameMode.CHALLENGE) {
                (lastQuestionTimeLeft - timeLeft).coerceAtLeast(0f)
            } else {
                checkpointConfig.timeLimit - timeLeft
            }
            
            speedBonus = calculateSpeedBonus(timeSpent)
            
            // Challenge Modu Özel: Hızlı cevap süreyi uzatır (Agresif Bonuslar)
            var timeBonusMsg = ""
            if (mode == com.mawelly.blitzmath.game.GameMode.CHALLENGE) {
                val bonus = when {
                    timeSpent <= 0.6f -> 5.0f
                    timeSpent <= 1.0f -> 3.0f
                    timeSpent <= 1.8f -> 2.0f
                    timeSpent <= 3.0f -> 1.0f
                    else -> 0f
                }
                
                if (bonus > 0f) {
                    timeLeft = (timeLeft + bonus).coerceAtMost(60.0f) // Max 1 dakika birikebilir
                    totalTimeBonusEarned += bonus
                    timeBonusMsg = "⚡ +${bonus.toInt()}s BONUS! "
                }
            }
            
            val basePoints = 10
            val streakBonus = (streak * 2).coerceAtMost(20)
            score += basePoints + speedBonus + streakBonus
            // Reset ZAP / slow motion for next question
            zapEliminatedOption = null
            slowMotionActive = false
            streak++
            consecutiveMistakes = 0
            if (score > highScore) highScore = score
            
            soundManager.playSuccess()
            
            // Decoupled Feedback
            val commentary = MotivationalMessages.getMessages(Strings.currentLanguage)
            
            // UI Label (Achievement)
            speedMessage = timeBonusMsg + getSpeedMessage(timeSpent, streak) + " +$speedBonus"

            // Voice Commentary (Autonomous Coach)
            if (isVoiceEnabled) {
                val isStreak = streak > 0 && streak % 5 == 0
                val isEliteStreak = streak >= 10 && streak % 10 == 0
                val isSpeed = timeSpent <= 0.8f
                
                val shouldSpeak = when {
                    isEliteStreak || isStreak || isSpeed -> true
                    checkpointConfig.difficulty == DifficultyLevel.EASY -> Random.nextDouble() < 0.3
                    checkpointConfig.difficulty == DifficultyLevel.NORMAL -> Random.nextDouble() < 0.5
                    else -> true
                }
                
                val voiceNote = when {
                    isEliteStreak -> commentary.elitePraise.random()
                    isStreak -> commentary.streak.random()
                    isSpeed -> commentary.speed.random()
                    shouldSpeak -> when (checkpointConfig.difficulty) {
                        DifficultyLevel.EASY -> commentary.correctEasy.random()
                        DifficultyLevel.NORMAL -> commentary.correctNormal.random()
                        else -> commentary.correctHard.random()
                    }
                    else -> null
                }
                voiceNote?.let { voiceManager?.speak(it) }
            }
            currentQuestionIndex++

            if (mode != GameMode.CHALLENGE && currentQuestionIndex >= questionsPerCheckpoint) {
                completeCheckpoint()
            } else {
                currentQuestion = generateQuestionForCheckpoint()
                if (mode != GameMode.CHALLENGE) resetTimer()
            }
            true
        } else {
            handleWrongAnswer()
            false
        }
    }

    private fun handleWrongAnswer(isTimeOut: Boolean = false) {
        consecutiveMistakes++
        // Newton shield - EXTRA_LIFE active skill (pre-activated shield)
        if (shieldsRemaining > 0 && !isGameOver && !isCheckpointComplete) {
            shieldsRemaining--
            soundManager.playSuccess()
            speedMessage = "🛡️ İKİNCİ ŞANS! ($shieldsRemaining kaldı)"
            if (isVoiceEnabled) voiceManager?.speak("Kalkan Seni Korudu!")
            speedBonus = 0
            zapEliminatedOption = null
            slowMotionActive = false

            currentQuestionIndex++
            if (currentQuestionIndex >= questionsPerCheckpoint) {
                completeCheckpoint()
            } else {
                currentQuestion = generateQuestionForCheckpoint()
                resetTimer()
            }
            return
        }

        val commentary = MotivationalMessages.getMessages(Strings.currentLanguage)
        
        // UI Message
        speedMessage = if (isTimeOut) Strings.getTimeOutMessage() else Strings.getWrongAnswerMessage()
        
        // Voice Commentary
        if (isVoiceEnabled) {
            val voiceNote = if (streak >= 4) {
                commentary.recovery.random()
            } else {
                speedMessage
            }
            voiceManager?.speak(voiceNote)
        }

        if (streak >= 4) lastHighStreak = streak
        speedBonus = 0
        streak = 0
        if (vibrationEnabled) {
            hapticManager.triggerError()
        }
        soundManager.playError()

        // Consume one heart/life
        if (livesRemaining > 0) {
            if (livesRemaining == MAX_LIVES) {
                lastLifeLossTime = platformServices.getCurrentTimeMillis()
            }
            livesRemaining--
            saveLivesToDisk()
        }

        if (livesRemaining == 0) {
            triggerGameOver()
        } else {
            currentQuestion = generateQuestionForCheckpoint()
            if (mode != GameMode.CHALLENGE) {
                resetTimer()
            }
        }
    }

    private fun handleTimeOut() {
        handleWrongAnswer(isTimeOut = true)
    }

    private fun completeCheckpoint() {
        isCheckpointComplete = true
        soundManager.playSuccess()
        if (isVoiceEnabled) {
            voiceManager?.speak(Strings.checkpointComplete)
        }
        submitScoreToLeaderboard()
    }

    fun nextCheckpoint() {
        if (!isCheckpointComplete) return
        currentCheckpoint++
        currentQuestionIndex = 0
        isCheckpointComplete = false
        speedMessage = ""
        speedBonus = 0
        consecutiveMistakes = 0
        currentQuestion = generateQuestionForCheckpoint()
        resetTimer()
    }

    private fun triggerGameOver() {
        isGameOver = true
        isSaveMePending = false
        showAdOnGameOver = true
        streak = 0
        
        // Oyun süresini hesapla
        gameDurationSeconds = (platformServices.getCurrentTimeMillis() - gameStartTimeMillis) / 1000f

        soundManager.playGameOver()
        if (isVoiceEnabled) {
            if (Strings.currentLanguage == AppLanguage.TURKISH) {
                val endNotes = listOf(
                    "Oyun bitti, yeniden deneyelim!", 
                    "Pes etmek yok, tekrar deneyelim!", 
                    "Yeniden deneyelim!",
                    "Daha iyisini yapabilirsin, yeniden deneyelim!"
                )
                voiceManager?.speak(endNotes.random())
            } else {
                voiceManager?.speak(Strings.gameOver)
            }
        }
        submitScoreToLeaderboard()
    }

    fun onAdShown() { showAdOnGameOver = false }

    fun updateTimer(deltaTime: Float) {
        if (isSaveMePending) {
            saveMeTimeLeft -= deltaTime
            if (saveMeTimeLeft <= 0) {
                saveMeTimeLeft = 0f
                triggerGameOver()
            }
            return
        }

        if (!isGameOver && !isCheckpointComplete) {
            if (timeFreezeLeft > 0) {
                timeFreezeLeft -= deltaTime
            } else {
                val actualDelta = if (slowMotionActive) deltaTime * 0.5f else deltaTime
                timeLeft -= actualDelta
                if (timeLeft <= 0) {
                    timeLeft = 0f
                    if (mode == GameMode.CHALLENGE) {
                        triggerGameOver()
                    } else {
                        handleTimeOut()
                    }
                }
            }
        }
    }

    private fun resetTimer() {
        timeLeft = if (mode == GameMode.CHALLENGE) 30f else 5f
        timeFreezeLeft = 0f
        slowMotionActive = false
        zapEliminatedOption = null
    }

    /**
     * Activates the joker skill for the given cardId.
     * Consumes one charge. Returns true if successfully activated.
     */
    fun activateSkill(cardId: String): Boolean {
        val card = ScientistCards.getCardById(cardId) ?: return false
        val charges = cardCharges[cardId] ?: 0
        if (charges <= 0 || isGameOver || isCheckpointComplete) return false

        // Deduct the charge
        val newCharges = charges - 1
        cardCharges[cardId] = newCharges
        
        // If we just dropped below max, start the recharge timer
        if (newCharges < card.maxCharges && cardLastUseTimes[cardId] == 0L) {
            cardLastUseTimes[cardId] = platformServices.getCurrentTimeMillis()
        }

        when (card.bonusType) {
            BonusType.ADD_TIME -> {
                timeLeft = (timeLeft + 5f).coerceAtMost(60f)
                speedMessage = "⏱️ +5 Saniye!"
            }
            BonusType.FREEZE_TIME -> {
                timeFreezeLeft = 4f
                speedMessage = "❄️ Zaman Donduruldu!"
            }
            BonusType.INSTANT_SCORE -> {
                val bonus = (score * 0.25f).toInt().coerceAtLeast(10)
                score += bonus
                speedMessage = "+$bonus Anlık Puan!"
            }
            BonusType.HINT_ANSWER -> {
                // Eliminate one wrong answer option index
                val correctAnswer = currentQuestion.correctAnswer
                val wrongIndices = currentQuestion.options.indices.filter { idx ->
                    currentQuestion.options[idx] != correctAnswer && idx != zapEliminatedOption
                }
                if (wrongIndices.isNotEmpty()) {
                    zapEliminatedOption = wrongIndices.random()
                    speedMessage = "🔍 Yanlış Şık Elendi!"
                }
            }
            BonusType.EXTRA_LIFE -> {
                shieldsRemaining++
                speedMessage = "🛡️ Can Kalkanı Aktif!"
            }
            BonusType.SLOW_MOTION -> {
                slowMotionActive = true
                speedMessage = "🌀 Zaman Yavaşladı!"
            }
            BonusType.SKIP_QUESTION -> {
                // Skip this question and count it as correct
                zapEliminatedOption = null
                slowMotionActive = false
                speedMessage = "⚡ Soru Atlandı!"
                score += 10
                streak++
                currentQuestionIndex++
                if (currentQuestionIndex >= questionsPerCheckpoint) {
                    completeCheckpoint()
                } else {
                    currentQuestion = generateQuestionForCheckpoint()
                    resetTimer()
                }
                return true
            }
            BonusType.TESLA_ZAP -> {
                // Zap one definitively-wrong answer option
                val correctAnswer = currentQuestion.correctAnswer
                val wrongIndices = currentQuestion.options.indices.filter { idx ->
                    currentQuestion.options[idx] != correctAnswer && idx != zapEliminatedOption
                }
                if (wrongIndices.isNotEmpty()) {
                    zapEliminatedOption = wrongIndices.random()
                    speedMessage = "⚡ Tesla Şimşeği!"
                    soundManager.playSuccess()
                }
            }
        }
        return true
    }

    private fun calculateSpeedBonus(timeSpent: Float): Int {
        return when {
            timeSpent <= 0.5f -> 100
            timeSpent <= 1.0f -> 80
            timeSpent <= 1.5f -> 60
            timeSpent <= 2.0f -> 40
            timeSpent <= 3.0f -> 20
            timeSpent <= 4.0f -> 10
            else -> 0
        }
    }

    private fun getSpeedMessage(timeSpent: Float, currentStreak: Int): String {
        val messages = SpeedMessages.getMessages(Strings.currentLanguage)
        return when {
            timeSpent <= 0.5f -> if (currentStreak >= 10) messages.godLike.random() else if (currentStreak >= 5) messages.legendary.random() else messages.ultraFast.random()
            timeSpent <= 1.0f -> if (currentStreak >= 5) messages.amazing.random() else messages.veryFast.random()
            timeSpent <= 1.5f -> messages.fast.random()
            timeSpent <= 2.0f -> messages.good.random()
            timeSpent <= 3.0f -> messages.normal.random()
            else -> messages.slow.random()
        }
    }

    private fun submitScoreToLeaderboard() {
        val lbm = leaderboardManager ?: return
        if (playerId.isEmpty() || playerName.isEmpty()) return

        val modeStr = when(mode) {
            GameMode.MIXED -> "mixed"
            GameMode.CHALLENGE -> "challenge"
            else -> "classic"
        }

        val totalScore = if (mode == GameMode.CHALLENGE) {
            score.toLong()
        } else {
            val baseScore = (currentCheckpoint * 1000L).toLong() + score.toLong()
            baseScore + (timeLeft * 10).toLong().coerceAtLeast(0)
        }
        
        scope?.launch {
            try { 
                lbm.submitScore(playerId, playerName, totalScore, currentCheckpoint, mode = modeStr)
                dataStore?.saveHighScore(totalScore.toInt(), mode)
            } catch (e: Exception) {}
        }
    }

    fun restart() {
        currentCheckpoint = 1
        currentQuestionIndex = 0
        score = 0
        streak = 0
        consecutiveMistakes = 0
        speedMessage = ""
        speedBonus = 0
        isGameOver = false
        isSaveMePending = false
        saveMeTimeLeft = 3.0f
        isCheckpointComplete = false
        showAdOnGameOver = false
        shieldsRemaining = 0
        timeFreezeLeft = 0f
        zapEliminatedOption = null
        slowMotionActive = false
        totalTimeBonusEarned = 0f
        gameDurationSeconds = 0f
        gameStartTimeMillis = platformServices.getCurrentTimeMillis()

        currentQuestion = generateQuestionForCheckpoint()
        resetTimer()
    }

    fun clearSpeedMessage() {
        speedMessage = ""
        speedBonus = 0
    }

    /** Adds a single charge to the given card (called after 1 ad reward). */
    fun refillCharges(cardId: String) {
        val card = ScientistCards.getCardById(cardId) ?: return
        val current = cardCharges[cardId] ?: 0
        if (current < card.maxCharges) {
            cardCharges[cardId] = current + 1
            // Eğer tamamen dolduysa zamanlayıcıyı sıfırla
            if (cardCharges[cardId] == card.maxCharges) {
                cardLastUseTimes[cardId] = 0L
            }
        }
    }

    /** Second-chance resurrection: refills all lives to MAX and resumes. */
    fun resurrect() {
        isGameOver = false
        isSaveMePending = false
        showAdOnGameOver = false
        livesRemaining = MAX_LIVES
        lastLifeLossTime = 0L
        saveLivesToDisk()
        timeLeft = 3f
        speedMessage = "❤️ CANLAR DOLDU!"
        if (isVoiceEnabled) voiceManager?.speak("İkinci şansın var!")
        currentQuestion = generateQuestionForCheckpoint()
    }

    /** Called periodically (e.g. from UI loop) to handle auto-refill across sessions */
    fun tickRefill() {
        checkAndRefillLives()
        updateRefillTimer()
    }

    var nextHeartRefillSeconds by mutableIntStateOf(0)
        private set

    private fun updateRefillTimer() {
        if (livesRemaining >= MAX_LIVES || lastLifeLossTime <= 0L) {
            nextHeartRefillSeconds = 0
            return
        }
        val now = platformServices.getCurrentTimeMillis()
        val timePassed = now - lastLifeLossTime
        val remainingMs = REFILL_TIME_MS - (timePassed % REFILL_TIME_MS)
        nextHeartRefillSeconds = (remainingMs / 1000).toInt()
    }

    fun getFormattedRefillTime(): String {
        if (livesRemaining >= MAX_LIVES) return ""
        val minutes = nextHeartRefillSeconds / 60
        val seconds = nextHeartRefillSeconds % 60
        val minStr = if (minutes < 10) "0$minutes" else "$minutes"
        val secStr = if (seconds < 10) "0$seconds" else "$seconds"
        return "$minStr:$secStr"
    }

    fun declineSaveMe() {
        triggerGameOver()
    }
}

data class SpeedMessageSet(
    val ultraFast: List<String>, val veryFast: List<String>, val fast: List<String>,
    val good: List<String>, val normal: List<String>, val slow: List<String>,
    val amazing: List<String>, val legendary: List<String>, val godLike: List<String>
)

object SpeedMessages {
    private val turkishMessages = SpeedMessageSet(
        ultraFast = listOf("⚡ EINSTEIN HIZI! ⚡", "🧠 NEWTON'DAN HIZLI! 🧠", "🔥 TESLA ENERJİSİ! 🔥"),
        veryFast = listOf("🎯 ARKİMEDES GİBİ! 🎯", "💡 GAUSS KADAR HIZLI! 💡", "🔥 FERMAT'IN TEORMİ! 🔥"),
        fast = listOf("💪 SÜPERSİN!", "🚀 MÜKEMMEL HIZ!", "🔥 HARİKA!"),
        good = listOf("👍 İYİ GİDİYORSUN!", "💡 AKILLICA!", "🎯 DOĞRU!"),
        normal = listOf("😐 NORMAL...", "⏰ BİRAZ HIZLAN!", "🐢 DAHA HIZLI!"),
        slow = listOf("🐌 KAPLUMBAĞA HIZI...", "⏰ SON SANİYE!", "😴 UYUDUN MU?"),
        amazing = listOf("🔥 GALİLEO GİBİ! 🔥", "⚡ KEPLER HIZI! ⚡", "🧠 COPERNICUS ZEKASI! 🧠"),
        legendary = listOf("👑 EULER'İN TAHTI! 👑", "🏆 GAUSS EFSANESİ! 🏆", "⚡ RAMANUJAN GİBİ! ⚡"),
        godLike = listOf("🌟 GAUSS + EINSTEIN! 🌟", "🔥 TESLA × NEWTON! 🔥", "👑 MATEMATİK TANRISI! 👑")
    )
    private val englishMessages = SpeedMessageSet(
        ultraFast = listOf("⚡ EINSTEIN SPEED! ⚡", "🧠 FASTER THAN NEWTON! 🧠", "🔥 TESLA ENERGY! 🔥"),
        veryFast = listOf("🎯 LIKE ARCHIMEDES! 🎯", "💡 GAUSS FAST! 💡", "🔥 FERMAT'S THEOREM! 🔥"),
        fast = listOf("💪 SUPERB!", "🚀 EXCELLENT SPEED!", "🔥 AWESOME!"),
        good = listOf("👍 GOOD JOB!", "💡 SMART!", "🎯 CORRECT!"),
        normal = listOf("😐 NORMAL...", "⏰ SPEED UP!", "🐢 FASTER!"),
        slow = listOf("🐌 TURTLE SPEED...", "⏰ LAST SECOND!", "😴 FALLING ASLEEP?"),
        amazing = listOf("🔥 LIKE GALILEO! 🔥", "⚡ KEPLER SPEED! ⚡", "🧠 COPERNICUS SMARTS! 🧠"),
        legendary = listOf("👑 EULER'S THRONE! 👑", "🏆 GAUSS LEGEND! 🏆", "⚡ LIKE RAMANUJAN! ⚡"),
        godLike = listOf("🌟 GAUSS + EINSTEIN! 🌟", "🔥 TESLA × NEWTON! 🔥", "👑 MATH GOD! 👑")
    )
    private val russianMessages = SpeedMessageSet(
        ultraFast = listOf("⚡ СКОРОСТЬ ЭЙНШТЕЙНА! ⚡", "🚀 БЫСТРЕЕ НЬЮТОНА! 🚀", "🔥 ЭНЕРГИЯ ТЕСЛЫ! 🔥"),
        veryFast = listOf("🧠 КАК АРХИМЕД! 🧠", "⚡ СКОРОСТЬ ГАУССА! ⚡", "🔥 ТЕОРЕМА ФЕРМА! 🔥"),
        fast = listOf("⚡ ПРЕВОСХОДНО!", "🔥 ОТЛИЧНАЯ СКОРОСТЬ!", "🚀 ПОТРЯСАЮЩЕ!"),
        good = listOf("👍 ХОРОШАЯ РАБОТА!", "🧠 УМНО!", "✅ ВЕРНО!"),
        normal = listOf("😐 НОРМАЛЬНО...", "⚡ УСКОРЯЙСЯ!", "🚀 БЫСТРЕЕ!"),
        slow = listOf("🐢 СКОРОСТЬ ЧЕРЕПАХИ...", "⏳ ПОСЛЕДНЯЯ СЕКУНДА!", "😴 ЗАСЫПАЕШЬ?"),
        amazing = listOf("🌟 КАК ГАЛИЛЕЙ! 🌟", "🚀 СКОРОСТЬ КЕПЛЕРА! 🚀", "🧠 УМ КОПЕРНИКА! 🧠"),
        legendary = listOf("👑 ТРОН ЭЙЛЕРА! 👑", "🌟 ЛЕГЕНДА ГАУССА! 🌟", "🔥 КАК РАМАНУДЖАН! 🔥"),
        godLike = listOf("👑 ГАУСС + ЭЙНШТЕЙН! 👑", "⚡ ТЕСЛА × НЬЮТОН! ⚡", "🌟 БОГ МАТЕМАТИКИ! 🌟")
    )
    private val spanishMessages = SpeedMessageSet(
        ultraFast = listOf("⚡ ¡VELOCIDAD EINSTEIN! ⚡", "🚀 ¡MÁS RÁPIDO QUE NEWTON! 🚀", "🔥 ¡ENERGÍA TESLA! 🔥"),
        veryFast = listOf("🧠 ¡COMO ARQUÍMEDES! 🧠", "⚡ ¡RÁPIDO COMO GAUSS! ⚡", "🔥 ¡TEOREMA DE FERMAT! 🔥"),
        fast = listOf("⚡ ¡SOBERBIO!", "🔥 ¡EXCELENTE VELOCIDAD!", "🚀 ¡INCREÍBLE!"),
        good = listOf("👍 ¡BUEN TRABAJO!", "🧠 ¡INTELIGENTE!", "✅ ¡CORRECTO!"),
        normal = listOf("😐 NORMAL...", "⚡ ¡ACELERA!", "🚀 ¡MÁS RÁPIDO!"),
        slow = listOf("🐢 VELOCIDAD DE TORTUGA...", "⏳ ¡ÚLTIMO SEGUNDO!", "😴 ¿TE ESTÁS DURMIENDO?"),
        amazing = listOf("🌟 ¡COMO GALILEO! 🌟", "🚀 ¡VELOCIDAD KEPLER! 🚀", "🧠 ¡INTELIGENCIA COPÉRNICO! 🧠"),
        legendary = listOf("👑 ¡TRONO DE EULER! 👑", "🌟 ¡LEYENDA GAUSS! 🌟", "🔥 ¡COMO RAMANUJAN! 🔥"),
        godLike = listOf("👑 ¡GAUSS + EINSTEIN! 👑", "⚡ ¡TESLA × NEWTON! ⚡", "🌟 ¡DIOS DE LAS MATES! 🌟")
    )
    private val germanMessages = SpeedMessageSet(
        ultraFast = listOf("⚡ EINSTEIN-TEMPO! ⚡", "🚀 SCHNELLER ALS NEWTON! 🚀", "🔥 TESLA-ENERGIE! 🔥"),
        veryFast = listOf("🧠 WIE ARCHIMEDES! 🧠", "⚡ GAUSS-SCHNELL! ⚡", "🔥 FERMATS SATZ! 🔥"),
        fast = listOf("⚡ HERVORRAGEND!", "🔥 AUSGEZEICHNETES TEMPO!", "🚀 GROSSARTIG!"),
        good = listOf("👍 GUTE ARBEIT!", "🧠 SCHLAU!", "✅ RICHTIG!"),
        normal = listOf("😐 NORMAL...", "⚡ SCHNELLER!", "🚀 GIB GAS!"),
        slow = listOf("🐢 SCHILDKRÖTEN-TEMPO...", "⏳ LETZTE SEKUNDE!", "😴 SCHLÄFST DU?"),
        amazing = listOf("🌟 WIE GALILEI! 🌟", "🚀 KEPLERS TEMPO! 🚀", "🧠 KOPERNIKUS-KLUGHEIT! 🧠"),
        legendary = listOf("👑 EULERS THRON! 👑", "🌟 GAUSS-LEGENDE! 🌟", "🔥 WIE RAMANUJAN! 🔥"),
        godLike = listOf("👑 GAUSS + EINSTEIN! 👑", "⚡ TESLA × NEWTON! ⚡", "🌟 MATHE-GOTT! 🌟")
    )
    private val frenchMessages = SpeedMessageSet(
        ultraFast = listOf("⚡ VITESSE EINSTEIN ! ⚡", "🚀 PLUS RAPIDE QUE NEWTON ! 🚀", "🔥 ÉNERGIE TESLA ! 🔥"),
        veryFast = listOf("🧠 COMME ARCHIMÈDE ! 🧠", "⚡ RAPIDE COMME GAUSS ! ⚡", "🔥 THÉORÈME DE FERMAT ! 🔥"),
        fast = listOf("⚡ SUPERBE !", "🔥 EXCELLENTE VITESSE !", "🚀 INCROYABLE !"),
        good = listOf("👍 BON TRAVAIL !", "🧠 MALIN !", "✅ CORRECT !"),
        normal = listOf("😐 NORMAL...", "⚡ ACCÉLÈRE !", "🚀 PLUS VITE !"),
        slow = listOf("🐢 VITESSE D'ESCARGOT...", "⏳ DERNIÈRE SECONDE !", "😴 TU T'ENDORS ?"),
        amazing = listOf("🌟 COMME GALILÉE ! 🌟", "🚀 VITESSE KEPLER ! 🚀", "🧠 INTELLIGENCE COPERNIC ! 🧠"),
        legendary = listOf("👑 TRÔNE D'EULER ! 👑", "🌟 LÉGENDE GAUSS ! 🌟", "🔥 COMME RAMANUJAN ! 🔥"),
        godLike = listOf("👑 GAUSS + EINSTEIN ! 👑", "⚡ TESLA × NEWTON ! ⚡", "🌟 DIEU DES MATHS ! 🌟")
    )
    private val italianMessages = SpeedMessageSet(
        ultraFast = listOf("⚡ VELOCITÀ EINSTEIN! ⚡", "🚀 PIÙ VELOCE DI NEWTON! 🚀", "🔥 ENERGIA TESLA! 🔥"),
        veryFast = listOf("🧠 COME ARCHIMEDE! 🧠", "⚡ VELOCE COME GAUSS! ⚡", "🔥 TEOREMA DI FERMAT! 🔥"),
        fast = listOf("⚡ SUPERBO!", "🔥 ECCELLENTE VELOCITÀ!", "🚀 STUPENDO!"),
        good = listOf("👍 BUON LAVORO!", "🧠 INTELLIGENTE!", "✅ CORRETTO!"),
        normal = listOf("😐 NORMALE...", "⚡ ACCELERA!", "🚀 PIÙ VELOCE!"),
        slow = listOf("🐢 VELOCITÀ DA LUMACA...", "⏳ ULTIMO SECONDO!", "😴 TI SEI ADDORMENTATO?"),
        amazing = listOf("🌟 COME GALILEO! 🌟", "🚀 VELOCITÀ KEPLER! 🚀", "🧠 INTELLIGENZA COPERNICO! 🧠"),
        legendary = listOf("👑 TRONO DI EULERO! 👑", "🌟 LEGGENDA GAUSS! 🌟", "🔥 COME RAMANUJAN! 🔥"),
        godLike = listOf("👑 GAUSS + EINSTEIN! 👑", "⚡ TESLA × NEWTON! ⚡", "🌟 DIO DELLA MATEMATICA! 🌟")
    )
    private val portugueseMessages = SpeedMessageSet(
        ultraFast = listOf("⚡ VELOCIDADE EINSTEIN! ⚡", "🚀 MAIS RÁPIDO QUE NEWTON! 🚀", "🔥 ENERGIA TESLA! 🔥"),
        veryFast = listOf("🧠 COMO ARQUIMEDES! 🧠", "⚡ RÁPIDO COMO GAUSS! ⚡", "🔥 TEOREMA DE FERMAT! 🔥"),
        fast = listOf("⚡ SOBERBO!", "🔥 EXCELENTE VELOCIDADE!", "🚀 INCRÍVEL!"),
        good = listOf("👍 BOM TRABALHO!", "🧠 INTELIGENTE!", "✅ CORRETO!"),
        normal = listOf("😐 NORMAL...", "⚡ ACELERE!", "🚀 MAIS RÁPIDO!"),
        slow = listOf("🐢 VELOCIDADE DE TARTARUGA...", "⏳ ÚLTIMO SEGUNDO!", "😴 ESTÁ DORMINDO?"),
        amazing = listOf("🌟 COMO GALILEU! 🌟", "🚀 VELOCIDADE KEPLER! 🚀", "🧠 INTELIGÊNCIA COPÉRNICO! 🧠"),
        legendary = listOf("👑 TRONO DE EULER! 👑", "🌟 LENDA GAUSS! 🌟", "🔥 COMO RAMANUJAN! 🔥"),
        godLike = listOf("👑 GAUSS + EINSTEIN! 👑", "⚡ TESLA × NEWTON! ⚡", "🌟 DEUS DA MATEMÁTICA! 🌟")
    )
    private val hindiMessages = SpeedMessageSet(
        ultraFast = listOf("⚡ आइंस्टीन की गति! ⚡", "🚀 न्यूटन से तेज़! 🚀", "🔥 टेस्ला की ऊर्जा! 🔥"),
        veryFast = listOf("🧠 आर्किमिडीज की तरह! 🧠", "⚡ गॉस की गति! ⚡", "🔥 फर्मा का प्रमेय! 🔥"),
        fast = listOf("⚡ शानदार!", "🔥 उत्कृष्ट गति!", "🚀 अद्भुत!"),
        good = listOf("👍 अच्छा काम!", "🧠 बुद्धिमान!", "✅ सही!"),
        normal = listOf("😐 सामान्य...", "⚡ तेज़ हो!", "🚀 और तेज़!"),
        slow = listOf("🐢 कछुए की गति...", "⏳ आखिरी सेकंड!", "😴 सो रहे हो?"),
        amazing = listOf("🌟 गैलीलियो की तरह! 🌟", "🚀 केप्लर की गति! 🚀", "🧠 कोपर्निकस की बुद्धि! 🧠"),
        legendary = listOf("👑 यूलर का सिंहासन! 👑", "🌟 गॉस की किंवदंती! 🌟", "🔥 रामानुजन की तरह! 🔥"),
        godLike = listOf("👑 गॉस + आइंस्टीन! 👑", "⚡ टेस्ला × न्यूटन! ⚡", "🌟 गणित के देवता! 🌟")
    )
    private val chineseMessages = SpeedMessageSet(
        ultraFast = listOf("⚡ 爱因斯坦速度！⚡", "🚀 比牛顿更快！🚀", "🔥 特斯拉能量！🔥"),
        veryFast = listOf("🧠 像阿基米德！🧠", "⚡ 高斯般快速！⚡", "🔥 费马定理！🔥"),
        fast = listOf("⚡ 精彩！", "🔥 出色的速度！", "🚀 太棒了！"),
        good = listOf("👍 干得好！", "🧠 聪明！", "✅ 正确！"),
        normal = listOf("😐 一般...", "⚡ 加速！", "🚀 更快！"),
        slow = listOf("🐢 乌龟速度...", "⏳ 最后一秒！", "😴 睡着了吗？"),
        amazing = listOf("🌟 像加利略！🌟", "🚀 开普勒速度！🚀", "🧠 哥白尼智慧！🧠"),
        legendary = listOf("👑 欧拉的王座！👑", "🌟 高斯传奇！🌟", "🔥 像拉马努金！🔥"),
        godLike = listOf("👑 高斯+爱因斯坦！👑", "⚡ 特斯拉×牛顿！⚡", "🌟 数学之神！🌟")
    )

    fun getMessages(language: AppLanguage): SpeedMessageSet {
        return when (language) {
            AppLanguage.TURKISH -> turkishMessages
            AppLanguage.RUSSIAN -> russianMessages
            AppLanguage.SPANISH -> spanishMessages
            AppLanguage.GERMAN -> germanMessages
            AppLanguage.FRENCH -> frenchMessages
            AppLanguage.ITALIAN -> italianMessages
            AppLanguage.PORTUGUESE -> portugueseMessages
            AppLanguage.HINDI -> hindiMessages
            AppLanguage.CHINESE -> chineseMessages
            else -> englishMessages
        }
    }
}

data class MotivationalMessageSet(
    val correctEasy: List<String>,
    val correctNormal: List<String>,
    val correctHard: List<String>,
    val speed: List<String>,
    val streak: List<String>,
    val recovery: List<String>,
    val elitePraise: List<String>
)

object MotivationalMessages {
    private val turkish = MotivationalMessageSet(
        correctEasy = listOf(
            "Kolay bir ısınmaydı.", "Odaklanman gayet iyi.", "Zihin açıcı bir soru.",
            "Güzel bir başlangıç.", "Gidişatın başarılı.", "Temel yeteneklerin sağlam.", 
            "Bunu rahatlıkla geçtin.", "Hata payı yok, harika.", "Dikkatinden kaçmadı.",
            "Isınma turu bitiyor.", "İşlem yeteneğin kuvvetli.", "Net ve doğru cevap.",
            "Gayet sakin ve başarılı.", "Pratik zekanı konuşturuyorsun.", "Kontrol tamamen sende.",
            "Bu seviye senin için basit kalıyor.", "Kendinden eminsin.", "Ritmini buldun.",
            "Matematik temelin çok güçlü.", "Hiç zorlanmadın, tebrikler."
        ),
        correctNormal = listOf(
            "İşte bu, gerçek bir meydan okuma başlıyor.", "Süper, analitik düşünüyorsun.", 
            "Harika bir ivme yakaladın.", "Mükemmel odaklanma!", "İşler zorlaşıyor ama sen hazırsın.",
            "Doğru mantık, doğru cevap.", "Zihninin sınırlarını genişletiyorsun.", "Beyin fırtınası işe yaradı.",
            "Keskin bir zeka örneği.", "Çok iyi analiz ettin.", "Matematiğin dilini iyi konuşuyorsun.",
            "Muazzam bir performans.", "Böyle devam edersen rekor kırarsın.", "Harika bir işlem dizisi.",
            "Baskı altında çok iyisin.", "Adım adım zirveye çıkıyorsun.", "Sarsılmaz bir mantık kurdun.",
            "Emin adımlarla ilerliyorsun.", "Hızlı ve isabetli bir karar.", "Tam bir problem çözücüsün."
        ),
        correctHard = listOf(
            "İnanılmaz! Bu gerçekten zor bir soruydu.", "Dahice bir çözümdü!", "Zoru başardın, tebrikler!",
            "Gerçek bir matematik dehası gibi çözdün.", "Bu soru herkesin harcı değildi.", "Zekanı ayakta alkışlıyorum.",
            "Bunu nasıl bu kadar çabuk hesapladın?", "Muazzam bir akıl yürütme!", "Limitleri zorlamaya devam ediyorsun.",
            "Matematik olimpiyatlarına hazır gibisin.", "Üst düzey bir performanstı.", "Zaman baskısı altında mükemmel karar.",
            "Senin için imkansız diye bir şey yok.", "Harikulade bir iş çıkardın.", "Bu cevabı vermek ustalık ister.",
            "Karmaşık hesapları basite indirgiyorsun.", "Şaşkınlık verici bir yetenek."
        ),
        speed = listOf(
            "Reflekslerin inanılmaz derecede keskin!", "Adeta bir bilgisayar gibi hesaplıyorsun.",
            "Işık hızında bir çözüm!", "Bu ne hız? Saniyesinde cevapladın.", "Göz açıp kapayıncaya kadar hallettin.",
            "Hızına yetişmek imkansız.", "Zamanı büktün resmen!", "Motor becerilerin ve zekan muhteşem eşleşmiş.",
            "Makine hızında işlem yapıyorsun.", "Sadece bir saniye sürdü, mükemmel!"
        ),
        streak = listOf(
            "Kırılması güç bir seriye imza atıyorsun.", "Kusursuz bir ilerleyiş, seriyi bozma!", 
            "Matematiksel bir ritim yakaladın, hiç durma.", "Bu seri senin ustalığının kanıtı.",
            "Hata yapmayı unuttun resmen!", "İnanılmaz istikrarlı gidiyorsun.", "Pür dikkatsin, odağın hiç kaybolmuyor.",
            "Serin giderek daha da tehlikeli bir hal alıyor.", "Böyle devam edersen efsane olacaksın.",
            "Sakinliğini koruyarak efsanevi bir seri yapıyorsun."
        ),
        recovery = listOf(
            "Ufak bir dikkat dağınıklığı, sorun değil.", "Bazen en iyiler bile hata yapar, devam edelim.",
            "Hemen toparlanıp eskisinden güçlü döneceksin.", "Moral bozmak yok, odağını geri kazan.",
            "Sadece küçük bir aksilik, sıradaki soruda hatanı telafi et.", "Bir sonraki işlemde bunu telafi edersin.",
            "Dikkatini tekrar topla, başarabilirsin.", "Pes etmek senin lügatında yok.",
            "Derin bir nefes al ve bir sonrakine odaklan.", "Bu sadece ısınmaydı, gerçek gücünü şimdi göster."
        ),
        elitePraise = listOf(
            "Durdurulamazsın! Tam bir efsanesin!", 
            "Matematik Tanrısı seviyesindesin!", 
            "İnanılmaz! Rekorlara doymuyorsun!", 
            "Zekana hayran kalmamak elde değil!", 
            "Efsanevi bir seriye imza attın!"
        )
    )
    private val english = MotivationalMessageSet(
        correctEasy = listOf("Easy peasy!", "That was simple.", "Piece of cake!", "Quick work!", "Too easy!"),
        correctNormal = listOf("Excellent!", "Correct!", "Great!", "Nice!", "That's it!"),
        correctHard = listOf("Wow, that was tough!", "Impressive! That was a hard one.", "You tackled a difficult one!", "Great job on a hard problem!", "Challenge accepted and won!"),
        speed = listOf("What a speed! Amazing!", "You're like a storm!", "How are you so fast?", "Moving at light speed!"),
        streak = listOf("Don't break the streak, legendary!", "You've become unstoppable!", "Are you a math genius?", "Doing perfectly, keep going!"),
        recovery = listOf("No problem at all, you'll get the next one!", "Don't lose heart, come on!", "It's okay, you'll do better!", "You can do it again!"),
        elitePraise = listOf(
            "Unstoppable! You are a legend!",
            "You've reached Math God status!",
            "Incredible! Records are falling!",
            "Absolute genius level performance!",
            "A legendary mastery of numbers!"
        )
    )
    private val spanish = MotivationalMessageSet(
        correctEasy = listOf("¡Muy fácil!", "¡Sencillo!", "¡Pan comido!"),
        correctNormal = listOf("¡Excelente!", "¡Correcto!", "¡Genial!", "¡Así se hace!"),
        correctHard = listOf("¡Increíble, era difícil!", "¡Vaya reto!", "¡Qué logro!"),
        speed = listOf("¡Qué velocidad! ¡Increíble!", "¡Eres como un rayo!", "¡Cómo eres tan rápido?", "¡Vas a la velocidad de la luz!"),
        streak = listOf("¡No rompas la racha, legendario!", "¡Te has vuelto imparable!", "¿Eres un genio de las mates?", "¡Lo haces perfecto, sigue así!"),
        recovery = listOf("¡No hay problema, la próxima vez lo logras!", "¡No te desanimar, vamos!", "¡Está bien, lo harás mejor!", "¡Puedes hacerlo de nuevo!"),
        elitePraise = listOf("Unstoppable!", "You are a legend!", "Math God status!", "Incredible!", "Legendary!")
    )
    private val german = MotivationalMessageSet(
        correctEasy = listOf("Ganz einfach!", "Kinderspiel!", "Leicht!"),
        correctNormal = listOf("Ausgezeichnet!", "Richtig!", "Super!", "Gut!", "Genau so!"),
        correctHard = listOf("Wahnsinn, das war schwer!", "Beeindruckend!", "Starke Leistung!"),
        speed = listOf("Was für ein Tempo! Wahnsinn!", "Du bist wie ein Sturm!", "Wie bist du so schnell?", "Lichtgeschwindigkeit!"),
        streak = listOf("Reiß die Serie nicht ab, legendär!", "Du bist unaufhaltbar geworden!", "Bist du ein Mathe-Genie?", "Perfekt so, mach weiter!"),
        recovery = listOf("Kein Problem, das nächste Mal schaffst du es!", "Lass den Kopf nicht hängen, komm schon!", "Alles okay, du wirst es besser machen!", "Du schaffst das noch mal!"),
        elitePraise = listOf("Unstoppable!", "You are a legend!", "Math God status!", "Incredible!", "Legendary!")
    )
    private val french = MotivationalMessageSet(
        correctEasy = listOf("Très facile !", "Simple comme bonjour !", "Facile !"),
        correctNormal = listOf("Excellent !", "Correct !", "Génial !", "Bien !", "C'est ça !"),
        correctHard = listOf("Incroyable, c'était dur !", "Quel défi !", "Impressionnant !"),
        speed = listOf("Quelle vitesse ! Incroyable !", "Tu es comme un éclair !", "Comment es-tu si rapide ?", "Vitesse de la lumière !"),
        streak = listOf("Ne casse pas la série, légendaire !", "Tu es devenu inarrêtable !", "Es-tu un génie des maths ?", "Parfait, continue comme ça !"),
        recovery = listOf("Pas de problème, tu réussiras la prochaine fois !", "Ne te décourage pas, allez !", "C'est bon, tu feras mieux !", "Tu peux le refaire !"),
        elitePraise = listOf("Unstoppable!", "You are a legend!", "Math God status!", "Incredible!", "Legendary!")
    )
    private val italian = MotivationalMessageSet(
        correctEasy = listOf("Molto facile!", "Semplice!", "Un gioco da ragazzi!"),
        correctNormal = listOf("Eccellente!", "Corretto!", "Grande!", "Bene!", "Così si fa!"),
        correctHard = listOf("Incredibile, era difficile!", "Che sfida!", "Impressionante!"),
        speed = listOf("Che velocità! Incredibile!", "Sei come un fulmine!", "Come fai a essere così veloce?", "Velocità della luce!"),
        streak = listOf("Non interrompere la serie, leggendario!", "Sei diventato inarrestabile!", "Sei un genio della matematica?", "Perfetto, continua così!"),
        recovery = listOf("Nessun problema, la prossima volta ce la farai!", "Non scoraggiarti, forza!", "Va bene, farai meglio!", "Puoi farlo di nuovo!"),
        elitePraise = listOf("Unstoppable!", "You are a legend!", "Math God status!", "Incredible!", "Legendary!")
    )
    private val portuguese = MotivationalMessageSet(
        correctEasy = listOf("Muito fácil!", "Simples!", "Moleza!"),
        correctNormal = listOf("Excelente!", "Correto!", "Ótimo!", "Bem!", "É isso aí!"),
        correctHard = listOf("Incrível, era difícil!", "Que desafio!", "Impressionante!"),
        speed = listOf("Que velocidade! Incrível!", "Você é como um raio!", "Como você é tão rápido?", "Velocidade da luz!"),
        streak = listOf("Não quebre a sequência, lendário!", "Você se tornou imparável!", "Você é um gênio da matematica?", "Perfeito, continue assim!"),
        recovery = listOf("Sem problemas, você consegue na próxima!", "Não desanime, vamos!", "Está tudo bem, você fará melhor!", "Você consegue de novo!"),
        elitePraise = listOf("Unstoppable!", "You are a legend!", "Math God status!", "Incredible!", "Legendary!")
    )
    private val hindi = MotivationalMessageSet(
        correctEasy = listOf("बहुत आसान!", "सरल!", "बच्चों का खेल!"),
        correctNormal = listOf("उत्कृष्ट!", "सही!", "बहुत बढ़िया!", "अच्छा!", "ऐसे ही करते रहो!"),
        correctHard = listOf("अद्भुत, यह कठिन था!", "क्या चुनौती है!", "प्रभावशाली!"),
        speed = listOf("क्या गति है! अद्भुत!", "तुम बिजली की तरह हो!", "तुम इतने तेज़ कैसे हो?", "प्रकाश की गति से आगे बढ़ रहे हो!"),
        streak = listOf("क्रम न तोड़ें, शानदार!", "तुम अजेy बन गए हो!", "क्या तुम गणित के प्रतिभाशाली हो?", "बिल्कुल सही, जारी रखें!"),
        recovery = listOf("कोई बात नहीं, अगली बार सफल होगे!", "हिम्मत न हारें, चलो!", "ठीक है, तुम बेहतर करोगे!", "तुम फिर से कर सकते हो!"),
        elitePraise = listOf("Unstoppable!", "You are a legend!", "Math God status!", "Incredible!", "Legendary!")
    )
    private val chinese = MotivationalMessageSet(
        correctEasy = listOf("非常容易！", "简单！", "小菜一碟！"),
        correctNormal = listOf("出色！", "正确！", "太棒了！", "不错！", "就是这样！"),
        correctHard = listOf("不思议，这很难！", "巨大的挑战！", "令人印象深刻！"),
        speed = listOf("好快的速度！不可思议！", "你就像一道闪电！", "你怎么这么快？", "光速前进！"),
        streak = listOf("别断了连胜，传奇！", "你已经势不可挡了！", "你是数学天才吗？", "做得完美，继续！"),
        recovery = listOf("没关系，下次一定行！", "别灰心，加油！", "没事，你会做得更好的！", "你可以再来一次！"),
        elitePraise = listOf("Unstoppable!", "You are a legend!", "Math God status!", "Incredible!", "Legendary!")
    )
    private val russian = MotivationalMessageSet(
        correctEasy = listOf("Очень легко!", "Просто!", "Проще простого!"),
        correctNormal = listOf("Отлично!", "Правильно!", "Супер!", "Хорошо!", "Так держать!"),
        correctHard = listOf("Невероятно, это было трудно!", "Какой вызов!", "Впечатляюще!"),
        speed = listOf("Какая скорость! Невероятно!", "Ты как молния!", "Как ты так быстро?", "Скорость света!"),
        streak = listOf("Не прерывай серию, легенда!", "Тебя не остановить!", "Ты гений математики?", "Отлично, продолжай!"),
        recovery = listOf("Ничего страшного, в следующий раз получится!", "Не сдавайся, давай!", "Все хорошо, ты справишься!", "Ты можешь сделать это снова!"),
        elitePraise = listOf("Unstoppable!", "You are a legend!", "Math God status!", "Incredible!", "Legendary!")
    )


    fun getMessages(language: AppLanguage): MotivationalMessageSet {
        return when (language) {
            AppLanguage.TURKISH -> turkish
            AppLanguage.ENGLISH -> english
            AppLanguage.SPANISH -> spanish
            AppLanguage.GERMAN -> german
            AppLanguage.FRENCH -> french
            AppLanguage.ITALIAN -> italian
            AppLanguage.PORTUGUESE -> portuguese
            AppLanguage.HINDI -> hindi
            AppLanguage.CHINESE -> chinese
            AppLanguage.RUSSIAN -> russian
        }
    }
}

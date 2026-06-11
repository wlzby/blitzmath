package com.mawelly.blitzmath.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mawelly.blitzmath.core.LocalPlatformServices
import com.mawelly.blitzmath.data.IGameDataStore
import com.mawelly.blitzmath.game.*
import com.mawelly.blitzmath.localization.Strings
import com.mawelly.blitzmath.ui.theme.LocalBlitzMathColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sqrt
import kotlin.math.floor

@Composable
fun IosGameScreen(
    mode: GameMode,
    startLevel: Int,
    dataStore: IGameDataStore,
    onBackToMenu: () -> Unit
) {
    val platformServices = LocalPlatformServices.current
    val colors = LocalBlitzMathColors.current
    val scope = rememberCoroutineScope()

    // Database states
    val unlockedCards by dataStore.unlockedCards.collectAsState(initial = emptySet())
    val equippedCards by dataStore.equippedCards.collectAsState(initial = emptySet())
    val starCount by dataStore.starCount.collectAsState(initial = 0)
    val playerXp by dataStore.playerXp.collectAsState(initial = 0)
    
    // Player Level
    val calculatedLevel = floor(sqrt(playerXp / 100.0)).toInt() + 1
    val playerLevel = calculatedLevel.coerceIn(1, 100)

    // Gameplay states
    var currentCheckpoint by remember { mutableStateOf(startLevel) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var streak by remember { mutableStateOf(0) }
    var livesRemaining by remember { mutableStateOf(5) }
    var isGameOver by remember { mutableStateOf(false) }
    var isCheckpointComplete by remember { mutableStateOf(false) }
    
    // Timer states
    val baseTimeLimit = if (mode == GameMode.CHALLENGE) 30f else 5f
    var timeLeft by remember { mutableStateOf(baseTimeLimit) }
    var timeFreezeLeft by remember { mutableStateOf(0f) }
    var slowMotionActive by remember { mutableStateOf(false) }

    // Question states
    var currentQuestion by remember { mutableStateOf<Question?>(null) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isAnswerCorrect by remember { mutableStateOf<Boolean?>(null) }
    var buttonLocked by remember { mutableStateOf(false) }
    
    // Skill/Joker states
    var shieldsRemaining by remember { mutableStateOf(0) }
    var zapEliminatedOption by remember { mutableStateOf<Int?>(null) }
    val cardCharges = remember { mutableStateMapOf<String, Int>() }

    // Setup first question
    val checkpointConfig = remember(currentCheckpoint, currentQuestionIndex) {
        val op = when (mode) {
            GameMode.CLASSIC -> CheckpointManager.getOperationForCheckpoint(currentCheckpoint)
            GameMode.MIXED -> listOf(OperationType.ADDITION, OperationType.SUBTRACTION, OperationType.MULTIPLICATION, OperationType.DIVISION).random()
            GameMode.CHALLENGE -> listOf(OperationType.ADDITION, OperationType.SUBTRACTION, OperationType.MULTIPLICATION, OperationType.DIVISION).random()
            else -> OperationType.ADDITION
        }
        val levelMultiplier = sqrt(playerLevel.toDouble())
        val minVal = (levelMultiplier * 2).toInt().coerceAtLeast(1)
        val maxVal = 10 + (levelMultiplier * 10).toInt()
        
        CheckpointConfig(
            checkpointNumber = currentCheckpoint,
            questionInCheckpoint = currentQuestionIndex + 1,
            operationType = op,
            difficulty = if (mode == GameMode.CHALLENGE) DifficultyLevel.NORMAL else CheckpointManager.getDifficulty(currentCheckpoint),
            timeLimit = baseTimeLimit,
            numberRange = minVal..maxVal
        )
    }

    fun handleWrongAnswer() {
        if (shieldsRemaining > 0) {
            shieldsRemaining--
            platformServices.soundManager.playClick()
            timeLeft = baseTimeLimit
            zapEliminatedOption = null
            slowMotionActive = false
            currentQuestionIndex++
            if (currentQuestionIndex >= 10 && mode != GameMode.CHALLENGE) {
                isCheckpointComplete = true
                platformServices.soundManager.playSuccess()
            }
            return
        }

        streak = 0
        platformServices.soundManager.playError()
        platformServices.hapticManager.triggerError()
        
        if (livesRemaining > 0) {
            livesRemaining--
        }
        
        if (livesRemaining <= 0) {
            isGameOver = true
            platformServices.soundManager.playGameOver()
            scope.launch {
                dataStore.saveHighScore(score, mode)
                dataStore.addStars(score / 10)
            }
        } else {
            selectedOptionIndex = null
            isAnswerCorrect = null
            zapEliminatedOption = null
            slowMotionActive = false
            currentQuestionIndex++
            if (currentQuestionIndex >= 10 && mode != GameMode.CHALLENGE) {
                isCheckpointComplete = true
                platformServices.soundManager.playSuccess()
            } else {
                timeLeft = baseTimeLimit
            }
        }
    }

    fun handleOptionClick(index: Int, optionValue: Int) {
        if (buttonLocked || isGameOver || isCheckpointComplete) return
        buttonLocked = true
        selectedOptionIndex = index
        
        val correct = optionValue == currentQuestion?.correctAnswer
        isAnswerCorrect = correct

        scope.launch {
            delay(350)
            if (correct) {
                platformServices.soundManager.playSuccess()
                platformServices.hapticManager.triggerSuccess()
                streak++
                score += 10 + (timeLeft.toInt() * 2).coerceAtMost(20)
                
                // Challenge mode gets a small time extension on correct answer
                if (mode == GameMode.CHALLENGE) {
                    timeLeft = (timeLeft + 3f).coerceAtMost(60f)
                }

                currentQuestionIndex++
                if (currentQuestionIndex >= 10 && mode != GameMode.CHALLENGE) {
                    isCheckpointComplete = true
                    platformServices.soundManager.playSuccess()
                    dataStore.saveHighScore(score, mode)
                } else {
                    selectedOptionIndex = null
                    isAnswerCorrect = null
                    buttonLocked = false
                }
            } else {
                handleWrongAnswer()
                buttonLocked = false
            }
        }
    }

    fun handleSkillActivate(cardId: String) {
        val card = ScientistCards.getCardById(cardId) ?: return
        val charges = cardCharges[cardId] ?: 0
        if (charges <= 0 || isGameOver || isCheckpointComplete) return
        
        cardCharges[cardId] = charges - 1
        platformServices.soundManager.playClick()
        platformServices.hapticManager.triggerLightImpact()

        when (card.bonusType) {
            BonusType.ADD_TIME -> {
                timeLeft = (timeLeft + 5f).coerceAtMost(60f)
            }
            BonusType.FREEZE_TIME -> {
                timeFreezeLeft = 4f
            }
            BonusType.INSTANT_SCORE -> {
                score += (score * 0.25f).toInt().coerceAtLeast(10)
            }
            BonusType.HINT_ANSWER, BonusType.TESLA_ZAP -> {
                val correctAnswer = currentQuestion?.correctAnswer
                val wrongIndices = currentQuestion?.options?.indices?.filter { idx ->
                    currentQuestion?.options?.get(idx) != correctAnswer && idx != zapEliminatedOption
                } ?: emptyList()
                if (wrongIndices.isNotEmpty()) {
                    zapEliminatedOption = wrongIndices.random()
                }
            }
            BonusType.EXTRA_LIFE -> {
                shieldsRemaining++
            }
            BonusType.SLOW_MOTION -> {
                slowMotionActive = true
            }
            BonusType.SKIP_QUESTION -> {
                score += 10
                streak++
                currentQuestionIndex++
                if (currentQuestionIndex >= 10 && mode != GameMode.CHALLENGE) {
                    isCheckpointComplete = true
                    platformServices.soundManager.playSuccess()
                }
            }
        }
    }

    // Load first question & sync card charges
    LaunchedEffect(Unit) {
        currentQuestion = MathGenerator.generateQuestion(checkpointConfig)
        equippedCards.forEach { id ->
            val card = ScientistCards.getCardById(id)
            if (card != null) {
                cardCharges[id] = card.maxCharges
            }
        }
    }

    // Refresh question on checkpoint/index change
    LaunchedEffect(checkpointConfig) {
        if (!isGameOver && !isCheckpointComplete) {
            currentQuestion = MathGenerator.generateQuestion(checkpointConfig)
            zapEliminatedOption = null
            if (mode != GameMode.CHALLENGE) {
                timeLeft = baseTimeLimit
            }
        }
    }

    // Timer Tick loop
    LaunchedEffect(isGameOver, isCheckpointComplete, currentQuestion) {
        if (!isGameOver && !isCheckpointComplete) {
            while (timeLeft > 0f) {
                delay(50)
                if (isGameOver || isCheckpointComplete) break
                if (timeFreezeLeft > 0f) {
                    timeFreezeLeft -= 0.05f
                } else {
                    val delta = if (slowMotionActive) 0.025f else 0.05f
                    timeLeft = (timeLeft - delta).coerceAtLeast(0f)
                }
            }
            if (timeLeft <= 0f && !isGameOver && !isCheckpointComplete) {
                // Timeout = Wrong answer
                handleWrongAnswer()
            }
        }
    }



    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToMenu,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                }

                Text(
                    text = if (mode == GameMode.CHALLENGE) "CHALLENGE" else "LEVEL $currentCheckpoint",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = livesRemaining.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Row(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = score.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            // Math Question Area
            currentQuestion?.let { question ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = question.displayText,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Timer Progress Bar
                    val progress = timeLeft / baseTimeLimit
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(8.dp)
                            .clip(CircleShape),
                        color = if (timeFreezeLeft > 0f) Color(0xFF00E5FF) else colors.accent,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                    
                    if (timeFreezeLeft > 0f) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("❄️ FREEZE", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Options Grid
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val options = question.options
                    for (i in 0 until 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            for (j in 0 until 2) {
                                val optionIndex = i * 2 + j
                                if (optionIndex < options.size) {
                                    val optionValue = options[optionIndex]
                                    val isZapped = optionIndex == zapEliminatedOption
                                    
                                    val optionBgColor = when {
                                        isZapped -> Color.Gray.copy(alpha = 0.2f)
                                        selectedOptionIndex == optionIndex -> {
                                            if (isAnswerCorrect == true) Color(0xFF4CAF50) else Color(0xFFF44336)
                                        }
                                        else -> Color.White.copy(alpha = 0.05f)
                                    }

                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(72.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .border(
                                                width = 1.dp,
                                                color = if (isZapped) Color.Transparent else Color.White.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .clickable(enabled = !buttonLocked && !isZapped) {
                                                handleOptionClick(optionIndex, optionValue)
                                            },
                                        colors = CardDefaults.cardColors(containerColor = optionBgColor)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (isZapped) "⚡" else optionValue.toString(),
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isZapped) Color.Gray else Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Scientist Jokers Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val equipped = ScientistCards.cards.filter { it.id in equippedCards }
                if (equipped.isEmpty()) {
                    Text(
                        text = "No jokers equipped",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 12.sp
                    )
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        equipped.forEach { card ->
                            val charges = cardCharges[card.id] ?: 0
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable(enabled = charges > 0) {
                                    handleSkillActivate(card.id)
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(
                                            if (charges > 0) colors.accent.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f),
                                            CircleShape
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (charges > 0) colors.accent else Color.Gray,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Whatshot,
                                        contentDescription = null,
                                        tint = if (charges > 0) colors.accent else Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = card.name.take(6),
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "x$charges",
                                    color = colors.accent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Checkpoint Complete Dialog
        if (isCheckpointComplete) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16213E))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CHECKPOINT COMPLETE!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD700)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Score: $score",
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                currentCheckpoint++
                                currentQuestionIndex = 0
                                isCheckpointComplete = false
                                selectedOptionIndex = null
                                isAnswerCorrect = null
                                buttonLocked = false
                                timeLeft = baseTimeLimit
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                        ) {
                            Text("Next Checkpoint", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Game Over Dialog
        if (isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "GAME OVER",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFF44336)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Score: $score",
                            fontSize = 18.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Earned: +${score / 10} Stars",
                            fontSize = 14.sp,
                            color = Color(0xFFFFD700)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = onBackToMenu,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                            ) {
                                Text("Menu", color = Color.White)
                            }
                            Button(
                                onClick = {
                                    score = 0
                                    currentQuestionIndex = 0
                                    livesRemaining = 5
                                    isGameOver = false
                                    selectedOptionIndex = null
                                    isAnswerCorrect = null
                                    buttonLocked = false
                                    timeLeft = baseTimeLimit
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                            ) {
                                Text("Restart", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

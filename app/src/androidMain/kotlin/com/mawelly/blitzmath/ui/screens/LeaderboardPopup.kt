package com.mawelly.blitzmath.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mawelly.blitzmath.LanguageManager
import com.mawelly.blitzmath.leaderboard.LeaderboardEntry
import com.mawelly.blitzmath.leaderboard.LeaderboardManager
import com.mawelly.blitzmath.localization.Strings
import com.mawelly.blitzmath.ui.components.SuccessConfetti
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
actual fun LeaderboardPopup(
    mode: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val languageManager = remember { LanguageManager(context) }
    val leaderboardManager = remember { LeaderboardManager() }
    val listState = rememberLazyListState()

    var leaderboard by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var playerRank by remember { mutableStateOf(0) }
    var showConfetti by remember { mutableStateOf(false) }
    var reachedRank by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    
    val playerId = remember { languageManager.getPlayerId() }

    // Entrance Animation State
    LaunchedEffect(Unit) {
        showContent = true
    }

    LaunchedEffect(Unit) {
        isLoading = true
        val result = leaderboardManager.getGlobalLeaderboard(100, mode = mode)
        result.onSuccess { entries ->
            leaderboard = entries
            isLoading = false
            
            if (playerId.isNotEmpty()) {
                val rankResult = leaderboardManager.getPlayerRank(playerId, mode = mode)
                rankResult.onSuccess { rank -> 
                    playerRank = rank 
                    
                    val targetIndex = entries.indexOfFirst { it.playerId == playerId }
                    if (targetIndex != -1) {
                        delay(1200) // Popup'ın açılma animasyonunun bitmesini bekle
                        
                        // --- PREMIUM TIRMANIŞ ANİMASYONU ---
                        // 1. Aşama: En alttan (veya 60 sıra altından) başlat
                        val climbStartOffset = 60
                        val startIndex = (targetIndex + climbStartOffset).coerceAtMost(entries.size - 1)
                        listState.scrollToItem(startIndex)
                        
                        delay(400) // Oyuncunun konumu görmesi için kısa bir duraksama
                        
                        // 2. Aşama: Hızlı ve Akıcı Tırmanış (Hedefi biraz geçerek/Overshoot)
                        listState.animateScrollToItem(
                            index = (targetIndex - 2).coerceAtLeast(0),
                            scrollOffset = -300
                        )
                        
                        // 3. Aşama: Yumuşak bir şekilde yerine oturma (Settle)
                        listState.animateScrollToItem(
                            index = (targetIndex - 1).coerceAtLeast(0),
                            scrollOffset = -100
                        )
                        
                        reachedRank = true
                        showConfetti = true
                    }
                }
            }
        }.onFailure { 
            isLoading = false
        }
    }

    // Scale and Alpha for the whole popup
    val scale by animateFloatAsState(
        targetValue = if (showContent) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "PopupScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = tween(600),
        label = "PopupAlpha"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background Confetti
            if (showConfetti) {
                SuccessConfetti()
            }

            // Glassmorphism Ana Kart
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .graphicsLayer {
                        this.scaleX = scale
                        this.scaleY = scale
                        this.alpha = alpha
                        shadowElevation = 40f
                        shape = RoundedCornerShape(32.dp)
                        clip = true
                    }
                    .border(
                        1.dp, 
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.4f), 
                                Color.White.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        ), 
                        RoundedCornerShape(32.dp)
                    ),
                color = Color(0xFF080812), // Derin uzay laciverti
                shape = RoundedCornerShape(32.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Modern Header with Animated Gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF1E3A8A),
                                        Color(0xFF080812)
                                    )
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.EmojiEvents, 
                                        null, 
                                        tint = Color(0xFFFFD700), 
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = Strings.globalLeaderboard,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        letterSpacing = 0.5.sp,
                                        maxLines = 1,
                                        modifier = Modifier.basicMarquee()
                                    )
                                }
                                AnimatedVisibility(
                                    visible = reachedRank,
                                    enter = fadeIn() + expandVertically() + slideInVertically()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 44.dp, top = 4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(Color(0xFF00D9FF), CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "LIVE RANKING UPDATED", 
                                            color = Color(0xFF00D9FF),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }
                            }
                            
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    if (isLoading) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                color = Color(0xFF00D9FF), 
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(
                                items = leaderboard,
                                key = { _, entry -> entry.playerId }
                            ) { index, entry ->
                                val isMe = entry.playerId == playerId
                                val rank = index + 1
                                
                                PopupLeaderboardItem(
                                    rank = rank,
                                    entry = entry,
                                    isCurrentPlayer = isMe,
                                    showLevel = mode != "challenge",
                                    isHighlighted = isMe && reachedRank
                                )
                            }
                        }
                    }
                    
                    // Premium Player Bar at the bottom
                    if (playerRank > 0 && !isLoading) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF1E3A8A).copy(alpha = 0.95f),
                            tonalElevation = 20.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 24.dp, vertical = 20.dp)
                                    .navigationBarsPadding(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Animated Rank Badge
                                    val badgeScale by animateFloatAsState(
                                        targetValue = if (reachedRank) 1.15f else 1f,
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy)
                                    )
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .graphicsLayer {
                                                scaleX = badgeScale
                                                scaleY = badgeScale
                                            }
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFF00D9FF), Color(0xFF3B82F6))
                                                ), 
                                                CircleShape
                                            )
                                            .shadow(if (reachedRank) 15.dp else 0.dp, CircleShape, spotColor = Color(0xFF00D9FF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "#$playerRank", 
                                            color = Color.White, 
                                            fontWeight = FontWeight.Black,
                                            fontSize = 18.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = Strings.yourRank.uppercase(),
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = leaderboard.find { it.playerId == playerId }?.playerName ?: "You",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    }
                                }
                                
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${leaderboard.find { it.playerId == playerId }?.totalScore ?: 0}",
                                        color = Color(0xFF00D9FF),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 24.sp
                                    )
                                    if (reachedRank) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .background(Color(0xFF4CAF50).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.KeyboardDoubleArrowUp, 
                                                null, 
                                                tint = Color(0xFF4CAF50), 
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                "RANK UP!", 
                                                color = Color(0xFF4CAF50), 
                                                fontSize = 10.sp, 
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PopupLeaderboardItem(
    rank: Int,
    entry: LeaderboardEntry,
    isCurrentPlayer: Boolean,
    showLevel: Boolean,
    isHighlighted: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isHighlighted) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val highlightColor = when (rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFE0E0E0)
        3 -> Color(0xFFCD7F32)
        else -> if (isCurrentPlayer) Color(0xFF00D9FF) else Color.White
    }

    val containerColor = if (isCurrentPlayer) {
        Color(0xFF00D9FF).copy(alpha = 0.12f)
    } else {
        Color.White.copy(alpha = 0.04f)
    }

    val borderColor = if (isCurrentPlayer) {
        if (isHighlighted) Color(0xFF00D9FF) else Color(0xFF00D9FF).copy(alpha = 0.5f)
    } else {
        Color.White.copy(alpha = 0.08f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = if (isHighlighted) pulseScale else 1f
                scaleY = if (isHighlighted) pulseScale else 1f
                shadowElevation = if (isHighlighted) 20f else 0f
            }
            .border(
                if (isCurrentPlayer) 2.dp else 1.dp,
                borderColor,
                RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(54.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (rank <= 3) {
                    Text(
                        text = when(rank) {
                            1 -> "🥇"
                            2 -> "🥈"
                            3 -> "🥉"
                            else -> ""
                        },
                        fontSize = 28.sp
                    )
                } else {
                    Text(
                        text = "#$rank",
                        color = highlightColor.copy(alpha = if (isCurrentPlayer) 1f else 0.7f),
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.playerName,
                    color = Color.White,
                    fontWeight = if (isCurrentPlayer) FontWeight.ExtraBold else FontWeight.Bold,
                    fontSize = 17.sp,
                    maxLines = 1
                )
                if (showLevel) {
                    Text(
                        text = "${Strings.level} ${entry.highestLevel}",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("%,d", entry.totalScore),
                    color = highlightColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
                if (isCurrentPlayer) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF00D9FF).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "YOU",
                            color = Color(0xFF00D9FF),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

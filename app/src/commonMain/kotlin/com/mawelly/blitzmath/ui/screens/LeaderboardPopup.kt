package com.mawelly.blitzmath.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mawelly.blitzmath.core.LocalPlatformServices
import com.mawelly.blitzmath.data.IGameDataStore
import com.mawelly.blitzmath.leaderboard.ILeaderboardManager
import com.mawelly.blitzmath.leaderboard.LeaderboardEntry
import com.mawelly.blitzmath.localization.Strings
import com.mawelly.blitzmath.ui.components.SuccessConfetti
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LeaderboardPopup(
    dataStore: IGameDataStore,
    mode: String,
    onDismiss: () -> Unit
) {
    val platformServices = LocalPlatformServices.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var leaderboard by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var playerRank by remember { mutableStateOf(0) }
    var showConfetti by remember { mutableStateOf(false) }
    var reachedRank by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    
    val playerId by dataStore.playerId.collectAsState(initial = "")
    val playerName by dataStore.playerName.collectAsState(initial = "")

    val classicHighScore by dataStore.highScore.collectAsState(initial = 0)
    val mixedHighScore by dataStore.mixedHighScore.collectAsState(initial = 0)
    val challengeHighScore by dataStore.challengeHighScore.collectAsState(initial = 0)
    
    val classicLevel by dataStore.classicLevel.collectAsState(initial = 1)
    val mixedLevel by dataStore.mixedLevel.collectAsState(initial = 1)

    val myLocalScore = when(mode) {
        "mixed" -> mixedHighScore
        "challenge" -> challengeHighScore
        else -> classicHighScore
    }
    
    val myLocalLevel = when(mode) {
        "mixed" -> mixedLevel
        "challenge" -> 1
        else -> classicLevel
    }

    // Entrance Animation State
    LaunchedEffect(Unit) {
        showContent = true
    }

    LaunchedEffect(playerId) {
        val manager = platformServices.leaderboardManager
        if (manager == null) {
            isLoading = false
            return@LaunchedEffect
        }
        isLoading = true
        val result = manager.getGlobalLeaderboard(100, mode = mode)
        if (result.isSuccess) {
            val entries = result.getOrNull() ?: emptyList()
            leaderboard = entries
            isLoading = false
            
            if (playerId.isNotEmpty()) {
                val rankResult = manager.getPlayerRank(playerId, mode = mode)
                if (rankResult.isSuccess) {
                    val rank = rankResult.getOrNull() ?: 0
                    playerRank = rank 
                    
                    val targetIndex = entries.indexOfFirst { it.playerId == playerId }
                    if (targetIndex != -1) {
                        delay(1200)
                        
                        val climbStartOffset = 60
                        val startIndex = (targetIndex + climbStartOffset).coerceAtMost(entries.size - 1)
                        listState.scrollToItem(startIndex)
                        
                        delay(400)
                        
                        listState.animateScrollToItem(
                            index = (targetIndex - 2).coerceAtLeast(0),
                            scrollOffset = -300
                        )
                        
                        listState.animateScrollToItem(
                            index = (targetIndex - 1).coerceAtLeast(0),
                            scrollOffset = -100
                        )
                        
                        reachedRank = true
                        showConfetti = true
                    } else if (rank > entries.size) {
                        delay(1200)
                        val startIndex = entries.size - 1
                        listState.scrollToItem(startIndex)
                        
                        delay(400)
                        listState.animateScrollToItem(
                            index = entries.size, // our appended item
                            scrollOffset = -100
                        )
                        
                        reachedRank = true
                        showConfetti = true
                    }
                }
            }
        } else {
            isLoading = false
        }
    }

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
        animationSpec = tween(300),
        label = "PopupAlpha"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable { onDismiss() }
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (showConfetti) {
                SuccessConfetti()
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        alpha = alpha
                    )
                    .clickable(enabled = false) {}
                    .border(
                        1.5.dp,
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        ),
                        RoundedCornerShape(32.dp)
                    ),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0F172A).copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = Strings.globalLeaderboard,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                            
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.05f))
                            ) {
                                Icon(Icons.Default.Close, null, tint = Color.White)
                            }
                        }

                        if (isLoading) {
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                            }
                        } else if (leaderboard.isEmpty()) {
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                Text(Strings.noScoresYet, color = Color.White.copy(alpha = 0.5f))
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                state = listState,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                itemsIndexed(leaderboard) { index, entry ->
                                    val rank = index + 1
                                    val isMe = entry.playerId == playerId
                                    
                                    val itemBgColor = when {
                                        isMe -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                        rank == 1 -> Color(0xFFFFD700).copy(alpha = 0.12f)
                                        rank == 2 -> Color(0xFFC0C0C0).copy(alpha = 0.1f)
                                        rank == 3 -> Color(0xFFCD7F32).copy(alpha = 0.08f)
                                        else -> Color.White.copy(alpha = 0.02f)
                                    }
                                    
                                    val itemBorderColor = when {
                                        isMe -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                                        rank == 1 -> Color(0xFFFFD700).copy(alpha = 0.4f)
                                        rank == 2 -> Color(0xFFC0C0C0).copy(alpha = 0.3f)
                                        rank == 3 -> Color(0xFFCD7F32).copy(alpha = 0.2f)
                                        else -> Color.White.copy(alpha = 0.05f)
                                    }

                                    val itemRankIcon = when (rank) {
                                        1 -> "👑"
                                        2 -> "🥈"
                                        3 -> "🥉"
                                        else -> "#$rank"
                                    }

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(
                                                width = if (isMe || rank <= 3) 1.5.dp else 1.dp,
                                                brush = Brush.linearGradient(listOf(itemBorderColor, Color.Transparent)),
                                                shape = RoundedCornerShape(16.dp)
                                            ),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = itemBgColor)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier.width(40.dp),
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                Text(
                                                    text = itemRankIcon,
                                                    fontSize = if (rank <= 3) 22.sp else 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White.copy(alpha = 0.8f)
                                                )
                                            }

                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val displayCountry = getDisplayCountry(entry.country, entry.playerId)
                                                PlatformFlag(
                                                    countryCode = displayCountry,
                                                    modifier = Modifier
                                                        .padding(end = 8.dp)
                                                        .size(20.dp),
                                                    fallbackSize = 14.sp
                                                )
                                                Column {
                                                    Text(
                                                        text = entry.playerName,
                                                        fontSize = 15.sp,
                                                        fontWeight = if (isMe || rank <= 3) FontWeight.Bold else FontWeight.Medium,
                                                        color = Color.White
                                                    )
                                                    if (mode != "challenge") {
                                                        Text(
                                                            text = "${Strings.level} ${entry.highestLevel}",
                                                            fontSize = 10.sp,
                                                            color = Color.White.copy(alpha = 0.5f)
                                                        )
                                                    }
                                                }
                                            }

                                            Text(
                                                text = "${entry.totalScore}",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = when (rank) {
                                                    1 -> Color(0xFFFFD700)
                                                    2 -> Color(0xFFE2E2E2)
                                                    3 -> Color(0xFFFFA07A)
                                                    else -> Color.White
                                                }
                                            )
                                        }
                                    }
                                }
                                
                                if (playerRank > leaderboard.size) {
                                    item {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "•••",
                                                color = Color.White.copy(alpha = 0.3f),
                                                fontSize = 14.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        
                                        val itemBgColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                        val itemBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                                        
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(
                                                    width = 1.5.dp,
                                                    brush = Brush.linearGradient(listOf(itemBorderColor, Color.Transparent)),
                                                    shape = RoundedCornerShape(16.dp)
                                                ),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = itemBgColor)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier.width(40.dp),
                                                    contentAlignment = Alignment.CenterStart
                                                ) {
                                                    Text(
                                                        text = "#$playerRank",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White.copy(alpha = 0.8f)
                                                    )
                                                }

                                                Row(
                                                    modifier = Modifier.weight(1f),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val displayCountry = platformServices.deviceCountry
                                                    PlatformFlag(
                                                        countryCode = displayCountry,
                                                        modifier = Modifier
                                                            .padding(end = 8.dp)
                                                            .size(20.dp),
                                                        fallbackSize = 14.sp
                                                    )
                                                    Column {
                                                        Text(
                                                            text = playerName,
                                                            fontSize = 15.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White
                                                        )
                                                        if (mode != "challenge") {
                                                            Text(
                                                                text = "${Strings.level} $myLocalLevel",
                                                                fontSize = 10.sp,
                                                                color = Color.White.copy(alpha = 0.5f)
                                                            )
                                                        }
                                                    }
                                                }

                                                Text(
                                                    text = "$myLocalScore",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Bottom Highlight indicator when we reached player rank
                        if (reachedRank && playerRank > 0) {
                            Row(
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Rank: #$playerRank",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

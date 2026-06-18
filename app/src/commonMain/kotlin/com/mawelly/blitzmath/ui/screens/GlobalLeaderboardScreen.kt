package com.mawelly.blitzmath.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.mawelly.blitzmath.leaderboard.ILeaderboardManager
import com.mawelly.blitzmath.leaderboard.LeaderboardEntry
import com.mawelly.blitzmath.localization.Strings
import com.mawelly.blitzmath.ui.theme.LocalBlitzMathColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GlobalLeaderboardScreen(
    dataStore: IGameDataStore,
    initialMode: String = "classic",
    scrollToPlayerId: String? = null,
    onBackToMenu: () -> Unit
) {
    val platformServices = LocalPlatformServices.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var leaderboard by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var playerRank by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedMode by remember { mutableStateOf(initialMode) }
    var lastRefresh by remember { mutableStateOf(0L) }
    var hasScrolledOnce by remember { mutableStateOf(false) }

    val playerId by dataStore.playerId.collectAsState(initial = "")
    val playerName by dataStore.playerName.collectAsState(initial = "")

    val classicHighScore by dataStore.highScore.collectAsState(initial = 0)
    val mixedHighScore by dataStore.mixedHighScore.collectAsState(initial = 0)
    val challengeHighScore by dataStore.challengeHighScore.collectAsState(initial = 0)
    
    val classicLevel by dataStore.classicLevel.collectAsState(initial = 1)
    val mixedLevel by dataStore.mixedLevel.collectAsState(initial = 1)

    val myLocalScore = when(selectedMode) {
        "mixed" -> mixedHighScore
        "challenge" -> challengeHighScore
        else -> classicHighScore
    }
    
    val myLocalLevel = when(selectedMode) {
        "mixed" -> mixedLevel
        "challenge" -> 1
        else -> classicLevel
    }

    fun loadData() {
        val manager = platformServices.leaderboardManager
        if (manager == null) {
            errorMessage = "Leaderboard not supported on this platform"
            isLoading = false
            return
        }
        scope.launch {
            isLoading = true
            errorMessage = null
            val result = manager.getGlobalLeaderboard(25, mode = selectedMode)
            if (result.isSuccess) {
                val entries = result.getOrNull() ?: emptyList()
                leaderboard = entries
                isLoading = false
                lastRefresh = platformServices.getCurrentTimeMillis()
                
                // Compute rank locally from fetched list — avoids a second API call
                if (playerId.isNotEmpty() && entries.isNotEmpty()) {
                    val idx = entries.indexOfFirst { it.playerId == playerId }
                    playerRank = if (idx != -1) idx + 1 else 0
                    
                    // Scroll to player if requested
                    if (scrollToPlayerId == playerId && !hasScrolledOnce && idx != -1) {
                        hasScrolledOnce = true
                        delay(800)
                        val startIndex = (idx + 10).coerceAtMost(entries.size - 1)
                        listState.scrollToItem(startIndex)
                        listState.animateScrollToItem(index = idx, scrollOffset = -100)
                    }
                }
            } else {
                val err = result.exceptionOrNull()?.message ?: "Unknown error"
                errorMessage = if (err.contains("429") || err.contains("quota") || err.contains("Too Many")) {
                    "⚠️ Sunucu meşgul. Lütfen 30 saniye bekleyip tekrar deneyin."
                } else {
                    "Bağlantı hatası: $err"
                }
                isLoading = false
            }
        }
    }

    LaunchedEffect(selectedMode, playerId) { loadData() }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val screenWidth = maxWidth
        
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-100).dp, y = (-50).dp)
                .background(Brush.radialGradient(listOf(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp)
                .background(Brush.radialGradient(listOf(Color(0xFF2196F3).copy(alpha = 0.1f), Color.Transparent)))
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBackToMenu) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                }
                
                Text(
                    text = Strings.globalLeaderboard,
                    fontSize = (screenWidth.value * 0.06f).coerceIn(20f, 28f).sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )

                IconButton(onClick = { loadData() }) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF2196F3))
                }
            }

            // Mode Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    "classic" to Strings.menuClassic,
                    "mixed" to Strings.menuMixed,
                    "challenge" to Strings.menuChallenge
                ).forEach { (mode, label) ->
                    val isSelected = selectedMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent)
                            .clickable { selectedMode = mode }
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label.replace(" MOD", "").replace(" MODE", ""),
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current Player Status
            AnimatedVisibility(
                visible = playerRank > 0,
                enter = fadeIn() + expandVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.05f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "#$playerRank",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val myCountry = platformServices.deviceCountry
                            if (myCountry.isNotEmpty()) {
                                PlatformFlag(
                                    countryCode = myCountry,
                                    modifier = Modifier
                                        .padding(end = 10.dp)
                                        .size(32.dp),
                                    fallbackSize = 24.sp
                                )
                            }
                            Column {
                                Text(
                                    text = playerName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${Strings.yourRank}",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            val currentPlayerEntry = leaderboard.find { it.playerId == playerId }
                            Text(
                                text = "${currentPlayerEntry?.totalScore ?: myLocalScore.toLong()}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = if (selectedMode == "challenge") Strings.statScore else "Lvl ${currentPlayerEntry?.highestLevel ?: myLocalLevel}",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // Leaderboard List
            if (isLoading) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFE94560))
                }
            } else if (errorMessage != null) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, null, tint = Color.Red, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(errorMessage!!, color = Color.White, textAlign = TextAlign.Center)
                        Button(onClick = { loadData() }, modifier = Modifier.padding(16.dp)) {
                            Text(Strings.retry)
                        }
                    }
                }
            } else if (leaderboard.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(Strings.noScoresYet, color = Color.White.copy(alpha = 0.5f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    state = listState,
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(leaderboard) { index, entry ->
                        val rank = index + 1
                        LeaderboardItem(
                            rank = rank,
                            entry = entry,
                            isCurrentPlayer = entry.playerId == playerId,
                            showLevel = selectedMode != "challenge"
                        )
                    }
                    
                    if (playerRank > leaderboard.size) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "•••",
                                    color = Color.White.copy(alpha = 0.3f),
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LeaderboardItem(
                                rank = playerRank,
                                entry = LeaderboardEntry(
                                    playerId = playerId,
                                    playerName = playerName,
                                    totalScore = myLocalScore.toLong(),
                                    highestLevel = myLocalLevel,
                                    country = platformServices.deviceCountry
                                ),
                                isCurrentPlayer = true,
                                showLevel = selectedMode != "challenge"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardItem(
    rank: Int,
    entry: LeaderboardEntry,
    isCurrentPlayer: Boolean,
    showLevel: Boolean = true
) {
    val backgroundColor = when (rank) {
        1 -> Color(0xFFFFD700).copy(alpha = 0.15f)
        2 -> Color(0xFFC0C0C0).copy(alpha = 0.12f)
        3 -> Color(0xFFCD7F32).copy(alpha = 0.1f)
        else -> if (isCurrentPlayer) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f)
    }

    val borderColor = when (rank) {
        1 -> Color(0xFFFFD700).copy(alpha = 0.5f)
        2 -> Color(0xFFC0C0C0).copy(alpha = 0.4f)
        3 -> Color(0xFFCD7F32).copy(alpha = 0.3f)
        else -> if (isCurrentPlayer) MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f)
    }

    val rankIcon = when (rank) {
        1 -> "👑"
        2 -> "🥈"
        3 -> "🥉"
        else -> "#$rank"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (rank <= 3 || isCurrentPlayer) 1.5.dp else 1.dp,
                brush = Brush.linearGradient(listOf(borderColor, Color.Transparent)),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Number/Icon
            Box(
                modifier = Modifier.width(44.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = rankIcon,
                    fontSize = if (rank <= 3) 24.sp else 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (rank <= 3) Color.Unspecified else Color.White.copy(alpha = 0.7f)
                )
            }

            // Player Info
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val displayCountry = getDisplayCountry(entry.country, entry.playerId)
                PlatformFlag(
                    countryCode = displayCountry,
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .size(24.dp),
                    fallbackSize = 18.sp
                )
                Column {
                    Text(
                        text = entry.playerName,
                        fontSize = 16.sp,
                        fontWeight = if (rank <= 3 || isCurrentPlayer) FontWeight.Bold else FontWeight.Medium,
                        color = Color.White
                    )
                    if (showLevel) {
                        Text(
                            text = "${Strings.level} ${entry.highestLevel}",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Score
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${entry.totalScore}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = when (rank) {
                        1 -> Color(0xFFFFD700)
                        2 -> Color(0xFFE2E2E2)
                        3 -> Color(0xFFFFA07A)
                        else -> Color.White
                    }
                )
                if (rank == 1) {
                    Text(
                        text = Strings.topScore,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700).copy(alpha = 0.7f),
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

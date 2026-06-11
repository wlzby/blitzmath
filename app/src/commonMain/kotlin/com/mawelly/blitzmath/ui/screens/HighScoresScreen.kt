package com.mawelly.blitzmath.ui.screens

import com.mawelly.blitzmath.core.LocalPlatformServices

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mawelly.blitzmath.localization.Strings

data class ScoreEntry(
    val rank: Int,
    val score: Int,
    val level: Int,
    val date: String
)

@Composable
fun HighScoresScreen(
    onBackToMenu: () -> Unit
) {
    // Örnek skorlar (gerçek veri yerine)
    val scores = remember {
        listOf(
            ScoreEntry(1, 5000, 25, "11.03.2026"),
            ScoreEntry(2, 4200, 20, "10.03.2026"),
            ScoreEntry(3, 3800, 18, "09.03.2026"),
            ScoreEntry(4, 3100, 15, "08.03.2026"),
            ScoreEntry(5, 2500, 12, "07.03.2026")
        )
    }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0D0D15),
            Color(0xFF1A1A2E),
            Color(0xFF16213E),
            Color(0xFF0F3460)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Üst Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackToMenu) {
                    Text(
                        text = "←",
                        fontSize = 30.sp,
                        color = Color.White
                    )
                }

                Text(
                    text = Strings.highScores,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Skor Listesi
            if (scores.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = Strings.noScoresYet,
                        fontSize = 18.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            } else {
                // Başlık Satırı
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "#",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE94560),
                        modifier = Modifier.width(40.dp)
                    )
                    Text(
                        text = Strings.score,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE94560),
                        modifier = Modifier.width(80.dp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = Strings.level,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE94560),
                        modifier = Modifier.width(60.dp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = Strings.date,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE94560),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }

                Divider(color = Color.White.copy(alpha = 0.2f))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    itemsIndexed(scores) { index, score ->
                        ScoreRow(
                            score = score,
                            isTop3 = index < 3
                        )
                    }
                }

                // Temizle Butonu
                Button(
                    onClick = { /* Skorları temizle */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE94560).copy(alpha = 0.8f)
                    )
                ) {
                    Text(
                        text = Strings.clearAll,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreRow(
    score: ScoreEntry,
    isTop3: Boolean
) {
    val rankColor = when (score.rank) {
        1 -> Color(0xFFFFD700) // Altın
        2 -> Color(0xFFC0C0C0) // Gümüş
        3 -> Color(0xFFCD7F32) // Bronz
        else -> Color.White
    }

    val backgroundColor = if (isTop3) {
        rankColor.copy(alpha = 0.1f)
    } else {
        Color.White.copy(alpha = 0.05f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank
            Box(
                modifier = Modifier.width(40.dp),
                contentAlignment = Alignment.Center
            ) {
                if (score.rank <= 3) {
                    Text(
                        text = when (score.rank) {
                            1 -> "🥇"
                            2 -> "🥈"
                            3 -> "🥉"
                            else -> score.rank.toString()
                        },
                        fontSize = 24.sp
                    )
                } else {
                    Text(
                        text = score.rank.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = rankColor
                    )
                }
            }

            // Score
            Text(
                text = score.score.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF00d9ff),
                modifier = Modifier.width(80.dp),
                textAlign = TextAlign.Center
            )

            // Level
            Text(
                text = score.level.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFf9a825),
                modifier = Modifier.width(60.dp),
                textAlign = TextAlign.Center
            )

            // Date
            Text(
                text = score.date,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }
    }
}
package com.mawelly.blitzmath.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
actual fun VsScreen(onBackToMenu: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "VS Mode is coming soon to iOS!",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBackToMenu) {
                Text("Back to Menu")
            }
        }
    }
}

@Composable
actual fun GlobalLeaderboardScreen(
    initialMode: String,
    scrollToPlayerId: String?,
    onBackToMenu: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Leaderboards are coming soon to iOS!",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBackToMenu) {
                Text("Back to Menu")
            }
        }
    }
}

@Composable
actual fun LeaderboardPopup(
    mode: String,
    onDismiss: () -> Unit
) {
    // No-op stub for iOS
}

@Composable
actual fun AppLifecycleObserver(onPause: () -> Unit, onResume: () -> Unit) {
    // No-op stub for iOS
}


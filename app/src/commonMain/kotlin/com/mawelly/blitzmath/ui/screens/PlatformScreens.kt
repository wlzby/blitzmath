package com.mawelly.blitzmath.ui.screens

import androidx.compose.runtime.Composable

@Composable
expect fun VsScreen(onBackToMenu: () -> Unit)

@Composable
expect fun GlobalLeaderboardScreen(
    initialMode: String = "classic",
    scrollToPlayerId: String? = null,
    onBackToMenu: () -> Unit
)

@Composable
expect fun LeaderboardPopup(
    mode: String,
    onDismiss: () -> Unit
)
@Composable
expect fun AppLifecycleObserver(onPause: () -> Unit, onResume: () -> Unit)

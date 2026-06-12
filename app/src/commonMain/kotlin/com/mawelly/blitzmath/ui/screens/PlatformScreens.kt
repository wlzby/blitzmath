package com.mawelly.blitzmath.ui.screens

import androidx.compose.runtime.Composable

@Composable
expect fun VsScreen(onBackToMenu: () -> Unit)


@Composable
expect fun AppLifecycleObserver(onPause: () -> Unit, onResume: () -> Unit)

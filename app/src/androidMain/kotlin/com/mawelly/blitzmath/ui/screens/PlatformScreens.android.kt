package com.mawelly.blitzmath.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
actual fun AppLifecycleObserver(onPause: () -> Unit, onResume: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> onPause()
                Lifecycle.Event.ON_RESUME -> onResume()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@Composable
actual fun PlatformFlag(
    countryCode: String,
    modifier: Modifier,
    fallbackSize: androidx.compose.ui.unit.TextUnit,
    fallbackScale: Float
) {
    GlossyCircularFlag(
        countryCode = countryCode,
        modifier = modifier,
        fallbackSize = fallbackSize,
        fallbackScale = fallbackScale
    )
}

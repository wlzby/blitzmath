package com.mawelly.blitzmath.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.DisposableEffect
import platform.Foundation.NSNotificationCenter
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationWillEnterForegroundNotification


@Composable
actual fun PlatformFlag(
    countryCode: String,
    modifier: Modifier,
    fallbackSize: androidx.compose.ui.unit.TextUnit,
    fallbackScale: Float
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = getFlagEmoji(countryCode),
            fontSize = fallbackSize,
            modifier = Modifier.scale(fallbackScale)
        )
    }
}

@Composable
actual fun AppLifecycleObserver(onPause: () -> Unit, onResume: () -> Unit) {
    DisposableEffect(Unit) {
        val pauseObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationDidEnterBackgroundNotification,
            `object` = null,
            queue = null
        ) { _ ->
            onPause()
        }

        val resumeObserver = NSNotificationCenter.defaultCenter.addObserverForName(
            name = UIApplicationWillEnterForegroundNotification,
            `object` = null,
            queue = null
        ) { _ ->
            onResume()
        }

        onDispose {
            NSNotificationCenter.defaultCenter.removeObserver(pauseObserver)
            NSNotificationCenter.defaultCenter.removeObserver(resumeObserver)
        }
    }
}


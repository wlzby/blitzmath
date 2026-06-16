package com.mawelly.blitzmath.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
expect fun VsScreen(onBackToMenu: () -> Unit)

@Composable
expect fun AppLifecycleObserver(onPause: () -> Unit, onResume: () -> Unit)

@Composable
expect fun PlatformFlag(
    countryCode: String,
    modifier: Modifier,
    fallbackSize: TextUnit = 24.sp,
    fallbackScale: Float = 1.0f
)

fun getFlagEmoji(countryCode: String): String {
    if (countryCode.length != 2) return "❓"
    val first = countryCode[0].uppercaseChar()
    val second = countryCode[1].uppercaseChar()
    if (first !in 'A'..'Z' || second !in 'A'..'Z') return "❓"
    
    val high1 = 0xD83C.toChar()
    val low1 = (0xDDE6 + (first.code - 'A'.code)).toChar()
    
    val high2 = 0xD83C.toChar()
    val low2 = (0xDDE6 + (second.code - 'A'.code)).toChar()
    
    return "$high1$low1$high2$low2"
}

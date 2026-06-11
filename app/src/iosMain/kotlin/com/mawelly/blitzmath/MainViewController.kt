package com.mawelly.blitzmath

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.ComposeUIViewController
import com.mawelly.blitzmath.core.IosPlatformServices
import com.mawelly.blitzmath.core.LocalPlatformServices

fun MainViewController() = ComposeUIViewController {
    CompositionLocalProvider(LocalPlatformServices provides IosPlatformServices()) {
        App()
    }
}

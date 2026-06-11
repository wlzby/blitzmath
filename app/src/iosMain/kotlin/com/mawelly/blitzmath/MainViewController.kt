package com.mawelly.blitzmath

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.mawelly.blitzmath.core.IosPlatformServices
import com.mawelly.blitzmath.core.LocalPlatformServices
import com.mawelly.blitzmath.data.IosGameDataStore

fun MainViewController() = ComposeUIViewController {
    val dataStore = remember { IosGameDataStore() }
    CompositionLocalProvider(LocalPlatformServices provides IosPlatformServices()) {
        App(dataStore = dataStore)
    }
}

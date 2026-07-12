package com.mawelly.blitzmath

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.mawelly.blitzmath.core.IAdController
import com.mawelly.blitzmath.core.IosPlatformServices
import com.mawelly.blitzmath.core.LocalPlatformServices
import com.mawelly.blitzmath.core.IMultiplayerController
import com.mawelly.blitzmath.data.IosGameDataStore
import com.mawelly.blitzmath.audio.IosVoiceManager


fun MainViewController(
    customAdController: IAdController? = null,
    customMultiplayerController: IMultiplayerController? = null
) = ComposeUIViewController {
    val dataStore = remember { IosGameDataStore() }
    val voiceManager = remember { IosVoiceManager() }
    CompositionLocalProvider(LocalPlatformServices provides IosPlatformServices(customAdController, customMultiplayerController)) {
        App(dataStore = dataStore, voiceManager = voiceManager)
    }
}

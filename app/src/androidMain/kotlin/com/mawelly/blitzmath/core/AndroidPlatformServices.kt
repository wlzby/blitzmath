package com.mawelly.blitzmath.core

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.mawelly.blitzmath.ads.IAdManager
import com.mawelly.blitzmath.analytics.AnalyticsManager
import com.mawelly.blitzmath.audio.SoundManager
import com.mawelly.blitzmath.utils.HapticManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AndroidSoundManager(private val soundManager: SoundManager) : ISoundManager {
    override fun playClick() = soundManager.playClick()
    override fun playSuccess() = soundManager.playSuccess()
    override fun playError() = soundManager.playError()
    override fun playGameOver() = soundManager.playGameOver()
    override fun setEnabled(enabled: Boolean) {
        if (enabled) soundManager.resumeBGM() else soundManager.stopBGM()
    }
}

class AndroidHapticManager(private val context: Context) : IHapticManager {
    override fun triggerLightImpact() = HapticManager.triggerLightImpact(context)
    override fun triggerMediumImpact() = HapticManager.triggerMediumImpact(context)
    override fun triggerHeavyImpact() = HapticManager.triggerHeavyImpact(context)
    override fun triggerError() = HapticManager.triggerError(context)
    override fun triggerSuccess() = HapticManager.triggerSuccess(context)
}

class AndroidAnalyticsManager(private val analyticsManager: AnalyticsManager) : IAnalyticsManager {
    override fun logEvent(eventName: String, params: Map<String, Any>) {
        // Simple mapping, actual implementation might vary
        analyticsManager.logEvent(eventName)
    }
}

class AndroidShareManager(private val context: Context) : IShareManager {
    override fun shareScore(score: Int) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "I just scored $score in BlitzMath!")
        }
        context.startActivity(Intent.createChooser(intent, "Share Score"))
    }
}

class AndroidAdController(
    private val activity: Activity,
    private val adManager: IAdManager
) : IAdController {
    override fun showInterstitialAd(onClosed: () -> Unit) {
        adManager.onGameOver(activity) { onClosed() }
    }

    override fun showRewardedAd(onReward: () -> Unit, onClosed: () -> Unit) {
        adManager.showAd(activity, IAdManager.Placement.SAVE_ME) {
            onReward()
            onClosed()
        }
    }
}

class AndroidPlatformServices(
    private val activity: Activity,
    private val context: Context,
    adManager: IAdManager,
    analyticsManager: AnalyticsManager,
    soundManager: SoundManager
) : PlatformServices {
    override val soundManager: ISoundManager = AndroidSoundManager(soundManager)
    override val hapticManager: IHapticManager = AndroidHapticManager(context)
    override val analyticsManager: IAnalyticsManager = AndroidAnalyticsManager(analyticsManager)
    override val shareManager: IShareManager = AndroidShareManager(context)
    override val adController: IAdController = AndroidAdController(activity, adManager)

    override fun getCurrentDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}

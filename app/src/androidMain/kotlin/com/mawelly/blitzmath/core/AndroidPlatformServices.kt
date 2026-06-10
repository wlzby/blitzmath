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
    override fun playClick() = soundManager.playCorrect()
    override fun playSuccess() = soundManager.playLevelUp()
    override fun playError() = soundManager.playWrong()
    override fun playGameOver() = soundManager.playGameOver()
    override fun setEnabled(enabled: Boolean) {
        if (enabled) soundManager.resumeBGM() else soundManager.stopBGM()
    }
}

class AndroidHapticManager(private val context: Context) : IHapticManager {
    private val hapticManager = HapticManager(context)
    override fun triggerLightImpact() = hapticManager.vibrateTick(true, 0.3f)
    override fun triggerMediumImpact() = hapticManager.vibrateTick(true, 0.6f)
    override fun triggerHeavyImpact() = hapticManager.vibrateTick(true, 1.0f)
    override fun triggerError() = hapticManager.vibrateWrong(true, 1.0f)
    override fun triggerSuccess() = hapticManager.vibrateTick(true, 1.0f)
}

class AndroidAnalyticsManager(private val analyticsManager: AnalyticsManager) : IAnalyticsManager {
    override fun logEvent(eventName: String, params: Map<String, Any>) {
        analyticsManager.logScreenView(eventName)
    }
    override fun logScreenView(screenName: String) = analyticsManager.logScreenView(screenName)
    override fun logModeSelection(modeName: String) = analyticsManager.logModeSelection(modeName)
    override fun logRefillLivesClick(source: String) = analyticsManager.logRefillLivesClick(source)
    override fun logAdClick(adUnitName: String) = analyticsManager.logAdClick(adUnitName)
    override fun logAdReward(adUnitName: String) = analyticsManager.logAdReward(adUnitName)
}

class AndroidShareManager(private val context: Context) : IShareManager {
    override fun shareScore(score: Int) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "I just scored \$score in BlitzMath!")
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
    override fun getCurrentTimeMillis(): Long = System.currentTimeMillis()
    override val shareManager: IShareManager = AndroidShareManager(context)
    override val adController: IAdController = AndroidAdController(activity, adManager)

    override fun getCurrentDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}

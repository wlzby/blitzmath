package com.mawelly.blitzmath.core

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.mawelly.blitzmath.ads.IAdManager
import com.mawelly.blitzmath.analytics.AnalyticsManager
import com.mawelly.blitzmath.audio.SoundManager
import com.mawelly.blitzmath.utils.HapticManager
import com.mawelly.blitzmath.leaderboard.ILeaderboardManager
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
    override fun stopBGM() = soundManager.stopBGM()
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
    override fun logGameEnd(mode: String, score: Long, success: Boolean) = analyticsManager.logGameEnd(mode, score, success)
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
    override fun getCurrentTimeMillis(): Long = System.currentTimeMillis()
    override val shareManager: IShareManager = AndroidShareManager(context)
    override val adController: IAdController = AndroidAdController(activity, adManager)
    override val leaderboardManager: ILeaderboardManager = com.mawelly.blitzmath.leaderboard.LeaderboardManager()

    override fun openUrl(url: String) {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    override fun getCurrentDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    override fun scheduleCardRecharge(cardId: String, delayMinutes: Long) {
        try {
            val workManager = androidx.work.WorkManager.getInstance(context.applicationContext)
            val workRequest = androidx.work.OneTimeWorkRequestBuilder<com.mawelly.blitzmath.notifications.RechargeWorker>()
                .setInitialDelay(delayMinutes, java.util.concurrent.TimeUnit.MINUTES)
                .setInputData(androidx.work.workDataOf("card_id" to cardId))
                .addTag("recharge_$cardId")
                .build()

            workManager.enqueueUniqueWork(
                "recharge_$cardId",
                androidx.work.ExistingWorkPolicy.REPLACE,
                workRequest
            )
        } catch (e: Exception) {
            android.util.Log.e("AndroidPlatformServices", "Error scheduling card recharge: ${e.message}")
        }
    }

    override fun cancelCardRecharge(cardId: String) {
        try {
            val workManager = androidx.work.WorkManager.getInstance(context.applicationContext)
            workManager.cancelUniqueWork("recharge_$cardId")
        } catch (e: Exception) {
            android.util.Log.e("AndroidPlatformServices", "Error canceling card recharge: ${e.message}")
        }
    }

    override fun showAppReview() {
        try {
            val datastoreImpl = com.mawelly.blitzmath.data.GameDataStore(context)
            com.mawelly.blitzmath.utils.AppReviewManager.showReviewDialog(activity, datastoreImpl) {
                android.util.Log.d("AndroidPlatformServices", "App review completed.")
            }
        } catch (e: Exception) {
            android.util.Log.e("AndroidPlatformServices", "Error triggering app review: ${e.message}")
        }
    }
}

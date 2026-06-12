package com.mawelly.blitzmath.core

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSURL
import platform.Foundation.NSBundle
import platform.UIKit.UIApplication
import platform.Foundation.timeIntervalSince1970

import platform.AVFAudio.AVAudioPlayer
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType

class IosSoundManager : ISoundManager {
    private var isEnabled = true
    private val players = mutableMapOf<String, AVAudioPlayer>()

    private fun getPlayer(fileName: String, type: String): AVAudioPlayer? {
        if (players.containsKey(fileName)) {
            return players[fileName]
        }
        val path = NSBundle.mainBundle.pathForResource("compose-resources/files/$fileName", type)
            ?: NSBundle.mainBundle.pathForResource(fileName, type)
        if (path != null) {
            val url = NSURL.fileURLWithPath(path)
            val player = AVAudioPlayer(contentsOfURL = url, error = null)
            player.prepareToPlay()
            players[fileName] = player
            return player
        }
        return null
    }

    private fun playSound(fileName: String, type: String) {
        if (!isEnabled) return
        val player = getPlayer(fileName, type)
        if (player != null) {
            player.setCurrentTime(0.0)
            player.play()
        }
    }

    override fun playClick() {
        playSound("sound_level_up", "wav")
    }

    override fun playSuccess() {
        playSound("sound_correct", "wav")
    }

    override fun playError() {
        playSound("sound_wrong", "wav")
    }

    override fun playGameOver() {
        playSound("sound_game_over", "wav")
    }

    override fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }
}

class IosHapticManager : IHapticManager {
    override fun triggerLightImpact() {
        UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleLight).impactOccurred()
    }

    override fun triggerMediumImpact() {
        UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium).impactOccurred()
    }

    override fun triggerHeavyImpact() {
        UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy).impactOccurred()
    }

    override fun triggerError() {
        UINotificationFeedbackGenerator().notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeError)
    }

    override fun triggerSuccess() {
        UINotificationFeedbackGenerator().notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
    }
}

class IosAnalyticsManager : IAnalyticsManager {
    override fun logEvent(eventName: String, params: Map<String, Any>) {}
    override fun logScreenView(screenName: String) {}
    override fun logModeSelection(modeName: String) {}
    override fun logRefillLivesClick(source: String) {}
    override fun logAdClick(adUnitName: String) {}
    override fun logAdReward(adUnitName: String) {}
    override fun logGameEnd(mode: String, score: Long, success: Boolean) {}
}

class IosShareManager : IShareManager {
    override fun shareScore(score: Int) {}
}

class IosAdController : IAdController {
    override fun showInterstitialAd(onClosed: () -> Unit) { onClosed() }
    override fun showRewardedAd(onReward: () -> Unit, onClosed: () -> Unit) {
        onReward()
        onClosed()
    }
}

class IosPlatformServices : PlatformServices {
    override val soundManager: ISoundManager = IosSoundManager()
    override val hapticManager: IHapticManager = IosHapticManager()
    override val analyticsManager: IAnalyticsManager = IosAnalyticsManager()
    override val shareManager: IShareManager = IosShareManager()
    override val adController: IAdController = IosAdController()

    override fun getCurrentTimeMillis(): Long {
        return (NSDate().timeIntervalSince1970 * 1000).toLong()
    }

    override fun getCurrentDateString(): String {
        val formatter = NSDateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter.stringFromDate(NSDate())
    }

    override fun openUrl(url: String) {
        val nsUrl = NSURL.URLWithString(url) ?: return
        UIApplication.sharedApplication.openURL(nsUrl)
    }
}

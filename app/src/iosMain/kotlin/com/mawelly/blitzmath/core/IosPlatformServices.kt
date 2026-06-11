package com.mawelly.blitzmath.core

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.Foundation.timeIntervalSince1970

class IosSoundManager : ISoundManager {
    override fun playClick() {}
    override fun playSuccess() {}
    override fun playError() {}
    override fun playGameOver() {}
    override fun setEnabled(enabled: Boolean) {}
}

class IosHapticManager : IHapticManager {
    override fun triggerLightImpact() {}
    override fun triggerMediumImpact() {}
    override fun triggerHeavyImpact() {}
    override fun triggerError() {}
    override fun triggerSuccess() {}
}

class IosAnalyticsManager : IAnalyticsManager {
    override fun logEvent(eventName: String, params: Map<String, Any>) {}
    override fun logScreenView(screenName: String) {}
    override fun logModeSelection(modeName: String) {}
    override fun logRefillLivesClick(source: String) {}
    override fun logAdClick(adUnitName: String) {}
    override fun logAdReward(adUnitName: String) {}
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

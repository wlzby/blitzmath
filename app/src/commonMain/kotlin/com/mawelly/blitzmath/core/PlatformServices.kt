package com.mawelly.blitzmath.core

interface ISoundManager {
    fun playClick()
    fun playSuccess()
    fun playError()
    fun playGameOver()
    fun setEnabled(enabled: Boolean)
}

interface IHapticManager {
    fun triggerLightImpact()
    fun triggerMediumImpact()
    fun triggerHeavyImpact()
    fun triggerError()
    fun triggerSuccess()
}

interface IAnalyticsManager {
    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap())
    fun logScreenView(screenName: String)
    fun logModeSelection(modeName: String)
    fun logRefillLivesClick(source: String)
    fun logAdClick(adUnitName: String)
    fun logAdReward(adUnitName: String)
}

interface IShareManager {
    fun shareScore(score: Int)
}

interface IAdController {
    fun showInterstitialAd(onClosed: () -> Unit)
    fun showRewardedAd(onReward: () -> Unit, onClosed: () -> Unit)
}

// A unified container for all platform-specific services
interface PlatformServices {
    fun getCurrentTimeMillis(): Long
    val soundManager: ISoundManager
    val hapticManager: IHapticManager
    val analyticsManager: IAnalyticsManager
    val shareManager: IShareManager
    val adController: IAdController
    fun getCurrentDateString(): String
}

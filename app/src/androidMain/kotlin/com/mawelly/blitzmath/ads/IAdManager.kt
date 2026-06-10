package com.mawelly.blitzmath.ads

import android.app.Activity

interface IAdManager {
    enum class Placement(val key: String) {
        SAVE_ME("save_me"),
        REFILL_CHARGES("refill_charges"),
        UNLOCK_SCIENTIST("unlock_scientist"),
        DAILY_BONUS("daily_bonus")
    }

    fun preloadAll()
    fun showAd(activity: Activity, placement: Placement, onReward: () -> Unit)
    fun onGameOver(activity: Activity, onAdClosed: () -> Unit = {})
    fun isAdReady(): Boolean
    fun isAdReady(placement: Placement): Boolean
}

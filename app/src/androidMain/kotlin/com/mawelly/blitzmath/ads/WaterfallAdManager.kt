package com.mawelly.blitzmath.ads

import android.app.Activity
import android.util.Log

class WaterfallAdManager(
    private val primary: IAdManager, // AdMob
    private val secondary: IAdManager // Unity
) : IAdManager {

    private val TAG = "WaterfallAdManager"

    override fun preloadAll() {
        primary.preloadAll()
        secondary.preloadAll()
    }

    override fun showAd(activity: Activity, placement: IAdManager.Placement, onReward: () -> Unit) {
        val isPrimaryReady = primary.isAdReady(placement)
        val isSecondaryReady = secondary.isAdReady(placement)
        
        Log.d(TAG, "Waterfall: showAd called for ${placement.key}. Primary ready: $isPrimaryReady, Secondary ready: $isSecondaryReady")

        when {
            isPrimaryReady -> {
                Log.d(TAG, "Waterfall: Using AdMob (Primary)")
                primary.showAd(activity, placement, onReward)
            }
            isSecondaryReady -> {
                Log.d(TAG, "Waterfall: AdMob not ready, using Unity (Secondary)")
                secondary.showAd(activity, placement, onReward)
            }
            else -> {
                Log.w(TAG, "Waterfall: No ads ready for ${placement.key}")
                // Kullanıcıya daha net bir mesaj gösteriyoruz
                val message = if (com.mawelly.blitzmath.BuildConfig.DEBUG) {
                    "Reklamlar henüz yüklenmedi. (AdMob: $isPrimaryReady, Unity: $isSecondaryReady)"
                } else {
                    "Reklam şu anda hazır değil, lütfen birkaç saniye sonra tekrar deneyin."
                }
                android.widget.Toast.makeText(activity, message, android.widget.Toast.LENGTH_SHORT).show()
                
                // Acil yükleme tetikle
                preloadAll()
            }
        }
    }

    override fun onGameOver(activity: Activity, onAdClosed: () -> Unit) {
        primary.onGameOver(activity, onAdClosed)
    }

    override fun isAdReady(): Boolean {
        return primary.isAdReady() || secondary.isAdReady()
    }

    override fun isAdReady(placement: IAdManager.Placement): Boolean {
        return primary.isAdReady(placement) || secondary.isAdReady(placement)
    }
}

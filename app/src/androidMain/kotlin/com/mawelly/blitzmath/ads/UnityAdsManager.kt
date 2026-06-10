package com.mawelly.blitzmath.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions
import com.mawelly.blitzmath.BuildConfig

class UnityAdsManager(private val context: Context) : IAdManager {

    private val gameId = "6094151" // Buraya kendi Unity Game ID'nizi girin
    private val testMode = BuildConfig.DEBUG
    private val TAG = "UnityAdsManager"

    // Unity Ads yerleşim isimleri (Placement IDs)
    private val placementIds = mapOf(
        IAdManager.Placement.SAVE_ME to "Rewarded_Android",
        IAdManager.Placement.REFILL_CHARGES to "Rewarded_Android",
        IAdManager.Placement.UNLOCK_SCIENTIST to "Rewarded_Android",
        IAdManager.Placement.DAILY_BONUS to "Rewarded_Android"
    )

    // Track loaded placements
    private val loadedPlacements = mutableSetOf<String>()

    init {
        initializeUnityAds()
    }

    private fun initializeUnityAds() {
        Log.d(TAG, "Initializing Unity Ads... (GameID: $gameId)")
        UnityAds.initialize(context, gameId, testMode, object : IUnityAdsInitializationListener {
            override fun onInitializationComplete() {
                Log.d(TAG, "✅ Unity Ads Initialization Complete")
                preloadAll()
            }

            override fun onInitializationFailed(error: UnityAds.UnityAdsInitializationError?, message: String?) {
                Log.e(TAG, "❌ Unity Ads Initialization Failed: $message. Retrying in 15s...")
                // Retry initialization after a delay
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    initializeUnityAds()
                }, 15000)
            }
        })
    }

    override fun preloadAll() {
        placementIds.values.distinct().forEach { placementId ->
            loadWithRetry(placementId)
        }
    }

    private fun loadWithRetry(placementId: String, delayMs: Long = 5000L) {
        Log.d(TAG, "Loading Unity Ad: $placementId (Next retry in ${delayMs/1000}s if fails)")
        
        UnityAds.load(placementId, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String?) {
                Log.d(TAG, "✅ Unity Ad Loaded: $placementId")
                placementId?.let { loadedPlacements.add(it) }
            }

            override fun onUnityAdsFailedToLoad(placementId: String?, error: UnityAds.UnityAdsLoadError?, message: String?) {
                Log.e(TAG, "❌ Unity Ad Failed to Load ($placementId): $message. Error Code: $error")
                placementId?.let { 
                    loadedPlacements.remove(it) 
                    // Retry with exponential backoff (max 1 minute)
                    val nextDelay = (delayMs * 2).coerceAtMost(60000L)
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        loadWithRetry(it, nextDelay)
                    }, delayMs)
                }
            }
        })
    }

    override fun showAd(activity: Activity, placement: IAdManager.Placement, onReward: () -> Unit) {
        val placementId = placementIds[placement] ?: "Rewarded_Android"
        
        Log.d(TAG, "Attempting to show Unity Ad for $placementId. Loaded: ${loadedPlacements.contains(placementId)}")

        UnityAds.show(activity, placementId, UnityAdsShowOptions(), object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(placementId: String?, error: UnityAds.UnityAdsShowError?, message: String?) {
                Log.e(TAG, "❌ Unity Ad Show Failure ($placementId): $message. Error: $error")
                placementId?.let { 
                    loadedPlacements.remove(it) 
                    loadWithRetry(it) 
                }
                android.widget.Toast.makeText(activity, "Reklam gösterilemedi: $message", android.widget.Toast.LENGTH_SHORT).show()
            }

            override fun onUnityAdsShowStart(placementId: String?) {
                Log.d(TAG, "Unity Ad Show Start: $placementId")
            }

            override fun onUnityAdsShowClick(placementId: String?) {
                Log.d(TAG, "Unity Ad Clicked")
            }

            override fun onUnityAdsShowComplete(placementId: String?, state: UnityAds.UnityAdsShowCompletionState?) {
                Log.d(TAG, "Unity Ad Show Complete: $placementId, State: $state")
                if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                    Log.d(TAG, "💰 Unity Reward Earned!")
                    onReward()
                }
                placementId?.let { 
                    loadedPlacements.remove(it) 
                    loadWithRetry(it) 
                }
            }
        })
    }

    override fun onGameOver(activity: Activity, onAdClosed: () -> Unit) {
        onAdClosed()
    }

    override fun isAdReady(): Boolean {
        return loadedPlacements.isNotEmpty()
    }

    override fun isAdReady(placement: IAdManager.Placement): Boolean {
        val placementId = placementIds[placement]
        return placementId != null && loadedPlacements.contains(placementId)
    }
}

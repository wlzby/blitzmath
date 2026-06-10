package com.mawelly.blitzmath.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.mawelly.blitzmath.BuildConfig

/**
 * AdMobManager - Gelişmiş Reklam Yönetimi
 * Artık her reklam yerleşimi (SaveMe, Refill, Unlock, Daily) için ayrı ID ve ayrı yükleme kanalı kullanır.
 * Bu sayede Gösterim Oranı (Show Rate) ve kazanç analizi optimize edilir.
 */
class AdMobManager(private val context: Context) : IAdManager {

    // Reklam Havuzu: Her yerleşim için birden fazla reklam tutar
    private val adPool = mutableMapOf<IAdManager.Placement, MutableList<RewardedAd>>()
    private val loadingPlacements = mutableSetOf<IAdManager.Placement>()
    private val maxPoolSize = 2 // 3 yerine 2 yapalım, bellek ve ağ kullanımı için daha ideal

    // Reklam Kimlikleri (Ad Unit IDs)
    private fun getAdUnitId(placement: IAdManager.Placement): String {
        return if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/5224354917" // Google Test ID
        } else {
            when (placement) {
                IAdManager.Placement.SAVE_ME -> "ca-app-pub-7719335438184188/2563738717"
                IAdManager.Placement.REFILL_CHARGES -> "ca-app-pub-7719335438184188/6160113655"
                IAdManager.Placement.UNLOCK_SCIENTIST -> "ca-app-pub-7719335438184188/6052302440"
                IAdManager.Placement.DAILY_BONUS -> "ca-app-pub-7719335438184188/2113057437"
            }
        }
    }

    companion object {
        private const val TAG = "AdMobManager"
    }

    init {
        try {
            // Havuz listelerini ilklendir
            IAdManager.Placement.entries.forEach { adPool[it] = mutableListOf() }

            // GMS varsa AdMob'u başlat
            if (com.mawelly.blitzmath.utils.ServiceChecker.isGmsAvailable(context)) {
                // Test cihazları konfigürasyonu
                val requestConfiguration = RequestConfiguration.Builder()
                    .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR, "E7EF42901A38832C19EA7ACAA26AC91E"))
                    .build()
                MobileAds.setRequestConfiguration(requestConfiguration)

                // SDK Başlatma
                MobileAds.initialize(context) { status ->
                    Log.d(TAG, "AdMob SDK Initialized")
                    preloadAll()
                }
            } else {
                Log.w(TAG, "GMS not available, skipping AdMob initialization")
            }
        } catch (t: Throwable) {
            Log.e(TAG, "AdMob initialization failed: ${t.message}")
        }
    }

    override fun preloadAll() {
        IAdManager.Placement.entries.forEach { fillPool(it) }
    }

    private fun fillPool(placement: IAdManager.Placement) {
        val currentSize = adPool[placement]?.size ?: 0
        if (currentSize < maxPoolSize && !loadingPlacements.contains(placement)) {
            loadRewardedAd(placement)
        }
    }

    private fun loadRewardedAd(placement: IAdManager.Placement) {
        if (loadingPlacements.contains(placement)) return
        
        loadingPlacements.add(placement)
        val adRequest = AdRequest.Builder().build()

        Log.d(TAG, "Loading AdMob Ad for ${placement.key}...")
        RewardedAd.load(
            context,
            getAdUnitId(placement),
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    loadingPlacements.remove(placement)
                    adPool[placement]?.add(ad)
                    Log.d(TAG, "✅ Ad Loaded for ${placement.key}. Pool: ${adPool[placement]?.size}")

                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            fillPool(placement)
                        }
                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            fillPool(placement)
                        }
                    }
                    
                    // Havuz hala boşsa (maxPoolSize > 1 ise) devam et
                    fillPool(placement)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    loadingPlacements.remove(placement)
                    Log.e(TAG, "❌ Ad Failed to Load for ${placement.key}: ${loadAdError.message}")
                    // 15 saniye sonra tekrar dene
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        fillPool(placement)
                    }, 15000)
                }
            }
        )
    }

    override fun showAd(activity: Activity, placement: IAdManager.Placement, onReward: () -> Unit) {
        val pool = adPool[placement]
        if (!pool.isNullOrEmpty()) {
            val ad = pool.removeAt(0)
            var isRewarded = false

            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "AdMob: Ad Dismissed")
                    fillPool(placement)
                    if (isRewarded) onReward()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "❌ AdMob: Failed to show: ${error.message}")
                    fillPool(placement)
                }
            }

            ad.show(activity) {
                isRewarded = true
                Log.d(TAG, "💰 AdMob: Reward earned for ${placement.key}")
            }
        } else {
            Log.d(TAG, "⚠️ AdMob: Pool empty for ${placement.key}, triggering load...")
            loadRewardedAd(placement)
        }
    }

    // Geriye dönük uyumluluk için eski fonksiyonları güncelleyelim
    fun showAdForCharges(activity: Activity, onAdFinished: () -> Unit) {
        showAd(activity, IAdManager.Placement.REFILL_CHARGES, onAdFinished)
    }

    override fun onGameOver(activity: Activity, onAdClosed: () -> Unit) {
        onAdClosed()
    }

    override fun isAdReady(): Boolean {
        return adPool.values.any { it.isNotEmpty() }
    }
    
    override fun isAdReady(placement: IAdManager.Placement): Boolean {
        return adPool[placement]?.isNotEmpty() == true
    }
}
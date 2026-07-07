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
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
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
    private val maxPoolSize = 1 // 1 adet önbelleğe alma yeterlidir, istek birikmesini engeller
    private val retryDelays = mutableMapOf<IAdManager.Placement, Long>()

    // Geçiş Reklamı (Interstitial)
    private var interstitialAd: InterstitialAd? = null
    private var loadingInterstitial = false
    private var lastInterstitialShownTime = 0L

    private fun getInterstitialAdUnitId(): String {
        return if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/1033061715" // Google Test Interstitial ID
        } else {
            "ca-app-pub-7719335438184188/9651528659" // Put production Interstitial ID here
        }
    }

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
            IAdManager.Placement.entries.forEach { 
                adPool[it] = mutableListOf() 
                retryDelays[it] = 15000L // Yeniden deneme gecikmesini 15 saniyeden başlat
            }

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
        // Sadece en çok kullanılan 2 placement'ı önceden yükle
        // Geri kalanlar kullanıcı ilgili ekrana gelince yüklenir (lazy)
        fillPool(IAdManager.Placement.SAVE_ME)
        fillPool(IAdManager.Placement.REFILL_CHARGES)
        loadInterstitialAd()
    }

    // Belirli bir yerleşimi bağlam odaklı önceden yükle (oyun başlarken, bitince, vs.)
    fun preloadForPlacement(placement: IAdManager.Placement) {
        fillPool(placement)
    }

    private fun loadInterstitialAd() {
        if (loadingInterstitial || interstitialAd != null) return
        loadingInterstitial = true
        val adRequest = AdRequest.Builder().build()
        
        Log.d(TAG, "Loading AdMob Interstitial Ad...")
        InterstitialAd.load(
            context,
            getInterstitialAdUnitId(),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    loadingInterstitial = false
                    interstitialAd = ad
                    Log.d(TAG, "✅ AdMob Interstitial Ad Loaded")
                    
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            interstitialAd = null
                            loadInterstitialAd()
                        }
                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            interstitialAd = null
                            loadInterstitialAd()
                        }
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    loadingInterstitial = false
                    Log.e(TAG, "❌ AdMob Interstitial Failed to Load: ${loadAdError.message}. Retrying in 30s...")
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        loadInterstitialAd()
                    }, 30000L)
                }
            }
        )
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
                    
                    // Başarılı yüklemede yeniden deneme süresini sıfırla
                    retryDelays[placement] = 15000L

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
                    val currentDelay = retryDelays[placement] ?: 30000L
                    // Üstel geri çekilme: 30s → 60s → 120s → ... en fazla 5 dakika
                    val nextDelay = (currentDelay * 2).coerceAtMost(300000L)
                    retryDelays[placement] = nextDelay
                    Log.e(TAG, "❌ Ad Failed for ${placement.key}: ${loadAdError.message}. Retry in ${currentDelay / 1000}s")
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        fillPool(placement)
                    }, currentDelay)
                }
            }
        )
    }

    override fun showAd(activity: Activity, placement: IAdManager.Placement, onReward: () -> Unit, onClosed: () -> Unit) {
        val pool = adPool[placement]
        if (!pool.isNullOrEmpty()) {
            val ad = pool.removeAt(0)
            var isRewarded = false

            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "AdMob: Ad Dismissed")
                    fillPool(placement)
                    if (isRewarded) onReward()
                    onClosed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "❌ AdMob: Failed to show: ${error.message}")
                    fillPool(placement)
                    onClosed()
                }
            }

            ad.show(activity) {
                isRewarded = true
                Log.d(TAG, "💰 AdMob: Reward earned for ${placement.key}")
            }
        } else {
            Log.d(TAG, "⚠️ AdMob: Pool empty for ${placement.key}, loading on-demand...")
            // Reklam henüz yüklenmemişse hemen yükle ve kullanıcıya 'yükleniyor' durumu göster
            loadRewardedAd(placement)
            onClosed() // UI'yi kilitleme, kullanıcıya geri dön
        }
    }

    // Geriye dönük uyumluluk için eski fonksiyonları güncelleyelim
    fun showAdForCharges(activity: Activity, onAdFinished: () -> Unit) {
        showAd(activity, IAdManager.Placement.REFILL_CHARGES, onAdFinished, {})
    }

    override fun onGameOver(activity: Activity, onAdClosed: () -> Unit) {
        val now = System.currentTimeMillis()
        val cooldownMs = 120000L // 2 minutes cooldown
        val isCooldownOver = (now - lastInterstitialShownTime) >= cooldownMs
        
        Log.d(TAG, "onGameOver called. Interstitial ready: ${interstitialAd != null}, cooldown over: $isCooldownOver")
        
        val ad = interstitialAd
        if (ad != null && isCooldownOver) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "AdMob Interstitial Dismissed")
                    interstitialAd = null
                    lastInterstitialShownTime = System.currentTimeMillis()
                    loadInterstitialAd()
                    onAdClosed()
                }
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "❌ AdMob Interstitial Failed to Show: ${adError.message}")
                    interstitialAd = null
                    loadInterstitialAd()
                    onAdClosed()
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "AdMob: Skipping interstitial ad (ready=${ad != null}, cooldownOver=$isCooldownOver)")
            if (ad == null) {
                loadInterstitialAd()
            }
            onAdClosed()
        }
    }

    override fun isAdReady(): Boolean {
        return adPool.values.any { it.isNotEmpty() }
    }
    
    override fun isAdReady(placement: IAdManager.Placement): Boolean {
        return adPool[placement]?.isNotEmpty() == true
    }
}
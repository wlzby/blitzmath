package com.mawelly.blitzmath.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.huawei.hms.analytics.HiAnalytics
import com.huawei.hms.analytics.HiAnalyticsInstance
import com.mawelly.blitzmath.utils.ServiceChecker

class AnalyticsManager private constructor(context: Context) {
    private var firebaseAnalytics: FirebaseAnalytics? = null
    private var huaweiAnalytics: HiAnalyticsInstance? = null

    init {
        val appContext = context.applicationContext
        
        // Firebase Analytics Başlatma (GMS varsa)
        try {
            if (ServiceChecker.isGmsAvailable(appContext)) {
                firebaseAnalytics = FirebaseAnalytics.getInstance(appContext)
            }
        } catch (t: Throwable) {
            android.util.Log.e("AnalyticsManager", "Firebase Analytics initialization failed: ${t.message}")
        }

        // Huawei Analytics Başlatma (HMS varsa)
        try {
            if (ServiceChecker.isHmsAvailable(appContext)) {
                huaweiAnalytics = HiAnalytics.getInstance(appContext)
                huaweiAnalytics?.setAnalyticsEnabled(true)
            }
        } catch (t: Throwable) {
            android.util.Log.e("AnalyticsManager", "Huawei Analytics initialization failed: ${t.message}")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AnalyticsManager? = null

        fun getInstance(context: Context): AnalyticsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AnalyticsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /**
     * Uygulama açılışını takip et
     */
    fun logAppOpen() {
        try {
            firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.APP_OPEN, null)
            huaweiAnalytics?.onEvent("\$AppOpen", null)
        } catch (t: Throwable) { t.printStackTrace() }
    }

    /**
     * Oyun modu seçimini takip et
     */
    fun logModeSelection(modeName: String) {
        try {
            val bundle = Bundle().apply {
                putString("mode_name", modeName)
            }
            firebaseAnalytics?.logEvent("mode_selection", bundle)
            huaweiAnalytics?.onEvent("mode_selection", bundle)
        } catch (t: Throwable) { t.printStackTrace() }
    }

    /**
     * Oyun başlangıcını takip et
     */
    fun logGameStart(mode: String, level: Int) {
        try {
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.LEVEL_NAME, mode)
                putInt(FirebaseAnalytics.Param.LEVEL, level)
            }
            firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.LEVEL_START, bundle)
            
            val hmsBundle = Bundle().apply {
                putString("\$LevelName", mode)
                putString("\$LevelId", level.toString())
            }
            huaweiAnalytics?.onEvent("\$StartLevel", hmsBundle)
        } catch (t: Throwable) { t.printStackTrace() }
    }

    /**
     * Oyun bitişini takip et (Skor ve Başarı)
     */
    fun logGameEnd(mode: String, score: Long, success: Boolean) {
        try {
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.LEVEL_NAME, mode)
                putLong(FirebaseAnalytics.Param.SCORE, score)
                putString("success", success.toString())
            }
            firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.LEVEL_END, bundle)

            val hmsBundle = Bundle().apply {
                putString("\$LevelName", mode)
                putLong("\$Score", score)
                putString("success", success.toString())
            }
            huaweiAnalytics?.onEvent("\$CompleteLevel", hmsBundle)
        } catch (t: Throwable) { t.printStackTrace() }
    }

    /**
     * "Diğer Oyunlar" butonuna tıklamayı takip et
     */
    fun logMoreGamesClick(gameName: String = "One Top Tower") {
        try {
            val bundle = Bundle().apply {
                putString("target_game", gameName)
            }
            firebaseAnalytics?.logEvent("more_games_click", bundle)
            huaweiAnalytics?.onEvent("more_games_click", bundle)
        } catch (t: Throwable) { t.printStackTrace() }
    }

    /**
     * Ayarlar değişimini takip et
     */
    fun logSettingChange(settingName: String, value: Float) {
        try {
            val bundle = Bundle().apply {
                putString("setting_name", settingName)
                putFloat("value", value)
            }
            firebaseAnalytics?.logEvent("setting_change", bundle)
            huaweiAnalytics?.onEvent("setting_change", bundle)
        } catch (t: Throwable) { t.printStackTrace() }
    }

    /**
     * Ekran geçişlerini takip et
     */
    fun logScreenView(screenName: String) {
        try {
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
            }
            firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
            
            val hmsBundle = Bundle().apply {
                putString("\$ScreenName", screenName)
            }
            huaweiAnalytics?.onEvent("\$ViewScreen", hmsBundle)
        } catch (t: Throwable) { t.printStackTrace() }
    }

    /**
     * Reklam butonuna tıklamayı takip et
     */
    fun logAdClick(adUnitName: String) {
        try {
            val bundle = Bundle().apply {
                putString("ad_unit_name", adUnitName)
            }
            firebaseAnalytics?.logEvent("rewarded_ad_click", bundle)
            huaweiAnalytics?.onEvent("rewarded_ad_click", bundle)
        } catch (t: Throwable) { t.printStackTrace() }
    }

    /**
     * Reklamın başarıyla izlenip ödülün alındığını takip et
     */
    fun logAdReward(adUnitName: String) {
        try {
            val bundle = Bundle().apply {
                putString("ad_unit_name", adUnitName)
            }
            firebaseAnalytics?.logEvent("rewarded_ad_earned", bundle)
            huaweiAnalytics?.onEvent("rewarded_ad_earned", bundle)
        } catch (t: Throwable) { t.printStackTrace() }
    }

    /**
     * Can doldurma butonuna tıklamayı takip et (Eskisiyle uyumlu kalsın)
     */
    fun logRefillLivesClick(source: String) {
        try {
            val bundle = Bundle().apply {
                putString("source", source)
            }
            firebaseAnalytics?.logEvent("refill_lives_clicked", bundle)
            huaweiAnalytics?.onEvent("refill_lives_clicked", bundle)
        } catch (t: Throwable) { t.printStackTrace() }
    }

    /**
     * Bilim insanı yeteneği şarj etme tıklamasını takip et
     */
    fun logAbilityRefillClick(scientistName: String) {
        try {
            val bundle = Bundle().apply {
                putString("scientist_name", scientistName)
            }
            firebaseAnalytics?.logEvent("ability_refill_clicked", bundle)
            huaweiAnalytics?.onEvent("ability_refill_clicked", bundle)
        } catch (t: Throwable) { t.printStackTrace() }
    }
}

package com.mawelly.blitzmath

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.mawelly.blitzmath.analytics.AnalyticsManager

class BlitzMathApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val context = this
        try {
            // Firebase'i başlat (Sadece GMS varsa veya güvenli şekilde)
            try {
                if (com.mawelly.blitzmath.utils.ServiceChecker.isGmsAvailable(context)) {
                    FirebaseApp.initializeApp(context)
                }
            } catch (t: Throwable) {
                android.util.Log.e("BlitzMathApp", "Firebase initialization failed: ${t.message}")
            }
            
            // Analytics'i güvenli bir şekilde başlat
            try {
                AnalyticsManager.getInstance(context).logAppOpen()
            } catch (t: Throwable) {
                android.util.Log.e("BlitzMathApp", "Analytics initialization failed: ${t.message}")
            }

            // AdMob'u başlat (Sadece GMS varsa)
            try {
                if (com.mawelly.blitzmath.utils.ServiceChecker.isGmsAvailable(context)) {
                    MobileAds.initialize(context) {}
                }
            } catch (t: Throwable) {
                android.util.Log.e("BlitzMathApp", "AdMob initialization failed: ${t.message}")
            }
            
            // Huawei Ads'i başlat (Sadece HMS varsa)
            try {
                if (com.mawelly.blitzmath.utils.ServiceChecker.isHmsAvailable(context)) {
                    val hwAdsClass = Class.forName("com.huawei.hms.ads.HwAds")
                    val initMethod = hwAdsClass.getMethod("init", android.content.Context::class.java)
                    initMethod.invoke(null, context)
                }
            } catch (t: Throwable) {
                // HMS Ads kütüphanesi yoksa sessizce devam et
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}
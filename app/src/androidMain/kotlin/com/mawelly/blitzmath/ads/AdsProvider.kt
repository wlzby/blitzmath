package com.mawelly.blitzmath.ads

import android.content.Context
import com.mawelly.blitzmath.utils.ServiceChecker

object AdsProvider {

    private var instance: IAdManager? = null

    fun getInstance(context: Context): IAdManager {
        if (instance == null) {
            val appContext = context.applicationContext
            
            // Unity Ads her zaman kullanılabilir
            val unityManager = UnityAdsManager(appContext)
            
            // AdMob sadece GMS varsa kullanılacak
            if (ServiceChecker.isGmsAvailable(appContext)) {
                val primaryManager = AdMobManager(appContext)
                instance = WaterfallAdManager(primaryManager, unityManager)
            } else {
                // GMS yoksa sadece Unity Ads kullan
                instance = unityManager
            }
        }
        return instance!!
    }
}

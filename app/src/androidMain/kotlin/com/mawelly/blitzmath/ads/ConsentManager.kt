package com.mawelly.blitzmath.ads

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Google User Messaging Platform (UMP) entegrasyonu.
 * GDPR (Avrupa Birliği) rıza formlarını yönetir.
 */
class ConsentManager(private val activity: Activity) {

    private var consentInformation: ConsentInformation? = null
    private val isMobileAdsInitializeCalled = AtomicBoolean(false)

    interface ConsentCallback {
        fun onConsentFinished()
    }

    /**
     * Rıza durumunu kontrol et ve gerekiyorsa formu göster.
     */
    fun gatherConsent(callback: ConsentCallback) {
        try {
            if (!com.mawelly.blitzmath.utils.ServiceChecker.isGmsAvailable(activity)) {
                Log.w("ConsentManager", "GMS not available, skipping consent gathering")
                callback.onConsentFinished()
                return
            }

            consentInformation = UserMessagingPlatform.getConsentInformation(activity)
            val info = consentInformation ?: run {
                callback.onConsentFinished()
                return
            }

            val params = ConsentRequestParameters.Builder()
                .setTagForUnderAgeOfConsent(false)
                .build()

            info.requestConsentInfoUpdate(
                activity,
                params,
                {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                        if (formError != null) {
                            Log.e("ConsentManager", "Consent form error: ${formError.message}")
                        }

                        if (info.canRequestAds()) {
                            initializeMobileAds()
                        }
                        callback.onConsentFinished()
                    }
                },
                { requestError ->
                    Log.e("ConsentManager", "Consent info update error: ${requestError.message}")
                    callback.onConsentFinished()
                }
            )

            // Eğer daha önceden rıza alınmışsa veya gerekmiyorsa direkt devam et
            if (info.canRequestAds()) {
                initializeMobileAds()
            }
        } catch (t: Throwable) {
            Log.e("ConsentManager", "Error in gatherConsent: ${t.message}")
            callback.onConsentFinished()
        }
    }

    private fun initializeMobileAds() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }
    }

    fun canRequestAds(): Boolean = consentInformation?.canRequestAds() ?: false
}

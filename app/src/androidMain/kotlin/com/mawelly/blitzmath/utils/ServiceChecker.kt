package com.mawelly.blitzmath.utils

import android.content.Context
import com.google.android.gms.common.GoogleApiAvailability
import com.huawei.hms.api.ConnectionResult
import com.huawei.hms.api.HuaweiApiAvailability

object ServiceChecker {

    /**
     * Google Play Services (GMS) kullanılabilir mi?
     */
    fun isGmsAvailable(context: Context): Boolean {
        return try {
            val gms = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
            gms == com.google.android.gms.common.ConnectionResult.SUCCESS
        } catch (t: Throwable) {
            false
        }
    }

    /**
     * Huawei Mobile Services (HMS) kullanılabilir mi?
     */
    fun isHmsAvailable(context: Context): Boolean {
        return try {
            val hms = HuaweiApiAvailability.getInstance().isHuaweiMobileServicesAvailable(context)
            hms == ConnectionResult.SUCCESS
        } catch (t: Throwable) {
            false
        }
    }

    /**
     * Tercih edilen servis tipini döner.
     * Google varsa öncelik Google'dadır (Google Play kuralları gereği).
     */
    fun getPreferredService(context: Context): ServiceType {
        return when {
            isGmsAvailable(context) -> ServiceType.GMS
            isHmsAvailable(context) -> ServiceType.HMS
            else -> ServiceType.NONE
        }
    }

    enum class ServiceType {
        GMS, HMS, NONE
    }
}

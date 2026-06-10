package com.mawelly.blitzmath.utils

import android.app.Activity
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory
import com.mawelly.blitzmath.data.GameDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object AppReviewManager {
    private const val TAG = "AppReviewManager"

    /**
     * Google Play In-App Review penceresini çağırır.
     * Kullanıcı pencereyi görsün ya da görmesin (daha önce oylamış olabilir), onComplete lambda'sı tetiklenir.
     */
    fun showReviewDialog(activity: Activity, dataStore: GameDataStore, onComplete: () -> Unit) {
        try {
            if (!com.mawelly.blitzmath.utils.ServiceChecker.isGmsAvailable(activity)) {
                Log.w(TAG, "GMS not available, skipping Play Review.")
                onComplete()
                return
            }
            
            val manager = ReviewManagerFactory.create(activity)
            val request = manager.requestReviewFlow()
            
            request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Request_success
                val reviewInfo = task.result
                Log.d(TAG, "Review Info successfully gathered.")
                
                val flow = manager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener { _ ->
                    // The flow has finished. API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown.
                    Log.d(TAG, "Review Flow completed.")
                    
                    // Ödül mantığı için Coroutine çalıştırıp 1000 yıldız verelim
                    val scope = CoroutineScope(Dispatchers.IO)
                    scope.launch {
                        dataStore.addStars(1000)
                        dataStore.saveIsReviewed(true)
                        
                        // Ana thread'de Toast göster
                        withContext(Dispatchers.Main) {
                            val msg = if (com.mawelly.blitzmath.localization.Strings.currentLanguage == com.mawelly.blitzmath.localization.AppLanguage.TURKISH) 
                                "Tebrikler! 1000 Yıldız hesabınıza eklendi!" 
                                else "Congratulations! 1000 Stars added to your account!"
                            android.widget.Toast.makeText(activity, msg, android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                    
                    onComplete()
                }
            } else {
                // There was some problem, log or handle the error code.
                Log.e(TAG, "Review Info error: ${task.exception?.message}")
                onComplete() // Her halükarda devam etmesini sağla
            }
        } // Close addOnCompleteListener
        } catch (e: Exception) {
            Log.e(TAG, "Exception starting review flow: ${e.message}")
            onComplete()
        }
    }
}

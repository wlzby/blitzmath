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
            // 1. Önce Google Play In-App Review API'yi dene
            if (com.mawelly.blitzmath.utils.ServiceChecker.isGmsAvailable(activity)) {
                val manager = ReviewManagerFactory.create(activity)
                val request = manager.requestReviewFlow()
                request.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val reviewInfo = task.result
                        val flow = manager.launchReviewFlow(activity, reviewInfo)
                        flow.addOnCompleteListener { _ ->
                            Log.d(TAG, "Review Flow completed.")
                        }
                    }
                }
            }

            // 2. HER HALÜKARDA (Test cihazı veya In-App Review açılmama durumunda) doğrudan Play Store Değerlendirme Sayfasını Aç
            try {
                val packageName = activity.packageName
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=$packageName")).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                activity.startActivity(intent)
            } catch (e: Exception) {
                val packageName = activity.packageName
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                activity.startActivity(intent)
            }

            onComplete()
        } catch (e: Exception) {
            Log.e(TAG, "Exception in showReviewDialog: ${e.message}")
            onComplete()
        }
    }
}

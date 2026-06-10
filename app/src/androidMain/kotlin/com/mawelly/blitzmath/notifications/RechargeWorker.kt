package com.mawelly.blitzmath.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mawelly.blitzmath.data.GameDataStore
import com.mawelly.blitzmath.game.ScientistCards
import kotlinx.coroutines.flow.first

class RechargeWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val cardId = inputData.getString("card_id") ?: return Result.failure()
        val card = ScientistCards.getCardById(cardId) ?: return Result.failure()
        
        val dataStore = GameDataStore(applicationContext)
        val language = dataStore.language.first()
        
        // Show the localized notification for this specific card
        NotificationHelper(applicationContext).showRechargeNotification(card.name, language)
        
        return Result.success()
    }
}

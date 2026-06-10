package com.mawelly.blitzmath.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mawelly.blitzmath.data.GameDataStore
import com.mawelly.blitzmath.game.ScientistCards
import kotlinx.coroutines.flow.first

class EngagementWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val dataStore = GameDataStore(applicationContext)
        val language = dataStore.language.first()
        val unlockedCards = dataStore.unlockedCards.first()
        
        val allCards = ScientistCards.cards
        val randomCard = allCards.randomOrNull()
        
        // %50 ihtimalle bilim insanı, %50 ihtimalle genel oyun sözü gönder
        if (randomCard != null && kotlin.random.Random.nextBoolean()) {
            val isUnlocked = unlockedCards.contains(randomCard.id)
            NotificationHelper(applicationContext).showScientistEngagementNotification(
                language = language,
                cardName = randomCard.name,
                imageId = randomCard.id,
                isUnlocked = isUnlocked
            )
        } else {
            NotificationHelper(applicationContext).showRetentionNotification(language)
        }
        
        return Result.success()
    }
}

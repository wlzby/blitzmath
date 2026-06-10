package com.mawelly.blitzmath.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mawelly.blitzmath.MainActivity
import com.mawelly.blitzmath.R
import com.mawelly.blitzmath.data.GameDataStore
import com.mawelly.blitzmath.leaderboard.LeaderboardManager
import com.mawelly.blitzmath.LanguageManager
import kotlinx.coroutines.flow.first

class RankCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val dataStore = GameDataStore(applicationContext)
        val languageManager = LanguageManager(applicationContext)
        val leaderboardManager = LeaderboardManager()
        
        val playerId = languageManager.getPlayerId()
        if (playerId.isEmpty()) return Result.success()

        // Her 3 mod için kontrol et
        checkRankForMode(dataStore, leaderboardManager, playerId, "classic")
        checkRankForMode(dataStore, leaderboardManager, playerId, "mixed")
        checkRankForMode(dataStore, leaderboardManager, playerId, "challenge")

        return Result.success()
    }

    private suspend fun checkRankForMode(
        dataStore: GameDataStore,
        leaderboardManager: LeaderboardManager,
        playerId: String,
        mode: String
    ) {
        val currentRankResult = leaderboardManager.getPlayerRank(playerId, mode)
        val currentRank = currentRankResult.getOrNull() ?: 0
        
        if (currentRank <= 0) return // Henüz sıralamada yoksa

        val lastRank = when (mode) {
            "mixed" -> dataStore.lastKnownMixedRank.first()
            "challenge" -> dataStore.lastKnownChallengeRank.first()
            else -> dataStore.lastKnownClassicRank.first()
        }

        // Eğer ilk kez bakılıyorsa sadece kaydet
        if (lastRank == 0) {
            dataStore.saveLastKnownRank(mode, currentRank)
            return
        }

        // Eğer sıralama düştüyse (Sayı büyüdüyse, örn: 5'ten 6'ya)
        if (currentRank > lastRank) {
            val modeName = when(mode) {
                "mixed" -> "Karışık"
                "challenge" -> "Meydan Okuma"
                else -> "Klasik"
            }
            showRankNotification(modeName, currentRank)
        }

        // Yeni sıralamayı kaydet
        dataStore.saveLastKnownRank(mode, currentRank)
    }

    private fun showRankNotification(modeName: String, newRank: Int) {
        val channelId = "rank_updates"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Sıralama Güncellemeleri",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Sıralamanız düştüğünde sizi uyarır."
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.blitzmath_logo)
            .setContentTitle("Sıralaman Düştü! 📉")
            .setContentText("$modeName modunda $newRank. sıraya geriledin. Hemen yerini geri al!")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        notificationManager.notify(modeName.hashCode(), notificationBuilder.build())
    }
}

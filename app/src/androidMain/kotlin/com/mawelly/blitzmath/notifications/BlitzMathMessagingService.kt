package com.mawelly.blitzmath.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mawelly.blitzmath.MainActivity
import com.mawelly.blitzmath.R

class BlitzMathMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Veri payload'u varsa (Cloud Functions'dan gelecek)
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: "BlitzMath"
            val message = remoteMessage.data["body"] ?: ""
            showNotification(title, message)
        }

        // Bildirim payload'u varsa
        remoteMessage.notification?.let {
            showNotification(it.title ?: "BlitzMath", it.body ?: "")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Token yenilendiğinde bunu Firestore'a kaydetmek üzere bir yere not edebiliriz
        // Ancak biz her uygulama açılışında zaten güncelleyeceğiz
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "leaderboard_updates"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Leaderboard Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Get notified when someone passes you in ranking!"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.blitzmath_logo) // Logo buraya gelecek
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}

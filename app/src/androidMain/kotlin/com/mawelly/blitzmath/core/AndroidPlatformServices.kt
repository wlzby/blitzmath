package com.mawelly.blitzmath.core

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.mawelly.blitzmath.ads.IAdManager
import com.mawelly.blitzmath.analytics.AnalyticsManager
import com.mawelly.blitzmath.audio.SoundManager
import com.mawelly.blitzmath.utils.HapticManager
import com.mawelly.blitzmath.leaderboard.ILeaderboardManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.*

class AndroidSoundManager(private val soundManager: SoundManager) : ISoundManager {
    override fun playClick() = soundManager.playCorrect()
    override fun playSuccess() = soundManager.playLevelUp()
    override fun playError() = soundManager.playWrong()
    override fun playGameOver() = soundManager.playGameOver()
    override fun setEnabled(enabled: Boolean) {
        if (enabled) soundManager.resumeBGM() else soundManager.stopBGM()
    }
    override fun stopBGM() = soundManager.stopBGM()
}

class AndroidHapticManager(private val context: Context) : IHapticManager {
    private val hapticManager = HapticManager(context)
    override fun triggerLightImpact() = hapticManager.vibrateTick(true, 0.3f)
    override fun triggerMediumImpact() = hapticManager.vibrateTick(true, 0.6f)
    override fun triggerHeavyImpact() = hapticManager.vibrateTick(true, 1.0f)
    override fun triggerError() = hapticManager.vibrateWrong(true, 1.0f)
    override fun triggerSuccess() = hapticManager.vibrateTick(true, 1.0f)
}

class AndroidAnalyticsManager(private val analyticsManager: AnalyticsManager) : IAnalyticsManager {
    override fun logEvent(eventName: String, params: Map<String, Any>) {
        analyticsManager.logScreenView(eventName)
    }
    override fun logScreenView(screenName: String) = analyticsManager.logScreenView(screenName)
    override fun logModeSelection(modeName: String) = analyticsManager.logModeSelection(modeName)
    override fun logRefillLivesClick(source: String) = analyticsManager.logRefillLivesClick(source)
    override fun logAdClick(adUnitName: String) = analyticsManager.logAdClick(adUnitName)
    override fun logAdReward(adUnitName: String) = analyticsManager.logAdReward(adUnitName)
    override fun logGameEnd(mode: String, score: Long, success: Boolean) = analyticsManager.logGameEnd(mode, score, success)
}

class AndroidShareManager(private val context: Context) : IShareManager {
    override fun shareScore(score: Int) {
        val currentLang = com.mawelly.blitzmath.localization.Strings.currentLanguage
        val shareIntro = when (currentLang) {
            com.mawelly.blitzmath.localization.AppLanguage.TURKISH -> "BlitzMath Challenge'da $score skoruna ulaştım! 🧠 Zihnini test etmeye hazır mısın? ⚡"
            com.mawelly.blitzmath.localization.AppLanguage.ENGLISH -> "I reached a score of $score in BlitzMath Challenge! 🧠 Are you ready to test your mind? ⚡"
            com.mawelly.blitzmath.localization.AppLanguage.SPANISH -> "¡Alcancé una puntuación de $score en BlitzMath Challenge! 🧠 ¿Estás listo para poner a prueba tu mente? ⚡"
            com.mawelly.blitzmath.localization.AppLanguage.GERMAN -> "Ich habe eine Punktzahl von $score in BlitzMath Challenge erreicht! 🧠 Bist du bereit, deinen Verstand zu testen? ⚡"
            com.mawelly.blitzmath.localization.AppLanguage.FRENCH -> "J'ai atteint un score de $score dans BlitzMath Challenge ! 🧠 Es-tu prêt à tester ton esprit ? ⚡"
            com.mawelly.blitzmath.localization.AppLanguage.ITALIAN -> "Ho raggiunto un punteggio di $score in BlitzMath Challenge! 🧠 Sei pronto a mettere alla prova la tua mente? ⚡"
            com.mawelly.blitzmath.localization.AppLanguage.PORTUGUESE -> "Alcancei uma pontuação de $score no BlitzMath Challenge! 🧠 Você está pronto para testar sua mente? ⚡"
            com.mawelly.blitzmath.localization.AppLanguage.HINDI -> "मैंने BlitzMath Challenge में $score का स्कोर हासिल किया! 🧠 क्या आप अपने दिमाग का परीक्षण करने के लिए तैयार हैं? ⚡"
            com.mawelly.blitzmath.localization.AppLanguage.CHINESE -> "我在 BlitzMath Challenge 中获得了 $score 分！🧠 你准备好挑战你的大脑了吗？⚡"
            com.mawelly.blitzmath.localization.AppLanguage.RUSSIAN -> "Я набрал $score очков в BlitzMath Challenge! 🧠 Готовы ли вы проверить свой разум? ⚡"
            else -> "I reached a score of $score in BlitzMath Challenge! 🧠 Are you ready to test your mind? ⚡"
        }
        
        val quote = com.mawelly.blitzmath.game.mathQuotes.random()
        val quoteText = quote.getQuote(currentLang)
        val quoteAuthor = quote.author
        val formattedQuote = "\n\n\"$quoteText\" - $quoteAuthor"
        
        val storeLinks = when (currentLang) {
            com.mawelly.blitzmath.localization.AppLanguage.TURKISH -> 
                "\n\n📲 Hemen İndir ve Yarış:\nAndroid (Google Play): https://play.google.com/store/apps/details?id=com.mawelly.blitzmath\niOS (App Store): https://apps.apple.com/app/blitzmath/id6503923303"
            else -> 
                "\n\n📲 Download & Play now:\nAndroid (Google Play): https://play.google.com/store/apps/details?id=com.mawelly.blitzmath\niOS (App Store): https://apps.apple.com/app/blitzmath/id6503923303"
        }
        val shareText = "$shareIntro$storeLinks$formattedQuote"
        
        try {
            val shareDir = java.io.File(context.cacheDir, "blitzmath_shares")
            if (!shareDir.exists()) shareDir.mkdirs()
            val shareFile = java.io.File(shareDir, "blitzmath_promo.webp")
            
            // blitzmath_logo.webp'yi önbelleğe kopyala
            if (!shareFile.exists()) {
                val inputStream = context.resources.openRawResource(com.mawelly.blitzmath.R.drawable.blitzmath_logo)
                val outputStream = java.io.FileOutputStream(shareFile)
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()
            }
            
            val authority = "${context.packageName}.fileprovider"
            val imageUri = androidx.core.content.FileProvider.getUriForFile(context, authority, shareFile)
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/webp"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                putExtra(Intent.EXTRA_TEXT, shareText)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Skoru Paylaş"))
        } catch (e: Exception) {
            android.util.Log.e("AndroidShareManager", "Görsel paylaşırken hata oluştu, metin olarak paylaşılıyor: ${e.message}")
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            context.startActivity(Intent.createChooser(intent, "Skoru Paylaş"))
        }
    }
}

class AndroidAdController(
    private val activity: Activity,
    private val adManager: IAdManager
) : IAdController {
    override fun showInterstitialAd(onClosed: () -> Unit) {
        adManager.onGameOver(activity) { onClosed() }
    }

    override fun showRewardedAd(placement: AdPlacement, onReward: () -> Unit, onClosed: () -> Unit) {
        val mappedPlacement = when (placement) {
            AdPlacement.SAVE_ME -> IAdManager.Placement.SAVE_ME
            AdPlacement.REFILL_CHARGES -> IAdManager.Placement.REFILL_CHARGES
            AdPlacement.UNLOCK_SCIENTIST -> IAdManager.Placement.UNLOCK_SCIENTIST
            AdPlacement.DAILY_BONUS -> IAdManager.Placement.DAILY_BONUS
        }
        adManager.showAd(
            activity = activity,
            placement = mappedPlacement,
            onReward = onReward,
            onClosed = onClosed
        )
    }
}

class AndroidPlatformServices(
    private val activity: Activity,
    private val context: Context,
    adManager: IAdManager,
    analyticsManager: AnalyticsManager,
    soundManager: SoundManager
) : PlatformServices {
    private var cachedCountry: String = "US"

    init {
        // Try telephony first (instantaneous, offline)
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? android.telephony.TelephonyManager
        val resolvedCountry = tm?.networkCountryIso?.uppercase()?.takeIf { it.isNotBlank() }
            ?: tm?.simCountryIso?.uppercase()?.takeIf { it.isNotBlank() }
            ?: java.util.Locale.getDefault().country.uppercase().takeIf { it.isNotBlank() }
            ?: "US"

        cachedCountry = resolvedCountry

        // Fetch asynchronously from ip2c.org to correct any language-setting mismatch (e.g. Wi-Fi/emulators)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val connection = java.net.URL("https://ip2c.org/self").openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 3000
                connection.readTimeout = 3000
                connection.requestMethod = "GET"
                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val parts = response.split(";")
                    if (parts.size >= 2 && parts[0] == "1") {
                        val ipCountry = parts[1].uppercase().trim()
                        if (ipCountry.length == 2) {
                            cachedCountry = ipCountry
                        }
                    }
                }
            } catch (t: Throwable) {
                // Ignore and keep using the telephony/locale fallback
            }
        }
    }

    override val soundManager: ISoundManager = AndroidSoundManager(soundManager)
    override val hapticManager: IHapticManager = AndroidHapticManager(context)
    override val analyticsManager: IAnalyticsManager = AndroidAnalyticsManager(analyticsManager)
    override fun getCurrentTimeMillis(): Long = System.currentTimeMillis()
    override val shareManager: IShareManager = AndroidShareManager(context)
    override val adController: IAdController = AndroidAdController(activity, adManager)
    override val multiplayerController: IMultiplayerController = AndroidMultiplayerController()
    override val leaderboardManager: ILeaderboardManager = com.mawelly.blitzmath.leaderboard.LeaderboardManager()
    override val deviceCountry: String get() = cachedCountry
    override fun generateUuid(): String = java.util.UUID.randomUUID().toString()

    override fun openUrl(url: String) {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    override fun getCurrentDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    override fun scheduleCardRecharge(cardId: String, delayMinutes: Long) {
        try {
            val workManager = androidx.work.WorkManager.getInstance(context.applicationContext)
            val workRequest = androidx.work.OneTimeWorkRequestBuilder<com.mawelly.blitzmath.notifications.RechargeWorker>()
                .setInitialDelay(delayMinutes, java.util.concurrent.TimeUnit.MINUTES)
                .setInputData(androidx.work.workDataOf("card_id" to cardId))
                .addTag("recharge_$cardId")
                .build()

            workManager.enqueueUniqueWork(
                "recharge_$cardId",
                androidx.work.ExistingWorkPolicy.REPLACE,
                workRequest
            )
        } catch (e: Exception) {
            android.util.Log.e("AndroidPlatformServices", "Error scheduling card recharge: ${e.message}")
        }
    }

    override fun cancelCardRecharge(cardId: String) {
        try {
            val workManager = androidx.work.WorkManager.getInstance(context.applicationContext)
            workManager.cancelUniqueWork("recharge_$cardId")
        } catch (e: Exception) {
            android.util.Log.e("AndroidPlatformServices", "Error canceling card recharge: ${e.message}")
        }
    }

    override fun showAppReview() {
        try {
            val datastoreImpl = com.mawelly.blitzmath.data.GameDataStore(context)
            com.mawelly.blitzmath.utils.AppReviewManager.showReviewDialog(activity, datastoreImpl) {
                android.util.Log.d("AndroidPlatformServices", "App review completed.")
            }
        } catch (e: Exception) {
            android.util.Log.e("AndroidPlatformServices", "Error triggering app review: ${e.message}")
        }
    }
}

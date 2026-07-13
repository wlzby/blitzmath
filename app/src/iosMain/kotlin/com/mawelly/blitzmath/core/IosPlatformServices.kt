@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, org.jetbrains.compose.resources.ExperimentalResourceApi::class)
package com.mawelly.blitzmath.core

import platform.Foundation.*
import platform.UIKit.*
import platform.AVFAudio.AVAudioPlayer
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.mawelly.blitzmath.leaderboard.ILeaderboardManager
import com.mawelly.blitzmath.leaderboard.LeaderboardEntry
import com.mawelly.blitzmath.core.AdPlacement
import com.mawelly.blitzmath.core.IAdController
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope

private operator fun NSDictionary.get(key: Any?): Any? = this.objectForKey(key)

class IosSoundManager : ISoundManager {
    private var isEnabled = true
    private val players = mutableMapOf<String, AVAudioPlayer>()

    private fun getPlayer(fileName: String, type: String): AVAudioPlayer? {
        if (players.containsKey(fileName)) {
            return players[fileName]
        }
        try {
            val uri = runBlocking {
                blitzmath.app.generated.resources.Res.getUri("files/$fileName.$type")
            }
            val url = NSURL(string = uri)
            if (url != null) {
                val player = AVAudioPlayer(contentsOfURL = url, error = null)
                player.prepareToPlay()
                players[fileName] = player
                return player
            }
        } catch (e: Exception) {
            println("IosSoundManager: Error loading sound $fileName via Res.getUri: ${e.message}")
        }
        val path = NSBundle.mainBundle.pathForResource("compose-resources/files/$fileName", type)
            ?: NSBundle.mainBundle.pathForResource(fileName, type)
        if (path != null) {
            val url = NSURL.fileURLWithPath(path)
            val player = AVAudioPlayer(contentsOfURL = url, error = null)
            player.prepareToPlay()
            players[fileName] = player
            return player
        }
        return null
    }

    private fun playSound(fileName: String, type: String) {
        if (!isEnabled) return
        val player = getPlayer(fileName, type)
        if (player != null) {
            player.setCurrentTime(0.0)
            player.play()
        }
    }

    override fun playClick() {
        playSound("sound_correct", "wav")
    }

    override fun playSuccess() {
        playSound("sound_level_up", "wav")
    }

    override fun playError() {
        playSound("sound_wrong", "wav")
    }

    override fun playGameOver() {
        playSound("sound_game_over", "wav")
    }

    override fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }
}

class IosHapticManager : IHapticManager {
    override fun triggerLightImpact() {
        UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleLight).impactOccurred()
    }

    override fun triggerMediumImpact() {
        UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium).impactOccurred()
    }

    override fun triggerHeavyImpact() {
        UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy).impactOccurred()
    }

    override fun triggerError() {
        UINotificationFeedbackGenerator().notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeError)
    }

    override fun triggerSuccess() {
        UINotificationFeedbackGenerator().notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
    }
}

class IosAnalyticsManager : IAnalyticsManager {
    override fun logEvent(eventName: String, params: Map<String, Any>) {}
    override fun logScreenView(screenName: String) {}
    override fun logModeSelection(modeName: String) {}
    override fun logRefillLivesClick(source: String) {}
    override fun logAdClick(adUnitName: String) {}
    override fun logAdReward(adUnitName: String) {}
    override fun logGameEnd(mode: String, score: Long, success: Boolean) {}
}

class IosShareManager : IShareManager {
    override fun shareScore(score: Int) {
        val currentLang = com.mawelly.blitzmath.localization.Strings.currentLanguage
        val shareIntro = when (currentLang) {
            com.mawelly.blitzmath.localization.AppLanguage.TURKISH -> "BlitzMath Challenge'da $score skoruna ulaştım! 🧠 Sen de katıl ve zihnini test et! ⚡"
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
        val items = mutableListOf<Any>(shareText)
        
        // Eğer iOS AppIcon yüklenirse paylaşıma ekle
        val appIconImage = platform.UIKit.UIImage.imageNamed("AppIcon")
        if (appIconImage != null) {
            items.add(appIconImage)
        }
        
        val activityController = platform.UIKit.UIActivityViewController(
            activityItems = items,
            applicationActivities = null
        )
        val windows = platform.UIKit.UIApplication.sharedApplication.windows
        val window = platform.UIKit.UIApplication.sharedApplication.keyWindow
            ?: (windows.firstOrNull() as? platform.UIKit.UIWindow)
        val rootVC = window?.rootViewController
        
        if (activityController.popoverPresentationController != null) {
            activityController.popoverPresentationController?.sourceView = window
            activityController.popoverPresentationController?.sourceRect = platform.CoreGraphics.CGRectMake(0.0, 0.0, 100.0, 100.0)
        }
        
        rootVC?.presentViewController(activityController, animated = true, completion = null)
    }
}

class IosAdController : IAdController {
    override fun showInterstitialAd(onClosed: () -> Unit) { onClosed() }
    override fun showRewardedAd(placement: AdPlacement, onReward: () -> Unit, onClosed: () -> Unit) {
        val windows = platform.UIKit.UIApplication.sharedApplication.windows
        val window = platform.UIKit.UIApplication.sharedApplication.keyWindow
            ?: (windows.firstOrNull() as? platform.UIKit.UIWindow)
        val rootVC = window?.rootViewController
        if (rootVC == null) {
            onReward()
            onClosed()
            return
        }

        // Show a rewarded ad simulation with a realistic countdown
        val totalSeconds = 15
        val alert = UIAlertController.alertControllerWithTitle(
            title = "📺 Reklam İzleniyor...",
            message = "Ödülünüz $totalSeconds saniye sonra verilecektir.\n\n▶  [████████████████████]  0%",
            preferredStyle = UIAlertControllerStyleAlert
        )

        var completed = false
        var countdownJob: kotlinx.coroutines.Job? = null

        rootVC.presentViewController(alert, animated = true, completion = null)

        countdownJob = CoroutineScope(Dispatchers.Main).launch {
            for (i in totalSeconds downTo 1) {
                val progress = ((totalSeconds - i).toFloat() / totalSeconds * 20).toInt()
                val bar = "█".repeat(progress) + "░".repeat(20 - progress)
                val percent = ((totalSeconds - i).toFloat() / totalSeconds * 100).toInt()
                alert.setTitle("📺 Reklam İzleniyor... ($i sn)")
                alert.setMessage("Ödülünüz $i saniye sonra verilecektir.\n\n▶  [$bar]  $percent%")
                delay(1000)
            }
            completed = true
            alert.setTitle("🎁 Ödül Kazanıldı!")
            alert.setMessage("Reklam tamamlandı! Ödülünüz veriliyor...")
            delay(800)
            alert.dismissViewControllerAnimated(true) {
                onReward()
                onClosed()
            }
        }
    }
}

class IosLeaderboardManager : ILeaderboardManager {
    private val apiKey = "AIzaSyBJfQl_ze9VsL_gQLMC5zFyje-wq3T8_IQ"

    private suspend fun performRequest(request: NSURLRequest): String? = suspendCancellableCoroutine { continuation ->
        val task = NSURLSession.sharedSession.dataTaskWithRequest(
            request = request,
            completionHandler = { data: NSData?, response: NSURLResponse?, error: NSError? ->
                platform.darwin.dispatch_async(platform.darwin.dispatch_get_main_queue()) {
                    if (error != null) {
                        println("IosLeaderboard: Network error: ${error.localizedDescription}")
                        continuation.resumeWithException(Exception("Ağ hatası: ${error.localizedDescription}"))
                    } else {
                        val httpResponse = response as? NSHTTPURLResponse
                        val statusCode = httpResponse?.statusCode?.toInt() ?: 200
                        println("IosLeaderboard: HTTP $statusCode")
                        if (statusCode == 429) {
                            println("IosLeaderboard: RATE LIMITED (429)")
                            continuation.resumeWithException(Exception("429: Sunucu meşgul. Lütfen bekleyin."))
                        } else if (statusCode == 404) {
                            println("IosLeaderboard: 404 Not Found (Document does not exist)")
                            continuation.resume(null)
                        } else if (statusCode >= 400) {
                            println("IosLeaderboard: HTTP error $statusCode")
                            var errorMsg = "HTTP $statusCode hatası"
                            if (data != null) {
                                try {
                                    val json = NSJSONSerialization.JSONObjectWithData(data, 0uL, null) as? NSDictionary
                                    val errorObj = json?.objectForKey("error") as? NSDictionary
                                    val msg = errorObj?.objectForKey("message") as? String
                                    if (msg != null) {
                                        errorMsg = msg
                                    }
                                } catch (e: Exception) {
                                    // ignore
                                }
                            }
                            continuation.resumeWithException(Exception(errorMsg))
                        } else if (data != null) {
                            val nsString = NSString.create(data = data, encoding = NSUTF8StringEncoding)
                            continuation.resume(nsString?.toString())
                        } else {
                            continuation.resume(null)
                        }
                    }
                }
            }
        )
        task.resume()
        continuation.invokeOnCancellation { task.cancel() }
    }


    private fun getCollectionName(mode: String): String {
        return when (mode.lowercase()) {
            "mixed" -> "mixed_leaderboard"
            "challenge" -> "challenge_leaderboard"
            else -> "global_leaderboard"
        }
    }

    override suspend fun submitScore(
        playerId: String,
        playerName: String,
        score: Long,
        level: Int,
        country: String,
        mode: String
    ): Result<Unit> = kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
        if (playerId.isEmpty()) return@withContext Result.failure(Exception("Empty Player ID"))

        return@withContext try {
            val collection = getCollectionName(mode)
            val getUrl = NSURL(string = "https://firestore.googleapis.com/v1/projects/blitz-math-challenge/databases/(default)/documents/$collection/$playerId?key=$apiKey") ?: return@withContext Result.failure(Exception("Invalid URL"))
            val getRequest = NSMutableURLRequest(uRL = getUrl).apply {
                setHTTPMethod("GET")
            }

            val getResponse = performRequest(getRequest)
            var existingScore = -1L
            var existingCountry = ""
            var existingName = ""
            var documentExists = false

            if (getResponse != null) {
                try {
                    val data = (getResponse as NSString).dataUsingEncoding(NSUTF8StringEncoding)
                    if (data != null) {
                        val json = NSJSONSerialization.JSONObjectWithData(data, 0uL, null) as? NSDictionary
                        if (json != null && json.get("fields") != null) {
                            documentExists = true
                            val fields = json.get("fields") as? NSDictionary
                            val totalScoreStr = (fields?.get("totalScore") as? NSDictionary)?.get("integerValue") as? String
                            if (totalScoreStr != null) {
                                existingScore = totalScoreStr.toLongOrNull() ?: 0L
                            }
                            existingCountry = (fields?.get("country") as? NSDictionary)?.get("stringValue") as? String ?: ""
                            existingName = (fields?.get("playerName") as? NSDictionary)?.get("stringValue") as? String ?: ""
                        }
                    }
                } catch (e: Exception) {
                    // Document might not exist (404)
                }
            }

            if (score > existingScore) {
                val patchUrl = NSURL(string = "https://firestore.googleapis.com/v1/projects/blitz-math-challenge/databases/(default)/documents/$collection/$playerId?updateMask.fieldPaths=playerId&updateMask.fieldPaths=playerName&updateMask.fieldPaths=totalScore&updateMask.fieldPaths=highestLevel&updateMask.fieldPaths=country&key=$apiKey") ?: return@withContext Result.failure(Exception("Invalid URL"))
                
                // Firestore REST API requires values to be string-wrapped
                val bodyJson = """
                {
                  "fields": {
                    "playerId": {"stringValue": "$playerId"},
                    "playerName": {"stringValue": "$playerName"},
                    "totalScore": {"integerValue": "$score"},
                    "highestLevel": {"integerValue": "$level"},
                    "country": {"stringValue": "$country"}
                  }
                }
                """.trimIndent()

                val patchRequest = NSMutableURLRequest(uRL = patchUrl).apply {
                    setHTTPMethod("PATCH")
                    setValue("application/json", forHTTPHeaderField = "Content-Type")
                    val bodyData = (bodyJson as NSString).dataUsingEncoding(NSUTF8StringEncoding)
                    if (bodyData != null) {
                        setHTTPBody(bodyData)
                    }
                }

                val patchResponse = performRequest(patchRequest)
                if (patchResponse != null) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to submit score"))
                }
            } else if (documentExists && (existingCountry.isEmpty() || existingName.isEmpty())) {
                val fieldsToUpdate = mutableListOf<String>()
                val fieldsJsonList = mutableListOf<String>()
                
                if (existingCountry.isEmpty() && country.isNotEmpty()) {
                    fieldsToUpdate.add("updateMask.fieldPaths=country")
                    fieldsJsonList.add("\"country\": {\"stringValue\": \"$country\"}")
                }
                if (existingName.isEmpty() && playerName.isNotEmpty()) {
                    fieldsToUpdate.add("updateMask.fieldPaths=playerName")
                    fieldsJsonList.add("\"playerName\": {\"stringValue\": \"$playerName\"}")
                }
                
                if (fieldsToUpdate.isNotEmpty()) {
                    val queryParams = fieldsToUpdate.joinToString("&")
                    val patchUrl = NSURL(string = "https://firestore.googleapis.com/v1/projects/blitz-math-challenge/databases/(default)/documents/$collection/$playerId?$queryParams&key=$apiKey") ?: return@withContext Result.failure(Exception("Invalid URL"))
                    val bodyJson = """
                    {
                      "fields": {
                        ${fieldsJsonList.joinToString(",\n")}
                      }
                    }
                    """.trimIndent()
                    
                    val patchRequest = NSMutableURLRequest(uRL = patchUrl).apply {
                        setHTTPMethod("PATCH")
                        setValue("application/json", forHTTPHeaderField = "Content-Type")
                        val bodyData = (bodyJson as NSString).dataUsingEncoding(NSUTF8StringEncoding)
                        if (bodyData != null) {
                            setHTTPBody(bodyData)
                        }
                    }
                    performRequest(patchRequest)
                }
                Result.success(Unit)
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPlayerRank(playerId: String, mode: String): Result<Int> {
        val result = getGlobalLeaderboard(1000, mode)
        return result.map { entries ->
            val index = entries.indexOfFirst { it.playerId == playerId }
            if (index != -1) index + 1 else 0
        }
    }

    override suspend fun getGlobalLeaderboard(limit: Int, mode: String): Result<List<LeaderboardEntry>> {
        return try {
            val collection = getCollectionName(mode)
            println("IosLeaderboard: Fetching $collection (limit=$limit)")
            
            val queryUrl = NSURL(string = "https://firestore.googleapis.com/v1/projects/blitz-math-challenge/databases/(default)/documents:runQuery?key=$apiKey")
                ?: return Result.failure(Exception("Invalid URL"))
            
            val queryJson = """
            {
              "structuredQuery": {
                "from": [{"collectionId": "$collection"}],
                "orderBy": [{"field": {"fieldPath": "totalScore"}, "direction": "DESCENDING"}],
                "limit": $limit
              }
            }
            """.trimIndent()

            val request = NSMutableURLRequest(uRL = queryUrl).apply {
                setHTTPMethod("POST")
                setValue("application/json", forHTTPHeaderField = "Content-Type")
                val bodyData = (queryJson as NSString).dataUsingEncoding(NSUTF8StringEncoding)
                if (bodyData != null) setHTTPBody(bodyData)
            }

            val response = performRequest(request)
            if (response == null) {
                println("IosLeaderboard: NULL response from Firestore")
                return Result.failure(Exception("No response from Firestore"))
            }
            
            println("IosLeaderboard: Response received, length=${response.length}")
            println("IosLeaderboard: Response preview=${response.take(200)}")
            
            val entries = mutableListOf<LeaderboardEntry>()
            val data = (response as NSString).dataUsingEncoding(NSUTF8StringEncoding)
            
            if (data != null) {
                val jsonObj = NSJSONSerialization.JSONObjectWithData(data, 0uL, null)
                val array = jsonObj as? NSArray
                
                if (array == null) {
                    println("IosLeaderboard: Response is not an array! jsonObj=$jsonObj")
                    val dict = jsonObj as? NSDictionary
                    val errorObj = dict?.objectForKey("error") as? NSDictionary
                    val message = errorObj?.objectForKey("message") as? String
                    if (message != null) {
                        return Result.failure(Exception(message))
                    }
                    return Result.success(emptyList())
                }
                
                println("IosLeaderboard: Array count=${array.count}")
                
                for (i in 0 until array.count.toInt()) {
                    val item = array.objectAtIndex(i.toULong()) as? NSDictionary ?: continue
                    val document = item.objectForKey("document") as? NSDictionary ?: continue
                    val fields = document.objectForKey("fields") as? NSDictionary ?: continue

                    val pId = (fields.objectForKey("playerId") as? NSDictionary)?.objectForKey("stringValue") as? String ?: ""
                    val pName = (fields.objectForKey("playerName") as? NSDictionary)?.objectForKey("stringValue") as? String ?: ""
                    val totalScoreStr = (fields.objectForKey("totalScore") as? NSDictionary)?.objectForKey("integerValue") as? String
                        ?: (fields.objectForKey("totalScore") as? NSDictionary)?.objectForKey("doubleValue")?.toString() ?: "0"
                    val highestLevelStr = (fields.objectForKey("highestLevel") as? NSDictionary)?.objectForKey("integerValue") as? String ?: "1"
                    val country = (fields.objectForKey("country") as? NSDictionary)?.objectForKey("stringValue") as? String ?: ""

                    println("IosLeaderboard: Entry[$i] pId=$pId pName=$pName score=$totalScoreStr")

                    if (pId.isNotEmpty()) {
                        entries.add(
                            LeaderboardEntry(
                                playerId = pId,
                                playerName = pName,
                                totalScore = totalScoreStr.toLongOrNull() ?: 0L,
                                highestLevel = highestLevelStr.toIntOrNull() ?: 1,
                                country = country
                            )
                        )
                    }
                }
            }
            
            println("IosLeaderboard: Returning ${entries.size} entries")
            Result.success(entries)
        } catch (e: Exception) {
            println("IosLeaderboard: Exception: ${e.message}")
            Result.failure(e)
        }
    }
}

class IosDummyMultiplayerController : IMultiplayerController {
    override fun startMatchmaking(
        playerId: String,
        playerName: String,
        level: Int,
        country: String,
        onMatched: (
            lobbyId: String,
            role: Int,
            opponentName: String,
            opponentLevel: Int,
            opponentCountry: String,
            seed: Long,
            startTime: Long
        ) -> Unit
    ) {}

    override fun cancelMatchmaking(playerId: String) {}

    override fun createCustomRoom(
        playerId: String,
        playerName: String,
        level: Int,
        country: String,
        onRoomCreated: (roomCode: String) -> Unit,
        onMatched: (
            lobbyId: String,
            role: Int,
            opponentName: String,
            opponentLevel: Int,
            opponentCountry: String,
            seed: Long,
            startTime: Long
        ) -> Unit,
        onError: (String) -> Unit
    ) {}

    override fun joinCustomRoom(
        roomCode: String,
        playerId: String,
        playerName: String,
        level: Int,
        country: String,
        onMatched: (
            lobbyId: String,
            role: Int,
            opponentName: String,
            opponentLevel: Int,
            opponentCountry: String,
            seed: Long,
            startTime: Long
        ) -> Unit,
        onError: (String) -> Unit
    ) {}

    override fun cancelCustomRoom(roomCode: String, playerId: String) {}

    override fun observeLobby(
        lobbyId: String,
        onUpdate: (LobbyState) -> Unit,
        onError: (String) -> Unit
    ) {}

    override fun stopObservingLobby() {}
    override fun updateScore(lobbyId: String, role: Int, score: Long) {}
    override fun sendEmote(lobbyId: String, role: Int, emoteText: String) {}
    override fun requestRematch(lobbyId: String, role: Int, request: Boolean) {}
    override fun updateLobbyStatus(lobbyId: String, status: String) {}
    override fun deleteLobby(lobbyId: String) {}
    override fun acceptRematch(lobbyId: String, role: Int) {}

    override fun submitCorrectAnswer(
        lobbyId: String,
        role: Int,
        playerId: String,
        questionIndex: Long,
        onResult: (Boolean) -> Unit
    ) {
        onResult(true)
    }

    override fun submitWrongAnswer(
        lobbyId: String,
        role: Int,
        questionIndex: Long,
        onResult: (Boolean) -> Unit
    ) {
        onResult(true)
    }

    override fun advanceQuestionIndex(
        lobbyId: String,
        currentIndex: Long,
        onResult: (Boolean) -> Unit
    ) {
        onResult(false)
    }
}

class IosPlatformServices(
    private val customAdController: IAdController? = null,
    private val customMultiplayerController: IMultiplayerController? = null
) : PlatformServices {
    private var cachedCountry: String = NSLocale.currentLocale.countryCode ?: "US"

    init {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val url = NSURL.URLWithString("https://ip2c.org/self")
                if (url != null) {
                    val responseString = NSString.stringWithContentsOfURL(url, NSUTF8StringEncoding, null) as String?
                    if (responseString != null) {
                        val parts = responseString.split(";")
                        if (parts.size >= 2 && parts[0] == "1") {
                            val ipCountry = parts[1].uppercase().trim()
                            if (ipCountry.length == 2) {
                                cachedCountry = ipCountry
                            }
                        }
                    }
                }
            } catch (t: Throwable) {
                // Keep default/locale fallback
            }
        }
    }

    override val soundManager: ISoundManager = IosSoundManager()
    override val hapticManager: IHapticManager = IosHapticManager()
    override val analyticsManager: IAnalyticsManager = IosAnalyticsManager()
    override val shareManager: IShareManager = IosShareManager()
    override val adController: IAdController = customAdController ?: IosAdController()
    override val multiplayerController: IMultiplayerController = customMultiplayerController ?: IosDummyMultiplayerController()
    override val leaderboardManager: ILeaderboardManager = IosLeaderboardManager()
    override val deviceCountry: String get() = cachedCountry
    override fun generateUuid(): String = platform.Foundation.NSUUID.UUID().UUIDString()

    override fun getCurrentTimeMillis(): Long {
        return (NSDate().timeIntervalSince1970 * 1000).toLong()
    }

    override fun getCurrentDateString(): String {
        val formatter = NSDateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter.stringFromDate(NSDate())
    }

    override fun openUrl(url: String) {
        val nsUrl = NSURL.URLWithString(url) ?: return
        UIApplication.sharedApplication.openURL(nsUrl)
    }

    override fun scheduleCardRecharge(cardId: String, delayMinutes: Long) {
        val card = com.mawelly.blitzmath.game.ScientistCards.getCardById(cardId) ?: return
        val center = platform.UserNotifications.UNUserNotificationCenter.currentNotificationCenter()
        
        val options = platform.UserNotifications.UNAuthorizationOptionAlert or 
                      platform.UserNotifications.UNAuthorizationOptionSound
                      
        center.requestAuthorizationWithOptions(options) { granted, error ->
            if (granted) {
                val content = platform.UserNotifications.UNMutableNotificationContent().apply {
                    setTitle("Enerji Doldu! ⚡")
                    setBody("${card.name} artık hazır. Gel ve zihnini tazele! 🧠")
                    setSound(platform.UserNotifications.UNNotificationSound.defaultSound())
                }
                
                val trigger = platform.UserNotifications.UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
                    timeInterval = delayMinutes * 60.0,
                    repeats = false
                )
                
                val request = platform.UserNotifications.UNNotificationRequest.requestWithIdentifier(
                    identifier = "recharge_$cardId",
                    content = content,
                    trigger = trigger
                )
                
                center.addNotificationRequest(request) { err ->
                    if (err != null) {
                        println("Notification Error: ${err.localizedDescription}")
                    }
                }
            }
        }
    }

    override fun cancelCardRecharge(cardId: String) {
        val center = platform.UserNotifications.UNUserNotificationCenter.currentNotificationCenter()
        center.removePendingNotificationRequestsWithIdentifiers(listOf("recharge_$cardId"))
    }
}

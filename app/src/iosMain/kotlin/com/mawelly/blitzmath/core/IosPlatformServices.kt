@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package com.mawelly.blitzmath.core

import platform.Foundation.*
import platform.UIKit.*
import platform.AVFAudio.AVAudioPlayer
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
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
        val text = "BlitzMath Challenge'da $score skoruna ulaştım! 🧠 Sen de katıl ve zihnini test et! ⚡"
        val items = listOf(text)
        val activityController = platform.UIKit.UIActivityViewController(
            activityItems = items,
            applicationActivities = null
        )
        val window = platform.UIKit.UIApplication.sharedApplication.keyWindow
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
        val window = platform.UIKit.UIApplication.sharedApplication.keyWindow
        val rootVC = window?.rootViewController
        if (rootVC == null) {
            onReward()
            onClosed()
            return
        }

        val alert = UIAlertController.alertControllerWithTitle(
            title = "Reklam İzleniyor",
            message = "Ödülünüz 5 saniye içinde verilecektir...",
            preferredStyle = UIAlertControllerStyleAlert
        )

        var completed = false
        var countdownJob: kotlinx.coroutines.Job? = null

        val cancelAction = UIAlertAction.actionWithTitle(
            title = "Reklamı Kapat",
            style = UIAlertActionStyleCancel,
            handler = { _ ->
                countdownJob?.cancel()
                if (!completed) {
                    onClosed()
                }
            }
        )
        alert.addAction(cancelAction)

        rootVC.presentViewController(alert, animated = true, completion = null)

        countdownJob = CoroutineScope(Dispatchers.Main).launch {
            for (i in 5 downTo 1) {
                alert.setMessage("Ödülünüz $i saniye içinde verilecektir...")
                delay(1000)
            }
            completed = true
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
        val task = NSURLSession.sharedSession.dataTaskWithRequest(request = request, completionHandler = { data: NSData?, response: NSURLResponse?, error: NSError? ->
            if (error != null) {
                continuation.resume(null)
            } else if (data != null) {
                val nsString = NSString.create(data = data, encoding = NSUTF8StringEncoding)
                continuation.resume(nsString?.toString())
            } else {
                continuation.resume(null)
            }
        })
        task.resume()
        continuation.invokeOnCancellation {
            task.cancel()
        }
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
            val queryUrl = NSURL(string = "https://firestore.googleapis.com/v1/projects/blitz-math-challenge/databases/(default)/documents:runQuery?key=$apiKey") ?: return Result.failure(Exception("Invalid URL"))
            
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
                if (bodyData != null) {
                    setHTTPBody(bodyData)
                }
            }

            val response = performRequest(request) ?: return Result.failure(Exception("No response from Firestore"))
            val entries = mutableListOf<LeaderboardEntry>()

            val data = (response as NSString).dataUsingEncoding(NSUTF8StringEncoding)
            if (data != null) {
                val json = NSJSONSerialization.JSONObjectWithData(data, 0uL, null)
                val array = json as? NSArray
                if (array != null) {
                    for (i in 0 until array.count.toInt()) {
                        val item = array.objectAtIndex(i.toULong()) as? NSDictionary ?: continue
                        val document = item["document"] as? NSDictionary ?: continue
                        val fields = document["fields"] as? NSDictionary ?: continue

                        val pId = (fields["playerId"] as? NSDictionary)?.get("stringValue") as? String ?: ""
                        val pName = (fields["playerName"] as? NSDictionary)?.get("stringValue") as? String ?: ""
                        val totalScoreStr = (fields["totalScore"] as? NSDictionary)?.get("integerValue") as? String ?: "0"
                        val highestLevelStr = (fields["highestLevel"] as? NSDictionary)?.get("integerValue") as? String ?: "1"
                        val country = (fields["country"] as? NSDictionary)?.get("stringValue") as? String ?: ""

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
            }
            Result.success(entries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class IosPlatformServices : PlatformServices {
    override val soundManager: ISoundManager = IosSoundManager()
    override val hapticManager: IHapticManager = IosHapticManager()
    override val analyticsManager: IAnalyticsManager = IosAnalyticsManager()
    override val shareManager: IShareManager = IosShareManager()
    override val adController: IAdController = IosAdController()
    override val leaderboardManager: ILeaderboardManager = IosLeaderboardManager()
    override val deviceCountry: String get() = NSLocale.currentLocale.countryCode ?: "US"
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

@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package com.mawelly.blitzmath.core

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSURL
import platform.Foundation.NSBundle
import platform.UIKit.UIApplication
import platform.Foundation.timeIntervalSince1970

import platform.AVFAudio.AVAudioPlayer
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSJSONSerialization
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSString
import platform.Foundation.NSURLRequest
import platform.Foundation.NSURLSession
import platform.Foundation.NSArray
import platform.Foundation.NSDictionary
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import com.mawelly.blitzmath.leaderboard.ILeaderboardManager
import com.mawelly.blitzmath.leaderboard.LeaderboardEntry

class IosSoundManager : ISoundManager {
    private var isEnabled = true
    private val players = mutableMapOf<String, AVAudioPlayer>()

    private fun getPlayer(fileName: String, type: String): AVAudioPlayer? {
        if (players.containsKey(fileName)) {
            return players[fileName]
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
    override fun shareScore(score: Int) {}
}

class IosAdController : IAdController {
    override fun showInterstitialAd(onClosed: () -> Unit) { onClosed() }
    override fun showRewardedAd(onReward: () -> Unit, onClosed: () -> Unit) {
        onReward()
        onClosed()
    }
}

class IosLeaderboardManager : ILeaderboardManager {
    private val apiKey = "AIzaSyBJfQl_ze9VsL_gQLMC5zFyje-wq3T8_IQ"

    private suspend fun performRequest(request: NSURLRequest): String? = suspendCancellableCoroutine { continuation ->
        val task = NSURLSession.sharedSession.dataTaskWithRequest(request) { data, response, error ->
            if (error != null) {
                continuation.resume(null)
            } else if (data != null) {
                val nsString = NSString.create(data = data, encoding = NSUTF8StringEncoding)
                continuation.resume(nsString?.toString())
            } else {
                continuation.resume(null)
            }
        }
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
    ): Result<Unit> {
        if (playerId.isEmpty()) return Result.failure(Exception("Empty Player ID"))

        return try {
            val collection = getCollectionName(mode)
            val getUrl = NSURL(string = "https://firestore.googleapis.com/v1/projects/blitz-math-challenge/databases/(default)/documents/$collection/$playerId?key=$apiKey")
            val getRequest = NSMutableURLRequest.requestWithURL(getUrl).apply {
                setHTTPMethod("GET")
            }

            val getResponse = performRequest(getRequest)
            var existingScore = -1L

            if (getResponse != null) {
                try {
                    val data = (getResponse as NSString).dataUsingEncoding(NSUTF8StringEncoding)
                    if (data != null) {
                        val json = NSJSONSerialization.JSONObjectWithData(data, 0L, null) as? NSDictionary
                        val fields = json?.get("fields") as? NSDictionary
                        val totalScoreStr = (fields?.get("totalScore") as? NSDictionary)?.get("integerValue") as? String
                        if (totalScoreStr != null) {
                            existingScore = totalScoreStr.toLongOrNull() ?: 0L
                        }
                    }
                } catch (e: Exception) {
                    // Document might not exist (404)
                }
            }

            if (score > existingScore) {
                val patchUrl = NSURL(string = "https://firestore.googleapis.com/v1/projects/blitz-math-challenge/databases/(default)/documents/$collection/$playerId?updateMask.fieldPaths=playerId&updateMask.fieldPaths=playerName&updateMask.fieldPaths=totalScore&updateMask.fieldPaths=highestLevel&updateMask.fieldPaths=country&key=$apiKey")
                
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

                val patchRequest = NSMutableURLRequest.requestWithURL(patchUrl).apply {
                    setHTTPMethod("PATCH")
                    setValue("application/json", forHTTPHeaderField = "Content-Type")
                    setHTTPBody((bodyJson as NSString).dataUsingEncoding(NSUTF8StringEncoding))
                }

                val patchResponse = performRequest(patchRequest)
                if (patchResponse != null) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to submit score"))
                }
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPlayerRank(playerId: String, mode: String): Result<Int> {
        val result = getGlobalLeaderboard(100, mode)
        return result.map { entries ->
            val index = entries.indexOfFirst { it.playerId == playerId }
            if (index != -1) index + 1 else 0
        }
    }

    override suspend fun getGlobalLeaderboard(limit: Int, mode: String): Result<List<LeaderboardEntry>> {
        return try {
            val collection = getCollectionName(mode)
            val queryUrl = NSURL(string = "https://firestore.googleapis.com/v1/projects/blitz-math-challenge/databases/(default)/documents:runQuery?key=$apiKey")
            
            val queryJson = """
            {
              "structuredQuery": {
                "from": [{"collectionId": "$collection"}],
                "orderBy": [{"field": {"fieldPath": "totalScore"}, "direction": "DESCENDING"}],
                "limit": $limit
              }
            }
            """.trimIndent()

            val request = NSMutableURLRequest.requestWithURL(queryUrl).apply {
                setHTTPMethod("POST")
                setValue("application/json", forHTTPHeaderField = "Content-Type")
                setHTTPBody((queryJson as NSString).dataUsingEncoding(NSUTF8StringEncoding))
            }

            val response = performRequest(request) ?: return Result.failure(Exception("No response from Firestore"))
            val entries = mutableListOf<LeaderboardEntry>()

            val data = (response as NSString).dataUsingEncoding(NSUTF8StringEncoding)
            if (data != null) {
                val json = NSJSONSerialization.JSONObjectWithData(data, 0L, null)
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
}

package com.mawelly.blitzmath.leaderboard

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import androidx.annotation.Keep
import kotlinx.coroutines.tasks.await
import java.util.Date

@Keep
data class LeaderboardEntry(
    var playerId: String = "",
    var playerName: String = "",
    var totalScore: Long = 0,
    var highestLevel: Int = 1,
    var country: String = "",
    var timestamp: Date = Date()
)

class LeaderboardManager {
    
    private val db: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            android.util.Log.e("LeaderboardManager", "Firestore initialization failed: ${e.message}")
            null
        }
    }
    
    private val classicRef by lazy { db?.collection("global_leaderboard") }
    private val mixedRef by lazy { db?.collection("mixed_leaderboard") }
    private val challengeRef by lazy { db?.collection("challenge_leaderboard") }

    private fun getRefForMode(mode: String): com.google.firebase.firestore.CollectionReference? {
        return when (mode.lowercase()) {
            "mixed" -> mixedRef
            "challenge" -> challengeRef
            else -> classicRef
        }
    }

    fun isScoreValid(score: Long, level: Int): Boolean {
        if (level <= 0) return false
        
        // Temel puan: level * 1000 (checkpoint bonus)
        // Soru puanı (max): Her soru 130 puan * 10 soru * level sayısı
        val maxBaseScore = (level * 1000L) + (level * 10 * 130L)
        
        // Marie Curie (+%25) buff'ı olsa bile, normal bir oyunda skorun 
        // teorik maksimumun 5 katını geçmesi imkansıza yakındır.
        val generousLimit = maxBaseScore * 5
        
        return score <= generousLimit
    }

    suspend fun submitScore(
        playerId: String,
        playerName: String,
        score: Long,
        level: Int,
        country: String = "",
        mode: String = "classic"
    ): Result<Unit> {
        if (playerId.isEmpty()) return Result.failure(Exception("Empty Player ID"))

        return try {
            if (!isScoreValid(score, level)) return Result.failure(Exception("Suspicious Score"))

            val targetRef = getRefForMode(mode) ?: return Result.failure(Exception("Firestore not available"))
            val targetDoc = targetRef.document(playerId)

            db?.runTransaction { transaction ->
                val snapshot = transaction.get(targetDoc)
                val existingScore = if (snapshot.exists()) snapshot.getLong("totalScore") ?: 0 else -1L
                
                if (score > existingScore) {
                    val entry = hashMapOf(
                        "playerId" to playerId,
                        "playerName" to playerName,
                        "totalScore" to score,
                        "highestLevel" to level,
                        "country" to country,
                        "timestamp" to Date()
                    )
                    transaction.set(targetDoc, entry)
                }
            }?.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGlobalLeaderboard(limit: Int = 100, mode: String = "classic"): Result<List<LeaderboardEntry>> {
        return try {
            val targetRef = getRefForMode(mode) ?: return Result.failure(Exception("Firestore not available"))
            val snapshot = targetRef
                .orderBy("totalScore", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val entries = snapshot.toObjects(LeaderboardEntry::class.java)
            Result.success(entries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPlayerRank(playerId: String, mode: String = "classic"): Result<Int> {
        return try {
            if (playerId.isEmpty()) return Result.success(0)

            val targetRef = getRefForMode(mode) ?: return Result.success(0)
            val snapshot = targetRef
                .orderBy("totalScore", Query.Direction.DESCENDING)
                .get()
                .await()

            var rank = 1
            for (doc in snapshot.documents) {
                if (doc.id == playerId) return Result.success(rank)
                rank++
            }
            Result.success(0)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFcmToken(playerId: String, token: String): Result<Unit> {
        if (playerId.isEmpty() || token.isEmpty()) return Result.failure(Exception("Invalid data"))
        val firestore = db ?: return Result.failure(Exception("Firestore not available"))
        return try {
            firestore.collection("users").document(playerId)
                .set(hashMapOf("fcmToken" to token, "lastActive" to Date()), com.google.firebase.firestore.SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
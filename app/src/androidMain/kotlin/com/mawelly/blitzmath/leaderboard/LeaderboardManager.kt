package com.mawelly.blitzmath.leaderboard

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import androidx.annotation.Keep
import kotlinx.coroutines.tasks.await
import java.util.Date



class LeaderboardManager : ILeaderboardManager {
    
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

    fun isScoreValid(score: Long, level: Int, mode: String): Boolean {
        android.util.Log.d("LeaderboardManager", "isScoreValid: Bypassed validation, returning true for score: $score, level: $level, mode: $mode")
        return true
    }

    override suspend fun submitScore(
        playerId: String,
        playerName: String,
        score: Long,
        level: Int,
        country: String,
        mode: String
    ): Result<Unit> = kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
        android.util.Log.i("LeaderboardManager", "submitScore: playerId: $playerId, playerName: $playerName, score: $score, level: $level, country: $country, mode: $mode")
        if (playerId.isEmpty()) {
            android.util.Log.w("LeaderboardManager", "submitScore: Empty Player ID")
            return@withContext Result.failure(Exception("Empty Player ID"))
        }

        return@withContext try {
            if (!isScoreValid(score, level, mode)) {
                android.util.Log.w("LeaderboardManager", "submitScore: Validation failed (Suspicious Score)")
                return@withContext Result.failure(Exception("Suspicious Score"))
            }

            val targetRef = getRefForMode(mode) ?: run {
                android.util.Log.w("LeaderboardManager", "submitScore: Firestore collection not available for mode $mode")
                return@withContext Result.failure(Exception("Firestore not available"))
            }
            val targetDoc = targetRef.document(playerId)

            db?.runTransaction { transaction ->
                val snapshot = transaction.get(targetDoc)
                val existingScore = if (snapshot.exists()) snapshot.getLong("totalScore") ?: 0 else -1L
                val existingCountry = if (snapshot.exists()) snapshot.getString("country") ?: "" else ""
                val existingName = if (snapshot.exists()) snapshot.getString("playerName") ?: "" else ""
                
                android.util.Log.d("LeaderboardManager", "submitScore transaction - existingScore: $existingScore, existingCountry: $existingCountry, existingName: $existingName")
                
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
                    android.util.Log.i("LeaderboardManager", "submitScore transaction: Updating score from $existingScore to $score")
                } else if (snapshot.exists() && (existingCountry.isEmpty() || existingName.isEmpty())) {
                    val updates = hashMapOf<String, Any>()
                    if (existingCountry.isEmpty() && country.isNotEmpty()) {
                        updates["country"] = country
                    }
                    if (existingName.isEmpty() && playerName.isNotEmpty()) {
                        updates["playerName"] = playerName
                    }
                    if (updates.isNotEmpty()) {
                        transaction.update(targetDoc, updates)
                        android.util.Log.i("LeaderboardManager", "submitScore transaction: Updating fields $updates")
                    }
                } else {
                    android.util.Log.d("LeaderboardManager", "submitScore transaction: No update needed (current score $score <= existing $existingScore)")
                }
            }?.await()

            android.util.Log.i("LeaderboardManager", "submitScore: Successfully submitted score $score for playerId $playerId")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("LeaderboardManager", "submitScore: Exception occurred: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getGlobalLeaderboard(limit: Int, mode: String): Result<List<LeaderboardEntry>> {
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

    override suspend fun getPlayerRank(playerId: String, mode: String): Result<Int> {
        return try {
            if (playerId.isEmpty()) return Result.success(0)

            val targetRef = getRefForMode(mode) ?: return Result.success(0)
            
            // First, find the score of the current player
            val playerDoc = targetRef.document(playerId).get().await()
            if (!playerDoc.exists()) return Result.success(0)
            
            val playerScore = playerDoc.getLong("totalScore") ?: 0L
            
            // Count how many players have a score strictly greater than the player's score
            val countQuery = targetRef.whereGreaterThan("totalScore", playerScore)
                .count()
                .get(com.google.firebase.firestore.AggregateSource.SERVER)
                .await()
                
            val rank = countQuery.count.toInt() + 1
            android.util.Log.d("LeaderboardManager", "getPlayerRank count query success - playerId: $playerId, score: $playerScore, rank: $rank")
            Result.success(rank)
        } catch (e: Exception) {
            android.util.Log.e("LeaderboardManager", "getPlayerRank failed: ${e.message}", e)
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
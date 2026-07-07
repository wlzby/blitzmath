package com.mawelly.blitzmath.core

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.*
import java.util.UUID
import kotlin.random.Random
import kotlin.coroutines.resume

class AndroidMultiplayerController : IMultiplayerController {
    private val db by lazy { FirebaseFirestore.getInstance() }
    private var matchmakingScope = CoroutineScope(Dispatchers.Main + Job())
    private var customRoomScope = CoroutineScope(Dispatchers.Main + Job())
    private var myTicketListener: ListenerRegistration? = null
    private var customRoomListener: ListenerRegistration? = null
    private var lobbyListener: ListenerRegistration? = null

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
    ) {
        val validPlayerId = if (playerId.isNotEmpty()) playerId else UUID.randomUUID().toString()
        cancelMatchmaking(validPlayerId)
        
        matchmakingScope = CoroutineScope(Dispatchers.Main + Job())
        val myCreatedAt = System.currentTimeMillis()
        
        val myTicketRef = db.collection("vs_queue").document(validPlayerId)
        val myTicketData = hashMapOf(
            "playerId" to validPlayerId,
            "playerName" to playerName,
            "level" to level,
            "country" to country,
            "status" to "searching",
            "matchedLobbyId" to "",
            "createdAt" to myCreatedAt
        )

        myTicketRef.set(myTicketData).addOnFailureListener { e ->
            Log.e("AndroidMultiplayer", "Failed to set matchmaking ticket in vs_queue: ${e.message}", e)
        }

        myTicketListener = myTicketRef.addSnapshotListener { snap, err ->
            if (err != null) {
                Log.e("AndroidMultiplayer", "Error listening to matchmaking ticket: ${err.message}", err)
                return@addSnapshotListener
            }
            if (snap != null && snap.exists()) {
                val status = snap.getString("status") ?: "searching"
                val lobbyId = snap.getString("matchedLobbyId") ?: ""
                if (status == "matched" && lobbyId.isNotEmpty()) {
                    matchmakingScope.launch {
                        var matched = false
                        for (attempt in 1..5) {
                            db.collection("vs_lobbies").document(lobbyId).get()
                                .addOnSuccessListener { lobbySnap ->
                                    if (lobbySnap != null && lobbySnap.exists()) {
                                        val p2Name = lobbySnap.getString("player2Name") ?: ""
                                        val p2Level = lobbySnap.getLong("player2Level")?.toInt() ?: 1
                                        val p2Country = lobbySnap.getString("player2Country") ?: "US"
                                        val startTime = lobbySnap.getLong("gameStartTimestamp") ?: 0L
                                        val seed = lobbySnap.getLong("seed") ?: 0L

                                        cancelMatchmaking(validPlayerId)
                                        onMatched(lobbyId, 1, p2Name, p2Level, p2Country, seed, startTime)
                                        matched = true
                                    }
                                }
                            if (matched) break
                            delay(400)
                        }
                    }
                }
            }
        }

        matchmakingScope.launch {
            while (isActive) {
                val snapshot = suspendGetSearchingTickets()
                if (!isActive) break

                val suitableDoc = if (snapshot != null && !snapshot.isEmpty) {
                    snapshot.documents
                        .filter { doc ->
                            val docId = doc.id
                            val docCreatedAt = doc.getLong("createdAt") ?: 0L
                            val isSearching = doc.getString("status") == "searching"

                            docId != validPlayerId &&
                            isSearching &&
                            (docCreatedAt < myCreatedAt || (docCreatedAt == myCreatedAt && docId < validPlayerId))
                        }
                        .minByOrNull { it.getLong("createdAt") ?: 0L }
                } else null

                if (suitableDoc != null) {
                    val olderTicketId = suitableDoc.id
                    val oppName = suitableDoc.getString("playerName") ?: ""
                    val oppLevel = suitableDoc.getLong("level")?.toInt() ?: 1
                    val oppCountry = suitableDoc.getString("country") ?: "US"

                    val lobbyId = UUID.randomUUID().toString().take(8)
                    val seed = Random.nextLong()
                    val startTime = System.currentTimeMillis() + 3500L

                    val success = suspendMatchTicketsTransaction(
                        olderTicketId = olderTicketId,
                        newerTicketId = validPlayerId,
                        lobbyId = lobbyId,
                        seed = seed,
                        startTime = startTime,
                        player1Id = olderTicketId,
                        player1Name = oppName,
                        player1Level = oppLevel,
                        player1Country = oppCountry,
                        player2Id = validPlayerId,
                        player2Name = playerName,
                        player2Level = level,
                        player2Country = country
                    )

                    if (success && isActive) {
                        cancelMatchmaking(validPlayerId)
                        onMatched(lobbyId, 2, oppName, oppLevel, oppCountry, seed, startTime)
                        break
                    }
                }

                delay(3000)
            }
        }
    }

    override fun cancelMatchmaking(playerId: String) {
        matchmakingScope.cancel()
        myTicketListener?.remove()
        myTicketListener = null
        if (playerId.isNotEmpty()) {
            db.collection("vs_queue").document(playerId).delete()
        }
    }

    override fun observeLobby(
        lobbyId: String,
        onUpdate: (LobbyState) -> Unit,
        onError: (String) -> Unit
    ) {
        lobbyListener?.remove()
        val lobbyRef = db.collection("vs_lobbies").document(lobbyId)
        lobbyListener = lobbyRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("AndroidMultiplayer", "Error listening to lobby: ${error.message}", error)
                onError(error.message ?: "Lobby error")
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val status = snapshot.getString("status") ?: "waiting"
                val p1Score = snapshot.getLong("player1Score") ?: 0L
                val p2Score = snapshot.getLong("player2Score") ?: 0L
                val dbIndex = snapshot.getLong("currentQuestionIndex") ?: 0L
                val lastAnswerer = snapshot.getString("lastAnswererId") ?: ""
                val p1Emote = snapshot.getString("p1Emote")
                val p2Emote = snapshot.getString("p2Emote")
                val rematchP1 = snapshot.getBoolean("rematchP1") ?: false
                val rematchP2 = snapshot.getBoolean("rematchP2") ?: false

                onUpdate(
                    LobbyState(
                        status = status,
                        player1Score = p1Score,
                        player2Score = p2Score,
                        currentQuestionIndex = dbIndex,
                        lastAnswererId = lastAnswerer,
                        p1Emote = p1Emote,
                        p2Emote = p2Emote,
                        rematchP1 = rematchP1,
                        rematchP2 = rematchP2
                    )
                )
            }
        }
    }

    override fun stopObservingLobby() {
        lobbyListener?.remove()
        lobbyListener = null
    }

    override fun updateScore(lobbyId: String, role: Int, score: Long) {
        val field = if (role == 1) "player1Score" else "player2Score"
        db.collection("vs_lobbies").document(lobbyId).update(field, score)
            .addOnFailureListener { e ->
                Log.e("AndroidMultiplayer", "Failed to update score: ${e.message}", e)
            }
    }

    override fun sendEmote(lobbyId: String, role: Int, emoteText: String) {
        val field = if (role == 1) "p1Emote" else "p2Emote"
        val payload = "$emoteText|${System.currentTimeMillis()}"
        db.collection("vs_lobbies").document(lobbyId).update(field, payload)
            .addOnFailureListener { e ->
                Log.e("AndroidMultiplayer", "Failed to send emote: ${e.message}", e)
            }
    }

    override fun requestRematch(lobbyId: String, role: Int, request: Boolean) {
        val field = if (role == 1) "rematchP1" else "rematchP2"
        db.collection("vs_lobbies").document(lobbyId).update(field, request)
            .addOnFailureListener { e ->
                Log.e("AndroidMultiplayer", "Failed to request rematch: ${e.message}", e)
            }
    }

    override fun updateLobbyStatus(lobbyId: String, status: String) {
        db.collection("vs_lobbies").document(lobbyId).update("status", status)
            .addOnFailureListener { e ->
                Log.e("AndroidMultiplayer", "Failed to update status to $status: ${e.message}", e)
            }
    }

    override fun deleteLobby(lobbyId: String) {
        db.collection("vs_lobbies").document(lobbyId).delete()
            .addOnFailureListener { e ->
                Log.e("AndroidMultiplayer", "Failed to delete lobby $lobbyId: ${e.message}", e)
            }
    }

    override fun acceptRematch(lobbyId: String, role: Int) {
        val lobbyRef = db.collection("vs_lobbies").document(lobbyId)
        val updateField = if (role == 1) "rematchP1" else "rematchP2"
        db.runTransaction { transaction ->
            val snap = transaction.get(lobbyRef)
            val p1Rematch = if (role == 1) true else (snap.getBoolean("rematchP1") ?: false)
            val p2Rematch = if (role == 2) true else (snap.getBoolean("rematchP2") ?: false)

            transaction.update(lobbyRef, updateField, true)

            if (p1Rematch && p2Rematch) {
                transaction.update(lobbyRef, "status", "active")
                transaction.update(lobbyRef, "player1Score", 0L)
                transaction.update(lobbyRef, "player2Score", 0L)
                transaction.update(lobbyRef, "p1Emote", null)
                transaction.update(lobbyRef, "p2Emote", null)
                transaction.update(lobbyRef, "rematchP1", false)
                transaction.update(lobbyRef, "rematchP2", false)
                transaction.update(lobbyRef, "currentQuestionIndex", 0L)
                transaction.update(lobbyRef, "gameStartTimestamp", System.currentTimeMillis() + 3500L)
            }
        }.addOnFailureListener { e ->
            Log.e("AndroidMultiplayer", "Failed to accept rematch: ${e.message}", e)
        }
    }

    override fun submitCorrectAnswer(
        lobbyId: String,
        role: Int,
        playerId: String,
        questionIndex: Long,
        onResult: (Boolean) -> Unit
    ) {
        val lobbyRef = db.collection("vs_lobbies").document(lobbyId)
        db.runTransaction { transaction ->
            val snap = transaction.get(lobbyRef)
            val dbIndex = snap.getLong("currentQuestionIndex") ?: 0L
            if (dbIndex == questionIndex) {
                val scoreField = if (role == 1) "player1Score" else "player2Score"
                val currentScore = snap.getLong(scoreField) ?: 0L
                transaction.update(
                    lobbyRef, mapOf(
                        "currentQuestionIndex" to dbIndex + 1,
                        scoreField to currentScore + 10,
                        "lastAnswererId" to playerId
                    )
                )
                true
            } else {
                false
            }
        }.addOnSuccessListener { result ->
            onResult(result)
        }.addOnFailureListener { e ->
            Log.e("AndroidMultiplayer", "Failed to submit correct answer: ${e.message}", e)
            onResult(false)
        }
    }

    override fun submitWrongAnswer(
        lobbyId: String,
        role: Int,
        questionIndex: Long,
        onResult: (Boolean) -> Unit
    ) {
        val lobbyRef = db.collection("vs_lobbies").document(lobbyId)
        db.runTransaction { transaction ->
            val snap = transaction.get(lobbyRef)
            val dbIndex = snap.getLong("currentQuestionIndex") ?: 0L
            if (dbIndex == questionIndex) {
                val myWrongField = if (role == 1) "player1WrongIndex" else "player2WrongIndex"
                val oppWrongField = if (role == 1) "player2WrongIndex" else "player1WrongIndex"
                
                val oppWrongIndex = snap.getLong(oppWrongField) ?: -1L
                if (oppWrongIndex == dbIndex) {
                    transaction.update(
                        lobbyRef, mapOf(
                            myWrongField to dbIndex,
                            "currentQuestionIndex" to dbIndex + 1
                        )
                    )
                } else {
                    transaction.update(lobbyRef, myWrongField, dbIndex)
                }
                true
            } else {
                false
            }
        }.addOnSuccessListener { result ->
            onResult(result)
        }.addOnFailureListener { e ->
            Log.e("AndroidMultiplayer", "Failed to submit wrong answer: ${e.message}", e)
            onResult(false)
        }
    }

    override fun advanceQuestionIndex(
        lobbyId: String,
        currentIndex: Long,
        onResult: (Boolean) -> Unit
    ) {
        val lobbyRef = db.collection("vs_lobbies").document(lobbyId)
        db.runTransaction { transaction ->
            val snap = transaction.get(lobbyRef)
            val dbIndex = snap.getLong("currentQuestionIndex") ?: 0L
            if (dbIndex == currentIndex) {
                transaction.update(lobbyRef, "currentQuestionIndex", dbIndex + 1)
                true
            } else {
                false
            }
        }.addOnSuccessListener { result ->
            onResult(result)
        }.addOnFailureListener { e ->
            Log.e("AndroidMultiplayer", "Failed to advance question index: ${e.message}", e)
            onResult(false)
        }
    }

    private suspend fun suspendGetSearchingTickets(): com.google.firebase.firestore.QuerySnapshot? =
        suspendCancellableCoroutine { continuation ->
            db.collection("vs_queue")
                .whereEqualTo("status", "searching")
                .limit(20)
                .get()
                .addOnSuccessListener { snapshot ->
                    continuation.resume(snapshot)
                }
                .addOnFailureListener { exception ->
                    Log.e("AndroidMultiplayer", "Failed to get searching tickets: ${exception.message}", exception)
                    continuation.resume(null)
                }
        }

    private suspend fun suspendMatchTicketsTransaction(
        olderTicketId: String,
        newerTicketId: String,
        lobbyId: String,
        seed: Long,
        startTime: Long,
        player1Id: String,
        player1Name: String,
        player1Level: Int,
        player1Country: String,
        player2Id: String,
        player2Name: String,
        player2Level: Int,
        player2Country: String
    ): Boolean = suspendCancellableCoroutine { continuation ->
        val olderTicketRef = db.collection("vs_queue").document(olderTicketId)
        val newerTicketRef = db.collection("vs_queue").document(newerTicketId)
        val lobbyRef = db.collection("vs_lobbies").document(lobbyId)

        db.runTransaction { transaction ->
            val snapOlder = transaction.get(olderTicketRef)
            val snapNewer = transaction.get(newerTicketRef)

            val statusOlder = snapOlder.getString("status") ?: "searching"
            val statusNewer = snapNewer.getString("status") ?: "searching"

            if (statusOlder == "searching" && statusNewer == "searching") {
                transaction.update(olderTicketRef, mapOf("status" to "matched", "matchedLobbyId" to lobbyId))
                transaction.update(newerTicketRef, mapOf("status" to "matched", "matchedLobbyId" to lobbyId))

                val newLobby = hashMapOf(
                    "lobbyId" to lobbyId,
                    "player1Id" to player1Id,
                    "player1Name" to player1Name,
                    "player1Level" to player1Level,
                    "player1Country" to player1Country,
                    "player1Score" to 0L,
                    "player2Id" to player2Id,
                    "player2Name" to player2Name,
                    "player2Level" to player2Level,
                    "player2Country" to player2Country,
                    "player2Score" to 0L,
                    "status" to "active",
                    "seed" to seed,
                    "currentQuestionIndex" to 0L,
                    "lastAnswererId" to "",
                    "gameStartTimestamp" to startTime,
                    "createdAt" to java.util.Date()
                )
                transaction.set(lobbyRef, newLobby)
                true
            } else {
                false
            }
        }.addOnSuccessListener { result ->
            continuation.resume(result)
        }.addOnFailureListener { exception ->
            Log.e("AndroidMultiplayer", "Matchmaking transaction failed: ${exception.message}", exception)
            continuation.resume(false)
        }
    }

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
    ) {
        val validPlayerId = if (playerId.isNotEmpty()) playerId else UUID.randomUUID().toString()
        val roomCode = generateRoomCode()
        onRoomCreated(roomCode)

        val roomRef = db.collection("vs_lobbies").document("room_$roomCode")
        val roomData = hashMapOf(
            "roomCode" to roomCode,
            "hostId" to validPlayerId,
            "hostName" to playerName,
            "hostLevel" to level,
            "hostCountry" to country,
            "status" to "waiting",
            "matchedLobbyId" to "",
            "seed" to 0L,
            "gameStartTimestamp" to 0L,
            "createdAt" to System.currentTimeMillis()
        )

        roomRef.set(roomData)
            .addOnFailureListener { e -> onError("Oda oluşturulamadı: ${e.message}") }

        customRoomListener = roomRef.addSnapshotListener { snap, err ->
            if (err != null) { onError("Oda hatası: ${err.message}"); return@addSnapshotListener }
            if (snap != null && snap.exists()) {
                val status = snap.getString("status") ?: "waiting"
                val lobbyId = snap.getString("matchedLobbyId") ?: ""
                if (status == "matched" && lobbyId.isNotEmpty()) {
                    val guestName = snap.getString("guestName") ?: "Konuk"
                    val guestLevel = snap.getLong("guestLevel")?.toInt() ?: 1
                    val guestCountry = snap.getString("guestCountry") ?: "US"
                    val seed = snap.getLong("seed") ?: 0L
                    val startTime = snap.getLong("gameStartTimestamp") ?: 0L
                    customRoomListener?.remove()
                    customRoomListener = null
                    onMatched(lobbyId, 1, guestName, guestLevel, guestCountry, seed, startTime)
                }
            }
        }
    }

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
    ) {
        val cleanCode = roomCode.trim()
        if (cleanCode.length != 4) { onError("Lütfen 4 haneli oda kodunu girin!"); return }

        val validPlayerId = if (playerId.isNotEmpty()) playerId else UUID.randomUUID().toString()
        val roomRef = db.collection("vs_lobbies").document("room_$cleanCode")

        roomRef.get()
            .addOnSuccessListener { snap ->
                if (snap == null || !snap.exists()) {
                    onError("Oda bulunamadı! Kod: $cleanCode")
                    return@addOnSuccessListener
                }
                val status = snap.getString("status") ?: ""
                if (status != "waiting") {
                    onError("Bu oda dolu veya kapanmış!")
                    return@addOnSuccessListener
                }
                val hostId = snap.getString("hostId") ?: ""
                if (hostId == validPlayerId) {
                    onError("Kendi oluşturduğunuz odaya katılamazsınız!")
                    return@addOnSuccessListener
                }
                val hostName = snap.getString("hostName") ?: "Ev Sahibi"
                val hostLevel = snap.getLong("hostLevel")?.toInt() ?: 1
                val hostCountry = snap.getString("hostCountry") ?: "US"

                val lobbyId = UUID.randomUUID().toString().take(8)
                val seed = Random.nextLong()
                val startTime = System.currentTimeMillis() + 3500L
                val lobbyRef = db.collection("vs_lobbies").document(lobbyId)

                db.runTransaction { transaction ->
                    val roomSnap = transaction.get(roomRef)
                    if (roomSnap.getString("status") != "waiting") throw Exception("Oda kapandı!")

                    transaction.update(roomRef, mapOf(
                        "status" to "matched",
                        "matchedLobbyId" to lobbyId,
                        "guestId" to validPlayerId,
                        "guestName" to playerName,
                        "guestLevel" to level,
                        "guestCountry" to country,
                        "seed" to seed,
                        "gameStartTimestamp" to startTime
                    ))
                    val newLobby = hashMapOf(
                        "lobbyId" to lobbyId,
                        "player1Id" to hostId,
                        "player1Name" to hostName,
                        "player1Level" to hostLevel,
                        "player1Country" to hostCountry,
                        "player1Score" to 0L,
                        "player2Id" to validPlayerId,
                        "player2Name" to playerName,
                        "player2Level" to level,
                        "player2Country" to country,
                        "player2Score" to 0L,
                        "status" to "active",
                        "seed" to seed,
                        "currentQuestionIndex" to 0L,
                        "lastAnswererId" to "",
                        "gameStartTimestamp" to startTime,
                        "createdAt" to java.util.Date()
                    )
                    transaction.set(lobbyRef, newLobby)
                }.addOnSuccessListener {
                    onMatched(lobbyId, 2, hostName, hostLevel, hostCountry, seed, startTime)
                }.addOnFailureListener { e ->
                    onError("Eşleştirme hatası: ${e.message}")
                }
            }
            .addOnFailureListener { e -> onError("Oda okunamadı: ${e.message}") }
    }

    override fun cancelCustomRoom(roomCode: String, playerId: String) {
        customRoomScope.cancel()
        customRoomListener?.remove()
        customRoomListener = null
        if (roomCode.isNotEmpty()) {
            db.collection("vs_lobbies").document("room_$roomCode").delete()
        }
    }

    private fun generateRoomCode(): String {
        val digits = "0123456789"
        return (1..4).map { digits.random() }.joinToString("")
    }
}

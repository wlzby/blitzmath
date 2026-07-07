package com.mawelly.blitzmath.core

data class LobbyState(
    val status: String = "waiting",
    val player1Score: Long = 0,
    val player2Score: Long = 0,
    val currentQuestionIndex: Long = 0,
    val lastAnswererId: String = "",
    val p1Emote: String? = null,
    val p2Emote: String? = null,
    val rematchP1: Boolean = false,
    val rematchP2: Boolean = false
)

interface IMultiplayerController {
    fun startMatchmaking(
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
    )

    fun cancelMatchmaking(playerId: String)

    fun createCustomRoom(
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
    )

    fun joinCustomRoom(
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
    )

    fun cancelCustomRoom(roomCode: String, playerId: String)

    fun observeLobby(
        lobbyId: String,
        onUpdate: (LobbyState) -> Unit,
        onError: (String) -> Unit
    )

    fun stopObservingLobby()

    fun updateScore(lobbyId: String, role: Int, score: Long)

    fun sendEmote(lobbyId: String, role: Int, emoteText: String)

    fun requestRematch(lobbyId: String, role: Int, request: Boolean)

    fun updateLobbyStatus(lobbyId: String, status: String)

    fun deleteLobby(lobbyId: String)

    fun acceptRematch(lobbyId: String, role: Int)

    fun submitCorrectAnswer(
        lobbyId: String,
        role: Int,
        playerId: String,
        questionIndex: Long,
        onResult: (Boolean) -> Unit
    )

    fun submitWrongAnswer(
        lobbyId: String,
        role: Int,
        questionIndex: Long,
        onResult: (Boolean) -> Unit
    )

    fun advanceQuestionIndex(
        lobbyId: String,
        currentIndex: Long,
        onResult: (Boolean) -> Unit
    )
}

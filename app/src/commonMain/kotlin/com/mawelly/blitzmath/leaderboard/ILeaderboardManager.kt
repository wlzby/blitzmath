package com.mawelly.blitzmath.leaderboard

interface ILeaderboardManager {
    suspend fun submitScore(
        playerId: String,
        playerName: String,
        score: Long,
        level: Int,
        country: String = "",
        mode: String = "classic"
    ): Result<Unit>

    suspend fun getPlayerRank(playerId: String, mode: String = "classic"): Result<Int>
}

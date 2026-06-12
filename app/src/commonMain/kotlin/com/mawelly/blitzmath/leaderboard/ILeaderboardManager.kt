package com.mawelly.blitzmath.leaderboard

data class LeaderboardEntry(
    var playerId: String = "",
    var playerName: String = "",
    var totalScore: Long = 0,
    var highestLevel: Int = 1,
    var country: String = ""
)

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

    suspend fun getGlobalLeaderboard(limit: Int = 100, mode: String = "classic"): Result<List<LeaderboardEntry>>
}

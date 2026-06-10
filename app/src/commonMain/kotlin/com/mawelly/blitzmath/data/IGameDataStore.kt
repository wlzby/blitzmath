package com.mawelly.blitzmath.data

import com.mawelly.blitzmath.game.GameMode
import com.mawelly.blitzmath.localization.AppLanguage
import com.mawelly.blitzmath.data.AppTheme
import kotlinx.coroutines.flow.Flow

interface IGameDataStore {
    val musicVolume: Flow<Float>
    val sfxVolume: Flow<Float>
    val classicLevel: Flow<Int>
    val mixedLevel: Flow<Int>
    val highScore: Flow<Int>
    val mixedHighScore: Flow<Int>
    val challengeHighScore: Flow<Int>
    val lastChallengePlayTime: Flow<Long>
    val challengePlaysToday: Flow<Int>
    val lastChallengeDate: Flow<String>
    val lastKnownClassicRank: Flow<Int>
    val lastKnownMixedRank: Flow<Int>
    val lastKnownChallengeRank: Flow<Int>
    val language: Flow<AppLanguage>
    val theme: Flow<AppTheme>
    val autoTheme: Flow<Boolean>
    val streakCount: Flow<Int>
    val lastClaimTime: Flow<Long>
    val starCount: Flow<Int>
    val unlockedCards: Flow<Set<String>>
    val equippedCards: Flow<Set<String>>
    val voiceEnabled: Flow<Boolean>
    val vibrationEnabled: Flow<Boolean>
    val vibrationStrength: Flow<Float>
    val livesCount: Flow<Int>
    val lastLifeLossTime: Flow<Long>
    val cardCharges: Flow<Map<String, Int>>
    val cardLastUseTime: Flow<Map<String, Long>>
    val gamesPlayed: Flow<Int>
    val isReviewed: Flow<Boolean>

    suspend fun saveMusicVolume(volume: Float)
    suspend fun saveSfxVolume(volume: Float)
    suspend fun saveClassicLevel(level: Int)
    suspend fun saveMixedLevel(level: Int)
    suspend fun saveHighScore(score: Int, mode: GameMode = GameMode.CLASSIC)
    suspend fun saveChallengeHighScore(score: Int)
    suspend fun saveChallengePlayInfo(playsToday: Int, dateStr: String)
    suspend fun saveLastChallengePlayTime(time: Long)
    suspend fun saveLanguage(language: AppLanguage)
    suspend fun saveTheme(theme: AppTheme)
    suspend fun saveAutoTheme(enabled: Boolean)
    suspend fun saveStreakCount(count: Int)
    suspend fun saveLastClaimTime(time: Long)
    suspend fun saveDailyReward(streak: Int, time: Long, starsToAdd: Int)
    suspend fun saveStarCount(count: Int)
    suspend fun spendStars(amount: Int): Boolean
    suspend fun addStars(amount: Int)
    suspend fun unlockCard(cardId: String)
    suspend fun toggleEquipCard(cardId: String)
    suspend fun saveVoiceEnabled(enabled: Boolean)
    suspend fun saveVibrationEnabled(enabled: Boolean)
    suspend fun saveVibrationStrength(strength: Float)
    suspend fun saveCardCharges(charges: Map<String, Int>)
    suspend fun saveCardLastUseTime(useTimes: Map<String, Long>)
    suspend fun incrementGamesPlayed()
    suspend fun saveLives(count: Int)
    suspend fun saveLastLifeLossTime(time: Long)
    suspend fun saveIsReviewed(reviewed: Boolean)
    suspend fun saveLastKnownRank(mode: String, rank: Int)
}

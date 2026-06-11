package com.mawelly.blitzmath.data

import com.mawelly.blitzmath.game.GameMode
import com.mawelly.blitzmath.localization.AppLanguage
import com.mawelly.blitzmath.data.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class IosGameDataStore : IGameDataStore {
    override val musicVolume = MutableStateFlow(0.05f)
    override val sfxVolume = MutableStateFlow(0.8f)
    override val classicLevel = MutableStateFlow(1)
    override val mixedLevel = MutableStateFlow(1)
    override val highScore = MutableStateFlow(0)
    override val mixedHighScore = MutableStateFlow(0)
    override val challengeHighScore = MutableStateFlow(0)
    override val lastChallengePlayTime = MutableStateFlow(0L)
    override val challengePlaysToday = MutableStateFlow(0)
    override val lastChallengeDate = MutableStateFlow("")
    override val lastKnownClassicRank = MutableStateFlow(0)
    override val lastKnownMixedRank = MutableStateFlow(0)
    override val lastKnownChallengeRank = MutableStateFlow(0)
    override val language = MutableStateFlow(AppLanguage.TURKISH)
    override val theme = MutableStateFlow(AppTheme.MIDNIGHT)
    override val autoTheme = MutableStateFlow(false)
    override val streakCount = MutableStateFlow(0)
    override val lastClaimTime = MutableStateFlow(0L)
    override val starCount = MutableStateFlow(100)
    override val unlockedCards = MutableStateFlow(emptySet<String>())
    override val equippedCards = MutableStateFlow(emptySet<String>())
    override val voiceEnabled = MutableStateFlow(true)
    override val vibrationEnabled = MutableStateFlow(true)
    override val vibrationStrength = MutableStateFlow(1.0f)
    override val livesCount = MutableStateFlow(5)
    override val lastLifeLossTime = MutableStateFlow(0L)
    override val cardCharges = MutableStateFlow(emptyMap<String, Int>())
    override val cardLastUseTime = MutableStateFlow(emptyMap<String, Long>())
    override val gamesPlayed = MutableStateFlow(0)
    override val isReviewed = MutableStateFlow(false)
    override val playerName = MutableStateFlow("iOS Player")
    override val playerId = MutableStateFlow("ios_player_id")
    override val playerXp = MutableStateFlow(0)

    override suspend fun saveMusicVolume(volume: Float) { musicVolume.value = volume }
    override suspend fun saveSfxVolume(volume: Float) { sfxVolume.value = volume }
    override suspend fun saveClassicLevel(level: Int) { classicLevel.value = level }
    override suspend fun saveMixedLevel(level: Int) { mixedLevel.value = level }
    
    override suspend fun saveHighScore(score: Int, mode: GameMode) {
        when (mode) {
            GameMode.CLASSIC -> if (score > highScore.value) highScore.value = score
            GameMode.MIXED -> if (score > mixedHighScore.value) mixedHighScore.value = score
            GameMode.CHALLENGE -> if (score > challengeHighScore.value) challengeHighScore.value = score
        }
    }
    
    override suspend fun saveChallengeHighScore(score: Int) { challengeHighScore.value = score }
    
    override suspend fun saveChallengePlayInfo(playsToday: Int, dateStr: String) {
        challengePlaysToday.value = playsToday
        lastChallengeDate.value = dateStr
    }
    
    override suspend fun saveLastChallengePlayTime(time: Long) { lastChallengePlayTime.value = time }
    override suspend fun saveLanguage(language: AppLanguage) { this.language.value = language }
    override suspend fun saveTheme(theme: AppTheme) { this.theme.value = theme }
    override suspend fun saveAutoTheme(enabled: Boolean) { autoTheme.value = enabled }
    override suspend fun saveStreakCount(count: Int) { streakCount.value = count }
    override suspend fun saveLastClaimTime(time: Long) { lastClaimTime.value = time }
    
    override suspend fun saveDailyReward(streak: Int, time: Long, starsToAdd: Int) {
        streakCount.value = streak
        lastClaimTime.value = time
        starCount.value += starsToAdd
    }
    
    override suspend fun saveStarCount(count: Int) { starCount.value = count }
    
    override suspend fun spendStars(amount: Int): Boolean {
        if (starCount.value >= amount) {
            starCount.value -= amount
            return true
        }
        return false
    }
    
    override suspend fun addStars(amount: Int) { starCount.value += amount }
    
    override suspend fun unlockCard(cardId: String) {
        unlockedCards.value = unlockedCards.value + cardId
    }
    
    override suspend fun toggleEquipCard(cardId: String) {
        val current = equippedCards.value.toMutableSet()
        if (current.contains(cardId)) {
            current.remove(cardId)
        } else {
            if (current.size < 2) {
                current.add(cardId)
            }
        }
        equippedCards.value = current
    }
    
    override suspend fun saveVoiceEnabled(enabled: Boolean) { voiceEnabled.value = enabled }
    override suspend fun saveVibrationEnabled(enabled: Boolean) { vibrationEnabled.value = enabled }
    override suspend fun saveVibrationStrength(strength: Float) { vibrationStrength.value = strength }
    override suspend fun saveCardCharges(charges: Map<String, Int>) { cardCharges.value = charges }
    override suspend fun saveCardLastUseTime(useTimes: Map<String, Long>) { cardLastUseTime.value = useTimes }
    override suspend fun incrementGamesPlayed() { gamesPlayed.value += 1 }
    override suspend fun saveLives(count: Int) { livesCount.value = count }
    override suspend fun saveLastLifeLossTime(time: Long) { lastLifeLossTime.value = time }
    override suspend fun saveIsReviewed(reviewed: Boolean) { isReviewed.value = reviewed }
    
    override suspend fun saveLastKnownRank(mode: String, rank: Int) {
        when (mode.lowercase()) {
            "mixed" -> lastKnownMixedRank.value = rank
            "challenge" -> lastKnownChallengeRank.value = rank
            else -> lastKnownClassicRank.value = rank
        }
    }
    
    override suspend fun savePlayerName(name: String) { playerName.value = name }
    override suspend fun saveLoginType(type: String) {}
    override suspend fun setFirstLaunchCompleted() {}
    override suspend fun savePlayerId(id: String) { playerId.value = id }
    override suspend fun savePlayerXp(xp: Int) { playerXp.value = xp }
}

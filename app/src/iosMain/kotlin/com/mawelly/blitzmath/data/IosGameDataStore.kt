package com.mawelly.blitzmath.data

import com.mawelly.blitzmath.game.GameMode
import com.mawelly.blitzmath.localization.AppLanguage
import com.mawelly.blitzmath.data.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUUID

class IosGameDataStore : IGameDataStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    override val musicVolume = MutableStateFlow(
        if (defaults.objectForKey("music_volume") != null) defaults.floatForKey("music_volume") else 0.05f
    )
    override val sfxVolume = MutableStateFlow(
        if (defaults.objectForKey("sfx_volume") != null) defaults.floatForKey("sfx_volume") else 0.8f
    )
    override val classicLevel = MutableStateFlow(
        if (defaults.objectForKey("classic_level") != null) defaults.integerForKey("classic_level").toInt() else 1
    )
    override val mixedLevel = MutableStateFlow(
        if (defaults.objectForKey("mixed_level") != null) defaults.integerForKey("mixed_level").toInt() else 1
    )
    override val highScore = MutableStateFlow(
        if (defaults.objectForKey("high_score") != null) defaults.integerForKey("high_score").toInt() else 0
    )
    override val mixedHighScore = MutableStateFlow(
        if (defaults.objectForKey("mixed_high_score") != null) defaults.integerForKey("mixed_high_score").toInt() else 0
    )
    override val challengeHighScore = MutableStateFlow(
        if (defaults.objectForKey("challenge_high_score") != null) defaults.integerForKey("challenge_high_score").toInt() else 0
    )
    override val lastChallengePlayTime = MutableStateFlow(
        if (defaults.objectForKey("last_challenge_play_time") != null) defaults.doubleForKey("last_challenge_play_time").toLong() else 0L
    )
    override val challengePlaysToday = MutableStateFlow(
        if (defaults.objectForKey("challenge_plays_today") != null) defaults.integerForKey("challenge_plays_today").toInt() else 0
    )
    override val lastChallengeDate = MutableStateFlow(
        defaults.stringForKey("last_challenge_date") ?: ""
    )
    override val lastKnownClassicRank = MutableStateFlow(
        if (defaults.objectForKey("last_known_classic_rank") != null) defaults.integerForKey("last_known_classic_rank").toInt() else 0
    )
    override val lastKnownMixedRank = MutableStateFlow(
        if (defaults.objectForKey("last_known_mixed_rank") != null) defaults.integerForKey("last_known_mixed_rank").toInt() else 0
    )
    override val lastKnownChallengeRank = MutableStateFlow(
        if (defaults.objectForKey("last_known_challenge_rank") != null) defaults.integerForKey("last_known_challenge_rank").toInt() else 0
    )
    override val lastSyncedClassicScore = MutableStateFlow(
        if (defaults.objectForKey("last_synced_classic_score") != null) defaults.integerForKey("last_synced_classic_score").toInt() else 0
    )
    override val lastSyncedMixedScore = MutableStateFlow(
        if (defaults.objectForKey("last_synced_mixed_score") != null) defaults.integerForKey("last_synced_mixed_score").toInt() else 0
    )
    override val lastSyncedChallengeScore = MutableStateFlow(
        if (defaults.objectForKey("last_synced_challenge_score") != null) defaults.integerForKey("last_synced_challenge_score").toInt() else 0
    )
    override val language = MutableStateFlow(
        try {
            val langStr = defaults.stringForKey("language")
            if (langStr != null) AppLanguage.valueOf(langStr) else AppLanguage.TURKISH
        } catch (e: Exception) {
            AppLanguage.TURKISH
        }
    )
    override val theme = MutableStateFlow(
        try {
            val themeStr = defaults.stringForKey("theme")
            if (themeStr != null) AppTheme.valueOf(themeStr) else AppTheme.MIDNIGHT
        } catch (e: Exception) {
            AppTheme.MIDNIGHT
        }
    )
    override val autoTheme = MutableStateFlow(
        if (defaults.objectForKey("auto_theme") != null) defaults.boolForKey("auto_theme") else false
    )
    override val streakCount = MutableStateFlow(
        if (defaults.objectForKey("streak_count") != null) defaults.integerForKey("streak_count").toInt() else 0
    )
    override val lastClaimTime = MutableStateFlow(
        if (defaults.objectForKey("last_claim_time") != null) defaults.doubleForKey("last_claim_time").toLong() else 0L
    )
    override val starCount = MutableStateFlow(
        if (defaults.objectForKey("star_count") != null) defaults.integerForKey("star_count").toInt() else 100
    )
    override val unlockedCards = MutableStateFlow(
        defaults.stringForKey("unlocked_cards")?.split(",")?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()
    )
    override val equippedCards = MutableStateFlow(
        (defaults.stringForKey("equipped_cards")?.split(",")?.filter { it.isNotEmpty() }?.toSet() ?: emptySet())
            .intersect(defaults.stringForKey("unlocked_cards")?.split(",")?.filter { it.isNotEmpty() }?.toSet() ?: emptySet())
    )
    override val voiceEnabled = MutableStateFlow(
        if (defaults.objectForKey("voice_enabled") != null) defaults.boolForKey("voice_enabled") else true
    )
    override val vibrationEnabled = MutableStateFlow(
        if (defaults.objectForKey("vibration_enabled") != null) defaults.boolForKey("vibration_enabled") else true
    )
    override val vibrationStrength = MutableStateFlow(
        if (defaults.objectForKey("vibration_strength") != null) defaults.floatForKey("vibration_strength") else 1.0f
    )
    override val livesCount = MutableStateFlow(
        if (defaults.objectForKey("lives_count") != null) defaults.integerForKey("lives_count").toInt() else 5
    )
    override val lastLifeLossTime = MutableStateFlow(
        if (defaults.objectForKey("last_life_loss_time") != null) defaults.doubleForKey("last_life_loss_time").toLong() else 0L
    )
    
    override val cardCharges = MutableStateFlow(
        parseCharges(defaults.stringForKey("card_charges") ?: "")
    )
    override val cardLastUseTime = MutableStateFlow(
        parseLastUseTime(defaults.stringForKey("card_last_use_time") ?: "")
    )
    override val gamesPlayed = MutableStateFlow(
        if (defaults.objectForKey("games_played") != null) defaults.integerForKey("games_played").toInt() else 0
    )
    override val isReviewed = MutableStateFlow(
        if (defaults.objectForKey("is_reviewed") != null) defaults.boolForKey("is_reviewed") else false
    )
    override val playerName = MutableStateFlow(
        defaults.stringForKey("player_name") ?: ""
    )
    override val playerId = MutableStateFlow(
        defaults.stringForKey("player_id") ?: ""
    )
    override val playerXp = MutableStateFlow(
        if (defaults.objectForKey("player_xp") != null) defaults.integerForKey("player_xp").toInt() else 0
    )

    private fun parseCharges(raw: String): Map<String, Int> {
        if (raw.isEmpty()) return emptyMap()
        return raw.split(",").mapNotNull {
            val parts = it.split(":")
            if (parts.size == 2) parts[0] to (parts[1].toIntOrNull() ?: 0) else null
        }.toMap()
    }

    private fun parseLastUseTime(raw: String): Map<String, Long> {
        if (raw.isEmpty()) return emptyMap()
        return raw.split(",").mapNotNull {
            val parts = it.split(":")
            if (parts.size == 2) parts[0] to (parts[1].toLongOrNull() ?: 0L) else null
        }.toMap()
    }

    override suspend fun saveMusicVolume(volume: Float) {
        musicVolume.value = volume
        defaults.setFloat(volume, forKey = "music_volume")
    }

    override suspend fun saveSfxVolume(volume: Float) {
        sfxVolume.value = volume
        defaults.setFloat(volume, forKey = "sfx_volume")
    }

    override suspend fun saveClassicLevel(level: Int) {
        classicLevel.value = level
        defaults.setInteger(level.toLong(), forKey = "classic_level")
    }

    override suspend fun saveMixedLevel(level: Int) {
        mixedLevel.value = level
        defaults.setInteger(level.toLong(), forKey = "mixed_level")
    }

    override suspend fun saveHighScore(score: Int, mode: GameMode) {
        when (mode) {
            GameMode.CLASSIC -> {
                if (score > highScore.value) {
                    highScore.value = score
                    defaults.setInteger(score.toLong(), forKey = "high_score")
                }
            }
            GameMode.MIXED -> {
                if (score > mixedHighScore.value) {
                    mixedHighScore.value = score
                    defaults.setInteger(score.toLong(), forKey = "mixed_high_score")
                }
            }
            GameMode.CHALLENGE -> {
                if (score > challengeHighScore.value) {
                    challengeHighScore.value = score
                    defaults.setInteger(score.toLong(), forKey = "challenge_high_score")
                }
            }
            else -> {}
        }
    }

    override suspend fun saveChallengeHighScore(score: Int) {
        challengeHighScore.value = score
        defaults.setInteger(score.toLong(), forKey = "challenge_high_score")
    }

    override suspend fun saveChallengePlayInfo(playsToday: Int, dateStr: String) {
        challengePlaysToday.value = playsToday
        lastChallengeDate.value = dateStr
        defaults.setInteger(playsToday.toLong(), forKey = "challenge_plays_today")
        defaults.setObject(dateStr, forKey = "last_challenge_date")
    }

    override suspend fun saveLastChallengePlayTime(time: Long) {
        lastChallengePlayTime.value = time
        defaults.setDouble(time.toDouble(), forKey = "last_challenge_play_time")
    }

    override suspend fun saveLanguage(language: AppLanguage) {
        this.language.value = language
        defaults.setObject(language.name, forKey = "language")
    }

    override suspend fun saveTheme(theme: AppTheme) {
        this.theme.value = theme
        defaults.setObject(theme.name, forKey = "theme")
    }

    override suspend fun saveAutoTheme(enabled: Boolean) {
        autoTheme.value = enabled
        defaults.setBool(enabled, forKey = "auto_theme")
    }

    override suspend fun saveStreakCount(count: Int) {
        streakCount.value = count
        defaults.setInteger(count.toLong(), forKey = "streak_count")
    }

    override suspend fun saveLastClaimTime(time: Long) {
        lastClaimTime.value = time
        defaults.setDouble(time.toDouble(), forKey = "last_claim_time")
    }

    override suspend fun saveDailyReward(streak: Int, time: Long, starsToAdd: Int) {
        streakCount.value = streak
        lastClaimTime.value = time
        starCount.value += starsToAdd
        
        defaults.setInteger(streak.toLong(), forKey = "streak_count")
        defaults.setDouble(time.toDouble(), forKey = "last_claim_time")
        defaults.setInteger(starCount.value.toLong(), forKey = "star_count")
    }

    override suspend fun saveStarCount(count: Int) {
        starCount.value = count
        defaults.setInteger(count.toLong(), forKey = "star_count")
    }

    override suspend fun spendStars(amount: Int): Boolean {
        if (starCount.value >= amount) {
            starCount.value -= amount
            defaults.setInteger(starCount.value.toLong(), forKey = "star_count")
            return true
        }
        return false
    }

    override suspend fun addStars(amount: Int) {
        starCount.value += amount
        defaults.setInteger(starCount.value.toLong(), forKey = "star_count")
    }

    override suspend fun unlockCard(cardId: String) {
        val newSet = unlockedCards.value + cardId
        unlockedCards.value = newSet
        defaults.setObject(newSet.joinToString(","), forKey = "unlocked_cards")
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
        defaults.setObject(current.joinToString(","), forKey = "equipped_cards")
    }

    override suspend fun saveVoiceEnabled(enabled: Boolean) {
        voiceEnabled.value = enabled
        defaults.setBool(enabled, forKey = "voice_enabled")
    }

    override suspend fun saveVibrationEnabled(enabled: Boolean) {
        vibrationEnabled.value = enabled
        defaults.setBool(enabled, forKey = "vibration_enabled")
    }

    override suspend fun saveVibrationStrength(strength: Float) {
        vibrationStrength.value = strength
        defaults.setFloat(strength, forKey = "vibration_strength")
    }

    override suspend fun saveCardCharges(charges: Map<String, Int>) {
        cardCharges.value = charges
        val raw = charges.entries.joinToString(",") { "${it.key}:${it.value}" }
        defaults.setObject(raw, forKey = "card_charges")
    }

    override suspend fun saveCardLastUseTime(useTimes: Map<String, Long>) {
        cardLastUseTime.value = useTimes
        val raw = useTimes.entries.joinToString(",") { "${it.key}:${it.value}" }
        defaults.setObject(raw, forKey = "card_last_use_time")
    }

    override suspend fun incrementGamesPlayed() {
        val newCount = gamesPlayed.value + 1
        gamesPlayed.value = newCount
        defaults.setInteger(newCount.toLong(), forKey = "games_played")
    }

    override suspend fun saveLives(count: Int) {
        livesCount.value = count
        defaults.setInteger(count.toLong(), forKey = "lives_count")
    }

    override suspend fun saveLastLifeLossTime(time: Long) {
        lastLifeLossTime.value = time
        defaults.setDouble(time.toDouble(), forKey = "last_life_loss_time")
    }

    override suspend fun saveIsReviewed(reviewed: Boolean) {
        isReviewed.value = reviewed
        defaults.setBool(reviewed, forKey = "is_reviewed")
    }

    override suspend fun saveLastKnownRank(mode: String, rank: Int) {
        when (mode.lowercase()) {
            "mixed" -> {
                lastKnownMixedRank.value = rank
                defaults.setInteger(rank.toLong(), forKey = "last_known_mixed_rank")
            }
            "challenge" -> {
                lastKnownChallengeRank.value = rank
                defaults.setInteger(rank.toLong(), forKey = "last_known_challenge_rank")
            }
            else -> {
                lastKnownClassicRank.value = rank
                defaults.setInteger(rank.toLong(), forKey = "last_known_classic_rank")
            }
        }
    }

    override suspend fun savePlayerName(name: String) {
        playerName.value = name
        defaults.setObject(name, forKey = "player_name")
        if (playerId.value.isEmpty()) {
            val uniqueId = NSUUID.UUID().UUIDString()
            savePlayerId(uniqueId)
        }
    }

    override suspend fun saveLoginType(type: String) {
        defaults.setObject(type, forKey = "login_type")
    }

    override suspend fun setFirstLaunchCompleted() {
        defaults.setBool(true, forKey = "first_launch")
    }

    override suspend fun savePlayerId(id: String) {
        playerId.value = id
        defaults.setObject(id, forKey = "player_id")
    }

    override suspend fun savePlayerXp(xp: Int) {
        playerXp.value = xp
        defaults.setInteger(xp.toLong(), forKey = "player_xp")
    }

    override suspend fun saveLastSyncedClassicScore(score: Int) {
        lastSyncedClassicScore.value = score
        defaults.setInteger(score.toLong(), forKey = "last_synced_classic_score")
    }

    override suspend fun saveLastSyncedMixedScore(score: Int) {
        lastSyncedMixedScore.value = score
        defaults.setInteger(score.toLong(), forKey = "last_synced_mixed_score")
    }

    override suspend fun saveLastSyncedChallengeScore(score: Int) {
        lastSyncedChallengeScore.value = score
        defaults.setInteger(score.toLong(), forKey = "last_synced_challenge_score")
    }
}

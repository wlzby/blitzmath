package com.mawelly.blitzmath.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.mawelly.blitzmath.localization.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map



val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_settings")

class GameDataStore(private val context: Context) {

    companion object {
        val MUSIC_VOLUME = floatPreferencesKey("music_volume")
        val SFX_VOLUME = floatPreferencesKey("sfx_volume")
        val CLASSIC_LEVEL = intPreferencesKey("classic_level")
        val MIXED_LEVEL = intPreferencesKey("mixed_level")
        val HIGH_SCORE = intPreferencesKey("high_score")
        val LANGUAGE = stringPreferencesKey("language")
        val THEME = stringPreferencesKey("theme")
        val AUTO_THEME = booleanPreferencesKey("auto_theme")
        val STREAK_COUNT = intPreferencesKey("streak_count")
        val LAST_CLAIM_TIME = longPreferencesKey("last_claim_time")
        val STAR_COUNT = intPreferencesKey("star_count")
        val UNLOCKED_CARDS = stringPreferencesKey("unlocked_cards")
        val EQUIPPED_CARDS = stringPreferencesKey("equipped_cards")
        val VOICE_ENABLED = booleanPreferencesKey("voice_enabled")
        val CARD_CHARGES = stringPreferencesKey("card_charges") // "cardId1:3,cardId2:2"
        val CARD_LAST_USE_TIME = stringPreferencesKey("card_last_use_time") // "cardId1:timestamp,cardId2:timestamp"
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val VIBRATION_STRENGTH = floatPreferencesKey("vibration_strength")
        val GAMES_PLAYED = intPreferencesKey("games_played")
        val LIVES_COUNT = intPreferencesKey("lives_count")
        val LAST_LIFE_LOSS_TIME = longPreferencesKey("last_life_loss_time")
        val IS_REVIEWED = booleanPreferencesKey("is_reviewed")
        val MIXED_HIGH_SCORE = intPreferencesKey("mixed_high_score")
        val CHALLENGE_HIGH_SCORE = intPreferencesKey("challenge_high_score")
        val LAST_CHALLENGE_PLAY_TIME = longPreferencesKey("last_challenge_play_time")
        val CHALLENGE_PLAYS_TODAY = intPreferencesKey("challenge_plays_today")
        val LAST_CHALLENGE_DATE = stringPreferencesKey("last_challenge_date")
        val LAST_KNOWN_CLASSIC_RANK = intPreferencesKey("last_known_classic_rank")
        val LAST_KNOWN_MIXED_RANK = intPreferencesKey("last_known_mixed_rank")
        val LAST_KNOWN_CHALLENGE_RANK = intPreferencesKey("last_known_challenge_rank")
    }

    // Ses Ayarları
    val musicVolume: Flow<Float> = context.dataStore.data.map { it[MUSIC_VOLUME] ?: 0.05f }
    val sfxVolume: Flow<Float> = context.dataStore.data.map { it[SFX_VOLUME] ?: 0.8f }

    // Level Kayıtları
    val classicLevel: Flow<Int> = context.dataStore.data.map { it[CLASSIC_LEVEL] ?: 1 }
    val mixedLevel: Flow<Int> = context.dataStore.data.map { it[MIXED_LEVEL] ?: 1 }
    val highScore: Flow<Int> = context.dataStore.data.map { it[HIGH_SCORE] ?: 0 }
    val mixedHighScore: Flow<Int> = context.dataStore.data.map { it[MIXED_HIGH_SCORE] ?: 0 }
    val challengeHighScore: Flow<Int> = context.dataStore.data.map { it[CHALLENGE_HIGH_SCORE] ?: 0 }
    val lastChallengePlayTime: Flow<Long> = context.dataStore.data.map { it[LAST_CHALLENGE_PLAY_TIME] ?: 0L }
    val challengePlaysToday: Flow<Int> = context.dataStore.data.map { it[CHALLENGE_PLAYS_TODAY] ?: 0 }
    val lastChallengeDate: Flow<String> = context.dataStore.data.map { it[LAST_CHALLENGE_DATE] ?: "" }
    val lastKnownClassicRank: Flow<Int> = context.dataStore.data.map { it[LAST_KNOWN_CLASSIC_RANK] ?: 0 }
    val lastKnownMixedRank: Flow<Int> = context.dataStore.data.map { it[LAST_KNOWN_MIXED_RANK] ?: 0 }
    val lastKnownChallengeRank: Flow<Int> = context.dataStore.data.map { it[LAST_KNOWN_CHALLENGE_RANK] ?: 0 }

    // Dil
    val language: Flow<AppLanguage> = context.dataStore.data.map {
        when (it[LANGUAGE]) {
            "en" -> AppLanguage.ENGLISH
            "es" -> AppLanguage.SPANISH
            "de" -> AppLanguage.GERMAN
            "fr" -> AppLanguage.FRENCH
            "it" -> AppLanguage.ITALIAN
            "pt" -> AppLanguage.PORTUGUESE
            "hi" -> AppLanguage.HINDI
            "zh" -> AppLanguage.CHINESE
            "ru" -> AppLanguage.RUSSIAN
            else -> AppLanguage.TURKISH
        }
    }

    // Tema
    val theme: Flow<AppTheme> = context.dataStore.data.map {
        when (it[THEME]) {
            "cyberpunk" -> AppTheme.CYBERPUNK
            "forest" -> AppTheme.FOREST
            "aqua" -> AppTheme.AQUA
            "deep_space" -> AppTheme.DEEP_SPACE
            "sunset" -> AppTheme.SUNSET
            "lavender" -> AppTheme.LAVENDER
            "fire" -> AppTheme.FIRE
            "glacier" -> AppTheme.GLACIER
            "golden" -> AppTheme.GOLDEN
            else -> AppTheme.MIDNIGHT
        }
    }

    val autoTheme: Flow<Boolean> = context.dataStore.data.map { it[AUTO_THEME] ?: false }
    val streakCount: Flow<Int> = context.dataStore.data.map { it[STREAK_COUNT] ?: 0 }
    val lastClaimTime: Flow<Long> = context.dataStore.data.map { it[LAST_CLAIM_TIME] ?: 0L }
    val starCount: Flow<Int> = context.dataStore.data.map { it[STAR_COUNT] ?: 0 }
    val unlockedCards: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[UNLOCKED_CARDS]?.split(",")?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()
    }
    val equippedCards: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        val unlocked = preferences[UNLOCKED_CARDS]?.split(",")?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()
        val equipped = preferences[EQUIPPED_CARDS]?.split(",")?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()
        equipped.intersect(unlocked)
    }
    val voiceEnabled: Flow<Boolean> = context.dataStore.data.map { it[VOICE_ENABLED] ?: true }
    val vibrationEnabled: Flow<Boolean> = context.dataStore.data.map { it[VIBRATION_ENABLED] ?: true }
    val vibrationStrength: Flow<Float> = context.dataStore.data.map { it[VIBRATION_STRENGTH] ?: 1.0f }
    val livesCount: Flow<Int> = context.dataStore.data.map { it[LIVES_COUNT] ?: 5 }
    val lastLifeLossTime: Flow<Long> = context.dataStore.data.map { it[LAST_LIFE_LOSS_TIME] ?: 0L }

    /** Returns a map of cardId -> remaining charges from DataStore */
    val cardCharges: Flow<Map<String, Int>> = context.dataStore.data.map { preferences ->
        val raw = preferences[CARD_CHARGES] ?: ""
        if (raw.isEmpty()) emptyMap()
        else raw.split(",").mapNotNull {
            val parts = it.split(":")
            if (parts.size == 2) parts[0] to (parts[1].toIntOrNull() ?: 0) else null
        }.toMap()
    }

    /** Returns a map of cardId -> last use timestamp from DataStore */
    val cardLastUseTime: Flow<Map<String, Long>> = context.dataStore.data.map { preferences ->
        val raw = preferences[CARD_LAST_USE_TIME] ?: ""
        if (raw.isEmpty()) emptyMap()
        else raw.split(",").mapNotNull {
            val parts = it.split(":")
            if (parts.size == 2) parts[0] to (parts[1].toLongOrNull() ?: 0L) else null
        }.toMap()
    }

    suspend fun saveMusicVolume(volume: Float) {
        context.dataStore.edit { it[MUSIC_VOLUME] = volume }
    }

    suspend fun saveSfxVolume(volume: Float) {
        context.dataStore.edit { it[SFX_VOLUME] = volume }
    }

    suspend fun saveClassicLevel(level: Int) {
        context.dataStore.edit { it[CLASSIC_LEVEL] = level }
    }

    suspend fun saveMixedLevel(level: Int) {
        context.dataStore.edit { it[MIXED_LEVEL] = level }
    }

    suspend fun saveHighScore(score: Int, mode: com.mawelly.blitzmath.game.GameMode = com.mawelly.blitzmath.game.GameMode.CLASSIC) {
        context.dataStore.edit {
            val key = when (mode) {
                com.mawelly.blitzmath.game.GameMode.CLASSIC -> HIGH_SCORE
                com.mawelly.blitzmath.game.GameMode.MIXED -> MIXED_HIGH_SCORE
                com.mawelly.blitzmath.game.GameMode.CHALLENGE -> CHALLENGE_HIGH_SCORE
                else -> HIGH_SCORE
            }
            val current = it[key] ?: 0
            if (score > current) it[key] = score
        }
    }

    suspend fun saveChallengeHighScore(score: Int) {
        context.dataStore.edit { it[CHALLENGE_HIGH_SCORE] = score }
    }

    suspend fun saveChallengePlayInfo(playsToday: Int, dateStr: String) {
        context.dataStore.edit {
            it[CHALLENGE_PLAYS_TODAY] = playsToday
            it[LAST_CHALLENGE_DATE] = dateStr
            it[LAST_CHALLENGE_PLAY_TIME] = System.currentTimeMillis()
        }
    }

    suspend fun saveLastChallengePlayTime(time: Long) {
        context.dataStore.edit { it[LAST_CHALLENGE_PLAY_TIME] = time }
    }


    suspend fun saveLanguage(language: AppLanguage) {
        context.dataStore.edit {
            it[LANGUAGE] = when (language) {
                AppLanguage.TURKISH -> "tr"
                AppLanguage.ENGLISH -> "en"
                AppLanguage.SPANISH -> "es"
                AppLanguage.GERMAN -> "de"
                AppLanguage.FRENCH -> "fr"
                AppLanguage.ITALIAN -> "it"
                AppLanguage.PORTUGUESE -> "pt"
                AppLanguage.HINDI -> "hi"
                AppLanguage.CHINESE -> "zh"
                AppLanguage.RUSSIAN -> "ru"
            }
        }
    }

    suspend fun saveTheme(theme: AppTheme) {
        context.dataStore.edit {
            it[THEME] = when (theme) {
                AppTheme.MIDNIGHT -> "midnight"
                AppTheme.DEEP_SPACE -> "deep_space"
                AppTheme.CYBERPUNK -> "cyberpunk"
                AppTheme.FOREST -> "forest"
                AppTheme.AQUA -> "aqua"
                AppTheme.SUNSET -> "sunset"
                AppTheme.LAVENDER -> "lavender"
                AppTheme.FIRE -> "fire"
                AppTheme.GLACIER -> "glacier"
                AppTheme.GOLDEN -> "golden"
            }
        }
    }

    suspend fun saveAutoTheme(enabled: Boolean) {
        context.dataStore.edit {
            it[AUTO_THEME] = enabled
        }
    }

    suspend fun saveStreakCount(count: Int) {
        context.dataStore.edit { it[STREAK_COUNT] = count }
    }

    suspend fun saveLastClaimTime(time: Long) {
        context.dataStore.edit { it[LAST_CLAIM_TIME] = time }
    }

    suspend fun saveDailyReward(streak: Int, time: Long, starsToAdd: Int) {
        context.dataStore.edit {
            it[STREAK_COUNT] = streak
            it[LAST_CLAIM_TIME] = time
            val currentStars = it[STAR_COUNT] ?: 0
            it[STAR_COUNT] = currentStars + starsToAdd
        }
    }

    suspend fun saveStarCount(count: Int) {
        context.dataStore.edit { it[STAR_COUNT] = count }
    }
    
    suspend fun spendStars(amount: Int): Boolean {
        var success = false
        context.dataStore.edit { preferences ->
            val currentStars = preferences[STAR_COUNT] ?: 0
            if (currentStars >= amount) {
                preferences[STAR_COUNT] = currentStars - amount
                success = true
            }
        }
        return success
    }

    suspend fun addStars(amount: Int) {
        context.dataStore.edit { preferences ->
            val current = preferences[STAR_COUNT] ?: 0
            preferences[STAR_COUNT] = current + amount
        }
    }

    suspend fun unlockCard(cardId: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[UNLOCKED_CARDS] ?: ""
            val list = current.split(",").filter { it.isNotEmpty() }.toMutableSet()
            if (list.add(cardId)) {
                preferences[UNLOCKED_CARDS] = list.joinToString(",")
            }
        }
    }
    
    suspend fun toggleEquipCard(cardId: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[EQUIPPED_CARDS] ?: ""
            val list = current.split(",").filter { it.isNotEmpty() }.toMutableSet()
            
            if (list.contains(cardId)) {
                list.remove(cardId)
            } else {
                if (list.size < 2) {
                    list.add(cardId)
                }
            }
            preferences[EQUIPPED_CARDS] = list.joinToString(",")
        }
    }

    suspend fun saveVoiceEnabled(enabled: Boolean) {
        context.dataStore.edit { it[VOICE_ENABLED] = enabled }
    }

    suspend fun saveVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[VIBRATION_ENABLED] = enabled }
    }

    suspend fun saveVibrationStrength(strength: Float) {
        context.dataStore.edit { it[VIBRATION_STRENGTH] = strength }
    }

    /** Saves the entire card charges map. Format: "cardId1:3,cardId2:1" */
    suspend fun saveCardCharges(charges: Map<String, Int>) {
        context.dataStore.edit { preferences ->
            preferences[CARD_CHARGES] = charges.entries.joinToString(",") { "${it.key}:${it.value}" }
        }
    }

    /** Saves the card last use time map. Format: "cardId1:timestamp,cardId2:timestamp" */
    suspend fun saveCardLastUseTime(useTimes: Map<String, Long>) {
        context.dataStore.edit { preferences ->
            preferences[CARD_LAST_USE_TIME] = useTimes.entries.joinToString(",") { "${it.key}:${it.value}" }
        }
    }

    val gamesPlayed: Flow<Int> = context.dataStore.data.map { it[GAMES_PLAYED] ?: 0 }

    suspend fun incrementGamesPlayed() {
        context.dataStore.edit { preferences ->
            val current = preferences[GAMES_PLAYED] ?: 0
            preferences[GAMES_PLAYED] = current + 1
        }
    }

    suspend fun saveLives(count: Int) {
        context.dataStore.edit { it[LIVES_COUNT] = count }
    }

    suspend fun saveLastLifeLossTime(time: Long) {
        context.dataStore.edit { it[LAST_LIFE_LOSS_TIME] = time }
    }

    val isReviewed: Flow<Boolean> = context.dataStore.data.map { it[IS_REVIEWED] ?: false }

    suspend fun saveIsReviewed(reviewed: Boolean) {
        context.dataStore.edit { it[IS_REVIEWED] = reviewed }
    }

    suspend fun saveLastKnownRank(mode: String, rank: Int) {
        context.dataStore.edit {
            val key = when (mode.lowercase()) {
                "mixed" -> LAST_KNOWN_MIXED_RANK
                "challenge" -> LAST_KNOWN_CHALLENGE_RANK
                else -> LAST_KNOWN_CLASSIC_RANK
            }
            it[key] = rank
        }
    }
}
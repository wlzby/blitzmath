package com.mawelly.blitzmath

import android.content.Context
import android.content.SharedPreferences
import com.mawelly.blitzmath.localization.Strings
import com.mawelly.blitzmath.localization.AppLanguage
import java.util.UUID

class LanguageManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "blitzmath_prefs"
        private const val KEY_LANGUAGE = "selected_language"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_PLAYER_NAME = "player_name"
        private const val KEY_PLAYER_ID = "player_id"
        private const val KEY_LOGIN_TYPE = "login_type"
        private const val KEY_PLAYER_XP = "player_xp"
    }

    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    fun setFirstLaunchCompleted() {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }

    fun saveLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.name).apply()
        Strings.setLanguage(language)
    }

    fun getSavedLanguage(): AppLanguage {
        val langName = prefs.getString(KEY_LANGUAGE, AppLanguage.TURKISH.name)
        return try {
            AppLanguage.valueOf(langName ?: AppLanguage.TURKISH.name)
        } catch (e: Exception) {
            AppLanguage.TURKISH
        }
    }

    fun loadSavedLanguage() {
        val lang = getSavedLanguage()
        Strings.setLanguage(lang)
    }

    // Kullanıcı adı işlemleri
    fun savePlayerName(name: String) {
        prefs.edit().putString(KEY_PLAYER_NAME, name).apply()
        // İlk kez kaydediyorsa unique ID oluştur
        if (getPlayerId().isEmpty()) {
            val uniqueId = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_PLAYER_ID, uniqueId).apply()
        }
    }

    fun getPlayerName(): String {
        return prefs.getString(KEY_PLAYER_NAME, "") ?: ""
    }

    fun getPlayerId(): String {
        return prefs.getString(KEY_PLAYER_ID, "") ?: ""
    }

    fun hasPlayerName(): Boolean {
        return getPlayerName().isNotEmpty()
    }

    fun saveLoginType(type: String) {
        prefs.edit().putString(KEY_LOGIN_TYPE, type).apply()
    }

    fun getLoginType(): String {
        return prefs.getString(KEY_LOGIN_TYPE, "guest") ?: "guest"
    }
    
    // XP and Level System
    fun getPlayerXP(): Int {
        return prefs.getInt(KEY_PLAYER_XP, 0)
    }

    fun addPlayerXP(amount: Int) {
        val currentXp = getPlayerXP()
        prefs.edit().putInt(KEY_PLAYER_XP, currentXp + amount).apply()
    }

    fun getPlayerLevel(): Int {
        val xp = getPlayerXP()
        // Dinamik zorlaşan seviye formülü: Seviye = √(XP / 100) + 1
        // Seviye 1 = 0 XP, Seviye 2 = 100 XP, Seviye 3 = 400 XP, Seviye 4 = 900 XP
        val calculatedLevel = Math.floor(Math.sqrt(xp / 100.0)).toInt() + 1
        return calculatedLevel.coerceAtMost(100)
    }

    // Returns a float between 0.0f and 1.0f representing progress towards the next level
    fun getPlayerLevelProgress(): Float {
        val currentLevel = getPlayerLevel()
        if (currentLevel >= 100) return 1f // Max level ulaşıldı

        val xp = getPlayerXP()
        val xpForCurrentLevel = Math.pow((currentLevel - 1).toDouble(), 2.0).toInt() * 100
        val xpForNextLevel = Math.pow(currentLevel.toDouble(), 2.0).toInt() * 100
        
        val progress = (xp - xpForCurrentLevel).toFloat() / (xpForNextLevel - xpForCurrentLevel).toFloat()
        return progress.coerceIn(0f, 1f)
    }
}
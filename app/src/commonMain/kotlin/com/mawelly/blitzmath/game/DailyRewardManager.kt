package com.mawelly.blitzmath.game

object DailyRewardManager {

    /**
     * Günlük ödül durumunu kontrol eder.
     * @param lastClaimTime Son ödül alma zamanı (ms)
     * @param currentTime Şu anki zaman (ms)
     */
    fun getRewardStatus(lastClaimTime: Long, currentTime: Long): DailyRewardStatus {
        if (lastClaimTime == 0L) return DailyRewardStatus.AVAILABLE

        // Basit gün hesaplaması (UTC)
        // 1 gün = 24 * 60 * 60 * 1000 = 86400000 ms
        val DAY_MS = 86400000L
        val currentDay = currentTime / DAY_MS
        val lastClaimDay = lastClaimTime / DAY_MS

        if (currentDay == lastClaimDay) {
            return DailyRewardStatus.CLAIMED
        }

        if (currentDay - lastClaimDay == 1L) {
            return DailyRewardStatus.AVAILABLE
        }

        return DailyRewardStatus.STREAK_RESET
    }

    /**
     * Yeni seri sayısını hesaplar.
     * @param currentStreak Mevcut seri
     * @param lastClaimTime Son ödül alma zamanı
     * @param currentTime Şu anki zaman (ms)
     * @return Yeni seri sayısı
     */
    fun calculateNewStreak(currentStreak: Int, lastClaimTime: Long, currentTime: Long): Int {
        val status = getRewardStatus(lastClaimTime, currentTime)
        return when (status) {
            DailyRewardStatus.AVAILABLE -> currentStreak + 1
            DailyRewardStatus.STREAK_RESET -> 1
            DailyRewardStatus.CLAIMED -> currentStreak
        }
    }

    /**
     * Seri bazlı yıldız ödülünü hesaplar.
     * @param streak Güncel seri
     * @return Kazanılan yıldız miktarı
     */
    fun getStarReward(streak: Int): Int {
        val dayInCycle = (streak % 7).let { if (it == 0 && streak > 0) 7 else it }
        return when (dayInCycle) {
            1 -> 10
            2 -> 20
            3 -> 30
            4 -> 40
            5 -> 50
            6 -> 60
            7 -> 100 // Mega Bonus
            else -> 10
        }
    }
}

enum class DailyRewardStatus {
    AVAILABLE,      // Bugün alınabilir
    CLAIMED,        // Bugün zaten alındı
    STREAK_RESET    // Seri bozuldu, yeniden başla
}

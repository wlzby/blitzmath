package com.mawelly.blitzmath.game

import java.util.Calendar

object DailyRewardManager {

    /**
     * Günlük ödül durumunu kontrol eder.
     * @param lastClaimTime Son ödül alma zamanı (ms)
     * @return DailyRewardStatus
     */
    fun getRewardStatus(lastClaimTime: Long): DailyRewardStatus {
        if (lastClaimTime == 0L) return DailyRewardStatus.AVAILABLE

        val now = System.currentTimeMillis()
        val lastClaimCal = Calendar.getInstance().apply { timeInMillis = lastClaimTime }
        val nowCal = Calendar.getInstance().apply { timeInMillis = now }

        // Aynı gün mü?
        if (isSameDay(lastClaimCal, nowCal)) {
            return DailyRewardStatus.CLAIMED
        }

        // Dün mü?
        val yesterdayCal = Calendar.getInstance().apply { 
            timeInMillis = now
            add(Calendar.DAY_OF_YEAR, -1)
        }

        return if (isSameDay(lastClaimCal, yesterdayCal)) {
            DailyRewardStatus.AVAILABLE
        } else {
            DailyRewardStatus.STREAK_RESET
        }
    }

    /**
     * Yeni seri sayısını hesaplar.
     * @param currentStreak Mevcut seri
     * @param lastClaimTime Son ödül alma zamanı
     * @return Yeni seri sayısı
     */
    fun calculateNewStreak(currentStreak: Int, lastClaimTime: Long): Int {
        val status = getRewardStatus(lastClaimTime)
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

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}

enum class DailyRewardStatus {
    AVAILABLE,      // Bugün alınabilir
    CLAIMED,        // Bugün zaten alındı
    STREAK_RESET    // Seri bozuldu, yeniden başla
}

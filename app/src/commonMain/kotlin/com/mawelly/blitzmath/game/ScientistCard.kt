package com.mawelly.blitzmath.game

import com.mawelly.blitzmath.localization.AppLanguage


enum class BonusType {
    ADD_TIME,
    FREEZE_TIME,
    INSTANT_SCORE,
    HINT_ANSWER,
    EXTRA_LIFE,
    SLOW_MOTION,
    SKIP_QUESTION,
    TESLA_ZAP
}

data class ScientistCard(
    val id: String,
    val name: String,
    val price: Int,
    val bonusType: BonusType,
    val maxCharges: Int = 3,
    val rechargeAdsRequired: Int = 3,
    val rechargeDurationMinutes: Int = 60, // Default 1 hour
    val imageResId: String? = null
) {
    val description: String
        get() = com.mawelly.blitzmath.localization.Strings.getScientistDescription(id)
}

object ScientistCards {
    val cards = listOf(
        ScientistCard(
            id = "pythagoras",
            name = "Pisagor",
            price = 500,
            bonusType = BonusType.ADD_TIME,
            maxCharges = 3,
            rechargeAdsRequired = 2,
            rechargeDurationMinutes = 30, // 30 min
            imageResId = "pythagoras_portrait"
        ),
        ScientistCard(
            id = "cahit_arf",
            name = "Cahit Arf",
            price = 1000,
            bonusType = BonusType.FREEZE_TIME,
            maxCharges = 2,
            rechargeAdsRequired = 4,
            rechargeDurationMinutes = 120, // 2 hours
            imageResId = "cahit_arf_portrait"
        ),
        ScientistCard(
            id = "curie",
            name = "Marie Curie",
            price = 1500,
            bonusType = BonusType.INSTANT_SCORE,
            maxCharges = 3,
            rechargeAdsRequired = 3,
            rechargeDurationMinutes = 60, // 1 hour
            imageResId = "curie_portrait"
        ),
        ScientistCard(
            id = "turing",
            name = "Alan Turing",
            price = 2500,
            bonusType = BonusType.HINT_ANSWER,
            maxCharges = 4,
            rechargeAdsRequired = 3,
            rechargeDurationMinutes = 60, // 1 hour
            imageResId = "turing_portrait"
        ),
        ScientistCard(
            id = "newton",
            name = "Isaac Newton",
            price = 4000,
            bonusType = BonusType.EXTRA_LIFE,
            maxCharges = 2,
            rechargeAdsRequired = 4,
            rechargeDurationMinutes = 120, // 2 hours
            imageResId = "newton_portrait"
        ),
        ScientistCard(
            id = "einstein",
            name = "Albert Einstein",
            price = 7500,
            bonusType = BonusType.SLOW_MOTION,
            maxCharges = 3,
            rechargeAdsRequired = 3,
            rechargeDurationMinutes = 60, // 1 hour
            imageResId = "einstein_portrait"
        ),
        ScientistCard(
            id = "gauss",
            name = "Carl Friedrich Gauss",
            price = 12000,
            bonusType = BonusType.SKIP_QUESTION,
            maxCharges = 2,
            rechargeAdsRequired = 5,
            rechargeDurationMinutes = 180, // 3 hours
            imageResId = "gauss_portrait"
        ),
        ScientistCard(
            id = "tesla",
            name = "Nikola Tesla",
            price = 20000,
            bonusType = BonusType.TESLA_ZAP,
            maxCharges = 5,
            rechargeAdsRequired = 2,
            rechargeDurationMinutes = 30, // 30 min
            imageResId = "tesla_portrait"
        )
    )

    fun getCardById(id: String) = cards.find { it.id == id }
}

package ru.lct2026.finedu.productcore.navigation.api

import kotlinx.serialization.Serializable

/**
 * Type-safe маршруты всех экранов MVP. Номера — из `decomposition.md`.
 */
sealed interface FinEduRoute {

    /** 1. Онбординг / Помощь. */
    @Serializable
    data object Onboarding : FinEduRoute

    /** 2. Создание героя. */
    @Serializable
    data object Hero : FinEduRoute

    /** 3. Главный. */
    @Serializable
    data object Home : FinEduRoute

    /** 4. План бюджета. */
    @Serializable
    data object Budget : FinEduRoute

    /** 5. Магазин. */
    @Serializable
    data object Shop : FinEduRoute

    /** 6. Копилка. */
    @Serializable
    data object Savings : FinEduRoute

    /** 7. Задания. */
    @Serializable
    data object Quests : FinEduRoute

    /** 8. Задание. */
    @Serializable
    data class Quest(val questId: String) : FinEduRoute

    /** 9. Итоги периода. */
    @Serializable
    data object PeriodSummary : FinEduRoute

    /** 10. Раздел для взрослого. */
    @Serializable
    data object Parent : FinEduRoute
}

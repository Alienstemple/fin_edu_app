package ru.lct2026.finedu.productcore.navigation.api

import kotlinx.serialization.Serializable

/**
 * Type-safe маршруты всех экранов. Экраны — по макету «Дзынь · 2026», см. `docs/implementation-plan.md`.
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

    /** «Полезное» и помощь (фича `learn`). */
    @Serializable
    data object Glossary : FinEduRoute

    /** Шортс «Дзынь объясняет за 15 секунд» и лента карточек (фича `learn`). */
    @Serializable
    data object Shorts : FinEduRoute
}

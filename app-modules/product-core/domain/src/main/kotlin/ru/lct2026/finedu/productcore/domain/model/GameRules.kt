package ru.lct2026.finedu.productcore.domain.model

/** Числовые правила игры. Формулы — в [GameEngine], [SavingsEngine], [QuestEngine]. */
object GameRules {
    /** Регулярный доход за неделю. */
    val WEEKLY_INCOME = Dzynki(300)

    /** Сколько недель в сюжете демо-режима. */
    const val DEMO_WEEKS = 5

    const val STAT_MIN = 10
    const val STAT_MAX = 100
    const val STAT_INITIAL = 60

    /** Показатель не выше этого значения — Дзынь «устал». */
    const val TIRED_THRESHOLD = 30

    /** На сколько действие (покупка, пополнение, «Подожду») поднимает показатель. */
    const val STAT_STEP = 10

    /** На сколько снижается каждый показатель к новой неделе. */
    const val WEEKLY_STAT_DECAY = 20

    /** После паузы показатели подтягиваются минимум до этого значения. */
    const val RETURN_STAT_FLOOR = 50

    /** Пауза, после которой срабатывает «возврат»: сутки. */
    const val RETURN_AFTER_MILLIS = 24L * 60 * 60 * 1000

    /** Опыт для стадий «Шустрик» и «Мастер мешочка». */
    const val SPRY_XP = 4
    const val MASTER_XP = 10
}

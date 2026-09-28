package ru.lct2026.finedu.productcore.domain.model

/** Числовые правила игры. Формулы — в [GameEngine]. */
object GameRules {
    /** Карманные деньги за неделю. */
    val WEEKLY_INCOME = Dzynki(500)

    /** Сколько недель в сюжете демо-режима. */
    const val DEMO_WEEKS = 5

    const val STAT_MIN = 10
    const val STAT_MAX = 100
    const val STAT_INITIAL = 60

    /** Показатель не выше этого значения — Дзынь «устал». */
    const val TIRED_THRESHOLD = 30

    /** На сколько снижается каждый показатель к новой неделе. */
    const val WEEKLY_STAT_DECAY = 20

    /** Спокойствие меняется на 1 за каждые столько дзынек, положенных в копилку или снятых из неё. */
    const val DZYNKI_PER_CALM_POINT = 5

    /** Опыт для стадий «Шустрик» и «Мастер мешочка». */
    const val SPRY_XP = 4
    const val MASTER_XP = 10
}

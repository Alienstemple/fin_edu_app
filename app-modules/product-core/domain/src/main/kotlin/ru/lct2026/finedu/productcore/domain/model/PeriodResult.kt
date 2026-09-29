package ru.lct2026.finedu.productcore.domain.model

/** За что питомец получил опыт за неделю: по +1 за каждую причину. */
enum class XpReason {
    PLANNED,
    NEEDS_CLOSED,

    /** Сам отложил в копилку: раскладкой или пополнением. */
    SAVED,
    QUEST_DONE
}

/** Итоги недели: «Ожидание / Реальность» и рост питомца. */
data class PeriodResult(
    val number: Int,
    val income: Dzynki,
    val plan: BagAmounts?,
    val actual: BagAmounts,
    /** Остатки «Нужного», «Хочу» и неразложенное, ушедшие в копилку при закрытии недели. */
    val leftoverToSavings: Dzynki,
    val withdrawn: Dzynki,
    val xpReasons: List<XpReason>,
    /** Неделя в рамках плана: на пледе появилась звёздочка. */
    val hasEarnedStar: Boolean,
    val stageBefore: PetStage,
    val stageAfter: PetStage
) {
    /** Сколько всего легло в копилку за неделю. */
    val savedTotal: Dzynki get() = actual.savings + leftoverToSavings
    val xpGained: Int get() = xpReasons.size
    val isNeedsClosed: Boolean get() = XpReason.NEEDS_CLOSED in xpReasons
    val isStageChanged: Boolean get() = stageAfter != stageBefore
}

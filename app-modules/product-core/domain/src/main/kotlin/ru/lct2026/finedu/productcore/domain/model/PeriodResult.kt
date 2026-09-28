package ru.lct2026.finedu.productcore.domain.model

/** За что питомец получил опыт за неделю: по +1 за каждую причину. */
enum class XpReason {
    NEEDS_CLOSED,
    SAVED,
    WITHIN_PLAN,
    QUEST_DONE
}

/** Итоги недели: план против факта и рост питомца. */
data class PeriodResult(
    val number: Int,
    val income: Dzynki,
    val plan: BagAmounts?,
    val actual: BagAmounts,
    val withdrawn: Dzynki,
    val xpReasons: List<XpReason>,
    val stageBefore: PetStage,
    val stageAfter: PetStage
) {
    val xpGained: Int get() = xpReasons.size
    val isNeedsClosed: Boolean get() = XpReason.NEEDS_CLOSED in xpReasons
    val isWithinPlan: Boolean get() = XpReason.WITHIN_PLAN in xpReasons
    val isStageChanged: Boolean get() = stageAfter != stageBefore
}

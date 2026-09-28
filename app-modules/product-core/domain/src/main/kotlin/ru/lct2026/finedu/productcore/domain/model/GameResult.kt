package ru.lct2026.finedu.productcore.domain.model

enum class FeedbackReason {
    BOUGHT_NEED,
    BOUGHT_WANT,
    DEPOSITED,
    WITHDREW,
    QUEST_DONE
}

/**
 * Что изменилось после действия (ТЗ 2.5.9): кошелёк, копилка, показатели питомца. Почему и что дальше —
 * по [reason] в UI.
 */
data class Feedback(
    val reason: FeedbackReason,
    val balanceDelta: Int = 0,
    val savingsDelta: Int = 0,
    val statChanges: Map<PetStat, Int> = emptyMap(),
    /** Награда за первое прохождение задания. */
    val reward: Dzynki = Dzynki.ZERO
)

sealed interface GameResult {
    data class Success(val state: GameState, val feedback: Feedback) : GameResult

    /** Не хватает [missing] дзынек. Состояние не меняется. */
    data class NotEnoughMoney(val missing: Dzynki) : GameResult
}

sealed interface PlanResult {
    data class Success(val state: GameState) : PlanResult

    /** План больше дохода на [excess]. */
    data class ExceedsIncome(val excess: Dzynki) : PlanResult

    /** План на эту неделю уже подтверждён и не меняется. */
    data object AlreadyConfirmed : PlanResult
}

/** Превью снятия из копилки: было → станет (ТЗ 2.5.7). Срок `null`, если его пока не посчитать. */
data class WithdrawPreview(
    val savingsBefore: Dzynki,
    val savingsAfter: Dzynki,
    val weeksBefore: Int?,
    val weeksAfter: Int?
)

data class PeriodClosing(val state: GameState, val result: PeriodResult)

package ru.lct2026.finedu.productcore.domain.model

enum class FeedbackReason {
    BOUGHT_NEED,
    BOUGHT_WANT,
    PAUSED,
    DEPOSITED,
    WITHDREW,
    GOAL_PLACED
}

data class Change(val from: Dzynki, val to: Dzynki)

data class StatChange(val stat: PetStat, val from: Int, val to: Int)

/**
 * Что изменилось после действия (ТЗ 2.5.9): баланс, мешочек, показатель. Почему и что дальше — по [reason] в UI.
 */
data class Feedback(
    val reason: FeedbackReason,
    val balance: Change,
    val bag: Bag,
    val bagChange: Change,
    val statChange: StatChange
)

sealed interface GameResult {
    data class Success(val state: GameState, val feedback: Feedback) : GameResult

    /** Не хватает [missing] дзынек в мешочке. Состояние не меняется. */
    data class NotEnoughMoney(val missing: Dzynki) : GameResult
}

sealed interface PlanResult {
    data class Success(val state: GameState) : PlanResult

    /**
     * Разложено не до нуля: [left] > 0 — ещё осталось разложить, [left] < 0 — разложено больше, чем есть.
     */
    data class NotBalanced(val left: Int) : PlanResult
}

/** Превью снятия из копилки: было → станет (ТЗ 2.5.7). Срок `null`, если его пока не посчитать. */
data class WithdrawPreview(
    val savingsBefore: Dzynki,
    val savingsAfter: Dzynki,
    val weeksBefore: Int?,
    val weeksAfter: Int?
)

data class PeriodClosing(val state: GameState, val result: PeriodResult)

/** Результат входа в игру: [isReturn] — была пауза, показатели подтянулись. */
data class Visit(val state: GameState, val isReturn: Boolean)

/** Результат задания: награда (0 при повторе) и изменения показателей. */
data class QuestOutcome(val state: GameState, val reward: Dzynki, val statChanges: Map<PetStat, Int>)

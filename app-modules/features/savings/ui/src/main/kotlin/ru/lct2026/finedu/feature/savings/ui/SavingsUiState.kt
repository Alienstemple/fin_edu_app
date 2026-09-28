package ru.lct2026.finedu.feature.savings.ui

import androidx.compose.runtime.Immutable
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.Feedback
import ru.lct2026.finedu.productcore.domain.model.Goal
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.PetStage
import ru.lct2026.finedu.productcore.domain.model.WithdrawPreview

@Immutable
internal sealed interface SavingsUiState {

    data object Loading : SavingsUiState

    /**
     * Копилка. [progress] `null` — все цели уже в уголке. [sheet], [feedback] и [ritual] — открытые поверх экрана
     * шторки и полноэкранный ритуал достигнутой цели.
     */
    data class Content(
        val look: PetLook,
        val stage: PetStage,
        val savings: Dzynki,
        val wantsLeft: Dzynki,
        val placedGoalIds: Set<String>,
        val stars: Int,
        val progress: GoalProgress?,
        val goals: List<GoalItem>,
        val sheet: SavingsSheet? = null,
        val feedback: SavingsFeedback? = null,
        val ritual: GoalRitual? = null
    ) : SavingsUiState {
        val canDeposit: Boolean get() = wantsLeft.amount >= SAVINGS_STEP
        val canWithdraw: Boolean get() = savings.amount >= SAVINGS_STEP
    }
}

/** Текущая цель: [saved] — накоплено на неё (не больше цены), [weeks] — срок `SavingsEngine.weeksToGoal`. */
@Immutable
internal data class GoalProgress(
    val goal: Goal,
    val saved: Dzynki,
    val left: Dzynki,
    val weeks: Int?,
    val weeklyAverage: Int
) {
    val isReached: Boolean get() = left == Dzynki.ZERO
}

@Immutable
internal data class GoalItem(val goal: Goal, val status: GoalStatus, val saved: Dzynki)

internal enum class GoalStatus {
    /** Стоит в уголке. */
    PLACED,

    /** Копим сейчас. */
    CURRENT,

    /** Следующие цели. */
    NEXT
}

/** Шторка со степпером: сумма от [SAVINGS_STEP] до [max] с шагом [SAVINGS_STEP]. */
@Immutable
internal sealed interface SavingsSheet {
    val amount: Dzynki
    val max: Dzynki

    data class Deposit(override val amount: Dzynki, override val max: Dzynki) : SavingsSheet

    data class Withdraw(
        override val amount: Dzynki,
        override val max: Dzynki,
        val preview: WithdrawPreview,
        val goalTitle: String?
    ) : SavingsSheet
}

/** Обратная связь после пополнения или снятия; [goalTitle], [left], [weeks] — для «Почему» (срок до цели). */
@Immutable
internal data class SavingsFeedback(val feedback: Feedback, val goalTitle: String?, val left: Dzynki, val weeks: Int?)

/** Ритуал достигнутой цели: [isPlaced] — предмет уже поставлен в уголок. */
@Immutable
internal data class GoalRitual(val goal: Goal, val isPlaced: Boolean)

/** Шаг степпера и минимальная сумма пополнения и снятия. */
internal const val SAVINGS_STEP = 10

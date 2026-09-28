package ru.lct2026.finedu.feature.period.ui

import androidx.compose.runtime.Immutable
import ru.lct2026.finedu.productcore.domain.model.PeriodResult
import ru.lct2026.finedu.productcore.domain.model.PetLook

@Immutable
internal sealed interface PeriodSummaryUiState {

    data object Loading : PeriodSummaryUiState

    data class Content(
        val result: PeriodResult,
        val look: PetLook,
        /** Опыт питомца после закрытия недели. */
        val petXp: Int,
        /** Звёздочки на пледе после закрытия недели. */
        val stars: Int,
        val placedGoalIds: Set<String>,
        val explanation: SummaryExplanation,
        val advice: SummaryAdvice,
        /** Показан экран роста стадии. */
        val isGrowthShown: Boolean = false
    ) : PeriodSummaryUiState {

        /** Сколько опыта осталось до следующей стадии; `null` — стадия последняя. */
        val xpToNextStage: Int? get() = result.stageAfter.next?.let { it.requiredXp - petXp }
    }
}

/** Пояснение цифрами: куда делись остатки недели. */
@Immutable
internal sealed interface SummaryExplanation {

    /** На «Хочу» ушло меньше плана, и разница уехала в копилку. */
    data class WantsToSavings(val spent: Int, val planned: Int, val extra: Int) : SummaryExplanation

    /** Остатки недели уехали в копилку. */
    data class LeftoverToSavings(val amount: Int) : SummaryExplanation

    /** Остатков нет. */
    data object AllSpent : SummaryExplanation
}

/** Совет «Что попробуем на следующей неделе». */
internal enum class SummaryAdvice { PLAN_EARLY, NEEDS_FIRST, SAVE_EARLY, TRY_CHALLENGE }

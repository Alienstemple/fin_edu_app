package ru.lct2026.finedu.feature.budget.ui

import androidx.compose.runtime.Immutable
import ru.lct2026.finedu.productcore.domain.model.BagAmounts
import ru.lct2026.finedu.productcore.domain.model.IncomeSource
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.PetStage

@Immutable
internal data class BudgetUiState(
    val mode: BudgetMode = BudgetMode.Loading,
    val look: PetLook = PetLook(PetFur.LILAC, PetHat.NONE),
    val stage: PetStage = PetStage.BABY,
    /** Задание, засчитанное за раскладку: «+N за задание „…“». */
    val questReward: QuestReward? = null
)

@Immutable
internal sealed interface BudgetMode {

    data object Loading : BudgetMode

    /** Раскладка: [income] дзынек ещё не разложено, [draft] — черновик по мешочкам. */
    data class Distribute(val income: Int, val sources: List<IncomeSource>, val draft: BagAmounts) : BudgetMode {
        val left: Int get() = income - draft.total.amount
    }

    /** План зафиксирован: [left] — остатки «Нужного» и «Хочу», в копилке — всё, что в ней лежит. */
    data class Fixed(val plan: BagAmounts, val actual: BagAmounts, val left: BagAmounts) : BudgetMode
}

internal data class QuestReward(val amount: Int, val title: String)

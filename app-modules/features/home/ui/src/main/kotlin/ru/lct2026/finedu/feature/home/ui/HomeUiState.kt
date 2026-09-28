package ru.lct2026.finedu.feature.home.ui

import androidx.compose.runtime.Immutable
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.Pet
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.PetStat
import ru.lct2026.finedu.productcore.ui.illustration.PetMood

@Immutable
internal sealed interface HomeUiState {

    data object Loading : HomeUiState

    data class Content(
        val weekNumber: Int,
        /** Сюжет недели; `null` — недели нет в контенте. */
        val weekTitle: String?,
        val isDemo: Boolean,
        val isYounger: Boolean,
        val look: PetLook,
        val pet: Pet,
        val mood: PetMood,
        val line: HomeLine,
        /** Плашка под сценой: возврат после паузы или подсказка в низком состоянии. */
        val notice: HomeNotice?,
        val isDim: Boolean,
        val placedGoalIds: Set<String>,
        val stars: Int,
        val balance: Dzynki,
        val needsLeft: Dzynki,
        val wantsLeft: Dzynki,
        val savings: Dzynki,
        val unallocated: Dzynki,
        /** `null` — все цели уже в уголке. */
        val goal: HomeGoal?,
        /** `null` — все задания пройдены. */
        val quest: HomeQuest?
    ) : HomeUiState
}

/** Реплика Дзыня над сценой. */
internal enum class HomeLine { NORMAL, UNALLOCATED, LOW, RETURN, EASTER_EGG }

/** Плашка под сценой. */
internal sealed interface HomeNotice {
    data object Return : HomeNotice

    /** Низкое состояние: подсказка по самому низкому показателю. */
    data class Low(val stat: PetStat) : HomeNotice
}

/** Текущая цель копилки. [weeksLeft] `null` — срок пока не посчитать, 0 — накоплено. */
internal data class HomeGoal(val title: String, val saved: Dzynki, val price: Dzynki, val weeksLeft: Int?)

internal data class HomeQuest(val id: String, val title: String)

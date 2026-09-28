package ru.lct2026.finedu.feature.quests.ui

import androidx.compose.runtime.Immutable
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.PetStage
import ru.lct2026.finedu.productcore.domain.model.QuestLevel
import ru.lct2026.finedu.productcore.domain.model.QuestTheme
import ru.lct2026.finedu.productcore.navigation.api.FinEduRoute
import ru.lct2026.finedu.productcore.ui.event.Event

@Immutable
internal sealed interface QuestsUiState {

    data object Loading : QuestsUiState

    data class Content(
        val weekNumber: Int,
        /** Сюжет недели; `null`, если для этой недели сюжета нет. */
        val weekTitle: String?,
        val weekTagline: String?,
        val petLook: PetLook,
        val petStage: PetStage,
        val groups: List<QuestGroup>,
        val isChallengeJoined: Boolean
    ) : QuestsUiState
}

@Immutable
internal data class QuestGroup(val theme: QuestTheme, val quests: List<QuestCardItem>)

@Immutable
internal data class QuestCardItem(
    val id: String,
    val title: String,
    val level: QuestLevel,
    val reward: Dzynki,
    val isCompleted: Boolean
)

/** Открыть экран задания или экран, где задание засчитывается действием. */
internal data class OpenRouteEvent(val route: FinEduRoute) : Event

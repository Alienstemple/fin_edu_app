package ru.lct2026.finedu.feature.quests.ui

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.lct2026.finedu.productcore.domain.model.GameContent
import ru.lct2026.finedu.productcore.domain.model.GameState
import ru.lct2026.finedu.productcore.domain.model.Quest
import ru.lct2026.finedu.productcore.domain.model.QuestTheme
import ru.lct2026.finedu.productcore.domain.model.QuestTrigger
import ru.lct2026.finedu.productcore.domain.repository.ContentRepository
import ru.lct2026.finedu.productcore.domain.repository.GameRepository
import ru.lct2026.finedu.productcore.navigation.api.FinEduRoute
import ru.lct2026.finedu.productcore.ui.viewmodel.StatefulViewModel

@HiltViewModel
internal class QuestsViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val contentRepository: ContentRepository
) : StatefulViewModel<QuestsUiState>(QuestsUiState.Loading) {

    private var quests: List<Quest> = emptyList()

    init {
        viewModelScope.launch {
            val content = contentRepository.content()
            quests = content.quests
            gameRepository.state.filterNotNull().collect { game -> setState(game.toUiState(content)) }
        }
    }

    fun onQuestClick(questId: String) {
        val quest = quests.firstOrNull { it.id == questId } ?: return
        offerEvent(OpenRouteEvent(quest.route()))
    }

    fun onChallengeJoinedChange(joined: Boolean) {
        viewModelScope.launch {
            val game = gameRepository.state.first() ?: return@launch
            gameRepository.save(game.copy(period = game.period.copy(isChallengeJoined = joined)))
        }
    }

    /** Задания с выбором проходятся здесь; задания-действия засчитывают экраны, где это действие делают. */
    private fun Quest.route(): FinEduRoute = when (this) {
        is Quest.Choice, is Quest.Scam -> FinEduRoute.Quest(id)

        is Quest.Action -> when (trigger) {
            QuestTrigger.PLAN_FIXED -> FinEduRoute.Budget
            QuestTrigger.SHORTS_WATCHED -> FinEduRoute.Shorts
        }
    }

    private fun GameState.toUiState(content: GameContent): QuestsUiState.Content {
        val week = content.weeks.firstOrNull { it.number == period.number }
        return QuestsUiState.Content(
            weekNumber = period.number,
            weekTitle = week?.title,
            weekTagline = week?.tagline,
            petLook = profile.look,
            petStage = pet.stage,
            groups = QuestTheme.entries.mapNotNull { theme ->
                content.quests
                    .filter { it.theme == theme }
                    .map { quest ->
                        QuestCardItem(
                            id = quest.id,
                            title = quest.title,
                            level = quest.level,
                            reward = quest.reward,
                            isCompleted = quest.id in completedQuestIds
                        )
                    }
                    .takeIf { it.isNotEmpty() }
                    ?.let { QuestGroup(theme, it) }
            },
            isChallengeJoined = period.isChallengeJoined
        )
    }
}

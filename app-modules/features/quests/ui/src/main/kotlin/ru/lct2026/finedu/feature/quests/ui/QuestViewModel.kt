package ru.lct2026.finedu.feature.quests.ui

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.lct2026.finedu.productcore.domain.model.Quest
import ru.lct2026.finedu.productcore.domain.model.QuestEngine
import ru.lct2026.finedu.productcore.domain.repository.ContentRepository
import ru.lct2026.finedu.productcore.domain.repository.GameRepository
import ru.lct2026.finedu.productcore.ui.event.CloseScreenEvent
import ru.lct2026.finedu.productcore.ui.viewmodel.StatefulViewModel

@HiltViewModel(assistedFactory = QuestViewModel.Factory::class)
internal class QuestViewModel @AssistedInject constructor(
    @Assisted private val questId: String,
    private val gameRepository: GameRepository,
    private val contentRepository: ContentRepository
) : StatefulViewModel<QuestUiState>(QuestUiState.Loading) {

    init {
        viewModelScope.launch {
            val quest = contentRepository.content().quests.firstOrNull { it.id == questId }
            val game = gameRepository.state.filterNotNull().first()
            val look = game.profile.look
            val stage = game.pet.stage
            when (quest) {
                is Quest.Choice -> setState(QuestUiState.Choice(quest, look, stage))

                is Quest.Scam -> setState(QuestUiState.Scam(quest, look, stage, ScamStep.Message(index = 0)))

                // Задания-действия засчитывают другие экраны, здесь их не проходят.
                is Quest.Action, null -> offerEvent(CloseScreenEvent)
            }
        }
    }

    /** Защита от двойного нажатия, пока выбор или итог сохраняются. */
    private var isSaving = false

    fun onOptionClick(optionId: String) {
        val state = when (val current = currentState) {
            is QuestUiState.Choice -> current.takeIf { it.result == null }
            QuestUiState.Loading, is QuestUiState.Scam -> null
        } ?: return
        val option = state.quest.options.firstOrNull { it.id == optionId } ?: return
        saveOnce {
            val game = gameRepository.state.first() ?: return@saveOnce
            val outcome = QuestEngine.choose(game, state.quest, optionId)
            gameRepository.save(outcome.state)
            setState(state.copy(result = ChoiceResult(option, outcome.reward)))
        }
    }

    fun onScamAnswer(answer: ScamAnswer) {
        updateScam { step ->
            when (step) {
                is ScamStep.Message -> ScamStep.Review(step.index, answer)
                is ScamStep.Review, is ScamStep.Summary -> step
            }
        }
    }

    fun onScamNextClick() {
        val state = when (val current = currentState) {
            is QuestUiState.Scam -> current
            QuestUiState.Loading, is QuestUiState.Choice -> null
        } ?: return
        val index = when (val step = state.step) {
            is ScamStep.Review -> step.index
            is ScamStep.Message, is ScamStep.Summary -> return
        }
        if (index < state.quest.messages.lastIndex) {
            setState(state.copy(step = ScamStep.Message(index + 1)))
        } else {
            saveOnce {
                val game = gameRepository.state.first() ?: return@saveOnce
                val outcome = QuestEngine.complete(game, state.quest)
                gameRepository.save(outcome.state)
                setState(state.copy(step = ScamStep.Summary(outcome.reward)))
            }
        }
    }

    fun onFinishClick() {
        offerEvent(CloseScreenEvent)
    }

    private fun saveOnce(block: suspend () -> Unit) {
        if (isSaving) return
        isSaving = true
        viewModelScope.launch {
            try {
                block()
            } finally {
                isSaving = false
            }
        }
    }

    private fun updateScam(block: (ScamStep) -> ScamStep) {
        updateState {
            when (this) {
                is QuestUiState.Scam -> copy(step = block(step))
                QuestUiState.Loading, is QuestUiState.Choice -> this
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(questId: String): QuestViewModel
    }
}

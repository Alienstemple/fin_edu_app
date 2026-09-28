package ru.lct2026.finedu.feature.onboarding.ui

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.lct2026.finedu.productcore.domain.repository.GameRepository
import ru.lct2026.finedu.productcore.ui.event.CloseScreenEvent
import ru.lct2026.finedu.productcore.ui.viewmodel.StatefulViewModel

/** Онбординг — учебная сцена: игровое состояние только читается, чтобы понять, куда вести после финала. */
@HiltViewModel
internal class OnboardingViewModel @Inject constructor(private val gameRepository: GameRepository) :
    StatefulViewModel<OnboardingUiState>(OnboardingUiState()) {

    init {
        viewModelScope.launch {
            val hasProfile = gameRepository.state.first() != null
            updateState { copy(hasProfile = hasProfile) }
        }
    }

    fun onPickUpClick() {
        updateState { copy(step = OnboardingStep.Scene(isHeld = true)) }
    }

    fun onSceneNextClick() {
        updateState { copy(step = OnboardingStep.Story(index = 0)) }
    }

    fun onStoryNextClick() {
        updateState {
            val next = when (val current = step) {
                is OnboardingStep.Story -> {
                    val nextIndex = current.index + 1
                    if (nextIndex <
                        OnboardingStep.STORY_COUNT
                    ) {
                        OnboardingStep.Story(nextIndex)
                    } else {
                        OnboardingStep.Choice
                    }
                }

                is OnboardingStep.Scene, OnboardingStep.Choice, is OnboardingStep.Result -> current
            }
            copy(step = next)
        }
    }

    fun onSkipClick() {
        updateState { copy(step = OnboardingStep.Choice) }
    }

    fun onChoiceClick(choice: FirstChoice) {
        updateState { copy(step = OnboardingStep.Result(choice)) }
    }

    fun onFinishClick() {
        viewModelScope.launch {
            val hasProfile = gameRepository.state.first() != null
            offerEvent(if (hasProfile) CloseScreenEvent else OnboardingEvent.OpenHero)
        }
    }
}

package ru.lct2026.finedu.feature.onboarding.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.lct2026.finedu.feature.onboarding.ui.component.ChoiceStep
import ru.lct2026.finedu.feature.onboarding.ui.component.ResultStep
import ru.lct2026.finedu.feature.onboarding.ui.component.SceneStep
import ru.lct2026.finedu.feature.onboarding.ui.component.StoryStep
import ru.lct2026.finedu.productcore.ui.components.FinButton
import ru.lct2026.finedu.productcore.ui.event.CloseScreenEvent
import ru.lct2026.finedu.productcore.ui.event.ObserveEvents
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

@Composable
internal fun OnboardingRoute(
    onOpenHero: () -> Unit,
    onClose: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    ObserveEvents(viewModel.events) { event ->
        when (event) {
            OnboardingEvent.OpenHero -> onOpenHero()
            CloseScreenEvent -> onClose()
        }
    }
    OnboardingScreen(
        state = state,
        onPickUpClick = viewModel::onPickUpClick,
        onSceneNextClick = viewModel::onSceneNextClick,
        onStoryNextClick = viewModel::onStoryNextClick,
        onSkipClick = viewModel::onSkipClick,
        onChoiceClick = viewModel::onChoiceClick,
        onFinishClick = viewModel::onFinishClick
    )
}

@Composable
internal fun OnboardingScreen(
    state: OnboardingUiState,
    onPickUpClick: () -> Unit,
    onSceneNextClick: () -> Unit,
    onStoryNextClick: () -> Unit,
    onSkipClick: () -> Unit,
    onChoiceClick: (FirstChoice) -> Unit,
    onFinishClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reduceMotion = FinEduTheme.reduceMotion
    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        bottomBar = {
            BottomAction(
                step = state.step,
                hasProfile = state.hasProfile,
                onPickUpClick = onPickUpClick,
                onSceneNextClick = onSceneNextClick,
                onStoryNextClick = onStoryNextClick,
                onFinishClick = onFinishClick
            )
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = state.step,
            contentKey = ::stepKey,
            transitionSpec = {
                if (reduceMotion) {
                    EnterTransition.None togetherWith ExitTransition.None
                } else {
                    fadeIn(tween(FADE_IN_MILLIS)) togetherWith fadeOut(tween(FADE_OUT_MILLIS))
                }
            },
            label = "onboardingStep",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { step ->
            when (step) {
                is OnboardingStep.Scene -> SceneStep(isHeld = step.isHeld)
                is OnboardingStep.Story -> StoryStep(index = step.index, onSkipClick = onSkipClick)
                OnboardingStep.Choice -> ChoiceStep(onChoiceClick = onChoiceClick)
                is OnboardingStep.Result -> ResultStep(choice = step.choice)
            }
        }
    }
}

@Composable
private fun BottomAction(
    step: OnboardingStep,
    hasProfile: Boolean,
    onPickUpClick: () -> Unit,
    onSceneNextClick: () -> Unit,
    onStoryNextClick: () -> Unit,
    onFinishClick: () -> Unit
) {
    val action: Pair<Int, () -> Unit>? = when (step) {
        is OnboardingStep.Scene -> if (step.isHeld) {
            R.string.onboarding_next to onSceneNextClick
        } else {
            R.string.onboarding_scene_pick_up to onPickUpClick
        }

        is OnboardingStep.Story -> if (step.index < OnboardingStep.STORY_COUNT - 1) {
            R.string.onboarding_next to onStoryNextClick
        } else {
            R.string.onboarding_story_go to onStoryNextClick
        }

        // Варианты выбора — сами кнопки.
        OnboardingStep.Choice -> null

        is OnboardingStep.Result -> if (hasProfile) {
            R.string.onboarding_result_back to onFinishClick
        } else {
            R.string.onboarding_result_create to onFinishClick
        }
    }
    if (action != null) {
        Box(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            FinButton(text = stringResource(action.first), onClick = action.second)
        }
    }
}

/** Смена шага — плавный переход; «взять на руки» внутри сцены анимирует только питомца. */
private fun stepKey(step: OnboardingStep): Int = when (step) {
    is OnboardingStep.Scene -> 0
    is OnboardingStep.Story -> 1 + step.index
    OnboardingStep.Choice -> 1 + OnboardingStep.STORY_COUNT
    is OnboardingStep.Result -> 2 + OnboardingStep.STORY_COUNT
}

private const val FADE_IN_MILLIS = 250
private const val FADE_OUT_MILLIS = 150

@Composable
private fun OnboardingPreview(step: OnboardingStep, hasProfile: Boolean = false) {
    FinEduPreview {
        OnboardingScreen(
            state = OnboardingUiState(step = step, hasProfile = hasProfile),
            onPickUpClick = {},
            onSceneNextClick = {},
            onStoryNextClick = {},
            onSkipClick = {},
            onChoiceClick = {},
            onFinishClick = {}
        )
    }
}

@Preview
@Composable
private fun OnboardingSceneInBoxPreview() {
    OnboardingPreview(OnboardingStep.Scene(isHeld = false))
}

@Preview
@Composable
private fun OnboardingSceneHeldPreview() {
    OnboardingPreview(OnboardingStep.Scene(isHeld = true))
}

@Preview
@Composable
private fun OnboardingStory1Preview() {
    OnboardingPreview(OnboardingStep.Story(index = 0))
}

@Preview
@Composable
private fun OnboardingStory2Preview() {
    OnboardingPreview(OnboardingStep.Story(index = 1))
}

@Preview
@Composable
private fun OnboardingStory3Preview() {
    OnboardingPreview(OnboardingStep.Story(index = 2))
}

@Preview
@Composable
private fun OnboardingChoicePreview() {
    OnboardingPreview(OnboardingStep.Choice)
}

@Preview
@Composable
private fun OnboardingResultPorridgePreview() {
    OnboardingPreview(OnboardingStep.Result(FirstChoice.PORRIDGE))
}

@Preview
@Composable
private fun OnboardingResultCookieReplayPreview() {
    OnboardingPreview(OnboardingStep.Result(FirstChoice.COOKIE), hasProfile = true)
}

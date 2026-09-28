package ru.lct2026.finedu.feature.onboarding.ui

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.lct2026.finedu.productcore.domain.model.GameEngine
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.Profile
import ru.lct2026.finedu.productcore.ui.event.CloseScreenEvent

class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val existingGame = GameEngine.newGame(
        Profile(playerName = "Капитан", petName = "Дзынь", look = PetLook(PetFur.MINT, PetHat.CAP), isDemo = false),
        nowMillis = 0L
    )

    @Test
    fun `онбординг начинается со сцены с коробкой`() {
        val viewModel = OnboardingViewModel(FakeGameRepository())

        assertEquals(OnboardingStep.Scene(isHeld = false), viewModel.currentState.step)
        assertFalse(viewModel.currentState.hasProfile)
    }

    @Test
    fun `взять на руки, затем дальше — первая сторис`() {
        val viewModel = OnboardingViewModel(FakeGameRepository())

        viewModel.onPickUpClick()
        assertEquals(OnboardingStep.Scene(isHeld = true), viewModel.currentState.step)

        viewModel.onSceneNextClick()
        assertEquals(OnboardingStep.Story(index = 0), viewModel.currentState.step)
    }

    @Test
    fun `сторис листаются по порядку, после третьей — первое решение`() {
        val viewModel = OnboardingViewModel(FakeGameRepository())
        viewModel.onSceneNextClick()

        viewModel.onStoryNextClick()
        assertEquals(OnboardingStep.Story(index = 1), viewModel.currentState.step)
        viewModel.onStoryNextClick()
        assertEquals(OnboardingStep.Story(index = 2), viewModel.currentState.step)
        viewModel.onStoryNextClick()
        assertEquals(OnboardingStep.Choice, viewModel.currentState.step)
    }

    @Test
    fun `пропустить сторис — сразу первое решение`() {
        val viewModel = OnboardingViewModel(FakeGameRepository())
        viewModel.onSceneNextClick()

        viewModel.onSkipClick()

        assertEquals(OnboardingStep.Choice, viewModel.currentState.step)
    }

    @Test
    fun `выбор показывает результат и ничего не пишет в игровое состояние`() {
        val repository = FakeGameRepository()
        val viewModel = OnboardingViewModel(repository)

        viewModel.onChoiceClick(FirstChoice.COOKIE)

        assertEquals(OnboardingStep.Result(FirstChoice.COOKIE), viewModel.currentState.step)
        assertTrue(repository.saved.isEmpty())
    }

    @Test
    fun `без профиля финальная кнопка ведёт к созданию питомца`() = runTest {
        val viewModel = OnboardingViewModel(FakeGameRepository())
        viewModel.onChoiceClick(FirstChoice.PORRIDGE)

        viewModel.events.flow.test {
            viewModel.onFinishClick()

            assertEquals(OnboardingEvent.OpenHero, awaitItem())
        }
    }

    @Test
    fun `повторный онбординг из словарика возвращает назад`() = runTest {
        val repository = FakeGameRepository(existingGame)
        val viewModel = OnboardingViewModel(repository)
        assertTrue(viewModel.currentState.hasProfile)

        viewModel.events.flow.test {
            viewModel.onFinishClick()

            assertEquals(CloseScreenEvent, awaitItem())
        }
        assertTrue(repository.saved.isEmpty())
    }
}

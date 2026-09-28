package ru.lct2026.finedu.feature.savings.ui

import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.lct2026.finedu.productcore.domain.model.BagAmounts
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.FeedbackReason
import ru.lct2026.finedu.productcore.domain.model.GameContent
import ru.lct2026.finedu.productcore.domain.model.GameEngine
import ru.lct2026.finedu.productcore.domain.model.GameState
import ru.lct2026.finedu.productcore.domain.model.Goal
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.PlanResult
import ru.lct2026.finedu.productcore.domain.model.Profile
import ru.lct2026.finedu.productcore.domain.model.ShortsStory
import ru.lct2026.finedu.productcore.domain.repository.ContentRepository
import ru.lct2026.finedu.productcore.domain.repository.GameRepository

class SavingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `до загрузки профиля показывается загрузка`() {
        val viewModel = viewModel(repository = FakeGameRepository(null))

        assertEquals(SavingsUiState.Loading, viewModel.stateFlow.value)
    }

    @Test
    fun `цели получают статусы, срок считается по среднему прибавлению`() {
        val state = planned.copy(placedGoalIds = setOf(plaid.id))

        val content = viewModel(FakeGameRepository(state)).content()

        assertEquals(
            listOf(GoalStatus.PLACED, GoalStatus.CURRENT, GoalStatus.NEXT),
            content.goals.map { it.status }
        )
        assertEquals(Dzynki(30), content.goals[1].saved)
        assertEquals(GoalProgress(lamp, Dzynki(30), Dzynki(70), weeks = 3, weeklyAverage = 30), content.progress)
    }

    @Test
    fun `без пополнений срок не считается`() {
        val content = viewModel(FakeGameRepository(start)).content()

        assertNull(content.progress?.weeks)
        assertFalse(content.canDeposit)
        assertFalse(content.canWithdraw)
    }

    @Test
    fun `все цели в уголке — текущей цели нет`() {
        val state = planned.copy(placedGoalIds = setOf(plaid.id, lamp.id, window.id))

        val content = viewModel(FakeGameRepository(state)).content()

        assertNull(content.progress)
        assertTrue(content.goals.all { it.status == GoalStatus.PLACED })
    }

    @Test
    fun `пополнение из «Хочу» — степпер в границах, сохранение и обратная связь со сроком`() {
        val repository = FakeGameRepository(planned.copy(wantsLeft = Dzynki(35)))
        val viewModel = viewModel(repository)

        viewModel.onDepositClick()
        viewModel.onAmountStep(up = false)
        assertEquals(SavingsSheet.Deposit(Dzynki(10), max = Dzynki(30)), viewModel.content().sheet)

        repeat(times = 3) { viewModel.onAmountStep(up = true) }
        assertEquals(Dzynki(30), viewModel.content().sheet?.amount)

        viewModel.onSheetConfirm()

        val saved = repository.flow.value!!
        assertEquals(Dzynki(60), saved.savings)
        assertEquals(Dzynki(5), saved.wantsLeft)
        val content = viewModel.content()
        assertNull(content.sheet)
        assertEquals(FeedbackReason.DEPOSITED, content.feedback?.feedback?.reason)
        assertEquals(plaid.title, content.feedback?.goalTitle)
        assertEquals(0, content.feedback?.weeks)

        viewModel.onFeedbackDismiss()
        assertNull(viewModel.content().feedback)
    }

    @Test
    fun `пополнить нельзя, если в «Хочу» пусто`() {
        val viewModel = viewModel(FakeGameRepository(planned.copy(wantsLeft = Dzynki.ZERO)))

        viewModel.onDepositClick()

        assertNull(viewModel.content().sheet)
    }

    @Test
    fun `снятие — превью было → станет, сохранение и возврат в «Хочу»`() {
        val repository = FakeGameRepository(planned)
        val viewModel = viewModel(repository)

        viewModel.onWithdrawClick()
        viewModel.onAmountStep(up = true)

        val sheet = viewModel.content().sheet
        assertTrue(sheet is SavingsSheet.Withdraw)
        when (sheet) {
            is SavingsSheet.Withdraw -> {
                assertEquals(Dzynki(20), sheet.amount)
                assertEquals(Dzynki(30), sheet.max)
                assertEquals(Dzynki(10), sheet.preview.savingsAfter)
                assertEquals(1, sheet.preview.weeksBefore)
                assertEquals(2, sheet.preview.weeksAfter)
                assertEquals(plaid.title, sheet.goalTitle)
            }

            is SavingsSheet.Deposit, null -> Unit
        }

        viewModel.onSheetConfirm()

        val saved = repository.flow.value!!
        assertEquals(Dzynki(10), saved.savings)
        assertEquals(Dzynki(140), saved.wantsLeft)
        assertEquals(FeedbackReason.WITHDREW, viewModel.content().feedback?.feedback?.reason)
    }

    @Test
    fun `закрытие шторки ничего не меняет`() {
        val repository = FakeGameRepository(planned)
        val viewModel = viewModel(repository)

        viewModel.onWithdrawClick()
        viewModel.onSheetDismiss()

        assertNull(viewModel.content().sheet)
        assertEquals(planned, repository.flow.value)
    }

    @Test
    fun `ритуал — цель ставится в уголок навсегда, затем переход в уголок`() = runTest {
        val repository = FakeGameRepository(planned.copy(savings = Dzynki(70)))
        val viewModel = viewModel(repository)

        viewModel.onPlaceGoalClick()
        assertEquals(GoalRitual(plaid, isPlaced = false), viewModel.content().ritual)

        viewModel.onRitualPlaceClick()

        val saved = repository.flow.value!!
        assertEquals(setOf(plaid.id), saved.placedGoalIds)
        assertEquals(Dzynki(10), saved.savings)
        val content = viewModel.content()
        assertEquals(GoalRitual(plaid, isPlaced = true), content.ritual)
        assertEquals(lamp, content.progress?.goal)

        viewModel.events.flow.test {
            viewModel.onGoHomeClick()
            assertEquals(SavingsEvent.OpenHome, awaitItem())
        }
        assertNull(viewModel.content().ritual)
    }

    @Test
    fun `ритуал не открывается, пока цель не накоплена, и закрывается без изменений`() {
        val repository = FakeGameRepository(planned)
        val viewModel = viewModel(repository)

        viewModel.onPlaceGoalClick()
        assertNull(viewModel.content().ritual)

        repository.flow.value = planned.copy(savings = Dzynki(60))
        viewModel.onPlaceGoalClick()
        viewModel.onRitualDismiss()

        assertNull(viewModel.content().ritual)
        assertEquals(emptySet<String>(), repository.flow.value?.placedGoalIds)
    }

    private fun viewModel(repository: GameRepository) = SavingsViewModel(repository, FakeContentRepository)

    private fun SavingsViewModel.content(): SavingsUiState.Content = when (val state = stateFlow.value) {
        SavingsUiState.Loading -> error("Ожидали содержимое, а идёт загрузка")
        is SavingsUiState.Content -> state
    }

    private class FakeGameRepository(initial: GameState?) : GameRepository {
        val flow = MutableStateFlow(initial)
        override val state = flow

        override suspend fun save(state: GameState) {
            flow.value = state
        }

        override suspend fun clear() {
            flow.value = null
        }
    }

    private object FakeContentRepository : ContentRepository {
        override suspend fun content() = GameContent(
            shopItems = emptyList(),
            goals = listOf(plaid, lamp, window),
            quests = emptyList(),
            weeks = emptyList(),
            glossary = emptyList(),
            shorts = ShortsStory(title = "", frames = emptyList()),
            feed = emptyList(),
            articles = emptyList(),
            scamSchemes = emptyList(),
            talkQuestions = emptyList()
        )
    }

    private companion object {
        val plaid = Goal("plaid", "Плед", Dzynki(60))
        val lamp = Goal("lamp", "Лампа для уголка", Dzynki(100))
        val window = Goal("window", "Окно с видом", Dzynki(200))

        val start = GameEngine.newGame(
            Profile("Капитан Носок", "Дзынь", PetLook(PetFur.MINT, PetHat.CAP), isDemo = true),
            nowMillis = 0L
        )

        /** Неделя 1: 150 / 120 / 30 — в копилке 30, в «Хочу» 120. */
        private val weekPlan = BagAmounts(Dzynki(150), Dzynki(120), Dzynki(30))

        val planned: GameState = when (val result = GameEngine.distribute(start, weekPlan)) {
            is PlanResult.Success -> result.state
            is PlanResult.NotBalanced -> error("План не сходится")
        }
    }
}

package ru.lct2026.finedu.feature.period.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.lct2026.finedu.productcore.domain.model.Bag
import ru.lct2026.finedu.productcore.domain.model.BagAmounts
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.GameEngine
import ru.lct2026.finedu.productcore.domain.model.GameResult
import ru.lct2026.finedu.productcore.domain.model.GameState
import ru.lct2026.finedu.productcore.domain.model.PeriodResult
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.PetStage
import ru.lct2026.finedu.productcore.domain.model.PlanResult
import ru.lct2026.finedu.productcore.domain.model.Profile
import ru.lct2026.finedu.productcore.domain.model.ShopItem
import ru.lct2026.finedu.productcore.domain.model.XpReason
import ru.lct2026.finedu.productcore.domain.repository.GameRepository

class PeriodSummaryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val profile = Profile("Капитан Носок", "Дзынь", PetLook(PetFur.MINT, PetHat.CAP), isDemo = true)
    private val start = GameEngine.newGame(profile, nowMillis = 0L)
    private val groceries = ShopItem("groceries", "Продукты на неделю", Dzynki(150), Bag.NEEDS, shortageLine = null)
    private val toy = ShopItem("toy", "Игрушка", Dzynki(90), Bag.WANTS, shortageLine = null)

    /** План 150 / 120 / 30, Нужное закрыто, на «Хочу» ушло 90. */
    private val goodWeek = start
        .distribute(BagAmounts(Dzynki(150), Dzynki(120), Dzynki(30)))
        .buy(groceries)
        .buy(toy)

    private val repository = FakeGameRepository(goodWeek)

    @Test
    fun `неделя закрывается один раз и итоги показываются по закрытию`() = runTest {
        val viewModel = PeriodSummaryViewModel(repository, SavedStateHandle())

        val content = viewModel.content()
        assertEquals(1, repository.saves)
        assertEquals(2, repository.current?.period?.number)
        assertEquals(1, content.result.number)
        assertEquals(listOf(XpReason.PLANNED, XpReason.NEEDS_CLOSED, XpReason.SAVED), content.result.xpReasons)
        assertEquals(Dzynki(60), content.result.savedTotal)
        assertEquals(1, content.stars)
        assertEquals(3, content.petXp)
        assertEquals(SummaryExplanation.WantsToSavings(spent = 90, planned = 120, extra = 30), content.explanation)
        assertEquals(SummaryAdvice.TRY_CHALLENGE, content.advice)
    }

    @Test
    fun `после пересоздания экрана неделя не закрывается повторно`() = runTest {
        val handle = SavedStateHandle()
        PeriodSummaryViewModel(repository, handle)

        val recreated = PeriodSummaryViewModel(repository, handle)

        val content = recreated.content()
        assertEquals(1, repository.saves)
        assertEquals(2, repository.current?.period?.number)
        assertEquals(1, content.result.number)
    }

    @Test
    fun `без роста стадии «Следующая неделя» ведёт на главный`() = runTest {
        val viewModel = PeriodSummaryViewModel(repository, SavedStateHandle())

        viewModel.events.flow.test {
            viewModel.onNextWeekClick()

            assertEquals(PeriodSummaryEvent.OpenHome, awaitItem())
        }
    }

    @Test
    fun `при росте стадии сначала показывается экран роста, затем главный`() = runTest {
        repository.current = goodWeek.copy(pet = goodWeek.pet.copy(xp = 2))
        val viewModel = PeriodSummaryViewModel(repository, SavedStateHandle())

        viewModel.events.flow.test {
            viewModel.onNextWeekClick()

            val content = viewModel.content()
            assertTrue(content.isGrowthShown)
            assertEquals(PetStage.SPRY, content.result.stageAfter)
            assertEquals(PetStage.MASTER.requiredXp - 5, content.xpToNextStage)
            expectNoEvents()

            viewModel.onGrowthNextClick()

            assertEquals(PeriodSummaryEvent.OpenHome, awaitItem())
        }
    }

    @Test
    fun `совет выбирается по первому, чего не хватило на неделе`() {
        assertEquals(SummaryAdvice.PLAN_EARLY, result(reasons = listOf(XpReason.SAVED)).advice())
        assertEquals(SummaryAdvice.NEEDS_FIRST, result(reasons = listOf(XpReason.PLANNED)).advice())
        assertEquals(
            SummaryAdvice.SAVE_EARLY,
            result(reasons = listOf(XpReason.PLANNED, XpReason.NEEDS_CLOSED)).advice()
        )
        assertEquals(
            SummaryAdvice.TRY_CHALLENGE,
            result(reasons = listOf(XpReason.PLANNED, XpReason.NEEDS_CLOSED, XpReason.SAVED)).advice()
        )
    }

    @Test
    fun `без остатков пояснение — всё потрачено`() {
        assertEquals(SummaryExplanation.AllSpent, result(leftover = Dzynki.ZERO).explanation())
    }

    @Test
    fun `остатки не только из «Хочу» — общее пояснение про копилку`() {
        val result = result(
            plan = BagAmounts(Dzynki(150), Dzynki(120), Dzynki(30)),
            actual = BagAmounts(Dzynki(100), Dzynki(90), Dzynki(30)),
            leftover = Dzynki(80)
        )

        assertEquals(SummaryExplanation.LeftoverToSavings(80), result.explanation())
    }

    @Test
    fun `без плана остатки уходят в копилку общим пояснением`() {
        assertEquals(
            SummaryExplanation.LeftoverToSavings(300),
            result(plan = null, leftover = Dzynki(300)).explanation()
        )
    }

    private fun result(
        reasons: List<XpReason> = emptyList(),
        plan: BagAmounts? = BagAmounts(),
        actual: BagAmounts = BagAmounts(),
        leftover: Dzynki = Dzynki.ZERO
    ) = PeriodResult(
        number = 1,
        income = Dzynki(300),
        plan = plan,
        actual = actual,
        leftoverToSavings = leftover,
        withdrawn = Dzynki.ZERO,
        xpReasons = reasons,
        hasEarnedStar = false,
        stageBefore = PetStage.BABY,
        stageAfter = PetStage.BABY
    )

    private fun PeriodSummaryViewModel.content(): PeriodSummaryUiState.Content = when (val state = currentState) {
        PeriodSummaryUiState.Loading -> error("Итоги ещё не загружены")
        is PeriodSummaryUiState.Content -> state
    }

    private fun GameState.distribute(plan: BagAmounts): GameState = when (
        val result = GameEngine.distribute(this, plan)
    ) {
        is PlanResult.Success -> result.state
        is PlanResult.NotBalanced -> error("План не сходится: ${result.left}")
    }

    private fun GameState.buy(item: ShopItem): GameState = when (val result = GameEngine.buy(this, item)) {
        is GameResult.Success -> result.state
        is GameResult.NotEnoughMoney -> error("Не хватает ${result.missing}")
    }

    private class FakeGameRepository(initial: GameState?) : GameRepository {
        private val flow = MutableStateFlow(initial)
        var saves = 0
            private set
        var current: GameState?
            get() = flow.value
            set(value) {
                flow.value = value
            }

        override val state: Flow<GameState?> = flow

        override suspend fun save(state: GameState) {
            saves++
            flow.value = state
        }

        override suspend fun clear() {
            flow.value = null
        }
    }
}

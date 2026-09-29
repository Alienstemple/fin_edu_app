package ru.lct2026.finedu.feature.budget.ui

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.lct2026.finedu.productcore.domain.model.Bag
import ru.lct2026.finedu.productcore.domain.model.BagAmounts
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.GameEngine
import ru.lct2026.finedu.productcore.domain.model.IncomeSource
import ru.lct2026.finedu.productcore.domain.model.PlanResult

class BudgetViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val gameRepository = FakeGameRepository(TestGame.start)

    private fun viewModel(content: FakeContentRepository = FakeContentRepository(TestGame.content())) =
        BudgetViewModel(gameRepository, content)

    /** Раскладка с нуля: сначала убираем раскладку по умолчанию. */
    private fun BudgetViewModel.fill(needs: Int, wants: Int, savings: Int) {
        Bag.entries.forEach { bag -> repeat(MAX_STEPS) { onMinusClick(bag) } }
        repeat(needs / 10) { onPlusClick(Bag.NEEDS) }
        repeat(wants / 10) { onPlusClick(Bag.WANTS) }
        repeat(savings / 10) { onPlusClick(Bag.SAVINGS) }
    }

    @Test
    fun `новая неделя начинает раскладку регулярного дохода с раскладки по умолчанию`() {
        val viewModel = viewModel()

        assertEquals(
            BudgetMode.Distribute(
                income = 300,
                sources = listOf(IncomeSource.POCKET_MONEY),
                draft = BagAmounts(Dzynki(150), Dzynki(120), Dzynki(30))
            ),
            viewModel.currentState.mode
        )
    }

    @Test
    fun `степпер не уходит ниже нуля и не раскладывает больше, чем пришло`() {
        val viewModel = viewModel()

        viewModel.fill(needs = 0, wants = 0, savings = 0)
        viewModel.onMinusClick(Bag.NEEDS)
        viewModel.fill(needs = 200, wants = 100, savings = 50)

        assertEquals(BagAmounts(Dzynki(200), Dzynki(100), Dzynki.ZERO), draft(viewModel))
    }

    @Test
    fun `минус убирает 10 из мешочка`() {
        val viewModel = viewModel()
        viewModel.fill(needs = 30, wants = 0, savings = 0)

        viewModel.onMinusClick(Bag.NEEDS)

        assertEquals(Dzynki(20), draft(viewModel).needs)
    }

    @Test
    fun `план с остатком не фиксируется`() = runTest {
        val viewModel = viewModel()
        viewModel.fill(needs = 150, wants = 100, savings = 0)

        viewModel.onFixClick()

        assertEquals(TestGame.start, gameRepository.current.value)
    }

    @Test
    fun `раскладка до нуля фиксирует план и засчитывает задание наградой в неразложенное`() = runTest {
        val viewModel = viewModel()
        viewModel.fill(needs = 150, wants = 120, savings = 30)

        viewModel.onFixClick()

        val saved = checkNotNull(gameRepository.current.value)
        assertEquals(BagAmounts(Dzynki(150), Dzynki(120), Dzynki(30)), saved.period.plan)
        assertEquals(Dzynki(20), saved.unallocated)
        assertTrue(TestGame.planQuest.id in saved.completedQuestIds)
        assertEquals(QuestReward(amount = 20, title = TestGame.planQuest.title), viewModel.currentState.questReward)
        assertEquals(
            BudgetMode.Distribute(
                income = 20,
                sources = listOf(IncomeSource.QUEST_REWARD),
                draft = GameEngine.suggestPlan(Dzynki(20))
            ),
            viewModel.currentState.mode
        )
    }

    @Test
    fun `без задания после раскладки показывается план против факта`() = runTest {
        val viewModel = viewModel(FakeContentRepository(TestGame.content(quests = emptyList())))
        viewModel.fill(needs = 150, wants = 120, savings = 30)

        viewModel.onFixClick()

        assertEquals(
            BudgetMode.Fixed(
                plan = BagAmounts(Dzynki(150), Dzynki(120), Dzynki(30)),
                actual = BagAmounts(savings = Dzynki(30)),
                left = BagAmounts(Dzynki(150), Dzynki(120), Dzynki(30))
            ),
            viewModel.currentState.mode
        )
        assertEquals(null, viewModel.currentState.questReward)
    }

    @Test
    fun `бонус после фиксации плана раскладывается отдельно и подписан как бонус`() {
        val planned = when (val result = GameEngine.distribute(TestGame.start, BagAmounts(Dzynki(300)))) {
            is PlanResult.Success -> result.state
            is PlanResult.NotBalanced -> error("План не сходится")
        }
        gameRepository.current.value = GameEngine.grantParentBonus(planned, Dzynki(50), "Помог с уборкой")

        val viewModel = viewModel()

        assertEquals(
            BudgetMode.Distribute(
                income = 50,
                sources = listOf(IncomeSource.PARENT_BONUS),
                draft = GameEngine.suggestPlan(Dzynki(50))
            ),
            viewModel.currentState.mode
        )
    }

    private fun draft(viewModel: BudgetViewModel): BagAmounts = when (val mode = viewModel.currentState.mode) {
        is BudgetMode.Distribute -> mode.draft
        BudgetMode.Loading, is BudgetMode.Fixed -> error("Ожидали раскладку: $mode")
    }

    private companion object {
        /** С запасом больше, чем шагов по 10 в любом мешочке. */
        const val MAX_STEPS = 40
    }
}

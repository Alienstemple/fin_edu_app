package ru.lct2026.finedu.feature.quests.ui

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.GameState
import ru.lct2026.finedu.productcore.ui.event.CloseScreenEvent

class QuestViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(
        questId: String,
        game: GameState = TestContent.game
    ): Pair<QuestViewModel, FakeGameRepository> {
        val repository = FakeGameRepository(game)
        return QuestViewModel(questId, repository, FakeContentRepository(TestContent.content)) to repository
    }

    private fun QuestViewModel.choice(): QuestUiState.Choice = when (val state = currentState) {
        is QuestUiState.Choice -> state
        QuestUiState.Loading, is QuestUiState.Scam -> error("Ожидали задание с выбором: $state")
    }

    private fun QuestViewModel.scamStep(): ScamStep = when (val state = currentState) {
        is QuestUiState.Scam -> state.step
        QuestUiState.Loading, is QuestUiState.Choice -> error("Ожидали «Это мошенники?»: $state")
    }

    @Test
    fun `выбор варианта сохраняет награду в неразложенное и показывает разбор`() {
        val (viewModel, repository) = viewModel("ad")

        viewModel.onOptionClick("later")

        val result = viewModel.choice().result
        assertEquals("later", result?.option?.id)
        assertEquals(Dzynki(20), result?.reward)
        val saved = repository.state.value!!
        assertEquals(TestContent.game.unallocated + Dzynki(20), saved.unallocated)
        assertTrue("ad" in saved.completedQuestIds)
    }

    @Test
    fun `повторное прохождение не даёт награду`() {
        val (viewModel, repository) = viewModel("ad", TestContent.game.copy(completedQuestIds = setOf("ad")))

        viewModel.onOptionClick("buy")

        assertEquals(Dzynki.ZERO, viewModel.choice().result?.reward)
        assertEquals(TestContent.game.unallocated, repository.state.value!!.unallocated)
    }

    @Test
    fun `второй выбор после разбора ничего не меняет`() {
        val (viewModel, repository) = viewModel("ad")
        viewModel.onOptionClick("later")
        val saved = repository.state.value

        viewModel.onOptionClick("buy")

        assertEquals("later", viewModel.choice().result?.option?.id)
        assertEquals(saved, repository.state.value)
    }

    @Test
    fun `«Это мошенники?» проходит сообщения по очереди и в итоге выдаёт награду`() {
        val (viewModel, repository) = viewModel("scam")
        assertEquals(ScamStep.Message(0), viewModel.scamStep())

        viewModel.onScamAnswer(ScamAnswer.NORMAL)
        assertEquals(ScamStep.Review(0, ScamAnswer.NORMAL), viewModel.scamStep())

        viewModel.onScamNextClick()
        assertEquals(ScamStep.Message(1), viewModel.scamStep())
        assertEquals(TestContent.game, repository.state.value)

        viewModel.onScamAnswer(ScamAnswer.SUSPICIOUS)
        viewModel.onScamNextClick()

        assertEquals(ScamStep.Summary(Dzynki(30)), viewModel.scamStep())
        assertTrue("scam" in repository.state.value!!.completedQuestIds)
    }

    @Test
    fun `неизвестное задание и задание-действие закрывают экран`() = runTest {
        listOf("unknown", "three_bags").forEach { id ->
            viewModel(id).first.events.flow.test {
                assertEquals(CloseScreenEvent, awaitItem())
            }
        }
    }

    @Test
    fun `Готово закрывает экран`() = runTest {
        val (viewModel, _) = viewModel("ad")

        viewModel.events.flow.test {
            viewModel.onFinishClick()
            assertEquals(CloseScreenEvent, awaitItem())
        }
    }
}

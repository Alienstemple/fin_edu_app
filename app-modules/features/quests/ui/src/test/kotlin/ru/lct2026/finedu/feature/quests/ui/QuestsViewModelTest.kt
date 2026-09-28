package ru.lct2026.finedu.feature.quests.ui

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.lct2026.finedu.productcore.domain.model.QuestTheme
import ru.lct2026.finedu.productcore.navigation.api.FinEduRoute

class QuestsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val game = TestContent.game.copy(completedQuestIds = setOf("three_bags"))
    private val gameRepository = FakeGameRepository(game)
    private val viewModel by lazy { QuestsViewModel(gameRepository, FakeContentRepository(TestContent.content)) }

    private fun content(): QuestsUiState.Content = when (val state = viewModel.currentState) {
        is QuestsUiState.Content -> state
        QuestsUiState.Loading -> error("Ожидали данные, а идёт загрузка")
    }

    @Test
    fun `задания сгруппированы по темам, пройденные отмечены, сюжет недели из контента`() {
        val state = content()

        assertEquals(listOf(QuestTheme.BUDGET, QuestTheme.SAVINGS, QuestTheme.PURCHASES), state.groups.map { it.theme })
        assertEquals(listOf(true, false), state.groups.first().quests.map { it.isCompleted })
        assertEquals(1, state.weekNumber)
        assertEquals("Первая зарплата", state.weekTitle)
    }

    @Test
    fun `задания с выбором и развод открывают экран задания, действия — свои экраны`() = runTest {
        viewModel.events.flow.test {
            viewModel.onQuestClick("ad")
            assertEquals(OpenRouteEvent(FinEduRoute.Quest("ad")), awaitItem())

            viewModel.onQuestClick("scam")
            assertEquals(OpenRouteEvent(FinEduRoute.Quest("scam")), awaitItem())

            viewModel.onQuestClick("three_bags")
            assertEquals(OpenRouteEvent(FinEduRoute.Budget), awaitItem())

            viewModel.onQuestClick("shorts")
            assertEquals(OpenRouteEvent(FinEduRoute.Shorts), awaitItem())
        }
    }

    @Test
    fun `челлендж недели сохраняется и отменяется`() {
        viewModel.onChallengeJoinedChange(true)

        assertTrue(gameRepository.state.value!!.period.isChallengeJoined)
        assertTrue(content().isChallengeJoined)

        viewModel.onChallengeJoinedChange(false)

        assertEquals(false, gameRepository.state.value!!.period.isChallengeJoined)
    }
}

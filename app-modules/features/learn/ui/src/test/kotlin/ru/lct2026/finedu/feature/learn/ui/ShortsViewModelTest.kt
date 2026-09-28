package ru.lct2026.finedu.feature.learn.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.GameState

class ShortsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val gameRepository = FakeGameRepository(LearnFixtures.game)

    private fun viewModel(game: GameState? = LearnFixtures.game): ShortsViewModel {
        gameRepository.flow.value = game
        return ShortsViewModel(gameRepository, FakeContentRepository(LearnFixtures.content))
    }

    private fun ShortsViewModel.content(): ShortsUiState.Content = when (val state = currentState) {
        ShortsUiState.Loading -> error("Контент не загружен")
        is ShortsUiState.Content -> state
    }

    @Test
    fun `открывается плеер с первого кадра и внешностью питомца`() {
        val state = viewModel().content()

        assertEquals(ShortsMode.PLAYER, state.mode)
        assertEquals(0, state.frameIndex)
        assertEquals(LearnFixtures.look, state.look)
        assertEquals(LearnFixtures.content.shorts.frames, state.frames)
    }

    @Test
    fun `назад на первом кадре остаётся на первом, дальше листает кадры`() {
        val viewModel = viewModel()

        viewModel.onPreviousFrameClick()
        assertEquals(0, viewModel.content().frameIndex)

        viewModel.onNextFrameClick()
        assertEquals(1, viewModel.content().frameIndex)

        viewModel.onPreviousFrameClick()
        assertEquals(0, viewModel.content().frameIndex)
    }

    @Test
    fun `после последнего кадра задание засчитано, награда в неразложенных, открыта лента`() {
        val viewModel = viewModel()

        viewModel.onNextFrameClick()
        viewModel.onNextFrameClick()

        val state = viewModel.content()
        assertEquals(ShortsMode.FEED, state.mode)
        assertEquals(ShortsReward(Dzynki(10), LearnFixtures.shortsQuest.title), state.reward)
        val saved = gameRepository.saved.single()
        assertEquals(LearnFixtures.game.unallocated + Dzynki(10), saved.unallocated)
        assertTrue("shorts" in saved.completedQuestIds)
    }

    @Test
    fun `повторный просмотр не даёт награду второй раз`() {
        val viewModel = viewModel(LearnFixtures.game.copy(completedQuestIds = setOf("shorts")))

        viewModel.onNextFrameClick()
        viewModel.onNextFrameClick()

        assertEquals(ShortsMode.FEED, viewModel.content().mode)
        assertNull(viewModel.content().reward)
        assertTrue(gameRepository.saved.isEmpty())
    }

    @Test
    fun `без профиля шортс смотрится, но ничего не сохраняется`() {
        val viewModel = viewModel(game = null)

        viewModel.onNextFrameClick()
        viewModel.onNextFrameClick()

        assertEquals(ShortsMode.FEED, viewModel.content().mode)
        assertTrue(gameRepository.saved.isEmpty())
    }

    @Test
    fun `нравится и понятно — переключатели`() {
        val viewModel = viewModel()

        viewModel.onLikeClick()
        viewModel.onUnderstoodClick()
        assertTrue(viewModel.content().isLiked)
        assertTrue(viewModel.content().isUnderstood)

        viewModel.onLikeClick()
        assertFalse(viewModel.content().isLiked)
    }

    @Test
    fun `следующая карточка идёт по кругу`() {
        val viewModel = viewModel()
        viewModel.onModeClick(ShortsMode.FEED)

        repeat(2) { viewModel.onNextCardClick() }
        assertEquals(2, viewModel.content().cardIndex)

        viewModel.onNextCardClick()
        assertEquals(0, viewModel.content().cardIndex)
    }

    @Test
    fun `смотреть из ленты открывает плеер с начала`() {
        val viewModel = viewModel()
        viewModel.onNextFrameClick()
        viewModel.onModeClick(ShortsMode.FEED)

        viewModel.onModeClick(ShortsMode.PLAYER)

        assertEquals(ShortsMode.PLAYER, viewModel.content().mode)
        assertEquals(0, viewModel.content().frameIndex)
    }
}

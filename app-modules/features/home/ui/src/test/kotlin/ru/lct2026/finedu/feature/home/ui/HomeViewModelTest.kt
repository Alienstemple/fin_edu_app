package ru.lct2026.finedu.feature.home.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.lct2026.finedu.productcore.domain.model.AgeMode
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.GameContent
import ru.lct2026.finedu.productcore.domain.model.GameEngine
import ru.lct2026.finedu.productcore.domain.model.GameState
import ru.lct2026.finedu.productcore.domain.model.Goal
import ru.lct2026.finedu.productcore.domain.model.Pet
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.PetStat
import ru.lct2026.finedu.productcore.domain.model.Profile
import ru.lct2026.finedu.productcore.domain.model.Quest
import ru.lct2026.finedu.productcore.domain.model.QuestLevel
import ru.lct2026.finedu.productcore.domain.model.QuestTheme
import ru.lct2026.finedu.productcore.domain.model.QuestTrigger
import ru.lct2026.finedu.productcore.domain.model.Settings
import ru.lct2026.finedu.productcore.domain.model.ShortsStory
import ru.lct2026.finedu.productcore.domain.model.WeekStory
import ru.lct2026.finedu.productcore.domain.repository.ContentRepository
import ru.lct2026.finedu.productcore.domain.repository.GameRepository
import ru.lct2026.finedu.productcore.ui.illustration.PetMood

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val gameRepository = FakeGameRepository()
    private val contentRepository = object : ContentRepository {
        override suspend fun content(): GameContent = content
    }

    @Test
    fun `без профиля остаётся загрузка`() {
        val viewModel = HomeViewModel(gameRepository, contentRepository)

        assertEquals(HomeUiState.Loading, viewModel.currentState)
    }

    @Test
    fun `новая неделя показывает сюжет, неразложенный доход, первую цель и первое задание`() {
        gameRepository.state.value = game()

        val state = HomeViewModel(gameRepository, contentRepository).content()

        assertEquals(1, state.weekNumber)
        assertEquals("Первая зарплата", state.weekTitle)
        assertEquals(Dzynki(300), state.unallocated)
        assertEquals(HomeLine.UNALLOCATED, state.line)
        assertEquals(PetMood.NEUTRAL, state.mood)
        assertNull(state.notice)
        assertFalse(state.isDim)
        assertEquals(HomeGoal(title = "Плед", saved = Dzynki.ZERO, price = Dzynki(60), weeksLeft = null), state.goal)
        assertEquals(HomeQuest(id = "three_bags", title = "Три мешочка"), state.quest)
    }

    @Test
    fun `вход сохраняет время визита`() {
        gameRepository.state.value = game(lastVisitMillis = 0L)

        HomeViewModel(gameRepository, contentRepository)

        assertTrue(requireNotNull(gameRepository.state.value).lastVisitMillis > 0L)
    }

    @Test
    fun `возврат после паузы подтягивает показатели и радует Дзыня`() {
        gameRepository.state.value = game(lastVisitMillis = 0L, pet = Pet(charge = 20, vibe = 20, calm = 20))

        val state = HomeViewModel(gameRepository, contentRepository).content()

        assertEquals(Pet(charge = 50, vibe = 50, calm = 50), state.pet)
        assertEquals(PetMood.HAPPY, state.mood)
        assertEquals(HomeLine.RETURN, state.line)
        assertEquals(HomeNotice.Return, state.notice)
    }

    @Test
    fun `низкий показатель приглушает сцену и подсказывает по самому низкому`() {
        gameRepository.state.value = game(pet = Pet(charge = 40, vibe = 20, calm = 30))

        val state = HomeViewModel(gameRepository, contentRepository).content()

        assertTrue(state.isDim)
        assertEquals(PetMood.COLD, state.mood)
        assertEquals(HomeLine.LOW, state.line)
        assertEquals(HomeNotice.Low(PetStat.VIBE), state.notice)
    }

    @Test
    fun `высокий заряд — Дзынь радуется, после раскладки обычная реплика`() {
        gameRepository.state.value = game(pet = Pet(charge = 90), unallocated = Dzynki.ZERO)

        val state = HomeViewModel(gameRepository, contentRepository).content()

        assertEquals(PetMood.HAPPY, state.mood)
        assertEquals(HomeLine.NORMAL, state.line)
    }

    @Test
    fun `после пятой недели сюжета нет, режим 7–8 лет передаётся экрану`() {
        val start = game()
        gameRepository.state.value = start.copy(
            period = start.period.copy(number = 6),
            settings = Settings(ageMode = AgeMode.YOUNGER)
        )

        val state = HomeViewModel(gameRepository, contentRepository).content()

        assertEquals(6, state.weekNumber)
        assertNull(state.weekTitle)
        assertTrue(state.isYounger)
    }

    @Test
    fun `пройденные задания и цели в уголке пропускаются`() {
        gameRepository.state.value = game().copy(
            completedQuestIds = setOf("three_bags"),
            placedGoalIds = setOf("plaid"),
            savings = Dzynki(40)
        )

        val state = HomeViewModel(gameRepository, contentRepository).content()

        assertEquals(HomeQuest(id = "shorts", title = "Шортс"), state.quest)
        assertEquals("Лампа", state.goal?.title)
        assertEquals(Dzynki(40), state.goal?.saved)
    }

    @Test
    fun `всё пройдено и всё в уголке — ни цели, ни задания`() {
        gameRepository.state.value = game().copy(
            completedQuestIds = setOf("three_bags", "shorts"),
            placedGoalIds = setOf("plaid", "lamp")
        )

        val state = HomeViewModel(gameRepository, contentRepository).content()

        assertNull(state.goal)
        assertNull(state.quest)
    }

    @Test
    fun `изменения состояния игры сразу видны на главном`() {
        gameRepository.state.value = game()
        val viewModel = HomeViewModel(gameRepository, contentRepository)

        gameRepository.state.value = requireNotNull(gameRepository.state.value).copy(
            unallocated = Dzynki.ZERO,
            needsLeft = Dzynki(150),
            wantsLeft = Dzynki(120)
        )

        assertEquals(Dzynki(270), viewModel.content().balance)
    }

    @Test
    fun `пасхалка показывается две секунды`() = runTest {
        gameRepository.state.value = game()
        val viewModel = HomeViewModel(gameRepository, contentRepository)

        viewModel.onPetLongPress()
        assertEquals(PetMood.SHOCK, viewModel.content().mood)
        assertEquals(HomeLine.EASTER_EGG, viewModel.content().line)

        advanceTimeBy(EASTER_EGG_MILLIS)
        runCurrent()
        assertEquals(PetMood.NEUTRAL, viewModel.content().mood)
        assertEquals(HomeLine.UNALLOCATED, viewModel.content().line)
    }

    @Test
    fun `тап по питомцу закрывает пасхалку`() {
        gameRepository.state.value = game()
        val viewModel = HomeViewModel(gameRepository, contentRepository)
        viewModel.onPetLongPress()

        viewModel.onPetClick()

        assertEquals(PetMood.NEUTRAL, viewModel.content().mood)
    }

    @Test
    fun `тап по пузырьку раскрывает его, повторный — сворачивает`() {
        gameRepository.state.value = game()
        val viewModel = HomeViewModel(gameRepository, contentRepository)

        viewModel.onBubbleClick(HomeBubble.Balance)
        assertEquals(HomeBubble.Balance, viewModel.content().openBubble)

        viewModel.onBubbleClick(HomeBubble.Balance)
        assertNull(viewModel.content().openBubble)
    }

    @Test
    fun `тап по другому пузырьку переключает раскрытый`() {
        gameRepository.state.value = game()
        val viewModel = HomeViewModel(gameRepository, contentRepository)
        viewModel.onBubbleClick(HomeBubble.Balance)

        viewModel.onBubbleClick(HomeBubble.Stat(PetStat.CALM))

        assertEquals(HomeBubble.Stat(PetStat.CALM), viewModel.content().openBubble)
    }

    @Test
    fun `тап мимо сворачивает пузырёк и переживает обновление игры`() {
        gameRepository.state.value = game()
        val viewModel = HomeViewModel(gameRepository, contentRepository)
        viewModel.onBubbleClick(HomeBubble.Goal)

        gameRepository.state.value = requireNotNull(gameRepository.state.value).copy(unallocated = Dzynki.ZERO)
        assertEquals(HomeBubble.Goal, viewModel.content().openBubble)

        viewModel.onBubbleDismiss()
        assertNull(viewModel.content().openBubble)
    }

    @Test
    fun `у младших нет пузырька задания, итоги недели — последним`() {
        gameRepository.state.value = game().copy(settings = Settings(ageMode = AgeMode.YOUNGER))

        val bubbles = HomeViewModel(gameRepository, contentRepository).content().bubbles

        assertFalse(HomeBubble.Quest in bubbles)
        assertEquals(HomeBubble.FinishWeek, bubbles.last())
    }

    private fun HomeViewModel.content(): HomeUiState.Content = when (val state = currentState) {
        HomeUiState.Loading -> error("Ожидали содержимое главного")
        is HomeUiState.Content -> state
    }

    private fun game(
        lastVisitMillis: Long = System.currentTimeMillis(),
        pet: Pet = Pet(),
        unallocated: Dzynki = Dzynki(300)
    ): GameState = GameEngine.newGame(profile, nowMillis = lastVisitMillis).copy(pet = pet, unallocated = unallocated)

    private class FakeGameRepository : GameRepository {
        override val state = MutableStateFlow<GameState?>(null)

        override suspend fun save(state: GameState) {
            this.state.value = state
        }

        override suspend fun clear() {
            state.value = null
        }
    }

    private companion object {
        const val EASTER_EGG_MILLIS = 2_000L

        val profile = Profile("Капитан Носок", "Дзынь", PetLook(PetFur.MINT, PetHat.CAP), isDemo = true)

        val content = GameContent(
            shopItems = emptyList(),
            goals = listOf(Goal("plaid", "Плед", Dzynki(60)), Goal("lamp", "Лампа", Dzynki(100))),
            quests = listOf(
                Quest.Action(
                    "three_bags",
                    QuestTheme.BUDGET,
                    QuestLevel.EASY,
                    "Три мешочка",
                    Dzynki(20),
                    QuestTrigger.PLAN_FIXED
                ),
                Quest.Action(
                    "shorts",
                    QuestTheme.BUDGET,
                    QuestLevel.EASY,
                    "Шортс",
                    Dzynki(20),
                    QuestTrigger.SHORTS_WATCHED
                )
            ),
            weeks = listOf(WeekStory(1, "Первая зарплата", "Пришли первые дзыньки.")),
            glossary = emptyList(),
            shorts = ShortsStory("Шортс", emptyList()),
            feed = emptyList(),
            articles = emptyList(),
            scamSchemes = emptyList(),
            talkQuestions = emptyList()
        )
    }
}

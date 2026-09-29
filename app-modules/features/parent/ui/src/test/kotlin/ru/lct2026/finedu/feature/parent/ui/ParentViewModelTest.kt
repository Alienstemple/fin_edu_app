package ru.lct2026.finedu.feature.parent.ui

import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.lct2026.finedu.feature.parent.domain.model.ThemeStatus
import ru.lct2026.finedu.productcore.domain.model.AgeMode
import ru.lct2026.finedu.productcore.domain.model.Article
import ru.lct2026.finedu.productcore.domain.model.BagAmounts
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.GameContent
import ru.lct2026.finedu.productcore.domain.model.GameEngine
import ru.lct2026.finedu.productcore.domain.model.GameState
import ru.lct2026.finedu.productcore.domain.model.Goal
import ru.lct2026.finedu.productcore.domain.model.IncomeSource
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.PlanResult
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

class ParentViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val profile = Profile("Капитан Носок", "Дзынь", PetLook(PetFur.MINT, PetHat.CAP), isDemo = true)
    private val gameRepository = FakeGameRepository(GameEngine.newGame(profile, nowMillis = 0L))
    private val ready =
        Article("waiting", "Психология", "Почему дети не умеют ждать", 6, listOf("Абзац"), listOf("Совет"))
    private val soon = Article("soon", "Практика", "Карманные деньги", 7, emptyList(), emptyList())
    private val content = GameContent(
        shopItems = emptyList(),
        goals = listOf(Goal("scooter", "Самокат", Dzynki(300))),
        quests = listOf(
            Quest.Action("plan", QuestTheme.BUDGET, QuestLevel.EASY, "План", Dzynki(20), QuestTrigger.PLAN_FIXED)
        ),
        weeks = listOf(WeekStory(1, "Первая зарплата", "")),
        glossary = emptyList(),
        shorts = ShortsStory("", emptyList()),
        feed = emptyList(),
        articles = listOf(ready, soon),
        scamSchemes = emptyList(),
        talkQuestions = emptyList()
    )
    private val contentRepository = object : ContentRepository {
        override suspend fun content(): GameContent = content
    }

    private fun viewModel() = ParentViewModel(gameRepository, contentRepository)

    private fun ParentViewModel.type(answer: Int) = answer.toString().forEach { onDigitClick(it.digitToInt()) }

    @Test
    fun `верный ответ открывает вкладки`() {
        val viewModel = viewModel()

        viewModel.type(viewModel.currentState.gate.answer)
        viewModel.onDoneClick()

        assertTrue(viewModel.currentState.isUnlocked)
    }

    @Test
    fun `неверный ответ очищает поле и просит ещё раз`() {
        val viewModel = viewModel()

        viewModel.type(viewModel.currentState.gate.answer + 1)
        viewModel.onDoneClick()

        assertFalse(viewModel.currentState.isUnlocked)
        assertTrue(viewModel.currentState.isWrongAnswer)
        assertEquals("", viewModel.currentState.input)
    }

    @Test
    fun `в поле не больше двух цифр, стереть убирает последнюю`() {
        val viewModel = viewModel()

        listOf(1, 2, 3).forEach(viewModel::onDigitClick)
        assertEquals("12", viewModel.currentState.input)

        viewModel.onEraseClick()
        assertEquals("1", viewModel.currentState.input)
    }

    @Test
    fun `прогресс собирается из состояния игры и контента`() {
        val progress = viewModel().currentState.progress

        assertEquals("Капитан Носок", progress?.playerName)
        assertEquals(1, progress?.week)
        assertEquals("Первая зарплата", progress?.storyTitle)
        assertEquals("Самокат", progress?.goalTitle)
        assertNull(progress?.chart)
        assertEquals(ThemeStatus.AHEAD, progress?.themes?.first { it.theme == QuestTheme.BUDGET }?.status)
    }

    @Test
    fun `статья без текста не открывается, с текстом — открывается`() {
        val viewModel = viewModel()

        viewModel.onArticleClick(soon)
        assertNull(viewModel.currentState.openedArticle)

        viewModel.onArticleClick(ready)
        assertEquals(ready, viewModel.currentState.openedArticle)

        viewModel.onTabSelect(ParentTab.SCAMS)
        assertNull(viewModel.currentState.openedArticle)
    }

    @Test
    fun `схема отмечается и снимается повторным нажатием`() {
        val viewModel = viewModel()

        viewModel.onSchemeToggle("sms")
        assertEquals(setOf("sms"), viewModel.currentState.discussedSchemeIds)

        viewModel.onSchemeToggle("sms")
        assertEquals(emptySet<String>(), viewModel.currentState.discussedSchemeIds)
    }

    @Test
    fun `бонус не выходит за 10 и 100`() {
        val viewModel = viewModel()

        repeat(20) { viewModel.onBonusPlusClick() }
        assertEquals(100, viewModel.currentState.bonus.amount)

        repeat(20) { viewModel.onBonusMinusClick() }
        assertEquals(10, viewModel.currentState.bonus.amount)
    }

    @Test
    fun `начисленный бонус попадает в неразложенное и журнал с причиной`() {
        val viewModel = viewModel()
        viewModel.onBonusPlusClick()

        viewModel.onGrantBonusClick("Помог дома")

        val game = gameRepository.current()
        assertEquals(Dzynki(340), game.unallocated)
        assertEquals(IncomeSource.PARENT_BONUS, game.ledger.last().source)
        assertEquals("Помог дома", game.ledger.last().note)
        assertEquals(40, viewModel.currentState.bonus.granted)
    }

    @Test
    fun `настройки сохраняются в состояние игры`() {
        val viewModel = viewModel()

        viewModel.onAgeModeSelect(AgeMode.YOUNGER)
        viewModel.onCalmModeToggle()
        viewModel.onLargeFontToggle()
        viewModel.onVolumeChange(4)
        viewModel.onMusicToggle()

        val expected = Settings(
            ageMode = AgeMode.YOUNGER,
            calmMode = true,
            largeFont = true,
            volume = 4,
            musicEnabled = false
        )
        assertEquals(expected, gameRepository.current().settings)
        assertEquals(expected, viewModel.currentState.settings)
    }

    @Test
    fun `громкость не выходит за пределы шкалы`() {
        val viewModel = viewModel()

        viewModel.onVolumeChange(Settings.MAX_VOLUME + 5)
        assertEquals(Settings.MAX_VOLUME, gameRepository.current().settings.volume)

        viewModel.onVolumeChange(-1)
        assertEquals(0, gameRepository.current().settings.volume)
    }

    @Test
    fun `сброс начинает первую неделю с тем же питомцем и настройками и ведёт на главный`() = runTest {
        val planned = GameEngine.distribute(
            gameRepository.current().copy(settings = Settings(largeFont = true)),
            BagAmounts(Dzynki(150), Dzynki(120), Dzynki(30))
        )
        val plannedState = when (planned) {
            is PlanResult.Success -> planned.state
            is PlanResult.NotBalanced -> error("План не сходится")
        }
        gameRepository.save(plannedState.copy(stars = 2))
        val viewModel = viewModel()

        viewModel.onResetClick()
        assertEquals(ParentDialog.RESET, viewModel.currentState.dialog)

        viewModel.events.flow.test {
            viewModel.onDialogConfirm()
            assertEquals(ParentEvent.OpenHome, awaitItem())
        }
        val game = gameRepository.current()
        assertEquals(0, game.stars)
        assertEquals(Dzynki(300), game.unallocated)
        assertEquals(profile, game.profile)
        assertTrue(game.settings.largeFont)
        assertNull(viewModel.currentState.dialog)
    }

    @Test
    fun `удаление профиля стирает игру и ведёт на онбординг`() = runTest {
        val viewModel = viewModel()
        viewModel.onDeleteClick()

        viewModel.events.flow.test {
            viewModel.onDialogConfirm()
            assertEquals(ParentEvent.OpenOnboarding, awaitItem())
        }
        assertNull(gameRepository.state.value)
    }

    @Test
    fun `отмена закрывает подтверждение без изменений`() {
        val viewModel = viewModel()
        val before = gameRepository.current()

        viewModel.onDeleteClick()
        viewModel.onDialogDismiss()

        assertNull(viewModel.currentState.dialog)
        assertEquals(before, gameRepository.current())
    }

    private class FakeGameRepository(initial: GameState?) : GameRepository {
        override val state = MutableStateFlow(initial)

        fun current(): GameState = checkNotNull(state.value)

        override suspend fun save(state: GameState) {
            this.state.value = state
        }

        override suspend fun clear() {
            state.value = null
        }
    }
}

package ru.lct2026.finedu.feature.learn.ui

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.FeedCard
import ru.lct2026.finedu.productcore.domain.model.GameContent
import ru.lct2026.finedu.productcore.domain.model.GameEngine
import ru.lct2026.finedu.productcore.domain.model.GameState
import ru.lct2026.finedu.productcore.domain.model.GlossaryTerm
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.Profile
import ru.lct2026.finedu.productcore.domain.model.Quest
import ru.lct2026.finedu.productcore.domain.model.QuestLevel
import ru.lct2026.finedu.productcore.domain.model.QuestTheme
import ru.lct2026.finedu.productcore.domain.model.QuestTrigger
import ru.lct2026.finedu.productcore.domain.model.ShortsFrame
import ru.lct2026.finedu.productcore.domain.model.ShortsStory
import ru.lct2026.finedu.productcore.domain.repository.ContentRepository
import ru.lct2026.finedu.productcore.domain.repository.GameRepository

internal class FakeGameRepository(initial: GameState?) : GameRepository {
    val flow = MutableStateFlow(initial)
    val saved = mutableListOf<GameState>()

    override val state: Flow<GameState?> = flow

    override suspend fun save(state: GameState) {
        saved += state
        flow.value = state
    }

    override suspend fun clear() {
        flow.value = null
    }
}

internal class FakeContentRepository(private val content: GameContent) : ContentRepository {
    override suspend fun content(): GameContent = content
}

internal object LearnFixtures {
    val look = PetLook(PetFur.MINT, PetHat.CAP)
    val game = GameEngine.newGame(Profile("Капитан Носок", "Дзынь", look, isDemo = true), nowMillis = 0L)

    val shortsQuest = Quest.Action(
        id = "shorts",
        theme = QuestTheme.BUDGET,
        level = QuestLevel.EASY,
        title = "Шортс: Дзынь объясняет за 15 секунд",
        reward = Dzynki(10),
        trigger = QuestTrigger.SHORTS_WATCHED
    )

    val content = GameContent(
        shopItems = emptyList(),
        goals = emptyList(),
        quests = listOf(shortsQuest),
        weeks = emptyList(),
        glossary = listOf(
            GlossaryTerm("income_regular", "Доход", "Деньги, что приходят."),
            GlossaryTerm("expense", "Расход", "Что уходит.")
        ),
        shorts = ShortsStory(
            title = "Дзынь объясняет",
            frames = listOf(
                ShortsFrame("Шаг 1:", "первая шляпа", "Шляпа за 100."),
                ShortsFrame("Итог:", "300 на голове", "Весь доход недели.")
            )
        ),
        feed = listOf(FeedCard("Доход", "Откуда"), FeedCard("Бюджет", "План"), FeedCard("Реклама", "Почему")),
        articles = emptyList(),
        scamSchemes = emptyList(),
        talkQuestions = emptyList()
    )
}

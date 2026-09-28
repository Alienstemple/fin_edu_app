package ru.lct2026.finedu.feature.budget.ui

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.GameContent
import ru.lct2026.finedu.productcore.domain.model.GameEngine
import ru.lct2026.finedu.productcore.domain.model.GameState
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.Profile
import ru.lct2026.finedu.productcore.domain.model.Quest
import ru.lct2026.finedu.productcore.domain.model.QuestLevel
import ru.lct2026.finedu.productcore.domain.model.QuestTheme
import ru.lct2026.finedu.productcore.domain.model.QuestTrigger
import ru.lct2026.finedu.productcore.domain.model.ShortsStory
import ru.lct2026.finedu.productcore.domain.repository.ContentRepository
import ru.lct2026.finedu.productcore.domain.repository.GameRepository

internal class FakeGameRepository(initial: GameState?) : GameRepository {
    val current = MutableStateFlow(initial)

    override val state: Flow<GameState?> = current

    override suspend fun save(state: GameState) {
        current.value = state
    }

    override suspend fun clear() {
        current.value = null
    }
}

internal class FakeContentRepository(private val content: GameContent) : ContentRepository {
    override suspend fun content(): GameContent = content
}

internal object TestGame {
    private val profile = Profile("Капитан Носок", "Дзынь", PetLook(PetFur.MINT, PetHat.CAP), isDemo = true)

    val start: GameState = GameEngine.newGame(profile, nowMillis = 0L)

    val planQuest = Quest.Action(
        id = "fix_plan",
        theme = QuestTheme.BUDGET,
        level = QuestLevel.EASY,
        title = "Разложи дзыньки",
        reward = Dzynki(20),
        trigger = QuestTrigger.PLAN_FIXED
    )

    fun content(quests: List<Quest> = listOf(planQuest)) = GameContent(
        shopItems = emptyList(),
        goals = emptyList(),
        quests = quests,
        weeks = emptyList(),
        glossary = emptyList(),
        shorts = ShortsStory(title = "", frames = emptyList()),
        feed = emptyList(),
        articles = emptyList(),
        scamSchemes = emptyList(),
        talkQuestions = emptyList()
    )
}

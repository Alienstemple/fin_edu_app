package ru.lct2026.finedu.feature.quests.ui

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
import ru.lct2026.finedu.productcore.domain.model.QuestOption
import ru.lct2026.finedu.productcore.domain.model.QuestTheme
import ru.lct2026.finedu.productcore.domain.model.QuestTrigger
import ru.lct2026.finedu.productcore.domain.model.ScamMessage
import ru.lct2026.finedu.productcore.domain.model.ShortsStory
import ru.lct2026.finedu.productcore.domain.model.WeekStory
import ru.lct2026.finedu.productcore.domain.repository.ContentRepository
import ru.lct2026.finedu.productcore.domain.repository.GameRepository

internal class FakeGameRepository(initial: GameState?) : GameRepository {
    override val state = MutableStateFlow(initial)

    override suspend fun save(state: GameState) {
        this.state.value = state
    }

    override suspend fun clear() {
        state.value = null
    }
}

internal class FakeContentRepository(private val content: GameContent) : ContentRepository {
    override suspend fun content(): GameContent = content
}

internal object TestContent {
    val look = PetLook(PetFur.MINT, PetHat.CAP)
    val game: GameState = GameEngine.newGame(Profile("Ася", "Дзынь", look, isDemo = true), nowMillis = 0L)

    val plan = Quest.Action(
        "three_bags",
        QuestTheme.BUDGET,
        QuestLevel.EASY,
        "Три мешочка",
        Dzynki(20),
        QuestTrigger.PLAN_FIXED
    )
    val shorts = Quest.Action(
        "shorts",
        QuestTheme.BUDGET,
        QuestLevel.EASY,
        "Шортс",
        Dzynki(10),
        QuestTrigger.SHORTS_WATCHED
    )
    val ad = Quest.Choice(
        id = "ad",
        theme = QuestTheme.PURCHASES,
        level = QuestLevel.EASY,
        title = "Никто: / Реклама:",
        reward = Dzynki(20),
        situation = "ТОЛЬКО СЕГОДНЯ!",
        options = listOf(
            QuestOption("buy", "Купить", "Шляпа подождёт"),
            QuestOption("later", "Отложить до завтра", "Смешно, да?")
        ),
        explanation = "Искусственная срочность"
    )
    private val message = ScamMessage("Сообщения", "Неизвестный номер", "Скажи код", emptyList(), "Код никому")
    val scam = Quest.Scam(
        id = "scam",
        theme = QuestTheme.SAVINGS,
        level = QuestLevel.MEDIUM,
        title = "Это мошенники?",
        reward = Dzynki(30),
        messages = listOf(message, message.copy(sender = "Мама (новый номер)")),
        rules = listOf("Коды из СМС — никому")
    )

    val content = GameContent(
        shopItems = emptyList(),
        goals = emptyList(),
        quests = listOf(plan, shorts, scam, ad),
        weeks = listOf(WeekStory(1, "Первая зарплата", "Пришли первые дзыньки.")),
        glossary = emptyList(),
        shorts = ShortsStory("Шортс", emptyList()),
        feed = emptyList(),
        articles = emptyList(),
        scamSchemes = emptyList(),
        talkQuestions = emptyList()
    )
}

package ru.lct2026.finedu.productcore.data.content

import javax.inject.Inject
import kotlinx.serialization.json.Json
import ru.lct2026.finedu.productcore.data.mapper.toDomain
import ru.lct2026.finedu.productcore.domain.model.GameContent

/** Собирает [GameContent] из JSON-файлов `assets/content/`. [read] возвращает текст файла по имени. */
internal class ContentParser @Inject constructor(private val json: Json) {

    fun parse(read: (fileName: String) -> String): GameContent {
        val learn = json.decodeFromString<LearnDto>(read(LEARN))
        val parent = json.decodeFromString<ParentDto>(read(PARENT))
        return GameContent(
            shopItems = json.decodeFromString<List<ShopItemDto>>(read(SHOP)).map { it.toDomain() },
            goals = json.decodeFromString<List<GoalDto>>(read(GOALS)).map { it.toDomain() },
            quests = json.decodeFromString<List<QuestDto>>(read(QUESTS)).map { it.toDomain() },
            weeks = json.decodeFromString<List<WeekStoryDto>>(read(WEEKS)).map { it.toDomain() },
            glossary = json.decodeFromString<List<GlossaryTermDto>>(read(GLOSSARY)).map { it.toDomain() },
            shorts = learn.shorts.toDomain(),
            feed = learn.feed.map { it.toDomain() },
            articles = parent.articles.map { it.toDomain() },
            scamSchemes = parent.scamSchemes.map { it.toDomain() },
            talkQuestions = parent.talkQuestions
        )
    }

    companion object {
        const val DIRECTORY = "content"
        const val SHOP = "shop.json"
        const val GOALS = "goals.json"
        const val QUESTS = "quests.json"
        const val WEEKS = "weeks.json"
        const val GLOSSARY = "glossary.json"
        const val LEARN = "learn.json"
        const val PARENT = "parent.json"
    }
}

package ru.lct2026.finedu.productcore.domain.model

/** Игровой контент из JSON (ТЗ 2.5.14): новое задание или товар — новая запись без правки кода. */
data class GameContent(
    val shopItems: List<ShopItem>,
    /** Цели-предметы уголка в порядке накопления. */
    val goals: List<Goal>,
    val quests: List<Quest>,
    val weeks: List<WeekStory>,
    val glossary: List<GlossaryTerm>,
    val shorts: ShortsStory,
    val feed: List<FeedCard>,
    val articles: List<Article>,
    val scamSchemes: List<ScamScheme>,
    val talkQuestions: List<String>
)

data class ShopItem(
    val id: String,
    val title: String,
    val price: Dzynki,
    val bag: Bag,
    /** Реплика Дзыня, если в мешочке не хватает. */
    val shortageLine: String?
) {
    init {
        require(bag != Bag.SAVINGS) { "Товар покупается из «Нужного» или «Хочу»: $id" }
    }
}

/** Цель копилки — предмет для уголка Дзыня. */
data class Goal(val id: String, val title: String, val price: Dzynki)

enum class QuestTheme { BUDGET, SAVINGS, PURCHASES }

enum class QuestLevel { EASY, MEDIUM }

/** Действие в игре, которое засчитывает задание [Quest.Action]. */
enum class QuestTrigger { PLAN_FIXED, SHORTS_WATCHED }

/** Задание — игровая ситуация с выбором и последствиями (ТЗ 2.5.8). */
sealed interface Quest {
    val id: String
    val theme: QuestTheme
    val level: QuestLevel
    val title: String

    /** Награда за первое прохождение, при любом выборе. */
    val reward: Dzynki

    /** Ситуация с вариантами; после выбора — разбор. */
    data class Choice(
        override val id: String,
        override val theme: QuestTheme,
        override val level: QuestLevel,
        override val title: String,
        override val reward: Dzynki,
        val situation: String,
        val options: List<QuestOption>,
        /** Разбор приёма, общий для всех вариантов. */
        val explanation: String
    ) : Quest

    /** «Это развод?»: сообщения с признаками мошенничества. */
    data class Scam(
        override val id: String,
        override val theme: QuestTheme,
        override val level: QuestLevel,
        override val title: String,
        override val reward: Dzynki,
        val messages: List<ScamMessage>,
        val rules: List<String>
    ) : Quest

    /** Засчитывается действием в игре. */
    data class Action(
        override val id: String,
        override val theme: QuestTheme,
        override val level: QuestLevel,
        override val title: String,
        override val reward: Dzynki,
        val trigger: QuestTrigger
    ) : Quest
}

/** Вариант ответа: [reaction] — что вышло после выбора. */
data class QuestOption(
    val id: String,
    val text: String,
    val reaction: String,
    val statChanges: Map<PetStat, Int> = emptyMap()
)

data class ScamMessage(
    val app: String,
    val sender: String,
    val text: String,
    val signs: List<ScamSign>,
    val tip: String
)

data class ScamSign(val name: String, val why: String)

/** Сюжетный поворот недели. */
data class WeekStory(val number: Int, val title: String, val tagline: String)

data class GlossaryTerm(val term: String, val definition: String)

/** Шортс «Дзынь объясняет за 15 секунд». */
data class ShortsStory(val title: String, val frames: List<ShortsFrame>)

data class ShortsFrame(val kicker: String, val headline: String, val subtitle: String)

data class FeedCard(val title: String, val hook: String)

/** Статья для взрослых. [paragraphs] пустой — статья пока только в списке. */
data class Article(
    val id: String,
    val rubric: String,
    val title: String,
    val minutes: Int,
    val paragraphs: List<String>,
    val tips: List<String>
)

/** Схема, на которую ловят детей, — пункт памятки для взрослых. */
data class ScamScheme(val id: String, val title: String, val text: String)

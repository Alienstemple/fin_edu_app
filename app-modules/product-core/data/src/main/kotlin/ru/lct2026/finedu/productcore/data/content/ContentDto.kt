package ru.lct2026.finedu.productcore.data.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ShopItemDto(
    val id: String,
    val title: String,
    val price: Int,
    val bag: String,
    val shortageLine: String? = null
)

@Serializable
internal data class GoalDto(val id: String, val title: String, val price: Int)

/** Задание; тип — поле `type`: `choice`, `scam` или `action`. */
@Serializable
internal sealed interface QuestDto {
    val id: String
    val theme: String
    val level: String
    val title: String
    val reward: Int

    @Serializable
    @SerialName("choice")
    data class Choice(
        override val id: String,
        override val theme: String,
        override val level: String,
        override val title: String,
        override val reward: Int,
        val situation: String,
        val options: List<QuestOptionDto>,
        val explanation: String
    ) : QuestDto

    @Serializable
    @SerialName("scam")
    data class Scam(
        override val id: String,
        override val theme: String,
        override val level: String,
        override val title: String,
        override val reward: Int,
        val messages: List<ScamMessageDto>,
        val rules: List<String>
    ) : QuestDto

    @Serializable
    @SerialName("action")
    data class Action(
        override val id: String,
        override val theme: String,
        override val level: String,
        override val title: String,
        override val reward: Int,
        val trigger: String
    ) : QuestDto
}

@Serializable
internal data class QuestOptionDto(
    val id: String,
    val text: String,
    val reaction: String,
    val statChanges: Map<String, Int> = emptyMap()
)

@Serializable
internal data class ScamMessageDto(
    val app: String,
    val sender: String,
    val text: String,
    val signs: List<ScamSignDto>,
    val tip: String
)

@Serializable
internal data class ScamSignDto(val name: String, val why: String)

@Serializable
internal data class WeekStoryDto(val number: Int, val title: String, val tagline: String)

@Serializable
internal data class GlossaryTermDto(val id: String, val term: String, val definition: String)

@Serializable
internal data class LearnDto(val shorts: ShortsStoryDto, val feed: List<FeedCardDto>)

@Serializable
internal data class ShortsStoryDto(val title: String, val frames: List<ShortsFrameDto>)

@Serializable
internal data class ShortsFrameDto(val kicker: String, val headline: String, val subtitle: String)

@Serializable
internal data class FeedCardDto(val title: String, val hook: String)

@Serializable
internal data class ParentDto(
    val articles: List<ArticleDto>,
    val scamSchemes: List<ScamSchemeDto>,
    val talkQuestions: List<String>
)

@Serializable
internal data class ArticleDto(
    val id: String,
    val rubric: String,
    val title: String,
    val minutes: Int,
    val paragraphs: List<String> = emptyList(),
    val tips: List<String> = emptyList()
)

@Serializable
internal data class ScamSchemeDto(val id: String, val title: String, val text: String)

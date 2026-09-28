package ru.lct2026.finedu.productcore.data.mapper

import ru.lct2026.finedu.productcore.data.content.ArticleDto
import ru.lct2026.finedu.productcore.data.content.FeedCardDto
import ru.lct2026.finedu.productcore.data.content.GlossaryTermDto
import ru.lct2026.finedu.productcore.data.content.GoalDto
import ru.lct2026.finedu.productcore.data.content.QuestDto
import ru.lct2026.finedu.productcore.data.content.QuestOptionDto
import ru.lct2026.finedu.productcore.data.content.ScamMessageDto
import ru.lct2026.finedu.productcore.data.content.ScamSchemeDto
import ru.lct2026.finedu.productcore.data.content.ShopItemDto
import ru.lct2026.finedu.productcore.data.content.ShortsStoryDto
import ru.lct2026.finedu.productcore.data.content.WeekStoryDto
import ru.lct2026.finedu.productcore.domain.model.Article
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.FeedCard
import ru.lct2026.finedu.productcore.domain.model.GlossaryTerm
import ru.lct2026.finedu.productcore.domain.model.Goal
import ru.lct2026.finedu.productcore.domain.model.PetStat
import ru.lct2026.finedu.productcore.domain.model.Quest
import ru.lct2026.finedu.productcore.domain.model.QuestOption
import ru.lct2026.finedu.productcore.domain.model.ScamMessage
import ru.lct2026.finedu.productcore.domain.model.ScamScheme
import ru.lct2026.finedu.productcore.domain.model.ScamSign
import ru.lct2026.finedu.productcore.domain.model.ShopItem
import ru.lct2026.finedu.productcore.domain.model.ShortsFrame
import ru.lct2026.finedu.productcore.domain.model.ShortsStory
import ru.lct2026.finedu.productcore.domain.model.WeekStory

internal fun ShopItemDto.toDomain() = ShopItem(id, title, Dzynki(price), bag.toEnum(), shortageLine)

internal fun GoalDto.toDomain() = Goal(id, title, Dzynki(price))

internal fun QuestDto.toDomain(): Quest = when (this) {
    is QuestDto.Choice -> Quest.Choice(
        id = id,
        theme = theme.toEnum(),
        level = level.toEnum(),
        title = title,
        reward = Dzynki(reward),
        situation = situation,
        options = options.map { it.toDomain() },
        explanation = explanation
    )

    is QuestDto.Scam -> Quest.Scam(
        id = id,
        theme = theme.toEnum(),
        level = level.toEnum(),
        title = title,
        reward = Dzynki(reward),
        messages = messages.map { it.toDomain() },
        rules = rules
    )

    is QuestDto.Action -> Quest.Action(
        id = id,
        theme = theme.toEnum(),
        level = level.toEnum(),
        title = title,
        reward = Dzynki(reward),
        trigger = trigger.toEnum()
    )
}

private fun QuestOptionDto.toDomain() =
    QuestOption(id, text, reaction, statChanges.entries.associate { (stat, delta) -> stat.toEnum<PetStat>() to delta })

private fun ScamMessageDto.toDomain() = ScamMessage(app, sender, text, signs.map { ScamSign(it.name, it.why) }, tip)

internal fun WeekStoryDto.toDomain() = WeekStory(number, title, tagline)

internal fun GlossaryTermDto.toDomain() = GlossaryTerm(term, definition)

internal fun ShortsStoryDto.toDomain() =
    ShortsStory(title, frames.map { ShortsFrame(it.kicker, it.headline, it.subtitle) })

internal fun FeedCardDto.toDomain() = FeedCard(title, hook)

internal fun ArticleDto.toDomain() = Article(id, rubric, title, minutes, paragraphs, tips)

internal fun ScamSchemeDto.toDomain() = ScamScheme(id, title, text)

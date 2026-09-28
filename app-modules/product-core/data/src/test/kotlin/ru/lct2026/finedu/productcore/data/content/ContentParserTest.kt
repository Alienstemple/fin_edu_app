package ru.lct2026.finedu.productcore.data.content

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.lct2026.finedu.productcore.data.TestGame
import ru.lct2026.finedu.productcore.domain.model.Bag
import ru.lct2026.finedu.productcore.domain.model.GameContent
import ru.lct2026.finedu.productcore.domain.model.GameRules
import ru.lct2026.finedu.productcore.domain.model.Quest
import ru.lct2026.finedu.productcore.domain.model.QuestTheme
import ru.lct2026.finedu.productcore.domain.model.QuestTrigger

/** Разбирает настоящие файлы `assets/content`: опечатка в JSON упадёт здесь, а не у ребёнка на телефоне. */
class ContentParserTest {

    private val content: GameContent = ContentParser(TestGame.json).parse { fileName ->
        File("src/main/assets/${ContentParser.DIRECTORY}/$fileName").readText()
    }

    @Test
    fun `в магазине по 8 товаров в «Нужном» и «Хочу» с уникальными id`() {
        assertEquals(8, content.shopItems.count { it.bag == Bag.NEEDS })
        assertEquals(8, content.shopItems.count { it.bag == Bag.WANTS })
        assertUnique(content.shopItems.map { it.id })
    }

    @Test
    fun `цели для уголка идут по возрастанию цены`() {
        assertEquals(listOf("plaid", "lamp", "window", "scooter"), content.goals.map { it.id })
        assertEquals(content.goals.sortedBy { it.price }, content.goals)
    }

    @Test
    fun `шесть заданий — по два на каждую тему`() {
        assertEquals(6, content.quests.size)
        assertUnique(content.quests.map { it.id })
        QuestTheme.entries.forEach { theme -> assertEquals(2, content.quests.count { it.theme == theme }) }
    }

    @Test
    fun `у каждого задания с выбором есть варианты и разбор, у «Это развод?» — три сообщения`() {
        content.quests.forEach { quest ->
            when (quest) {
                is Quest.Choice -> {
                    assertTrue(quest.id, quest.options.size >= 2)
                    assertTrue(quest.id, quest.explanation.isNotBlank())
                }

                is Quest.Scam -> {
                    assertEquals(3, quest.messages.size)
                    assertTrue(quest.messages.all { it.signs.isNotEmpty() })
                    assertEquals(3, quest.rules.size)
                }

                is Quest.Action -> Unit
            }
        }
    }

    @Test
    fun `каждое действие засчитывает ровно одно задание`() {
        val triggers = content.quests.mapNotNull { quest ->
            when (quest) {
                is Quest.Action -> quest.trigger
                is Quest.Choice, is Quest.Scam -> null
            }
        }
        assertEquals(QuestTrigger.entries.toSet(), triggers.toSet())
        assertUnique(triggers)
    }

    @Test
    fun `сюжет на все недели демо-режима`() {
        assertEquals((1..GameRules.DEMO_WEEKS).toList(), content.weeks.map { it.number })
    }

    @Test
    fun `словарик, шортс, лента и материалы для взрослого на месте`() {
        assertEquals(11, content.glossary.size)
        assertEquals(4, content.shorts.frames.size)
        assertEquals(4, content.feed.size)
        assertEquals(3, content.articles.size)
        assertEquals(1, content.articles.count { it.paragraphs.isNotEmpty() })
        assertEquals(5, content.scamSchemes.size)
        assertEquals(3, content.talkQuestions.size)
    }

    private fun <T> assertUnique(values: List<T>) = assertEquals(values.distinct(), values)
}

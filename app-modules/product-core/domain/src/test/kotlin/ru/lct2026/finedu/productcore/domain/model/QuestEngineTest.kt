package ru.lct2026.finedu.productcore.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.lct2026.finedu.productcore.domain.model.Fixtures.start

class QuestEngineTest {

    private val ad = Quest.Choice(
        id = "ad",
        theme = QuestTheme.PURCHASES,
        level = QuestLevel.EASY,
        title = "«Никто: / Реклама:»",
        reward = Dzynki(20),
        situation = "ТОЛЬКО СЕГОДНЯ! Шляпа за 100",
        options = listOf(
            QuestOption("buy", "Купить", "Она подождёт", mapOf(PetStat.VIBE to 10)),
            QuestOption("later", "Отложить до завтра", "Завтра таймер снова 10 минут", mapOf(PetStat.CALM to 10))
        ),
        explanation = "Искусственная срочность"
    )

    private val scam = Quest.Scam(
        id = "scam",
        theme = QuestTheme.SAVINGS,
        level = QuestLevel.MEDIUM,
        title = "Это развод?",
        reward = Dzynki(30),
        messages = emptyList(),
        rules = emptyList()
    )

    private val plan = Quest.Action(
        id = "plan",
        theme = QuestTheme.BUDGET,
        level = QuestLevel.EASY,
        title = "Три мешочка — один план",
        reward = Dzynki(20),
        trigger = QuestTrigger.PLAN_FIXED
    )

    @Test
    fun `выбор меняет показатели по варианту, а награда уходит в неразложенное`() {
        val outcome = QuestEngine.choose(start, ad, "later")

        assertEquals(Dzynki(20), outcome.reward)
        assertEquals(mapOf(PetStat.CALM to 10), outcome.statChanges)
        assertEquals(Dzynki(320), outcome.state.unallocated)
        assertEquals(setOf("ad"), outcome.state.completedQuestIds)
        assertEquals(setOf("ad"), outcome.state.period.completedQuestIds)
        assertEquals(LedgerEntry(1, IncomeSource.QUEST_REWARD, Dzynki(20), "ad"), outcome.state.ledger.last())
    }

    @Test
    fun `повторное прохождение не даёт награды`() {
        val once = QuestEngine.complete(start, scam).state

        val again = QuestEngine.complete(once, scam)

        assertEquals(Dzynki.ZERO, again.reward)
        assertEquals(once.unallocated, again.state.unallocated)
        assertEquals(once.ledger, again.state.ledger)
    }

    @Test
    fun `задание засчитывается нужным действием, пока не пройдено`() {
        val quests = listOf(ad, scam, plan)

        assertEquals(plan, QuestEngine.questFor(start, quests, QuestTrigger.PLAN_FIXED))
        assertNull(QuestEngine.questFor(start, quests, QuestTrigger.SHORTS_WATCHED))

        val done = QuestEngine.complete(start, plan).state
        assertNull(QuestEngine.questFor(done, quests, QuestTrigger.PLAN_FIXED))
    }

    @Test
    fun `пройденное задание засчитывается в опыт недели`() {
        val done = QuestEngine.complete(start, scam).state

        assertEquals(listOf(XpReason.QUEST_DONE), GameEngine.closePeriod(done).result.xpReasons)
    }
}

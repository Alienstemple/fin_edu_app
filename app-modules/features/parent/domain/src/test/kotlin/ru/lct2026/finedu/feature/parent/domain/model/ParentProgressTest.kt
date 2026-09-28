package ru.lct2026.finedu.feature.parent.domain.model

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.lct2026.finedu.productcore.domain.model.BagAmounts
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.PeriodResult
import ru.lct2026.finedu.productcore.domain.model.PetStage
import ru.lct2026.finedu.productcore.domain.model.Quest
import ru.lct2026.finedu.productcore.domain.model.QuestLevel
import ru.lct2026.finedu.productcore.domain.model.QuestTheme
import ru.lct2026.finedu.productcore.domain.model.QuestTrigger

class ParentProgressTest {

    private val quests = listOf(
        action("plan", QuestTheme.BUDGET),
        action("shorts", QuestTheme.BUDGET),
        action("scooter", QuestTheme.SAVINGS)
    )

    @Test
    fun `тема пройдена, когда пройдены все её задания`() {
        val status = ParentProgress.themeStatus(QuestTheme.BUDGET, quests, setOf("plan", "shorts"))

        assertEquals(ThemeStatus.DONE, status)
    }

    @Test
    fun `тема в процессе, когда пройдена часть заданий`() {
        val status = ParentProgress.themeStatus(QuestTheme.BUDGET, quests, setOf("plan", "scooter"))

        assertEquals(ThemeStatus.IN_PROGRESS, status)
    }

    @Test
    fun `тема впереди, когда задания не пройдены или их нет`() {
        assertEquals(ThemeStatus.AHEAD, ParentProgress.themeStatus(QuestTheme.SAVINGS, quests, setOf("plan")))
        assertEquals(ThemeStatus.AHEAD, ParentProgress.themeStatus(QuestTheme.PURCHASES, quests, setOf("plan")))
    }

    @Test
    fun `графика нет, пока не закрыта ни одна неделя`() {
        assertNull(ParentProgress.savingsChart(emptyList(), totalWeeks = 5))
    }

    @Test
    fun `копилка по неделям накопительная, прогноз — по среднему до конца сюжета`() {
        val history = listOf(week(1, saved = 30, leftover = 30), week(2, saved = 40, leftover = 20))

        val chart = ParentProgress.savingsChart(history, totalWeeks = 5)

        assertEquals(listOf(Dzynki(60), Dzynki(120)), chart?.weekly)
        assertEquals(listOf(Dzynki(180), Dzynki(240), Dzynki(300)), chart?.forecast)
        assertEquals(Dzynki(60), chart?.averagePerWeek)
    }

    @Test
    fun `после конца сюжета прогноз всё равно на неделю вперёд`() {
        val history = (1..5).map { week(it, saved = 10, leftover = 0) }

        val chart = ParentProgress.savingsChart(history, totalWeeks = 5)

        assertEquals(listOf(Dzynki(60)), chart?.forecast)
    }

    @Test
    fun `барьер принимает только произведение множителей`() {
        val gate = ParentGate(7, 8)

        assertTrue(gate.isCorrect("56"))
        assertFalse(gate.isCorrect("54"))
        assertFalse(gate.isCorrect(""))
    }

    @Test
    fun `случайный пример — из однозначных множителей от 3 до 9`() {
        val random = Random(seed = 42)

        repeat(100) {
            val gate = ParentGate.random(random)
            assertTrue(gate.left in 3..9 && gate.right in 3..9)
        }
    }

    private fun action(id: String, theme: QuestTheme): Quest =
        Quest.Action(id, theme, QuestLevel.EASY, id, Dzynki(10), QuestTrigger.PLAN_FIXED)

    private fun week(number: Int, saved: Int, leftover: Int) = PeriodResult(
        number = number,
        income = Dzynki(300),
        plan = null,
        actual = BagAmounts(savings = Dzynki(saved)),
        leftoverToSavings = Dzynki(leftover),
        withdrawn = Dzynki.ZERO,
        xpReasons = emptyList(),
        hasEarnedStar = false,
        stageBefore = PetStage.BABY,
        stageAfter = PetStage.BABY
    )
}

package ru.lct2026.finedu.productcore.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.lct2026.finedu.productcore.domain.model.Fixtures.lamp
import ru.lct2026.finedu.productcore.domain.model.Fixtures.planned
import ru.lct2026.finedu.productcore.domain.model.Fixtures.scooter
import ru.lct2026.finedu.productcore.domain.model.Fixtures.start
import ru.lct2026.finedu.productcore.domain.model.Fixtures.success
import ru.lct2026.finedu.productcore.domain.model.Fixtures.window

class SavingsEngineTest {

    @Test
    fun `пополнение берёт из остатка «Хочу» и добавляет спокойствия`() {
        val result = SavingsEngine.deposit(planned, Dzynki(30)).success()

        assertEquals(Dzynki(90), result.state.wantsLeft)
        assertEquals(Dzynki(60), result.state.savings)
        assertEquals(Dzynki(60), result.state.period.actual.savings)
        assertEquals(
            Feedback(
                reason = FeedbackReason.DEPOSITED,
                balance = Change(Dzynki(270), Dzynki(240)),
                bag = Bag.SAVINGS,
                bagChange = Change(Dzynki(30), Dzynki(60)),
                statChange = StatChange(PetStat.CALM, 60, 70)
            ),
            result.feedback
        )
    }

    @Test
    fun `пополнение больше остатка «Хочу» не проходит`() {
        assertEquals(GameResult.NotEnoughMoney(Dzynki(30)), SavingsEngine.deposit(planned, Dzynki(150)))
    }

    @Test
    fun `снятие возвращает в «Хочу» и не трогает показатели`() {
        val result = SavingsEngine.withdraw(planned, Dzynki(20)).success()

        assertEquals(Dzynki(10), result.state.savings)
        assertEquals(Dzynki(140), result.state.wantsLeft)
        assertEquals(Dzynki(20), result.state.period.withdrawn)
        assertEquals(planned.pet, result.state.pet)
        assertEquals(StatChange(PetStat.CALM, 60, 60), result.feedback.statChange)
    }

    @Test
    fun `снять больше, чем в копилке, нельзя`() {
        assertEquals(GameResult.NotEnoughMoney(Dzynki(20)), SavingsEngine.withdraw(planned, Dzynki(50)))
        assertNull(SavingsEngine.previewWithdraw(planned, Dzynki(50), scooter))
    }

    @Test
    fun `срок цели — остаток, делённый на среднее прибавление, с округлением вверх`() {
        assertEquals(9, SavingsEngine.weeksToGoal(planned, scooter))
    }

    @Test
    fun `срок цели учитывает остатки, ушедшие в копилку в прошлые недели`() {
        val week2 = GameEngine.closePeriod(planned).state
        val bike = Goal("bike", "Велосипед", Dzynki(600))

        assertEquals(2, SavingsEngine.weeksToGoal(week2, bike))
    }

    @Test
    fun `срок цели не считается без прибавлений и равен нулю, когда накоплено`() {
        assertNull(SavingsEngine.weeksToGoal(start, scooter))
        assertEquals(0, SavingsEngine.weeksToGoal(start.copy(savings = Dzynki(300)), scooter))
    }

    @Test
    fun `превью снятия показывает сумму и срок было → станет`() {
        val preview = SavingsEngine.previewWithdraw(planned, Dzynki(10), scooter)

        assertEquals(WithdrawPreview(Dzynki(30), Dzynki(20), weeksBefore = 9, weeksAfter = 10), preview)
    }

    @Test
    fun `текущая цель — первая, ещё не поставленная в уголок`() {
        val goals = listOf(lamp, window, scooter)

        assertEquals(lamp, SavingsEngine.currentGoal(start, goals))
        assertEquals(window, SavingsEngine.currentGoal(start.copy(placedGoalIds = setOf("lamp")), goals))
        assertNull(SavingsEngine.currentGoal(start.copy(placedGoalIds = goals.map { it.id }.toSet()), goals))
    }

    @Test
    fun `накопленная цель встаёт в уголок навсегда`() {
        val result = SavingsEngine.placeGoal(start.copy(savings = Dzynki(120)), lamp).success()

        assertEquals(Dzynki(20), result.state.savings)
        assertEquals(setOf("lamp"), result.state.placedGoalIds)
        assertEquals(FeedbackReason.GOAL_PLACED, result.feedback.reason)
    }

    @Test
    fun `ненакопленную цель поставить нельзя`() {
        assertEquals(GameResult.NotEnoughMoney(Dzynki(70)), SavingsEngine.placeGoal(planned, lamp))
    }
}

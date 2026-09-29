package ru.lct2026.finedu.productcore.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.lct2026.finedu.productcore.domain.model.Fixtures.groceries
import ru.lct2026.finedu.productcore.domain.model.Fixtures.hat
import ru.lct2026.finedu.productcore.domain.model.Fixtures.kasha
import ru.lct2026.finedu.productcore.domain.model.Fixtures.planned
import ru.lct2026.finedu.productcore.domain.model.Fixtures.start
import ru.lct2026.finedu.productcore.domain.model.Fixtures.success
import ru.lct2026.finedu.productcore.domain.model.Fixtures.weekPlan

class GameEngineTest {

    @Test
    fun `новая игра начинается с неразложенного дохода недели и записью об источнике`() {
        assertEquals(GameRules.WEEKLY_INCOME, start.unallocated)
        assertEquals(Dzynki.ZERO, start.balance)
        assertEquals(listOf(LedgerEntry(1, IncomeSource.POCKET_MONEY, GameRules.WEEKLY_INCOME)), start.ledger)
    }

    @Test
    fun `раскладка до нуля наполняет мешочки и сразу пополняет копилку`() {
        assertEquals(Dzynki.ZERO, planned.unallocated)
        assertEquals(Dzynki(150), planned.needsLeft)
        assertEquals(Dzynki(120), planned.wantsLeft)
        assertEquals(Dzynki(30), planned.savings)
        assertEquals(Dzynki(270), planned.balance)
        assertEquals(weekPlan, planned.period.plan)
        assertEquals(Dzynki(30), planned.period.actual.savings)
    }

    @Test
    fun `раскладка не до нуля не проходит и показывает остаток или превышение`() {
        assertEquals(
            PlanResult.NotBalanced(20),
            GameEngine.distribute(start, BagAmounts(Dzynki(150), Dzynki(100), Dzynki(30)))
        )
        assertEquals(
            PlanResult.NotBalanced(-50),
            GameEngine.distribute(start, BagAmounts(Dzynki(200), Dzynki(120), Dzynki(30)))
        )
    }

    @Test
    fun `раскладка по умолчанию — половина на Нужное, десятая в копилку, остаток в Хочу`() {
        assertEquals(BagAmounts(Dzynki(150), Dzynki(120), Dzynki(30)), GameEngine.suggestPlan(Dzynki(300)))
    }

    @Test
    fun `раскладка по умолчанию кратна 10 и всегда сходится до нуля`() {
        listOf(0, 10, 20, 50, 70, 320, 350).forEach { amount ->
            val plan = GameEngine.suggestPlan(Dzynki(amount))
            assertEquals(Dzynki(amount), plan.total)
            assertTrue(listOf(plan.needs, plan.wants, plan.savings).all { it.amount % GameRules.AMOUNT_STEP == 0 })
        }
        assertEquals(BagAmounts(Dzynki(10), Dzynki(10)), GameEngine.suggestPlan(Dzynki(20)))
    }

    @Test
    fun `бонус взрослого раскладывается повторно и добавляется к плану`() {
        val bonus = GameEngine.grantParentBonus(planned, Dzynki(50), "Помог с уборкой")

        val state = GameEngine.distribute(bonus, BagAmounts(wants = Dzynki(50))).success()

        assertEquals(Dzynki(170), state.wantsLeft)
        assertEquals(Dzynki(170), state.period.plan?.wants)
        assertEquals(
            LedgerEntry(1, IncomeSource.PARENT_BONUS, Dzynki(50), "Помог с уборкой"),
            bonus.ledger.last()
        )
    }

    @Test
    fun `покупка списывает из остатка мешочка, пишет факт и поднимает показатель`() {
        val result = GameEngine.buy(planned, kasha).success()

        assertEquals(Dzynki(130), result.state.needsLeft)
        assertEquals(Dzynki(20), result.state.period.actual.needs)
        assertEquals(
            Feedback(
                reason = FeedbackReason.BOUGHT_NEED,
                balance = Change(Dzynki(270), Dzynki(250)),
                bag = Bag.NEEDS,
                bagChange = Change(Dzynki(150), Dzynki(130)),
                statChange = StatChange(PetStat.CHARGE, 60, 70)
            ),
            result.feedback
        )
    }

    @Test
    fun `покупка желаемого поднимает вайб`() {
        val result = GameEngine.buy(planned, hat).success()

        assertEquals(FeedbackReason.BOUGHT_WANT, result.feedback.reason)
        assertEquals(Dzynki(20), result.state.wantsLeft)
        assertEquals(70, result.state.pet.vibe)
    }

    @Test
    fun `нехватка считается по остатку мешочка, а не по всему балансу`() {
        val state = planned.copy(wantsLeft = Dzynki(60))

        assertEquals(GameResult.NotEnoughMoney(Dzynki(40)), GameEngine.buy(state, hat))
    }

    @Test
    fun `«Подожду» ничего не списывает и добавляет спокойствия`() {
        val result = GameEngine.pause(planned, hat)

        assertEquals(planned.wantsLeft, result.state.wantsLeft)
        assertEquals(Change(Dzynki(270), Dzynki(270)), result.feedback.balance)
        assertEquals(StatChange(PetStat.CALM, 60, 70), result.feedback.statChange)
    }

    @Test
    fun `неделя в рамках плана — опыт, звёздочка и остатки в копилку`() {
        val week = GameEngine.buy(planned, groceries).success().state

        val closing = GameEngine.closePeriod(week)

        assertEquals(listOf(XpReason.PLANNED, XpReason.NEEDS_CLOSED, XpReason.SAVED), closing.result.xpReasons)
        assertTrue(closing.result.hasEarnedStar)
        assertEquals(Dzynki(120), closing.result.leftoverToSavings)
        assertEquals(Dzynki(150), closing.result.savedTotal)
        assertEquals(Dzynki(150), closing.state.savings)
        assertEquals(1, closing.state.stars)
    }

    @Test
    fun `снятие из копилки лишает звёздочки, но не опыта`() {
        val week = GameEngine.buy(planned, groceries).success().state
        val withdrew = SavingsEngine.withdraw(week, Dzynki(10)).success().state

        val closing = GameEngine.closePeriod(withdrew)

        assertFalse(closing.result.hasEarnedStar)
        assertTrue(closing.result.isNeedsClosed)
        assertEquals(0, closing.state.stars)
    }

    @Test
    fun `неделя без раскладки не даёт опыта, неразложенное уходит в копилку`() {
        val closing = GameEngine.closePeriod(start)

        assertEquals(0, closing.result.xpGained)
        assertEquals(Dzynki(300), closing.state.savings)
        assertFalse(closing.result.isStageChanged)
    }

    @Test
    fun `закрытие недели обнуляет мешочки, начисляет доход и снижает показатели`() {
        val closing = GameEngine.closePeriod(planned)

        assertEquals(GameRules.WEEKLY_INCOME, closing.state.unallocated)
        assertEquals(Dzynki.ZERO, closing.state.balance)
        assertEquals(Period(number = 2, income = GameRules.WEEKLY_INCOME), closing.state.period)
        assertEquals(Pet(charge = 40, vibe = 40, calm = 40, xp = 2), closing.state.pet)
        assertEquals(listOf(closing.result), closing.state.history)
        assertEquals(LedgerEntry(2, IncomeSource.POCKET_MONEY, GameRules.WEEKLY_INCOME), closing.state.ledger.last())
    }

    @Test
    fun `показатели не опускаются ниже границы «устал»`() {
        val tired = GameEngine.closePeriod(start.copy(pet = Pet(charge = 15))).state.pet

        assertEquals(GameRules.STAT_MIN, tired.charge)
        assertTrue(tired.isTired(PetStat.CHARGE))
    }

    @Test
    fun `пять разумных недель подряд — Мастер мешочка и пять звёздочек`() {
        var state = start
        repeat(GameRules.DEMO_WEEKS) { state = GameEngine.closePeriod(sensibleWeek(state)).state }

        assertEquals(6, state.period.number)
        assertEquals(List(GameRules.DEMO_WEEKS) { 3 }, state.history.map { it.xpGained })
        assertEquals(PetStage.MASTER, state.pet.stage)
        assertEquals(listOf(2, 4), state.history.filter { it.isStageChanged }.map { it.number })
        assertEquals(GameRules.DEMO_WEEKS, state.stars)
    }

    @Test
    fun `опыт, звёздочки и предметы уголка не отнимаются после пустой недели`() {
        val grown = start.copy(pet = Pet(xp = 4), stars = 2, placedGoalIds = setOf("lamp"))

        val state = GameEngine.closePeriod(grown).state

        assertEquals(PetStage.SPRY, state.pet.stage)
        assertEquals(2, state.stars)
        assertEquals(setOf("lamp"), state.placedGoalIds)
    }

    @Test
    fun `после суток паузы показатели подтягиваются`() {
        val low = start.copy(pet = Pet(charge = 20, vibe = 30, calm = 70))

        val visit = GameEngine.welcomeBack(low, GameRules.RETURN_AFTER_MILLIS)

        assertTrue(visit.isReturn)
        assertEquals(Pet(charge = 50, vibe = 50, calm = 70), visit.state.pet)
        assertEquals(GameRules.RETURN_AFTER_MILLIS, visit.state.lastVisitMillis)
    }

    @Test
    fun `вход без паузы ничего не меняет, кроме времени визита`() {
        val low = start.copy(pet = Pet(charge = 20))

        val visit = GameEngine.welcomeBack(low, 1_000L)

        assertFalse(visit.isReturn)
        assertEquals(low.copy(lastVisitMillis = 1_000L), visit.state)
    }

    @Test
    fun `сброс — новая игра с тем же профилем`() {
        val played = GameEngine.closePeriod(sensibleWeek(start)).state

        assertEquals(start, GameEngine.newGame(played.profile, nowMillis = 0L))
    }

    /** Раскладка, закрытое «Нужное» и копилка: +3 опыта и звёздочка. */
    private fun sensibleWeek(state: GameState): GameState {
        val week = GameEngine.distribute(state, weekPlan).success()
        return GameEngine.buy(week, groceries).success().state
    }
}

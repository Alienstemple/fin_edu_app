package ru.lct2026.finedu.productcore.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    private val profile = Profile("Аня", "Дзынь", PetLook(PetFur.MINT, PetHat.CAP), isDemo = true)
    private val start = GameEngine.newGame(profile)

    private val food = ShopItem("food", "Корм", Dzynki(100), Bag.NEEDS, statBoost = 20)
    private val hat = ShopItem("hat", "Шляпа", Dzynki(150), Bag.WANTS, statBoost = 30)
    private val scooter = Goal("scooter", "Самокат", Dzynki(300))

    private val choice = Quest.Choice(
        id = "ad",
        theme = QuestTheme.PAYMENTS,
        week = 2,
        title = "Реклама",
        situation = "КУПИ СЕЙЧАС",
        reward = Dzynki(30),
        options = listOf(
            QuestOption("buy", "Купить", "Поспешил", Dzynki(40), Bag.WANTS, mapOf(PetStat.VIBE to 10)),
            QuestOption("skip", "Не покупать", "Подумал", statChanges = mapOf(PetStat.CALM to 5)),
            QuestOption("piggy", "Взять из копилки", "Подушка", Dzynki(50), Bag.SAVINGS)
        )
    )

    private val basket = Quest.Basket(
        id = "list",
        theme = QuestTheme.BUDGET_PLANNING,
        week = 1,
        title = "Список покупок",
        situation = "Собери корзину",
        reward = Dzynki(20),
        budget = Dzynki(100),
        items = listOf(
            BasketItem("bread", "Хлеб", Dzynki(40), isNeeded = true),
            BasketItem("milk", "Молоко", Dzynki(50), isNeeded = true),
            BasketItem("candy", "Конфеты", Dzynki(30), isNeeded = false)
        )
    )

    @Test
    fun `новая игра начинается с дохода недели в кошельке и записью об источнике`() {
        assertEquals(GameRules.WEEKLY_INCOME, start.balance)
        assertEquals(Dzynki.ZERO, start.savings)
        assertEquals(1, start.period.number)
        assertEquals(listOf(LedgerEntry(1, IncomeSource.POCKET_MONEY, GameRules.WEEKLY_INCOME)), start.ledger)
    }

    @Test
    fun `план в пределах дохода подтверждается`() {
        val plan = BagAmounts(Dzynki(200), Dzynki(100), Dzynki(100))

        val result = GameEngine.confirmPlan(start, plan)

        assertEquals(plan, result.confirmed().state.period.plan)
    }

    @Test
    fun `план больше дохода не подтверждается и показывает превышение`() {
        val result = GameEngine.confirmPlan(start, BagAmounts(Dzynki(300), Dzynki(200), Dzynki(50)))

        assertEquals(PlanResult.ExceedsIncome(Dzynki(50)), result)
    }

    @Test
    fun `подтверждённый план не меняется`() {
        val planned = confirmed(BagAmounts(Dzynki(100)))

        assertEquals(PlanResult.AlreadyConfirmed, GameEngine.confirmPlan(planned, BagAmounts()))
    }

    @Test
    fun `покупка списывает с кошелька, пишет факт в мешочек и поднимает показатель`() {
        val result = GameEngine.buy(start, food).success()

        assertEquals(Dzynki(400), result.state.balance)
        assertEquals(Dzynki(100), result.state.period.actual.needs)
        assertEquals(80, result.state.pet.charge)
        assertEquals(
            Feedback(FeedbackReason.BOUGHT_NEED, balanceDelta = -100, statChanges = mapOf(PetStat.CHARGE to 20)),
            result.feedback
        )
    }

    @Test
    fun `покупка желаемого поднимает вайб и не выходит за максимум`() {
        val result = GameEngine.buy(start.copy(pet = Pet(vibe = 90)), hat).success()

        assertEquals(FeedbackReason.BOUGHT_WANT, result.feedback.reason)
        assertEquals(GameRules.STAT_MAX, result.state.pet.vibe)
        assertEquals(mapOf(PetStat.VIBE to 10), result.feedback.statChanges)
    }

    @Test
    fun `при нехватке покупка не проходит и показывает сколько не хватает`() {
        val poor = start.copy(balance = Dzynki(110))

        assertEquals(GameResult.NotEnoughMoney(Dzynki(40)), GameEngine.buy(poor, hat))
    }

    @Test
    fun `пополнение копилки переносит из кошелька и поднимает спокойствие`() {
        val result = GameEngine.deposit(start, Dzynki(50)).success()

        assertEquals(Dzynki(450), result.state.balance)
        assertEquals(Dzynki(50), result.state.savings)
        assertEquals(Dzynki(50), result.state.period.actual.savings)
        assertEquals(70, result.state.pet.calm)
    }

    @Test
    fun `пополнение больше кошелька не проходит`() {
        assertEquals(GameResult.NotEnoughMoney(Dzynki(100)), GameEngine.deposit(start, Dzynki(600)))
    }

    @Test
    fun `снятие из копилки возвращает в кошелёк и снижает спокойствие`() {
        val saved = deposited(Dzynki(200))

        val result = GameEngine.withdraw(saved, Dzynki(100)).success()

        assertEquals(Dzynki(100), result.state.savings)
        assertEquals(Dzynki(400), result.state.balance)
        assertEquals(Dzynki(100), result.state.period.withdrawn)
        assertEquals(mapOf(PetStat.CALM to -20), result.feedback.statChanges)
    }

    @Test
    fun `снять больше, чем в копилке, нельзя`() {
        val saved = deposited(Dzynki(30))

        assertEquals(GameResult.NotEnoughMoney(Dzynki(20)), GameEngine.withdraw(saved, Dzynki(50)))
        assertNull(GameEngine.previewWithdraw(saved, Dzynki(50), scooter))
    }

    @Test
    fun `срок цели — остаток, делённый на среднее пополнение, с округлением вверх`() {
        val saved = deposited(Dzynki(70))

        assertEquals(4, GameEngine.weeksToGoal(saved, scooter))
    }

    @Test
    fun `срок цели не считается без пополнений и равен нулю, когда накоплено`() {
        assertNull(GameEngine.weeksToGoal(start, scooter))
        assertEquals(0, GameEngine.weeksToGoal(start.copy(savings = Dzynki(300)), scooter))
    }

    @Test
    fun `срок цели учитывает пополнения прошлых недель`() {
        val week2 = GameEngine.closePeriod(deposited(Dzynki(100))).state

        assertEquals(4, GameEngine.weeksToGoal(week2, scooter))
    }

    @Test
    fun `превью снятия показывает сумму и срок до и после`() {
        val saved = deposited(Dzynki(100))

        val preview = GameEngine.previewWithdraw(saved, Dzynki(50), scooter)

        assertEquals(WithdrawPreview(Dzynki(100), Dzynki(50), weeksBefore = 2, weeksAfter = 3), preview)
    }

    @Test
    fun `выбор цели запоминается`() {
        assertEquals("scooter", GameEngine.selectGoal(start, scooter).selectedGoalId)
    }

    @Test
    fun `задание с выбором списывает цену варианта, меняет показатель и даёт награду`() {
        val result = QuestEngine.completeChoice(start, choice, "buy").success()

        assertEquals(Dzynki(490), result.state.balance)
        assertEquals(Dzynki(40), result.state.period.actual.wants)
        assertEquals(setOf("ad"), result.state.completedQuestIds)
        assertEquals(setOf("ad"), result.state.period.completedQuestIds)
        assertEquals(LedgerEntry(1, IncomeSource.QUEST_REWARD, Dzynki(30), "ad"), result.state.ledger.last())
        assertEquals(
            Feedback(
                FeedbackReason.QUEST_DONE,
                balanceDelta = -10,
                statChanges = mapOf(PetStat.VIBE to 10),
                reward = Dzynki(30)
            ),
            result.feedback
        )
    }

    @Test
    fun `вариант из копилки списывается снятием`() {
        val result = QuestEngine.completeChoice(deposited(Dzynki(80)), choice, "piggy").success()

        assertEquals(Dzynki(30), result.state.savings)
        assertEquals(Dzynki(50), result.state.period.withdrawn)
        assertEquals(-50, result.feedback.savingsDelta)
    }

    @Test
    fun `вариант дороже копилки не проходит`() {
        val result = QuestEngine.completeChoice(deposited(Dzynki(20)), choice, "piggy")

        assertEquals(GameResult.NotEnoughMoney(Dzynki(30)), result)
    }

    @Test
    fun `вариант дороже кошелька не проходит`() {
        val result = QuestEngine.completeChoice(start.copy(balance = Dzynki(10)), choice, "buy")

        assertEquals(GameResult.NotEnoughMoney(Dzynki(30)), result)
    }

    @Test
    fun `повторное прохождение задания не даёт награду`() {
        val once = QuestEngine.completeChoice(start, choice, "skip").success().state

        val again = QuestEngine.completeChoice(once, choice, "skip").success()

        assertEquals(Dzynki.ZERO, again.feedback.reward)
        assertEquals(once.balance, again.state.balance)
        assertEquals(once.ledger, again.state.ledger)
    }

    @Test
    fun `корзина успешна, если уложился и взял всё нужное`() {
        assertTrue(QuestEngine.checkBasket(basket, setOf("bread", "milk")).isSuccess)
    }

    @Test
    fun `корзина без нужного или дороже бюджета не успешна`() {
        assertEquals(
            BasketCheck(Dzynki(70), fitsBudget = true, hasAllNeeded = false),
            QuestEngine.checkBasket(basket, setOf("bread", "candy"))
        )
        assertEquals(
            BasketCheck(Dzynki(120), fitsBudget = false, hasAllNeeded = true),
            QuestEngine.checkBasket(basket, setOf("bread", "milk", "candy"))
        )
    }

    @Test
    fun `корзина не тратит кошелёк и даёт награду`() {
        val result = QuestEngine.completeBasket(start, basket).success()

        assertEquals(Dzynki(520), result.state.balance)
        assertEquals(Dzynki(20), result.feedback.reward)
    }

    @Test
    fun `разумная неделя даёт опыт по всем четырём причинам`() {
        var state = confirmed(BagAmounts(Dzynki(100), Dzynki(150), Dzynki(50)))
        state = GameEngine.buy(state, food).success().state
        state = GameEngine.buy(state, hat).success().state
        state = GameEngine.deposit(state, Dzynki(50)).success().state
        state = QuestEngine.completeBasket(state, basket).success().state

        val result = GameEngine.closePeriod(state).result

        assertEquals(XpReason.entries, result.xpReasons)
        assertTrue(result.isNeedsClosed)
        assertTrue(result.isWithinPlan)
    }

    @Test
    fun `неделя без плана и трат не даёт опыта`() {
        val result = GameEngine.closePeriod(start).result

        assertEquals(0, result.xpGained)
        assertFalse(result.isStageChanged)
    }

    @Test
    fun `перерасход на желаемое — не в плане, нужное не закрыто`() {
        val state = GameEngine.buy(confirmed(BagAmounts(Dzynki(100), Dzynki(100))), hat).success().state

        val result = GameEngine.closePeriod(state).result

        assertFalse(result.isWithinPlan)
        assertFalse(result.isNeedsClosed)
    }

    @Test
    fun `закрытие недели начисляет доход, снижает показатели и сохраняет итоги`() {
        val closing = GameEngine.closePeriod(start)

        assertEquals(Dzynki(1000), closing.state.balance)
        assertEquals(Period(number = 2, income = GameRules.WEEKLY_INCOME), closing.state.period)
        assertEquals(Pet(charge = 40, vibe = 40, calm = 40), closing.state.pet)
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
    fun `пять разумных недель подряд выращивают до Мастера мешочка`() {
        var state = start
        repeat(GameRules.DEMO_WEEKS) { state = GameEngine.closePeriod(sensibleWeek(state)).state }

        assertEquals(6, state.period.number)
        assertEquals(List(GameRules.DEMO_WEEKS) { 3 }, state.history.map { it.xpGained })
        assertEquals(PetStage.MASTER, state.pet.stage)
        assertEquals(listOf(2, 4), state.history.filter { it.isStageChanged }.map { it.number })
    }

    @Test
    fun `опыт не отнимается и стадия не откатывается после плохой недели`() {
        val grown = start.copy(pet = Pet(xp = 4))

        val closing = GameEngine.closePeriod(grown)

        assertEquals(4, closing.state.pet.xp)
        assertEquals(PetStage.SPRY, closing.result.stageAfter)
    }

    @Test
    fun `бонус взрослого попадает в кошелёк с причиной`() {
        val state = GameEngine.grantParentBonus(start, Dzynki(50), "Помог с уборкой")

        assertEquals(Dzynki(550), state.balance)
        assertEquals(LedgerEntry(1, IncomeSource.PARENT_BONUS, Dzynki(50), "Помог с уборкой"), state.ledger.last())
    }

    @Test
    fun `сброс — новая игра с тем же профилем`() {
        val played = GameEngine.closePeriod(sensibleWeek(start)).state

        assertEquals(start, GameEngine.newGame(played.profile))
    }

    private fun confirmed(plan: BagAmounts): GameState = GameEngine.confirmPlan(start, plan).confirmed().state

    private fun deposited(amount: Dzynki): GameState = GameEngine.deposit(start, amount).success().state

    /** План, закрытое «Нужное» и копилка: +3 опыта. */
    private fun sensibleWeek(state: GameState): GameState {
        var week = GameEngine.confirmPlan(state, BagAmounts(Dzynki(100), Dzynki(100), Dzynki(50))).confirmed().state
        week = GameEngine.buy(week, food).success().state
        return GameEngine.deposit(week, Dzynki(50)).success().state
    }

    private fun GameResult.success(): GameResult.Success = when (this) {
        is GameResult.Success -> this
        is GameResult.NotEnoughMoney -> error("Ожидали успех, а не хватает $missing")
    }

    private fun PlanResult.confirmed(): PlanResult.Success = when (this) {
        is PlanResult.Success -> this
        is PlanResult.ExceedsIncome, PlanResult.AlreadyConfirmed -> error("План не подтверждён: $this")
    }
}

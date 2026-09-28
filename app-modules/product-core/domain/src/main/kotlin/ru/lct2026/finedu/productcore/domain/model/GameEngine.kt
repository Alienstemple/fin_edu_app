package ru.lct2026.finedu.productcore.domain.model

import kotlin.math.ceil

/**
 * Игровая экономика: чистые функции над [GameState]. Числа — в [GameRules].
 */
object GameEngine {

    /** Новая игра: первая неделя, карманные деньги уже в кошельке. Сброс прогресса — тоже новая игра. */
    fun newGame(profile: Profile, income: Dzynki = GameRules.WEEKLY_INCOME): GameState = GameState(
        profile = profile,
        balance = income,
        savings = Dzynki.ZERO,
        selectedGoalId = null,
        period = Period(number = 1, income = income),
        pet = Pet(),
        completedQuestIds = emptySet(),
        history = emptyList(),
        ledger = listOf(LedgerEntry(1, IncomeSource.POCKET_MONEY, income))
    )

    /** Фиксирует план недели. План не больше дохода и после подтверждения не меняется. */
    fun confirmPlan(state: GameState, plan: BagAmounts): PlanResult {
        val income = state.period.income
        return when {
            state.period.plan != null -> PlanResult.AlreadyConfirmed
            plan.total > income -> PlanResult.ExceedsIncome(shortfall(have = income, need = plan.total))
            else -> PlanResult.Success(state.copy(period = state.period.copy(plan = plan)))
        }
    }

    /** Покупка из кошелька в мешочек товара. В минус уйти нельзя. */
    fun buy(state: GameState, item: ShopItem): GameResult {
        val balance = state.balance.minusOrNull(item.price)
            ?: return GameResult.NotEnoughMoney(shortfall(state.balance, item.price))
        val pet = state.pet.change(item.bag.stat, item.statBoost)
        val newState = state.copy(
            balance = balance,
            pet = pet,
            period = state.period.copy(actual = state.period.actual.add(item.bag, item.price))
        )
        val reason = when (item.bag) {
            Bag.NEEDS -> FeedbackReason.BOUGHT_NEED
            Bag.WANTS, Bag.SAVINGS -> FeedbackReason.BOUGHT_WANT
        }
        return GameResult.Success(
            newState,
            Feedback(reason, balanceDelta = -item.price.amount, statChanges = pet.changesSince(state.pet))
        )
    }

    /** Из кошелька в копилку. */
    fun deposit(state: GameState, amount: Dzynki): GameResult {
        require(amount > Dzynki.ZERO) { "Сумма пополнения должна быть больше нуля" }
        val balance = state.balance.minusOrNull(amount)
            ?: return GameResult.NotEnoughMoney(shortfall(state.balance, amount))
        val pet = state.pet.change(PetStat.CALM, amount.amount / GameRules.DZYNKI_PER_CALM_POINT)
        val newState = state.copy(
            balance = balance,
            savings = state.savings + amount,
            pet = pet,
            period = state.period.copy(actual = state.period.actual.add(Bag.SAVINGS, amount))
        )
        return GameResult.Success(
            newState,
            Feedback(
                FeedbackReason.DEPOSITED,
                balanceDelta = -amount.amount,
                savingsDelta = amount.amount,
                statChanges = pet.changesSince(state.pet)
            )
        )
    }

    /** Что будет с копилкой и сроком цели, если снять [amount]. `null`, если в копилке меньше. */
    fun previewWithdraw(state: GameState, amount: Dzynki, goal: Goal?): WithdrawPreview? {
        val after = state.savings.minusOrNull(amount) ?: return null
        return WithdrawPreview(
            savingsBefore = state.savings,
            savingsAfter = after,
            weeksBefore = goal?.let { weeksToGoal(state, it) },
            weeksAfter = goal?.let { weeksToGoal(state.copy(savings = after), it) }
        )
    }

    /** Из копилки в кошелёк. Подтверждение с превью — на стороне UI. */
    fun withdraw(state: GameState, amount: Dzynki): GameResult {
        require(amount > Dzynki.ZERO) { "Сумма снятия должна быть больше нуля" }
        val savings = state.savings.minusOrNull(amount)
            ?: return GameResult.NotEnoughMoney(shortfall(state.savings, amount))
        val pet = state.pet.change(PetStat.CALM, -amount.amount / GameRules.DZYNKI_PER_CALM_POINT)
        val newState = state.copy(
            balance = state.balance + amount,
            savings = savings,
            pet = pet,
            period = state.period.copy(withdrawn = state.period.withdrawn + amount)
        )
        return GameResult.Success(
            newState,
            Feedback(
                FeedbackReason.WITHDREW,
                balanceDelta = amount.amount,
                savingsDelta = -amount.amount,
                statChanges = pet.changesSince(state.pet)
            )
        )
    }

    fun selectGoal(state: GameState, goal: Goal): GameState = state.copy(selectedGoalId = goal.id)

    /**
     * Сколько недель копить до цели (ТЗ 2.5.7): `ceil(осталось / среднее пополнение за неделю)`.
     * 0 — цель уже накоплена, `null` — пополнений ещё не было и срок не посчитать.
     */
    fun weeksToGoal(state: GameState, goal: Goal): Int? {
        val left = goal.price.amount - state.savings.amount
        val deposited = state.history.sumOf { it.actual.savings.amount } + state.period.actual.savings.amount
        val average = deposited.toDouble() / state.period.number
        return when {
            left <= 0 -> 0
            deposited == 0 -> null
            else -> ceil(left / average).toInt()
        }
    }

    /**
     * Закрывает неделю: опыт за разумные решения, итоги «план против факта», показатели снижаются к новой
     * неделе, начисляется доход следующей недели.
     */
    fun closePeriod(state: GameState, nextIncome: Dzynki = GameRules.WEEKLY_INCOME): PeriodClosing {
        val period = state.period
        val plan = period.plan
        val actual = period.actual
        val reasons = buildList {
            if (plan != null && plan.needs > Dzynki.ZERO && actual.needs >= plan.needs) add(XpReason.NEEDS_CLOSED)
            if (actual.savings > Dzynki.ZERO) add(XpReason.SAVED)
            if (plan != null && isWithinPlan(plan, actual)) add(XpReason.WITHIN_PLAN)
            if (period.completedQuestIds.isNotEmpty()) add(XpReason.QUEST_DONE)
        }
        val grown = state.pet.copy(xp = state.pet.xp + reasons.size)
        val result = PeriodResult(
            number = period.number,
            income = period.income,
            plan = plan,
            actual = actual,
            withdrawn = period.withdrawn,
            xpReasons = reasons,
            stageBefore = state.pet.stage,
            stageAfter = grown.stage
        )
        val next = period.number + 1
        val newState = state.copy(
            balance = state.balance + nextIncome,
            period = Period(number = next, income = nextIncome),
            pet = PetStat.entries.fold(grown) { pet, stat -> pet.change(stat, -GameRules.WEEKLY_STAT_DECAY) },
            history = state.history + result,
            ledger = state.ledger + LedgerEntry(next, IncomeSource.POCKET_MONEY, nextIncome)
        )
        return PeriodClosing(newState, result)
    }

    /** Бонус от взрослого с причиной (ТЗ 2.5.4). */
    fun grantParentBonus(state: GameState, amount: Dzynki, reason: String): GameState {
        require(amount > Dzynki.ZERO) { "Бонус должен быть больше нуля" }
        return state.copy(
            balance = state.balance + amount,
            ledger = state.ledger + LedgerEntry(state.period.number, IncomeSource.PARENT_BONUS, amount, reason)
        )
    }

    private fun isWithinPlan(plan: BagAmounts, actual: BagAmounts): Boolean =
        actual.wants <= plan.wants && actual.needs + actual.wants <= plan.needs + plan.wants
}

/** Сколько не хватает: [need] больше [have]. */
internal fun shortfall(have: Dzynki, need: Dzynki): Dzynki = Dzynki(need.amount - have.amount)

package ru.lct2026.finedu.productcore.domain.model

/**
 * Неделя и покупки: доход, раскладка по мешочкам, траты, закрытие недели. Копилка — [SavingsEngine],
 * задания — [QuestEngine]. Числа — в [GameRules].
 */
object GameEngine {

    /** Новая игра: первая неделя, доход ещё не разложен. Сброс прогресса — тоже новая игра. */
    fun newGame(profile: Profile, nowMillis: Long, settings: Settings = Settings()): GameState {
        val income = GameRules.WEEKLY_INCOME
        return GameState(
            profile = profile,
            unallocated = income,
            needsLeft = Dzynki.ZERO,
            wantsLeft = Dzynki.ZERO,
            savings = Dzynki.ZERO,
            placedGoalIds = emptySet(),
            stars = 0,
            period = Period(number = 1, income = income),
            pet = Pet(),
            completedQuestIds = emptySet(),
            history = emptyList(),
            ledger = listOf(LedgerEntry(1, IncomeSource.POCKET_MONEY, income)),
            settings = settings,
            lastVisitMillis = nowMillis
        )
    }

    /**
     * Раскладывает неразложенное по мешочкам — строго до нуля. Копилка пополняется сразу, «Нужное» и «Хочу»
     * становятся остатками на неделю. Повторная раскладка (после награды или бонуса) добавляется к плану.
     */
    fun distribute(state: GameState, plan: BagAmounts): PlanResult {
        val left = state.unallocated.amount - plan.total.amount
        if (left != 0) return PlanResult.NotBalanced(left)
        val period = state.period
        return PlanResult.Success(
            state.copy(
                unallocated = Dzynki.ZERO,
                needsLeft = state.needsLeft + plan.needs,
                wantsLeft = state.wantsLeft + plan.wants,
                savings = state.savings + plan.savings,
                period = period.copy(
                    plan = (period.plan ?: BagAmounts()) + plan,
                    actual = period.actual.add(Bag.SAVINGS, plan.savings)
                )
            )
        )
    }

    /** Покупка из остатка мешочка товара. В минус уйти нельзя. */
    fun buy(state: GameState, item: ShopItem): GameResult {
        val left = state.amountIn(item.bag).minusOrNull(item.price)
            ?: return GameResult.NotEnoughMoney(shortfall(state.amountIn(item.bag), item.price))
        val spent = when (item.bag) {
            Bag.NEEDS -> state.copy(needsLeft = left)
            Bag.WANTS, Bag.SAVINGS -> state.copy(wantsLeft = left)
        }
        val after = spent.copy(
            pet = state.pet.change(item.bag.stat, GameRules.STAT_STEP),
            period = state.period.copy(actual = state.period.actual.add(item.bag, item.price))
        )
        val reason = when (item.bag) {
            Bag.NEEDS -> FeedbackReason.BOUGHT_NEED
            Bag.WANTS, Bag.SAVINGS -> FeedbackReason.BOUGHT_WANT
        }
        return GameResult.Success(after, feedback(reason, state, after, item.bag, item.bag.stat))
    }

    /** «Подожду»: ничего не списывается, пауза перед покупкой добавляет спокойствия. */
    fun pause(state: GameState, item: ShopItem): GameResult.Success {
        val after = state.copy(pet = state.pet.change(PetStat.CALM, GameRules.STAT_STEP))
        return GameResult.Success(after, feedback(FeedbackReason.PAUSED, state, after, item.bag, PetStat.CALM))
    }

    /**
     * Закрывает неделю: остатки и неразложенное уходят в копилку, опыт за разумные решения, звёздочка за неделю
     * в рамках плана, показатели снижаются к новой неделе, приходит доход следующей недели.
     */
    fun closePeriod(state: GameState): PeriodClosing {
        val period = state.period
        val plan = period.plan
        val leftover = state.needsLeft + state.wantsLeft + state.unallocated
        val reasons = buildList {
            if (plan != null) add(XpReason.PLANNED)
            if (plan != null && plan.needs > Dzynki.ZERO && period.actual.needs >= plan.needs) {
                add(XpReason.NEEDS_CLOSED)
            }
            if (period.actual.savings > Dzynki.ZERO) add(XpReason.SAVED)
            if (period.completedQuestIds.isNotEmpty()) add(XpReason.QUEST_DONE)
        }
        val hasStar = XpReason.NEEDS_CLOSED in reasons && period.withdrawn == Dzynki.ZERO
        val grown = state.pet.copy(xp = state.pet.xp + reasons.size)
        val result = PeriodResult(
            number = period.number,
            income = period.income,
            plan = plan,
            actual = period.actual,
            leftoverToSavings = leftover,
            withdrawn = period.withdrawn,
            xpReasons = reasons,
            hasEarnedStar = hasStar,
            stageBefore = state.pet.stage,
            stageAfter = grown.stage
        )
        val next = period.number + 1
        val income = GameRules.WEEKLY_INCOME
        val newState = state.copy(
            unallocated = income,
            needsLeft = Dzynki.ZERO,
            wantsLeft = Dzynki.ZERO,
            savings = state.savings + leftover,
            stars = if (hasStar) state.stars + 1 else state.stars,
            period = Period(number = next, income = income),
            pet = PetStat.entries.fold(grown) { pet, stat -> pet.change(stat, -GameRules.WEEKLY_STAT_DECAY) },
            history = state.history + result,
            ledger = state.ledger + LedgerEntry(next, IncomeSource.POCKET_MONEY, income)
        )
        return PeriodClosing(newState, result)
    }

    /** Бонус от взрослого с причиной (ТЗ 2.5.4). Ребёнок разложит его по мешочкам сам. */
    fun grantParentBonus(state: GameState, amount: Dzynki, reason: String): GameState {
        require(amount > Dzynki.ZERO) { "Бонус должен быть больше нуля" }
        return state.copy(
            unallocated = state.unallocated + amount,
            ledger = state.ledger + LedgerEntry(state.period.number, IncomeSource.PARENT_BONUS, amount, reason)
        )
    }

    /** Вход в игру. После паузы показатели подтягиваются, ничего не отнимается. */
    fun welcomeBack(state: GameState, nowMillis: Long): Visit {
        val isReturn = nowMillis - state.lastVisitMillis >= GameRules.RETURN_AFTER_MILLIS
        val pet = if (isReturn) state.pet.raiseTo(GameRules.RETURN_STAT_FLOOR) else state.pet
        return Visit(state.copy(pet = pet, lastVisitMillis = nowMillis), isReturn)
    }
}

/** Сколько не хватает: [need] больше [have]. */
internal fun shortfall(have: Dzynki, need: Dzynki): Dzynki = Dzynki(need.amount - have.amount)

internal fun feedback(reason: FeedbackReason, before: GameState, after: GameState, bag: Bag, stat: PetStat): Feedback =
    Feedback(
        reason = reason,
        balance = Change(before.balance, after.balance),
        bag = bag,
        bagChange = Change(before.amountIn(bag), after.amountIn(bag)),
        statChange = StatChange(stat, before.pet[stat], after.pet[stat])
    )

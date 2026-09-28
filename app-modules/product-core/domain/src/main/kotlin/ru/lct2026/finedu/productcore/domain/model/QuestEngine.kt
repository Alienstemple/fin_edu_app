package ru.lct2026.finedu.productcore.domain.model

/** Прохождение заданий: последствия выбора и награда. */
object QuestEngine {

    /** Проходит задание с выбором. Награда — только за первое прохождение. */
    fun completeChoice(state: GameState, quest: Quest.Choice, optionId: String): GameResult {
        val option = quest.options.first { it.id == optionId }
        val paid = pay(state, option.cost, option.bag) ?: return GameResult.NotEnoughMoney(
            shortfall(if (option.bag == Bag.SAVINGS) state.savings else state.balance, option.cost)
        )
        val pet = option.statChanges.entries.fold(paid.pet) { pet, (stat, delta) -> pet.change(stat, delta) }
        return finishQuest(state, paid.copy(pet = pet), quest)
    }

    /** Проверка корзины: уложился ли в бюджет и взял ли всё нужное. */
    fun checkBasket(quest: Quest.Basket, itemIds: Set<String>): BasketCheck {
        val chosen = quest.items.filter { it.id in itemIds }
        val total = chosen.fold(Dzynki.ZERO) { sum, item -> sum + item.price }
        return BasketCheck(
            total = total,
            fitsBudget = total <= quest.budget,
            hasAllNeeded = quest.items.filter { it.isNeeded }.all { it.id in itemIds }
        )
    }

    /** Проходит задание-корзину. Корзина учебная: кошелёк не тратится, награда — при любом результате. */
    fun completeBasket(state: GameState, quest: Quest.Basket): GameResult = finishQuest(state, state, quest)

    /** Списывает [cost] из мешочка [bag]; `null`, если не хватает. */
    private fun pay(state: GameState, cost: Dzynki, bag: Bag?): GameState? = when (bag) {
        null -> state

        Bag.SAVINGS -> state.savings.minusOrNull(cost)?.let {
            state.copy(savings = it, period = state.period.copy(withdrawn = state.period.withdrawn + cost))
        }

        Bag.NEEDS, Bag.WANTS -> state.balance.minusOrNull(cost)?.let {
            state.copy(balance = it, period = state.period.copy(actual = state.period.actual.add(bag, cost)))
        }
    }

    private fun finishQuest(before: GameState, after: GameState, quest: Quest): GameResult {
        val reward = if (quest.id in before.completedQuestIds) Dzynki.ZERO else quest.reward
        val periodNumber = after.period.number
        val newState = after.copy(
            balance = after.balance + reward,
            completedQuestIds = after.completedQuestIds + quest.id,
            period = after.period.copy(completedQuestIds = after.period.completedQuestIds + quest.id),
            ledger = if (reward > Dzynki.ZERO) {
                after.ledger + LedgerEntry(periodNumber, IncomeSource.QUEST_REWARD, reward, quest.id)
            } else {
                after.ledger
            }
        )
        return GameResult.Success(
            newState,
            Feedback(
                FeedbackReason.QUEST_DONE,
                balanceDelta = newState.balance.amount - before.balance.amount,
                savingsDelta = newState.savings.amount - before.savings.amount,
                statChanges = newState.pet.changesSince(before.pet),
                reward = reward
            )
        )
    }
}

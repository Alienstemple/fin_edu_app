package ru.lct2026.finedu.productcore.domain.model

import kotlin.math.ceil

/** Копилка и цели-предметы уголка. */
object SavingsEngine {

    /** Из остатка «Хочу» в копилку. */
    fun deposit(state: GameState, amount: Dzynki): GameResult {
        require(amount > Dzynki.ZERO) { "Сумма пополнения должна быть больше нуля" }
        val wants = state.wantsLeft.minusOrNull(amount)
            ?: return GameResult.NotEnoughMoney(shortfall(state.wantsLeft, amount))
        val after = state.copy(
            wantsLeft = wants,
            savings = state.savings + amount,
            pet = state.pet.change(PetStat.CALM, GameRules.STAT_STEP),
            period = state.period.copy(actual = state.period.actual.add(Bag.SAVINGS, amount))
        )
        return GameResult.Success(after, feedback(FeedbackReason.DEPOSITED, state, after, Bag.SAVINGS, PetStat.CALM))
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

    /** Из копилки в остаток «Хочу». Показатели не меняются: копилка для того и есть, чтобы выручать. */
    fun withdraw(state: GameState, amount: Dzynki): GameResult {
        require(amount > Dzynki.ZERO) { "Сумма снятия должна быть больше нуля" }
        val savings = state.savings.minusOrNull(amount)
            ?: return GameResult.NotEnoughMoney(shortfall(state.savings, amount))
        val after = state.copy(
            wantsLeft = state.wantsLeft + amount,
            savings = savings,
            period = state.period.copy(withdrawn = state.period.withdrawn + amount)
        )
        return GameResult.Success(after, feedback(FeedbackReason.WITHDREW, state, after, Bag.SAVINGS, PetStat.CALM))
    }

    /** Текущая цель — первая из [goals], ещё не поставленная в уголок. */
    fun currentGoal(state: GameState, goals: List<Goal>): Goal? = goals.firstOrNull { it.id !in state.placedGoalIds }

    /**
     * Сколько недель копить до цели (ТЗ 2.5.7): `ceil(осталось / среднее прибавление копилки за неделю)`.
     * 0 — уже накоплено, `null` — прибавлений ещё не было и срок не посчитать.
     */
    fun weeksToGoal(state: GameState, goal: Goal): Int? {
        val left = goal.price.amount - state.savings.amount
        val saved = state.history.sumOf { it.savedTotal.amount } + state.period.actual.savings.amount
        return when {
            left <= 0 -> 0
            saved == 0 -> null
            else -> ceil(left / (saved.toDouble() / state.period.number)).toInt()
        }
    }

    /** Цель накоплена: предмет встаёт в уголок навсегда, копилка уменьшается на цену. */
    fun placeGoal(state: GameState, goal: Goal): GameResult {
        val savings = state.savings.minusOrNull(goal.price)
            ?: return GameResult.NotEnoughMoney(shortfall(state.savings, goal.price))
        val after = state.copy(savings = savings, placedGoalIds = state.placedGoalIds + goal.id)
        return GameResult.Success(after, feedback(FeedbackReason.GOAL_PLACED, state, after, Bag.SAVINGS, PetStat.CALM))
    }
}

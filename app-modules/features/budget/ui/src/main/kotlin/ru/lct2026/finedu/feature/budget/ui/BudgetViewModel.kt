package ru.lct2026.finedu.feature.budget.ui

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.lct2026.finedu.productcore.domain.model.Bag
import ru.lct2026.finedu.productcore.domain.model.BagAmounts
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.GameEngine
import ru.lct2026.finedu.productcore.domain.model.GameState
import ru.lct2026.finedu.productcore.domain.model.IncomeSource
import ru.lct2026.finedu.productcore.domain.model.PlanResult
import ru.lct2026.finedu.productcore.domain.model.QuestEngine
import ru.lct2026.finedu.productcore.domain.model.QuestTrigger
import ru.lct2026.finedu.productcore.domain.repository.ContentRepository
import ru.lct2026.finedu.productcore.domain.repository.GameRepository
import ru.lct2026.finedu.productcore.ui.components.STEPPER_STEP
import ru.lct2026.finedu.productcore.ui.viewmodel.StatefulViewModel

@HiltViewModel
internal class BudgetViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val contentRepository: ContentRepository
) : StatefulViewModel<BudgetUiState>(BudgetUiState()) {

    init {
        observeGame()
    }

    private fun observeGame() {
        viewModelScope.launch {
            gameRepository.state.filterNotNull().collect { game ->
                updateState {
                    copy(mode = game.toMode(previous = mode), look = game.profile.look, stage = game.pet.stage)
                }
            }
        }
    }

    fun onPlusClick(bag: Bag) {
        updateDraft { mode ->
            if (mode.left <= 0) mode.draft else mode.draft.add(bag, Dzynki(minOf(STEPPER_STEP, mode.left)))
        }
    }

    fun onMinusClick(bag: Bag) {
        updateDraft { mode ->
            val current = mode.draft[bag].amount
            mode.draft.with(bag, Dzynki(current - minOf(STEPPER_STEP, current)))
        }
    }

    fun onFixClick() {
        val draft = when (val mode = currentState.mode) {
            is BudgetMode.Distribute -> mode.draft.takeIf { mode.left == 0 }
            BudgetMode.Loading, is BudgetMode.Fixed -> null
        } ?: return
        viewModelScope.launch {
            val game = gameRepository.state.first() ?: return@launch
            when (val result = GameEngine.distribute(game, draft)) {
                is PlanResult.Success -> saveWithQuest(result.state)
                is PlanResult.NotBalanced -> Unit
            }
        }
    }

    /** Раскладка засчитывает задание «Зафиксируй план», если оно ещё не пройдено. */
    private suspend fun saveWithQuest(state: GameState) {
        val quest = QuestEngine.questFor(state, contentRepository.content().quests, QuestTrigger.PLAN_FIXED)
        if (quest == null) {
            gameRepository.save(state)
            return
        }
        val outcome = QuestEngine.complete(state, quest)
        if (outcome.reward > Dzynki.ZERO) {
            updateState { copy(questReward = QuestReward(outcome.reward.amount, quest.title)) }
        }
        gameRepository.save(outcome.state)
    }

    private fun updateDraft(block: (BudgetMode.Distribute) -> BagAmounts) {
        updateState {
            when (mode) {
                is BudgetMode.Distribute -> copy(mode = mode.copy(draft = block(mode)))
                BudgetMode.Loading, is BudgetMode.Fixed -> this
            }
        }
    }
}

private fun GameState.toMode(previous: BudgetMode): BudgetMode {
    val income = unallocated.amount
    if (income == 0) {
        return BudgetMode.Fixed(
            plan = period.plan ?: BagAmounts(),
            actual = period.actual,
            left = BagAmounts(needs = needsLeft, wants = wantsLeft, savings = savings)
        )
    }
    val draft = when (previous) {
        is BudgetMode.Distribute -> previous.draft.takeIf { previous.income == income }
        BudgetMode.Loading, is BudgetMode.Fixed -> null
    }
    return BudgetMode.Distribute(
        income = income,
        sources = incomeSources(),
        draft =
            draft ?: GameEngine.suggestPlan(unallocated)
    )
}

/** Откуда пришли неразложенные дзыньки: регулярный доход — до первой раскладки недели, плюс бонусы и награды. */
private fun GameState.incomeSources(): List<IncomeSource> {
    val thisWeek = ledger.filter { it.periodNumber == period.number }.map { it.source }.toSet()
    return buildList {
        if (period.plan == null) add(IncomeSource.POCKET_MONEY)
        if (IncomeSource.PARENT_BONUS in thisWeek) add(IncomeSource.PARENT_BONUS)
        if (IncomeSource.QUEST_REWARD in thisWeek) add(IncomeSource.QUEST_REWARD)
    }
}

private fun BagAmounts.with(bag: Bag, amount: Dzynki): BagAmounts = when (bag) {
    Bag.NEEDS -> copy(needs = amount)
    Bag.WANTS -> copy(wants = amount)
    Bag.SAVINGS -> copy(savings = amount)
}

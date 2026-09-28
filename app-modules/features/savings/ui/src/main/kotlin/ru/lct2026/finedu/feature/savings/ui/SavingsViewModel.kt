package ru.lct2026.finedu.feature.savings.ui

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.GameResult
import ru.lct2026.finedu.productcore.domain.model.GameState
import ru.lct2026.finedu.productcore.domain.model.Goal
import ru.lct2026.finedu.productcore.domain.model.SavingsEngine
import ru.lct2026.finedu.productcore.domain.repository.ContentRepository
import ru.lct2026.finedu.productcore.domain.repository.GameRepository
import ru.lct2026.finedu.productcore.ui.viewmodel.StatefulViewModel

@HiltViewModel
internal class SavingsViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val contentRepository: ContentRepository
) : StatefulViewModel<SavingsUiState>(SavingsUiState.Loading),
    SavingsActions {

    private var goals: List<Goal> = emptyList()

    init {
        viewModelScope.launch {
            goals = contentRepository.content().goals
            gameRepository.state.filterNotNull().collect { game ->
                val fresh = game.toContent()
                updateState {
                    when (this) {
                        SavingsUiState.Loading -> fresh
                        is SavingsUiState.Content -> fresh.copy(sheet = sheet, feedback = feedback, ritual = ritual)
                    }
                }
            }
        }
    }

    override fun onDepositClick() {
        updateContent {
            if (canDeposit) copy(sheet = SavingsSheet.Deposit(STEP, floorToStep(wantsLeft))) else this
        }
    }

    override fun onWithdrawClick() {
        viewModelScope.launch {
            val game = gameRepository.state.first() ?: return@launch
            updateContent { if (canWithdraw) copy(sheet = withdrawSheet(game, STEP)) else this }
        }
    }

    override fun onAmountStep(up: Boolean) {
        val delta = if (up) SAVINGS_STEP else -SAVINGS_STEP
        viewModelScope.launch {
            val game = gameRepository.state.first() ?: return@launch
            updateContent {
                val current = sheet ?: return@updateContent this
                val amount = Dzynki((current.amount.amount + delta).coerceIn(SAVINGS_STEP, current.max.amount))
                val newSheet = when (current) {
                    is SavingsSheet.Deposit -> current.copy(amount = amount)
                    is SavingsSheet.Withdraw -> withdrawSheet(game, amount)
                }
                copy(sheet = newSheet)
            }
        }
    }

    override fun onSheetDismiss() {
        updateContent { copy(sheet = null) }
    }

    override fun onSheetConfirm() {
        val sheet = content()?.sheet ?: return
        viewModelScope.launch {
            val game = gameRepository.state.first() ?: return@launch
            val result = when (sheet) {
                is SavingsSheet.Deposit -> SavingsEngine.deposit(game, sheet.amount)
                is SavingsSheet.Withdraw -> SavingsEngine.withdraw(game, sheet.amount)
            }
            when (result) {
                is GameResult.Success -> {
                    gameRepository.save(result.state)
                    val progress = result.state.progress()
                    val feedback = SavingsFeedback(
                        feedback = result.feedback,
                        goalTitle = progress?.goal?.title,
                        left = progress?.left ?: Dzynki.ZERO,
                        weeks = progress?.weeks
                    )
                    updateContent { copy(sheet = null, feedback = feedback) }
                }

                is GameResult.NotEnoughMoney -> updateContent { copy(sheet = null) }
            }
        }
    }

    override fun onFeedbackDismiss() {
        updateContent { copy(feedback = null) }
    }

    override fun onPlaceGoalClick() {
        updateContent {
            val goal = progress?.takeIf { it.isReached }?.goal
            if (goal != null) copy(ritual = GoalRitual(goal, isPlaced = false)) else this
        }
    }

    override fun onRitualPlaceClick() {
        val ritual = content()?.ritual?.takeIf { !it.isPlaced } ?: return
        viewModelScope.launch {
            val game = gameRepository.state.first() ?: return@launch
            when (val result = SavingsEngine.placeGoal(game, ritual.goal)) {
                is GameResult.Success -> {
                    gameRepository.save(result.state)
                    updateContent { copy(ritual = ritual.copy(isPlaced = true)) }
                }

                is GameResult.NotEnoughMoney -> updateContent { copy(ritual = null) }
            }
        }
    }

    override fun onRitualDismiss() {
        updateContent { copy(ritual = null) }
    }

    override fun onGoHomeClick() {
        updateContent { copy(ritual = null) }
        offerEvent(SavingsEvent.OpenHome)
    }

    private fun withdrawSheet(game: GameState, amount: Dzynki): SavingsSheet.Withdraw? {
        val goal = SavingsEngine.currentGoal(game, goals)
        val preview = SavingsEngine.previewWithdraw(game, amount, goal) ?: return null
        return SavingsSheet.Withdraw(amount, floorToStep(game.savings), preview, goal?.title)
    }

    private fun content(): SavingsUiState.Content? = when (val state = currentState) {
        SavingsUiState.Loading -> null
        is SavingsUiState.Content -> state
    }

    private fun updateContent(block: SavingsUiState.Content.() -> SavingsUiState.Content) {
        updateState {
            when (this) {
                SavingsUiState.Loading -> this
                is SavingsUiState.Content -> block()
            }
        }
    }

    private fun GameState.toContent(): SavingsUiState.Content {
        val current = SavingsEngine.currentGoal(this, goals)
        return SavingsUiState.Content(
            look = profile.look,
            stage = pet.stage,
            savings = savings,
            wantsLeft = wantsLeft,
            placedGoalIds = placedGoalIds,
            stars = stars,
            progress = progress(),
            goals = goals.map { goal ->
                when {
                    goal.id in placedGoalIds -> GoalItem(goal, GoalStatus.PLACED, goal.price)
                    goal == current -> GoalItem(goal, GoalStatus.CURRENT, minOf(savings, goal.price))
                    else -> GoalItem(goal, GoalStatus.NEXT, Dzynki.ZERO)
                }
            }
        )
    }

    private fun GameState.progress(): GoalProgress? {
        val goal = SavingsEngine.currentGoal(this, goals) ?: return null
        val saved = history.sumOf { it.savedTotal.amount } + period.actual.savings.amount
        return GoalProgress(
            goal = goal,
            saved = minOf(savings, goal.price),
            left = goal.price.minusOrNull(savings) ?: Dzynki.ZERO,
            weeks = SavingsEngine.weeksToGoal(this, goal),
            weeklyAverage = (saved.toDouble() / period.number).roundToInt()
        )
    }

    private companion object {
        val STEP = Dzynki(SAVINGS_STEP)

        fun floorToStep(amount: Dzynki): Dzynki = Dzynki(amount.amount / SAVINGS_STEP * SAVINGS_STEP)
    }
}

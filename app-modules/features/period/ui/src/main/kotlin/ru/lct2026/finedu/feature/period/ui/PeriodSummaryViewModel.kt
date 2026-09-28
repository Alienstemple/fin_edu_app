package ru.lct2026.finedu.feature.period.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.lct2026.finedu.productcore.domain.model.GameEngine
import ru.lct2026.finedu.productcore.domain.model.GameState
import ru.lct2026.finedu.productcore.domain.model.PeriodResult
import ru.lct2026.finedu.productcore.domain.model.XpReason
import ru.lct2026.finedu.productcore.domain.repository.GameRepository
import ru.lct2026.finedu.productcore.ui.viewmodel.StatefulViewModel

/**
 * Итоги недели. Неделя закрывается здесь ровно один раз: факт закрытия хранится в [SavedStateHandle], поэтому
 * после пересоздания экрана показываются уже сохранённые итоги из истории.
 */
@HiltViewModel
internal class PeriodSummaryViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val savedStateHandle: SavedStateHandle
) : StatefulViewModel<PeriodSummaryUiState>(PeriodSummaryUiState.Loading) {

    init {
        viewModelScope.launch { closeWeekOnce() }
    }

    fun onNextWeekClick() {
        when (val state = currentState) {
            PeriodSummaryUiState.Loading -> Unit

            is PeriodSummaryUiState.Content ->
                if (state.result.isStageChanged && !state.isGrowthShown) {
                    setState(state.copy(isGrowthShown = true))
                } else {
                    offerEvent(PeriodSummaryEvent.OpenHome)
                }
        }
    }

    fun onGrowthNextClick() {
        offerEvent(PeriodSummaryEvent.OpenHome)
    }

    private suspend fun closeWeekOnce() {
        val game = gameRepository.state.first() ?: return
        if (savedStateHandle.get<Boolean>(KEY_CLOSED) == true) {
            val result = game.history.lastOrNull() ?: return
            setState(content(game, result))
        } else {
            val closing = GameEngine.closePeriod(game)
            gameRepository.save(closing.state)
            savedStateHandle[KEY_CLOSED] = true
            setState(content(closing.state, closing.result))
        }
    }

    private fun content(game: GameState, result: PeriodResult) = PeriodSummaryUiState.Content(
        result = result,
        look = game.profile.look,
        petXp = game.pet.xp,
        stars = game.stars,
        placedGoalIds = game.placedGoalIds,
        explanation = result.explanation(),
        advice = result.advice()
    )

    private companion object {
        const val KEY_CLOSED = "period_closed"
    }
}

/** Совет по первому, чего не хватило на неделе: план → Нужное → копилка; всё есть — челлендж. */
internal fun PeriodResult.advice(): SummaryAdvice = when {
    XpReason.PLANNED !in xpReasons -> SummaryAdvice.PLAN_EARLY
    XpReason.NEEDS_CLOSED !in xpReasons -> SummaryAdvice.NEEDS_FIRST
    XpReason.SAVED !in xpReasons -> SummaryAdvice.SAVE_EARLY
    else -> SummaryAdvice.TRY_CHALLENGE
}

internal fun PeriodResult.explanation(): SummaryExplanation {
    val leftover = leftoverToSavings.amount
    val plan = plan
    val wantsUnderPlan = if (plan != null) plan.wants.amount - actual.wants.amount else 0
    return when {
        leftover == 0 -> SummaryExplanation.AllSpent

        plan != null && wantsUnderPlan == leftover ->
            SummaryExplanation.WantsToSavings(
                spent = actual.wants.amount,
                planned = plan.wants.amount,
                extra = leftover
            )

        else -> SummaryExplanation.LeftoverToSavings(leftover)
    }
}

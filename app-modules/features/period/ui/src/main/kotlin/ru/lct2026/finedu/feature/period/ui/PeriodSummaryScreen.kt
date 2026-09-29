package ru.lct2026.finedu.feature.period.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.lct2026.finedu.feature.period.ui.component.ExpectationCard
import ru.lct2026.finedu.feature.period.ui.component.RealityCard
import ru.lct2026.finedu.feature.period.ui.component.StageGrowthContent
import ru.lct2026.finedu.feature.period.ui.component.SummaryTags
import ru.lct2026.finedu.feature.period.ui.component.TextCard
import ru.lct2026.finedu.feature.period.ui.component.XpCard
import ru.lct2026.finedu.feature.period.ui.component.labelRes
import ru.lct2026.finedu.productcore.domain.model.BagAmounts
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.PeriodResult
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.PetStage
import ru.lct2026.finedu.productcore.domain.model.XpReason
import ru.lct2026.finedu.productcore.ui.components.FinButton
import ru.lct2026.finedu.productcore.ui.components.FinTopBar
import ru.lct2026.finedu.productcore.ui.components.SpeechBubble
import ru.lct2026.finedu.productcore.ui.components.dzynkiText
import ru.lct2026.finedu.productcore.ui.event.ObserveEvents
import ru.lct2026.finedu.productcore.ui.illustration.PetMood
import ru.lct2026.finedu.productcore.ui.illustration.PetView
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview
import ru.lct2026.finedu.productcore.ui.sound.LocalSoundPlayer
import ru.lct2026.finedu.productcore.ui.sound.SoundEffect
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

@Composable
internal fun PeriodSummaryRoute(
    onNextWeek: () -> Unit,
    onHelpClick: () -> Unit,
    onParentClick: () -> Unit,
    viewModel: PeriodSummaryViewModel = hiltViewModel()
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    ObserveEvents(viewModel.events) { event ->
        when (event) {
            PeriodSummaryEvent.OpenHome -> onNextWeek()
        }
    }
    PeriodSummaryScreen(
        state = state,
        onNextWeekClick = viewModel::onNextWeekClick,
        onGrowthNextClick = viewModel::onGrowthNextClick,
        onHelpClick = onHelpClick,
        onParentClick = onParentClick
    )
}

@Composable
internal fun PeriodSummaryScreen(
    state: PeriodSummaryUiState,
    onNextWeekClick: () -> Unit,
    onGrowthNextClick: () -> Unit,
    onHelpClick: () -> Unit,
    onParentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        // Закрытие недели занимает доли секунды: пока виден только общий фон.
        PeriodSummaryUiState.Loading -> Box(modifier = modifier.fillMaxSize())

        is PeriodSummaryUiState.Content -> if (state.isGrowthShown) {
            StageGrowthContent(
                look = state.look,
                stageBefore = state.result.stageBefore,
                stageAfter = state.result.stageAfter,
                xpToNextStage = state.xpToNextStage,
                stars = state.stars,
                placedGoalIds = state.placedGoalIds,
                onNextClick = onGrowthNextClick,
                modifier = modifier
            )
        } else {
            SummaryContent(
                state = state,
                onNextWeekClick = onNextWeekClick,
                onHelpClick = onHelpClick,
                onParentClick = onParentClick,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun SummaryContent(
    state: PeriodSummaryUiState.Content,
    onNextWeekClick: () -> Unit,
    onHelpClick: () -> Unit,
    onParentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val result = state.result
    val sound = LocalSoundPlayer.current
    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            FinTopBar(
                title = stringResource(R.string.period_summary_title),
                subtitle = stringResource(R.string.period_summary_week, result.number),
                onHelp = onHelpClick,
                onParent = onParentClick
            )
        },
        bottomBar = {
            FinButton(
                text = stringResource(R.string.period_summary_next_week),
                // Новая неделя — новый доход.
                onClick = {
                    sound.play(SoundEffect.INCOME)
                    onNextWeekClick()
                },
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ExpectationCard(plan = result.plan)
            RealityCard(result = result)
            SummaryTags(result = result)
            PetReply(state = state)
            TextCard(
                title = stringResource(R.string.period_summary_advice_title),
                text = stringResource(state.advice.textRes),
                titleColor = FinEduTheme.colors.gold
            )
            XpCard(
                xpReasons = result.xpReasons,
                totalXp = state.petXp,
                stageLabel = stringResource(result.stageAfter.labelRes)
            )
            if (result.hasEarnedStar) {
                TextCard(
                    title = stringResource(R.string.period_summary_star_title),
                    text = stringResource(R.string.period_summary_star_text, state.stars)
                )
            }
        }
    }
}

/** Реплика Дзыня по итогам и пояснение цифрами. Без стыда: не закрытое Нужное — мягкая подсказка. */
@Composable
private fun PetReply(state: PeriodSummaryUiState.Content) {
    val isGoodWeek = state.result.isNeedsClosed
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PetView(
                look = state.look,
                mood = if (isGoodWeek) PetMood.PROUD else PetMood.NEUTRAL,
                stage = state.result.stageBefore,
                modifier = Modifier.width(120.dp)
            )
            SpeechBubble(
                text = stringResource(
                    if (isGoodWeek) R.string.period_summary_line_good else R.string.period_summary_line_needs_open
                ),
                modifier = Modifier.weight(1f)
            )
        }
        Text(text = explanationText(state.explanation), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun explanationText(explanation: SummaryExplanation): String = when (explanation) {
    is SummaryExplanation.WantsToSavings -> stringResource(
        R.string.period_summary_explain_wants,
        explanation.spent,
        explanation.planned,
        explanation.extra
    )

    is SummaryExplanation.LeftoverToSavings ->
        stringResource(R.string.period_summary_explain_leftover, dzynkiText(explanation.amount))

    SummaryExplanation.AllSpent -> stringResource(R.string.period_summary_explain_all_spent)
}

private val SummaryAdvice.textRes: Int
    get() = when (this) {
        SummaryAdvice.PLAN_EARLY -> R.string.period_summary_advice_plan
        SummaryAdvice.NEEDS_FIRST -> R.string.period_summary_advice_needs
        SummaryAdvice.SAVE_EARLY -> R.string.period_summary_advice_save
        SummaryAdvice.TRY_CHALLENGE -> R.string.period_summary_advice_challenge
    }

private val previewGoodWeek = PeriodSummaryUiState.Content(
    result = PeriodResult(
        number = 2,
        income = Dzynki(300),
        plan = BagAmounts(Dzynki(150), Dzynki(120), Dzynki(30)),
        actual = BagAmounts(Dzynki(150), Dzynki(90), Dzynki(30)),
        leftoverToSavings = Dzynki(30),
        withdrawn = Dzynki.ZERO,
        xpReasons = listOf(XpReason.PLANNED, XpReason.NEEDS_CLOSED, XpReason.SAVED),
        hasEarnedStar = true,
        stageBefore = PetStage.BABY,
        stageAfter = PetStage.SPRY
    ),
    look = PetLook(PetFur.MINT, PetHat.CAP),
    petXp = 5,
    stars = 2,
    placedGoalIds = emptySet(),
    explanation = SummaryExplanation.WantsToSavings(spent = 90, planned = 120, extra = 30),
    advice = SummaryAdvice.TRY_CHALLENGE
)

@Preview(heightDp = 1600)
@Composable
private fun PeriodSummaryGoodWeekPreview() {
    FinEduPreview {
        PeriodSummaryScreen(
            state = previewGoodWeek,
            onNextWeekClick = {},
            onGrowthNextClick = {},
            onHelpClick = {},
            onParentClick = {}
        )
    }
}

@Preview(heightDp = 1400)
@Composable
private fun PeriodSummaryNoPlanPreview() {
    FinEduPreview {
        PeriodSummaryScreen(
            state = previewGoodWeek.copy(
                result = previewGoodWeek.result.copy(
                    plan = null,
                    actual = BagAmounts(wants = Dzynki(60)),
                    leftoverToSavings = Dzynki(240),
                    xpReasons = emptyList(),
                    hasEarnedStar = false,
                    stageAfter = PetStage.BABY
                ),
                petXp = 1,
                explanation = SummaryExplanation.LeftoverToSavings(240),
                advice = SummaryAdvice.PLAN_EARLY
            ),
            onNextWeekClick = {},
            onGrowthNextClick = {},
            onHelpClick = {},
            onParentClick = {}
        )
    }
}

@Preview
@Composable
private fun PeriodSummaryGrowthPreview() {
    FinEduPreview {
        PeriodSummaryScreen(
            state = previewGoodWeek.copy(isGrowthShown = true),
            onNextWeekClick = {},
            onGrowthNextClick = {},
            onHelpClick = {},
            onParentClick = {}
        )
    }
}

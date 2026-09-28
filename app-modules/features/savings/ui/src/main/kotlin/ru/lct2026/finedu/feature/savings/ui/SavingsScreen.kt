package ru.lct2026.finedu.feature.savings.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.lct2026.finedu.feature.savings.ui.component.AmountSheet
import ru.lct2026.finedu.feature.savings.ui.component.GoalRitualScreen
import ru.lct2026.finedu.productcore.domain.model.Bag
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.FeedbackReason
import ru.lct2026.finedu.productcore.domain.model.Goal
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.PetStage
import ru.lct2026.finedu.productcore.ui.R as CoreR
import ru.lct2026.finedu.productcore.ui.components.BagIcon
import ru.lct2026.finedu.productcore.ui.components.DzynkiAmount
import ru.lct2026.finedu.productcore.ui.components.FeedbackSheet
import ru.lct2026.finedu.productcore.ui.components.FinBottomBar
import ru.lct2026.finedu.productcore.ui.components.FinButton
import ru.lct2026.finedu.productcore.ui.components.FinButtonStyle
import ru.lct2026.finedu.productcore.ui.components.FinTab
import ru.lct2026.finedu.productcore.ui.components.FinTopBar
import ru.lct2026.finedu.productcore.ui.components.MinTouchTarget
import ru.lct2026.finedu.productcore.ui.components.SpeechBubble
import ru.lct2026.finedu.productcore.ui.components.glass
import ru.lct2026.finedu.productcore.ui.event.ObserveEvents
import ru.lct2026.finedu.productcore.ui.illustration.PetMood
import ru.lct2026.finedu.productcore.ui.illustration.PetView
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

@Composable
internal fun SavingsRoute(
    onHelp: () -> Unit,
    onParent: () -> Unit,
    onTabSelect: (FinTab) -> Unit,
    viewModel: SavingsViewModel = hiltViewModel()
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    ObserveEvents(viewModel.events) { event ->
        when (event) {
            SavingsEvent.OpenHome -> onTabSelect(FinTab.HOME)
        }
    }
    SavingsScreen(
        state = state,
        actions = viewModel,
        onHelp = onHelp,
        onParent = onParent,
        onTabSelect = onTabSelect
    )
}

/** Действия пользователя на экране копилки; реализует [SavingsViewModel]. */
internal interface SavingsActions {
    fun onDepositClick()
    fun onWithdrawClick()
    fun onAmountStep(up: Boolean)
    fun onSheetConfirm()
    fun onSheetDismiss()
    fun onFeedbackDismiss()
    fun onPlaceGoalClick()
    fun onRitualPlaceClick()
    fun onRitualDismiss()
    fun onGoHomeClick()
}

@Composable
internal fun SavingsScreen(
    state: SavingsUiState,
    actions: SavingsActions,
    onHelp: () -> Unit,
    onParent: () -> Unit,
    onTabSelect: (FinTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val ritual = when (state) {
        SavingsUiState.Loading -> null
        is SavingsUiState.Content -> state.ritual
    }
    when {
        state is SavingsUiState.Content && ritual != null -> {
            BackHandler(onBack = if (ritual.isPlaced) actions::onGoHomeClick else actions::onRitualDismiss)
            GoalRitualScreen(
                ritual = ritual,
                look = state.look,
                stage = state.stage,
                placedGoalIds = state.placedGoalIds,
                stars = state.stars,
                nextGoal = state.progress,
                onPlace = actions::onRitualPlaceClick,
                onGoHome = actions::onGoHomeClick,
                modifier = modifier
            )
        }

        else -> SavingsMain(
            state = state,
            actions = actions,
            onHelp = onHelp,
            onParent = onParent,
            onTabSelect = onTabSelect,
            modifier = modifier
        )
    }
}

@Composable
private fun SavingsMain(
    state: SavingsUiState,
    actions: SavingsActions,
    onHelp: () -> Unit,
    onParent: () -> Unit,
    onTabSelect: (FinTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            FinTopBar(
                title = stringResource(R.string.savings_title),
                onHelp = onHelp,
                onParent = onParent
            )
        },
        bottomBar = {
            FinBottomBar(
                selected = FinTab.SAVINGS,
                onSelect = onTabSelect,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    ) { innerPadding ->
        when (state) {
            SavingsUiState.Loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                val description = stringResource(R.string.savings_loading)
                CircularProgressIndicator(modifier = Modifier.semantics { contentDescription = description })
            }

            is SavingsUiState.Content -> SavingsContent(
                state = state,
                actions = actions,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun SavingsContent(state: SavingsUiState.Content, actions: SavingsActions, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OverviewCard(state = state, onPlaceGoal = actions::onPlaceGoalClick)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FinButton(
                text = stringResource(R.string.savings_deposit),
                onClick = actions::onDepositClick,
                enabled = state.canDeposit,
                modifier = Modifier.weight(1f)
            )
            FinButton(
                text = stringResource(R.string.savings_withdraw),
                onClick = actions::onWithdrawClick,
                enabled = state.canWithdraw,
                style = FinButtonStyle.Secondary,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp)
            )
        }
        if (!state.canDeposit) {
            Text(
                text = stringResource(R.string.savings_deposit_unavailable),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        GoalsList(goals = state.goals)
    }
    state.sheet?.let { sheet ->
        AmountSheet(
            sheet = sheet,
            look = state.look,
            stage = state.stage,
            wantsLeft = state.wantsLeft,
            onMinus = { actions.onAmountStep(up = false) },
            onPlus = { actions.onAmountStep(up = true) },
            onConfirm = actions::onSheetConfirm,
            onDismiss = actions::onSheetDismiss
        )
    }
    state.feedback?.let { feedback ->
        FeedbackSheet(
            feedback = feedback.feedback,
            look = state.look,
            stage = state.stage,
            title = stringResource(feedback.titleRes()),
            why = feedbackWhy(feedback),
            onDismiss = actions::onFeedbackDismiss,
            onPrimary = actions::onGoHomeClick
        )
    }
}

@Composable
private fun OverviewCard(state: SavingsUiState.Content, onPlaceGoal: () -> Unit) {
    val progress = state.progress
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glass()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(contentAlignment = Alignment.BottomEnd) {
                PetView(
                    look = state.look,
                    mood = if (progress?.isReached != false) PetMood.PROUD else PetMood.HAPPY,
                    stage = state.stage,
                    contentDescription = stringResource(R.string.savings_pet_a11y),
                    modifier = Modifier.width(112.dp)
                )
                BagIcon(bag = Bag.SAVINGS)
            }
            SpeechBubble(text = stringResource(R.string.savings_pet_line))
        }
        if (progress == null) {
            Text(
                text = stringResource(R.string.savings_all_placed_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() }
            )
            Text(text = stringResource(R.string.savings_all_placed_text), style = MaterialTheme.typography.bodyMedium)
            AmountLine(label = stringResource(R.string.savings_in_savings), amount = state.savings)
        } else {
            GoalProgressBlock(progress = progress, savings = state.savings, onPlaceGoal = onPlaceGoal)
        }
    }
}

@Composable
private fun GoalProgressBlock(progress: GoalProgress, savings: Dzynki, onPlaceGoal: () -> Unit) {
    Text(
        text = stringResource(R.string.savings_goal_label, progress.goal.title),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.semantics { heading() }
    )
    AmountLine(label = stringResource(R.string.savings_saved), amount = savings)
    AmountLine(label = stringResource(R.string.savings_left), amount = progress.left)
    ProgressBar(saved = progress.saved.amount, total = progress.goal.price.amount)
    when (val weeks = progress.weeks) {
        null -> BodyText(stringResource(R.string.savings_weeks_unknown))

        0 -> {
            Text(
                text = stringResource(R.string.savings_reached),
                style = MaterialTheme.typography.titleMedium,
                color = FinEduTheme.colors.gold
            )
            FinButton(text = stringResource(R.string.savings_place_goal), onClick = onPlaceGoal)
        }

        else -> Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(CoreR.drawable.ic_clock),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column {
                Text(
                    text = pluralStringResource(R.plurals.savings_weeks, weeks, weeks),
                    style = MaterialTheme.typography.titleMedium
                )
                BodyText(stringResource(R.string.savings_per_week, progress.weeklyAverage))
            }
        }
    }
}

@Composable
private fun AmountLine(label: String, amount: Dzynki) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        DzynkiAmount(amount = amount, style = MaterialTheme.typography.titleLarge)
    }
}

/** Полоса прогресса цели с подписью «40 / 100»: смысл не только цветом. */
@Composable
private fun ProgressBar(saved: Int, total: Int) {
    val description = stringResource(R.string.savings_progress_a11y, saved, total)
    Row(
        modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(FinEduTheme.colors.glassBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (total > 0) saved.toFloat() / total else 1f)
                    .height(12.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(FinEduTheme.colors.savings)
            )
        }
        Text(
            text = stringResource(R.string.savings_progress, saved, total),
            style = MaterialTheme.typography.titleSmall
        )
    }
}

@Composable
private fun GoalsList(goals: List<GoalItem>) {
    Text(
        text = stringResource(R.string.savings_goals_title),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.semantics { heading() }
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        goals.forEach { GoalRow(it) }
    }
    if (goals.isNotEmpty() && goals.all { it.status == GoalStatus.PLACED }) {
        Text(
            text = stringResource(R.string.savings_goals_done),
            style = MaterialTheme.typography.titleMedium,
            color = FinEduTheme.colors.gold
        )
    }
}

@Composable
private fun GoalRow(item: GoalItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = MinTouchTarget)
            .glass()
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val placed = item.status == GoalStatus.PLACED
        Icon(
            painter = painterResource(if (placed) CoreR.drawable.ic_check else CoreR.drawable.ic_goal),
            contentDescription = null,
            tint = if (placed) FinEduTheme.colors.savings else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.goal.title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(item.status.labelRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (!placed) {
            Text(
                text = stringResource(R.string.savings_progress, item.saved.amount, item.goal.price.amount),
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Composable
private fun BodyText(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

private val GoalStatus.labelRes: Int
    get() = when (this) {
        GoalStatus.PLACED -> R.string.savings_goal_placed
        GoalStatus.CURRENT -> R.string.savings_goal_current
        GoalStatus.NEXT -> R.string.savings_goal_next
    }

private fun SavingsFeedback.titleRes(): Int = when (feedback.reason) {
    FeedbackReason.WITHDREW -> R.string.savings_feedback_withdraw_title

    FeedbackReason.DEPOSITED,
    FeedbackReason.BOUGHT_NEED,
    FeedbackReason.BOUGHT_WANT,
    FeedbackReason.PAUSED,
    FeedbackReason.GOAL_PLACED -> R.string.savings_feedback_deposit_title
}

/** «Почему»: сколько осталось до цели и примерный срок; снятие — без осуждения. */
@Composable
private fun feedbackWhy(feedback: SavingsFeedback): String {
    val title = feedback.goalTitle
    val weeks = feedback.weeks
    val goalLine = when {
        title == null -> null

        weeks == 0 -> stringResource(R.string.savings_feedback_why_reached, title)

        weeks == null -> null

        else -> stringResource(
            R.string.savings_feedback_why_weeks,
            title,
            feedback.left.amount,
            pluralStringResource(R.plurals.savings_feedback_weeks, weeks, weeks)
        )
    }
    return when (feedback.feedback.reason) {
        FeedbackReason.WITHDREW -> listOfNotNull(stringResource(R.string.savings_feedback_withdraw_prefix), goalLine)
            .joinToString(" ")

        FeedbackReason.DEPOSITED,
        FeedbackReason.BOUGHT_NEED,
        FeedbackReason.BOUGHT_WANT,
        FeedbackReason.PAUSED,
        FeedbackReason.GOAL_PLACED -> goalLine ?: stringResource(CoreR.string.feedback_why_deposit)
    }
}

private object PreviewActions : SavingsActions {
    override fun onDepositClick() = Unit
    override fun onWithdrawClick() = Unit
    override fun onAmountStep(up: Boolean) = Unit
    override fun onSheetConfirm() = Unit
    override fun onSheetDismiss() = Unit
    override fun onFeedbackDismiss() = Unit
    override fun onPlaceGoalClick() = Unit
    override fun onRitualPlaceClick() = Unit
    override fun onRitualDismiss() = Unit
    override fun onGoHomeClick() = Unit
}

internal val PreviewGoals = listOf(
    Goal("plaid", "Плед", Dzynki(60)),
    Goal("lamp", "Лампа для уголка", Dzynki(100)),
    Goal("window", "Окно с видом", Dzynki(200)),
    Goal("scooter", "Самокат", Dzynki(300))
)

internal val PreviewContent = SavingsUiState.Content(
    look = PetLook(PetFur.LILAC, PetHat.CAP),
    stage = PetStage.SPRY,
    savings = Dzynki(40),
    wantsLeft = Dzynki(60),
    placedGoalIds = setOf("plaid"),
    stars = 1,
    progress = GoalProgress(PreviewGoals[1], Dzynki(40), Dzynki(60), weeks = 2, weeklyAverage = 30),
    goals = listOf(
        GoalItem(PreviewGoals[0], GoalStatus.PLACED, Dzynki(60)),
        GoalItem(PreviewGoals[1], GoalStatus.CURRENT, Dzynki(40)),
        GoalItem(PreviewGoals[2], GoalStatus.NEXT, Dzynki.ZERO),
        GoalItem(PreviewGoals[3], GoalStatus.NEXT, Dzynki.ZERO)
    )
)

@Preview(heightDp = 1100)
@Composable
private fun SavingsScreenPreview() {
    FinEduPreview {
        SavingsScreen(
            state = PreviewContent,
            actions = PreviewActions,
            onHelp = {},
            onParent = {},
            onTabSelect = {}
        )
    }
}

@Preview(heightDp = 1100)
@Composable
private fun SavingsScreenReachedPreview() {
    FinEduPreview {
        SavingsScreen(
            state = PreviewContent.copy(
                savings = Dzynki(110),
                wantsLeft = Dzynki.ZERO,
                progress = GoalProgress(PreviewGoals[1], Dzynki(100), Dzynki.ZERO, weeks = 0, weeklyAverage = 30)
            ),
            actions = PreviewActions,
            onHelp = {},
            onParent = {},
            onTabSelect = {}
        )
    }
}

@Preview(heightDp = 1100)
@Composable
private fun SavingsScreenNoTermPreview() {
    FinEduPreview {
        SavingsScreen(
            state = PreviewContent.copy(
                savings = Dzynki.ZERO,
                placedGoalIds = emptySet(),
                progress = GoalProgress(PreviewGoals[0], Dzynki.ZERO, Dzynki(60), weeks = null, weeklyAverage = 0)
            ),
            actions = PreviewActions,
            onHelp = {},
            onParent = {},
            onTabSelect = {}
        )
    }
}

@Preview(heightDp = 1100)
@Composable
private fun SavingsScreenAllPlacedPreview() {
    FinEduPreview {
        SavingsScreen(
            state = PreviewContent.copy(
                progress = null,
                placedGoalIds = PreviewGoals.map { it.id }.toSet(),
                goals = PreviewGoals.map { GoalItem(it, GoalStatus.PLACED, it.price) }
            ),
            actions = PreviewActions,
            onHelp = {},
            onParent = {},
            onTabSelect = {}
        )
    }
}

@Preview
@Composable
private fun SavingsScreenLoadingPreview() {
    FinEduPreview {
        SavingsScreen(
            state = SavingsUiState.Loading,
            actions = PreviewActions,
            onHelp = {},
            onParent = {},
            onTabSelect = {}
        )
    }
}

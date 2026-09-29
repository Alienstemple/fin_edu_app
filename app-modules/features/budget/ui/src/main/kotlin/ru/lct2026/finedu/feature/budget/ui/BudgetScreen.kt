package ru.lct2026.finedu.feature.budget.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.lct2026.finedu.feature.budget.ui.component.DistributionBar
import ru.lct2026.finedu.feature.budget.ui.component.PlanFactBar
import ru.lct2026.finedu.productcore.domain.model.Bag
import ru.lct2026.finedu.productcore.domain.model.BagAmounts
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.GameEngine
import ru.lct2026.finedu.productcore.domain.model.IncomeSource
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.PetStage
import ru.lct2026.finedu.productcore.ui.components.AmountStepper
import ru.lct2026.finedu.productcore.ui.components.BagIcon
import ru.lct2026.finedu.productcore.ui.components.DzynkiAmount
import ru.lct2026.finedu.productcore.ui.components.FinButton
import ru.lct2026.finedu.productcore.ui.components.FinButtonStyle
import ru.lct2026.finedu.productcore.ui.components.FinTopBar
import ru.lct2026.finedu.productcore.ui.components.GlassStyle
import ru.lct2026.finedu.productcore.ui.components.SpeechBubble
import ru.lct2026.finedu.productcore.ui.components.color
import ru.lct2026.finedu.productcore.ui.components.glass
import ru.lct2026.finedu.productcore.ui.components.labelRes
import ru.lct2026.finedu.productcore.ui.illustration.PetMood
import ru.lct2026.finedu.productcore.ui.illustration.PetView
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview
import ru.lct2026.finedu.productcore.ui.sound.PlaySoundOnce
import ru.lct2026.finedu.productcore.ui.sound.SoundEffect

@Composable
internal fun BudgetRoute(
    onBack: () -> Unit,
    onHelp: () -> Unit,
    onParent: () -> Unit,
    onShop: () -> Unit,
    onHome: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    BudgetScreen(
        state = state,
        onBack = onBack,
        onHelp = onHelp,
        onParent = onParent,
        onMinusClick = viewModel::onMinusClick,
        onPlusClick = viewModel::onPlusClick,
        onFixClick = viewModel::onFixClick,
        onShop = onShop,
        onHome = onHome
    )
}

@Composable
internal fun BudgetScreen(
    state: BudgetUiState,
    onBack: () -> Unit,
    onHelp: () -> Unit,
    onParent: () -> Unit,
    onMinusClick: (Bag) -> Unit,
    onPlusClick: (Bag) -> Unit,
    onFixClick: () -> Unit,
    onShop: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            FinTopBar(
                title = stringResource(R.string.budget_title),
                onBack = onBack,
                onHelp = onHelp,
                onParent = onParent
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            state.questReward?.let { QuestRewardBanner(it) }
            when (val mode = state.mode) {
                BudgetMode.Loading -> Unit

                is BudgetMode.Distribute -> DistributeContent(
                    mode = mode,
                    look = state.look,
                    stage = state.stage,
                    onMinusClick = onMinusClick,
                    onPlusClick = onPlusClick,
                    onFixClick = onFixClick
                )

                is BudgetMode.Fixed -> FixedContent(
                    mode = mode,
                    look = state.look,
                    stage = state.stage,
                    onShop = onShop,
                    onHome = onHome
                )
            }
        }
    }
}

@Composable
private fun DistributeContent(
    mode: BudgetMode.Distribute,
    look: PetLook,
    stage: PetStage,
    onMinusClick: (Bag) -> Unit,
    onPlusClick: (Bag) -> Unit,
    onFixClick: () -> Unit
) {
    val left = mode.left
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glass()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.budget_income_title),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        DzynkiAmount(
            amount = Dzynki(mode.income),
            style = MaterialTheme.typography.displaySmall,
            coinSize = 32.dp
        )
        if (mode.sources.isNotEmpty()) {
            Text(text = sourcesText(mode.sources), style = MaterialTheme.typography.bodyMedium)
        }
        DistributionBar(
            draft = mode.draft,
            total = mode.income,
            description = stringResource(
                R.string.budget_bar_a11y,
                mode.draft.needs.amount,
                mode.draft.wants.amount,
                mode.draft.savings.amount,
                left
            ),
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = stringResource(R.string.budget_left, left, mode.income),
            style = MaterialTheme.typography.titleMedium
        )
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glass()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Bag.entries.forEach { bag ->
            val value = mode.draft[bag]
            val label = stringResource(bag.labelRes)
            val description = stringResource(R.string.budget_bag_a11y, label, value.amount)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BagIcon(bag = bag)
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = description }
                )
                AmountStepper(
                    value = value,
                    onMinus = { onMinusClick(bag) },
                    onPlus = { onPlusClick(bag) },
                    canMinus = value > Dzynki.ZERO,
                    canPlus = left > 0
                )
            }
        }
    }
    DzynLine(
        look = look,
        stage = stage,
        mood = if (left == 0) PetMood.JOY else PetMood.THINKING,
        text = if (left == 0 && mode.draft == GameEngine.suggestPlan(Dzynki(mode.income))) {
            stringResource(R.string.budget_dzyn_suggested)
        } else if (left == 0) {
            stringResource(R.string.budget_dzyn_full)
        } else {
            stringResource(R.string.budget_dzyn_left, left)
        }
    )
    FinButton(text = stringResource(R.string.budget_fix), onClick = onFixClick, enabled = left == 0)
}

@Composable
private fun FixedContent(
    mode: BudgetMode.Fixed,
    look: PetLook,
    stage: PetStage,
    onShop: () -> Unit,
    onHome: () -> Unit
) {
    Text(
        text = stringResource(R.string.budget_fixed_title),
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.semantics { heading() }
    )
    Text(
        text = stringResource(R.string.budget_plan_vs_fact),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    val max = Bag.entries.maxOf { maxOf(mode.plan[it].amount, mode.actual[it].amount) }
    Bag.entries.forEach { bag ->
        FactCard(bag = bag, plan = mode.plan[bag].amount, fact = mode.actual[bag].amount, left = mode.left[bag], max)
    }
    val isDone = mode.actual.needs >= mode.plan.needs && mode.actual.wants >= mode.plan.wants
    DzynLine(
        look = look,
        stage = stage,
        mood = if (isDone) PetMood.PROUD else PetMood.HAPPY,
        text = stringResource(if (isDone) R.string.budget_dzyn_done else R.string.budget_dzyn_behind)
    )
    FinButton(text = stringResource(R.string.budget_to_shop), onClick = onShop)
    FinButton(text = stringResource(R.string.budget_to_home), onClick = onHome, style = FinButtonStyle.Secondary)
}

@Composable
private fun FactCard(bag: Bag, plan: Int, fact: Int, left: Dzynki, max: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glass()
            .semantics(mergeDescendants = true) {}
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BagIcon(bag = bag)
            Text(
                text = stringResource(bag.labelRes),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            val rest = when (bag) {
                Bag.NEEDS, Bag.WANTS -> stringResource(R.string.budget_rest, left.amount)
                Bag.SAVINGS -> stringResource(R.string.budget_in_savings, left.amount)
            }
            Text(text = rest, style = MaterialTheme.typography.bodyMedium)
        }
        PlanFactBar(plan = plan, fact = fact, max = max, color = bag.color)
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.budget_plan, plan),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val factText = when (bag) {
                Bag.NEEDS, Bag.WANTS -> stringResource(R.string.budget_spent, fact)
                Bag.SAVINGS -> stringResource(R.string.budget_saved, fact)
            }
            Text(text = factText, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun DzynLine(look: PetLook, stage: PetStage, mood: PetMood, text: String) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PetView(look = look, mood = mood, stage = stage, modifier = Modifier.width(72.dp))
        SpeechBubble(text = text, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun QuestRewardBanner(reward: QuestReward) {
    PlaySoundOnce(SoundEffect.INCOME, reward)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glass(style = GlassStyle.Strong)
            .semantics(mergeDescendants = true) {}
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(R.string.budget_quest_reward, reward.amount, reward.title),
            style = MaterialTheme.typography.titleMedium
        )
        Text(text = stringResource(R.string.budget_quest_reward_hint), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun sourcesText(sources: List<IncomeSource>): String {
    val names = sources.map { source ->
        when (source) {
            IncomeSource.POCKET_MONEY -> stringResource(R.string.budget_source_pocket)
            IncomeSource.PARENT_BONUS -> stringResource(R.string.budget_source_bonus)
            IncomeSource.QUEST_REWARD -> stringResource(R.string.budget_source_reward)
        }
    }
    return names.joinToString(separator = stringResource(R.string.budget_source_separator))
}

@Preview(heightDp = 800)
@Composable
private fun BudgetDistributePreview() {
    FinEduPreview {
        BudgetScreen(
            state = BudgetUiState(
                mode = BudgetMode.Distribute(
                    income = 300,
                    sources = listOf(IncomeSource.POCKET_MONEY),
                    draft = BagAmounts(Dzynki(150), Dzynki(90), Dzynki(20))
                )
            ),
            onBack = {},
            onHelp = {},
            onParent = {},
            onMinusClick = {},
            onPlusClick = {},
            onFixClick = {},
            onShop = {},
            onHome = {}
        )
    }
}

@Preview(heightDp = 800)
@Composable
private fun BudgetRewardPreview() {
    FinEduPreview {
        BudgetScreen(
            state = BudgetUiState(
                mode = BudgetMode.Distribute(
                    income = 20,
                    sources = listOf(IncomeSource.QUEST_REWARD),
                    draft = BagAmounts(savings = Dzynki(20))
                ),
                questReward = QuestReward(amount = 20, title = "Разложи дзыньки по мешочкам")
            ),
            onBack = {},
            onHelp = {},
            onParent = {},
            onMinusClick = {},
            onPlusClick = {},
            onFixClick = {},
            onShop = {},
            onHome = {}
        )
    }
}

@Preview(heightDp = 900)
@Composable
private fun BudgetFixedPreview() {
    FinEduPreview {
        BudgetScreen(
            state = BudgetUiState(
                mode = BudgetMode.Fixed(
                    plan = BagAmounts(Dzynki(150), Dzynki(120), Dzynki(30)),
                    actual = BagAmounts(Dzynki(80), Dzynki(60), Dzynki(30)),
                    left = BagAmounts(Dzynki(70), Dzynki(60), Dzynki(30))
                )
            ),
            onBack = {},
            onHelp = {},
            onParent = {},
            onMinusClick = {},
            onPlusClick = {},
            onFixClick = {},
            onShop = {},
            onHome = {}
        )
    }
}

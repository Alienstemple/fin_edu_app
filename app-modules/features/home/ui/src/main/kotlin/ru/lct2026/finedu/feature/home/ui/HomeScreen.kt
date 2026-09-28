package ru.lct2026.finedu.feature.home.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.lct2026.finedu.productcore.domain.model.Bag
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.GameRules
import ru.lct2026.finedu.productcore.domain.model.Pet
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.PetStat
import ru.lct2026.finedu.productcore.navigation.api.FinEduRoute
import ru.lct2026.finedu.productcore.ui.R as CoreR
import ru.lct2026.finedu.productcore.ui.components.BagChip
import ru.lct2026.finedu.productcore.ui.components.DzynkiAmount
import ru.lct2026.finedu.productcore.ui.components.FinBottomBar
import ru.lct2026.finedu.productcore.ui.components.FinButton
import ru.lct2026.finedu.productcore.ui.components.FinTab
import ru.lct2026.finedu.productcore.ui.components.FinTopBar
import ru.lct2026.finedu.productcore.ui.components.GlassStyle
import ru.lct2026.finedu.productcore.ui.components.MinTouchTarget
import ru.lct2026.finedu.productcore.ui.components.SpeechBubble
import ru.lct2026.finedu.productcore.ui.components.TubeIndicator
import ru.lct2026.finedu.productcore.ui.components.color
import ru.lct2026.finedu.productcore.ui.components.dzynkiText
import ru.lct2026.finedu.productcore.ui.components.glass
import ru.lct2026.finedu.productcore.ui.components.iconRes
import ru.lct2026.finedu.productcore.ui.illustration.PetMood
import ru.lct2026.finedu.productcore.ui.illustration.PetView
import ru.lct2026.finedu.productcore.ui.illustration.RoomItems
import ru.lct2026.finedu.productcore.ui.illustration.RoomScene
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

@Composable
internal fun HomeRoute(
    onNavigate: (FinEduRoute) -> Unit,
    onSelectTab: (FinTab) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onNavigate = onNavigate,
        onSelectTab = onSelectTab,
        onPetClick = viewModel::onPetClick,
        onPetLongPress = viewModel::onPetLongPress
    )
}

@Composable
internal fun HomeScreen(
    state: HomeUiState,
    onNavigate: (FinEduRoute) -> Unit,
    onSelectTab: (FinTab) -> Unit,
    onPetClick: () -> Unit,
    onPetLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            when (state) {
                HomeUiState.Loading -> Unit
                is HomeUiState.Content -> HomeTopBar(state = state, onNavigate = onNavigate)
            }
        },
        bottomBar = {
            FinBottomBar(
                selected = FinTab.HOME,
                onSelect = onSelectTab,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    ) { innerPadding ->
        when (state) {
            HomeUiState.Loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(R.string.home_loading), style = MaterialTheme.typography.bodyMedium)
            }

            is HomeUiState.Content -> HomeContent(
                state = state,
                onNavigate = onNavigate,
                onPetClick = onPetClick,
                onPetLongPress = onPetLongPress,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun HomeTopBar(state: HomeUiState.Content, onNavigate: (FinEduRoute) -> Unit) {
    Column {
        FinTopBar(
            title = state.weekTitle ?: stringResource(R.string.home_title_default),
            subtitle = if (state.weekNumber <= GameRules.DEMO_WEEKS) {
                stringResource(R.string.home_week_of, state.weekNumber, GameRules.DEMO_WEEKS)
            } else {
                stringResource(R.string.home_week, state.weekNumber)
            },
            onHelp = { onNavigate(FinEduRoute.Glossary) },
            onParent = { onNavigate(FinEduRoute.Parent) }
        )
        if (state.isDemo) {
            Text(
                text = stringResource(R.string.home_demo),
                style = MaterialTheme.typography.bodyMedium,
                color = FinEduTheme.colors.gold,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .glass(shape = CircleShape, style = GlassStyle.Strong)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState.Content,
    onNavigate: (FinEduRoute) -> Unit,
    onPetClick: () -> Unit,
    onPetLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HomeScene(state = state, onPetClick = onPetClick, onPetLongPress = onPetLongPress)
        state.notice?.let { HomeNoticeBanner(notice = it) }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            PetStat.entries.forEach { stat ->
                TubeIndicator(stat = stat, value = state.pet[stat], showValue = !state.isYounger)
            }
        }
        if (state.unallocated > Dzynki.ZERO) {
            FinButton(
                text = stringResource(R.string.home_distribute, dzynkiText(state.unallocated)),
                onClick = { onNavigate(FinEduRoute.Budget) }
            )
        }
        BalanceCard(state = state, onClick = { onNavigate(FinEduRoute.Budget) })
        GoalCard(goal = state.goal, isYounger = state.isYounger, onClick = { onNavigate(FinEduRoute.Savings) })
        if (!state.isYounger) QuestStrip(quest = state.quest, onNavigate = onNavigate)
        FinButton(
            text = stringResource(R.string.home_finish_week),
            onClick = { onNavigate(FinEduRoute.PeriodSummary) },
            modifier = if (state.isYounger) Modifier.heightIn(min = YoungerButtonHeight) else Modifier
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeScene(state: HomeUiState.Content, onPetClick: () -> Unit, onPetLongPress: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(SceneHeight)
            .glass()
    ) {
        RoomScene(
            placedGoalIds = state.placedGoalIds,
            stars = state.stars,
            dim = state.isDim,
            modifier = Modifier.fillMaxSize()
        ) { petModifier ->
            PetView(
                look = state.look,
                mood = state.mood,
                stage = state.pet.stage,
                contentDescription = stringResource(R.string.home_pet_a11y),
                modifier = petModifier.combinedClickable(onClick = onPetClick, onLongClick = onPetLongPress)
            )
        }
        SpeechBubble(
            text = stringResource(state.line.textRes),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        )
    }
}

@Composable
private fun HomeNoticeBanner(notice: HomeNotice) {
    val text = when (notice) {
        HomeNotice.Return -> stringResource(R.string.home_notice_return)
        is HomeNotice.Low -> stringResource(notice.stat.lowNoticeRes)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glass(style = GlassStyle.Strong)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val stat = when (notice) {
            HomeNotice.Return -> null
            is HomeNotice.Low -> notice.stat
        }
        if (stat != null) {
            Icon(
                painter = painterResource(stat.iconRes),
                contentDescription = null,
                tint = stat.color,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun BalanceCard(state: HomeUiState.Content, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glass()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (state.isYounger) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                BigAmount(label = stringResource(R.string.home_balance), amount = state.balance)
                BigAmount(label = stringResource(CoreR.string.bag_savings), amount = state.savings)
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.home_balance),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(end = 12.dp)
                )
                DzynkiAmount(amount = state.balance, modifier = Modifier.weight(1f))
                if (state.unallocated == Dzynki.ZERO) {
                    Text(
                        text = stringResource(R.string.home_plan_link),
                        style = MaterialTheme.typography.bodyMedium,
                        color = FinEduTheme.colors.gold
                    )
                }
            }
            Bag.entries.forEach { bag -> BagChip(bag = bag, amount = state.amountIn(bag)) }
        }
    }
}

@Composable
private fun BigAmount(label: String, amount: Dzynki) {
    Column(modifier = Modifier.semantics(mergeDescendants = true) {}) {
        Text(text = label, style = MaterialTheme.typography.titleMedium)
        DzynkiAmount(amount = amount, style = MaterialTheme.typography.displaySmall, coinSize = 32.dp)
    }
}

@Composable
private fun GoalCard(goal: HomeGoal?, isYounger: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glass()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (goal == null) {
            Text(text = stringResource(R.string.home_goals_done), style = MaterialTheme.typography.titleMedium)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isYounger) {
                        stringResource(R.string.home_goal_younger, goal.title)
                    } else {
                        stringResource(R.string.home_goal_progress, goal.title, goal.saved.amount, goal.price.amount)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                if (!isYounger) {
                    Text(
                        text = goalWeeksText(goal.weeksLeft),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            LinearProgressIndicator(
                progress = { (goal.saved.amount.toFloat() / goal.price.amount).coerceIn(0f, 1f) },
                color = FinEduTheme.colors.savings,
                trackColor = FinEduTheme.colors.glassBorder,
                drawStopIndicator = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
        }
    }
}

@Composable
private fun goalWeeksText(weeksLeft: Int?): String = when (weeksLeft) {
    null -> stringResource(R.string.home_goal_weeks_unknown)
    0 -> stringResource(R.string.home_goal_reached)
    else -> pluralStringResource(R.plurals.home_goal_weeks, weeksLeft, weeksLeft)
}

@Composable
private fun QuestStrip(quest: HomeQuest?, onNavigate: (FinEduRoute) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = MinTouchTarget)
            .glass()
            .clickable { onNavigate(quest?.let { FinEduRoute.Quest(it.id) } ?: FinEduRoute.Quests) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(CoreR.drawable.ic_nav_quests),
            contentDescription = null,
            tint = FinEduTheme.colors.gold,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = quest?.let { stringResource(R.string.home_quest, it.title) }
                ?: stringResource(R.string.home_quests_done),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = painterResource(CoreR.drawable.ic_chevron_right),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
    }
}

private fun HomeUiState.Content.amountIn(bag: Bag): Dzynki = when (bag) {
    Bag.NEEDS -> needsLeft
    Bag.WANTS -> wantsLeft
    Bag.SAVINGS -> savings
}

private val HomeLine.textRes: Int
    get() = when (this) {
        HomeLine.NORMAL -> R.string.home_line_normal
        HomeLine.UNALLOCATED -> R.string.home_line_unallocated
        HomeLine.LOW -> R.string.home_line_low
        HomeLine.RETURN -> R.string.home_line_return
        HomeLine.EASTER_EGG -> R.string.home_line_easter_egg
    }

private val PetStat.lowNoticeRes: Int
    get() = when (this) {
        PetStat.CHARGE -> R.string.home_notice_low_charge
        PetStat.VIBE -> R.string.home_notice_low_vibe
        PetStat.CALM -> R.string.home_notice_low_calm
    }

private val SceneHeight = 240.dp
private val YoungerButtonHeight = 72.dp

private val previewState = HomeUiState.Content(
    weekNumber = 2,
    weekTitle = "Хочу всё",
    isDemo = false,
    isYounger = false,
    look = PetLook(PetFur.LILAC, PetHat.CAP),
    pet = Pet(charge = 70, vibe = 60, calm = 50, xp = 5),
    mood = PetMood.NEUTRAL,
    line = HomeLine.NORMAL,
    notice = null,
    isDim = false,
    placedGoalIds = setOf(RoomItems.PLAID),
    stars = 1,
    balance = Dzynki(130),
    needsLeft = Dzynki(70),
    wantsLeft = Dzynki(60),
    savings = Dzynki(40),
    unallocated = Dzynki.ZERO,
    goal = HomeGoal(title = "Лампа для уголка", saved = Dzynki(40), price = Dzynki(100), weeksLeft = 6),
    quest = HomeQuest(id = "ad", title = "«Никто: / Реклама:»")
)

@Composable
private fun HomePreview(state: HomeUiState) {
    FinEduPreview {
        HomeScreen(state = state, onNavigate = {}, onSelectTab = {}, onPetClick = {}, onPetLongPress = {})
    }
}

@Preview(heightDp = 1100)
@Composable
private fun HomeScreenPreview() {
    HomePreview(previewState)
}

@Preview(heightDp = 1100)
@Composable
private fun HomeScreenStartPreview() {
    HomePreview(
        previewState.copy(
            weekNumber = 1,
            weekTitle = "Первая зарплата",
            isDemo = true,
            line = HomeLine.UNALLOCATED,
            balance = Dzynki.ZERO,
            needsLeft = Dzynki.ZERO,
            wantsLeft = Dzynki.ZERO,
            savings = Dzynki.ZERO,
            unallocated = Dzynki(300),
            goal = HomeGoal(title = "Плед", saved = Dzynki.ZERO, price = Dzynki(60), weeksLeft = null)
        )
    )
}

@Preview(heightDp = 1100)
@Composable
private fun HomeScreenLowPreview() {
    HomePreview(
        previewState.copy(
            pet = Pet(charge = 20, vibe = 30, calm = 40),
            mood = PetMood.COLD,
            line = HomeLine.LOW,
            notice = HomeNotice.Low(PetStat.CHARGE),
            isDim = true,
            balance = Dzynki.ZERO,
            needsLeft = Dzynki.ZERO,
            wantsLeft = Dzynki.ZERO
        )
    )
}

@Preview(heightDp = 1100)
@Composable
private fun HomeScreenReturnPreview() {
    HomePreview(
        previewState.copy(
            pet = Pet(charge = 60, vibe = 60, calm = 50),
            mood = PetMood.HAPPY,
            line = HomeLine.RETURN,
            notice = HomeNotice.Return
        )
    )
}

@Preview(heightDp = 1000)
@Composable
private fun HomeScreenYoungerPreview() {
    HomePreview(previewState.copy(isYounger = true))
}

@Preview(heightDp = 1100)
@Composable
private fun HomeScreenEasterEggPreview() {
    HomePreview(
        previewState.copy(
            mood = PetMood.SHOCK,
            line = HomeLine.EASTER_EGG,
            placedGoalIds = emptySet(),
            goal = null,
            quest = null
        )
    )
}

@Preview
@Composable
private fun HomeScreenLoadingPreview() {
    HomePreview(HomeUiState.Loading)
}

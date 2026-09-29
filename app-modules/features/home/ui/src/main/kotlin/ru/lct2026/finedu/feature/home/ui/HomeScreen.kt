package ru.lct2026.finedu.feature.home.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.lct2026.finedu.feature.home.ui.component.BubbleField
import ru.lct2026.finedu.feature.home.ui.component.CollapsedBubble
import ru.lct2026.finedu.feature.home.ui.component.ExpandedBubble
import ru.lct2026.finedu.feature.home.ui.component.lowNoticeRes
import ru.lct2026.finedu.feature.home.ui.component.slot
import ru.lct2026.finedu.feature.home.ui.component.style
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.GameRules
import ru.lct2026.finedu.productcore.domain.model.Pet
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.PetStat
import ru.lct2026.finedu.productcore.navigation.api.FinEduRoute
import ru.lct2026.finedu.productcore.ui.components.FinBottomBar
import ru.lct2026.finedu.productcore.ui.components.FinTab
import ru.lct2026.finedu.productcore.ui.components.FinTopBar
import ru.lct2026.finedu.productcore.ui.components.GlassStyle
import ru.lct2026.finedu.productcore.ui.components.SpeechBubble
import ru.lct2026.finedu.productcore.ui.components.color
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
        onPetLongPress = viewModel::onPetLongPress,
        onBubbleClick = viewModel::onBubbleClick,
        onBubbleDismiss = viewModel::onBubbleDismiss
    )
}

/**
 * Главный: уголок Дзыня на весь экран, поверх — прозрачные шапка и нижнее меню, а вокруг питомца — пузырьки
 * с показателями, деньгами, целью, заданием и итогами недели.
 */
@Composable
internal fun HomeScreen(
    state: HomeUiState,
    onNavigate: (FinEduRoute) -> Unit,
    onSelectTab: (FinTab) -> Unit,
    onPetClick: () -> Unit,
    onPetLongPress: () -> Unit,
    onBubbleClick: (HomeBubble) -> Unit,
    onBubbleDismiss: () -> Unit,
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

            is HomeUiState.Content -> BubbleField(
                bubbles = state.bubbles,
                openBubble = state.openBubble,
                slot = { it.slot(state.isYounger) },
                style = { it.style(state) },
                onBubbleClick = onBubbleClick,
                onDismiss = onBubbleDismiss,
                contentPadding = innerPadding,
                background = { HomeRoom(state = state, onPetClick = onPetClick, onPetLongPress = onPetLongPress) },
                header = { HomeHeader(state) },
                collapsed = { CollapsedBubble(bubble = it, state = state) },
                expanded = {
                    ExpandedBubble(bubble = it, state = state, onNavigate = onNavigate, onSelectTab = onSelectTab)
                },
                collapseLabel = stringResource(R.string.home_bubble_collapse),
                modifier = Modifier.fillMaxSize()
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeRoom(state: HomeUiState.Content, onPetClick: () -> Unit, onPetLongPress: () -> Unit) {
    RoomScene(
        placedGoalIds = state.placedGoalIds,
        stars = state.stars,
        dim = state.isDim,
        fillScreen = true,
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
}

/** Над кольцом пузырьков: реплика Дзыня и плашка о возвращении или низком показателе. */
@Composable
private fun HomeHeader(state: HomeUiState.Content) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SpeechBubble(text = stringResource(state.line.textRes))
        state.notice?.let { HomeNoticeBanner(notice = it) }
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

private val HomeLine.textRes: Int
    get() = when (this) {
        HomeLine.NORMAL -> R.string.home_line_normal
        HomeLine.UNALLOCATED -> R.string.home_line_unallocated
        HomeLine.LOW -> R.string.home_line_low
        HomeLine.RETURN -> R.string.home_line_return
        HomeLine.EASTER_EGG -> R.string.home_line_easter_egg
    }

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
        HomeScreen(
            state = state,
            onNavigate = {},
            onSelectTab = {},
            onPetClick = {},
            onPetLongPress = {},
            onBubbleClick = {},
            onBubbleDismiss = {}
        )
    }
}

@Preview(heightDp = 860)
@Composable
private fun HomeScreenPreview() {
    HomePreview(previewState)
}

@Preview(heightDp = 860)
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

@Preview(heightDp = 860)
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

@Preview(heightDp = 860)
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

@Preview(heightDp = 860)
@Composable
private fun HomeScreenYoungerPreview() {
    HomePreview(previewState.copy(isYounger = true))
}

@Preview(heightDp = 860)
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

@Preview(heightDp = 860)
@Composable
private fun HomeScreenOpenBubblePreview() {
    HomePreview(previewState.copy(openBubble = HomeBubble.Balance))
}

@Preview
@Composable
private fun HomeScreenLoadingPreview() {
    HomePreview(HomeUiState.Loading)
}

package ru.lct2026.finedu.feature.learn.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.lct2026.finedu.feature.learn.ui.component.ShortsFeed
import ru.lct2026.finedu.feature.learn.ui.component.ShortsPlayer
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.FeedCard
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.ShortsFrame
import ru.lct2026.finedu.productcore.ui.components.FinTopBar
import ru.lct2026.finedu.productcore.ui.components.MinTouchTarget
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview

@Composable
internal fun ShortsRoute(onBack: () -> Unit, viewModel: ShortsViewModel = hiltViewModel()) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    ShortsScreen(
        state = state,
        onBack = onBack,
        actions = ShortsActions(
            onModeClick = viewModel::onModeClick,
            onPreviousFrameClick = viewModel::onPreviousFrameClick,
            onNextFrameClick = viewModel::onNextFrameClick,
            onLikeClick = viewModel::onLikeClick,
            onUnderstoodClick = viewModel::onUnderstoodClick,
            onNextCardClick = viewModel::onNextCardClick
        )
    )
}

/** Действия экрана шортсов — одним объектом, чтобы не раздувать сигнатуры. */
internal class ShortsActions(
    val onModeClick: (ShortsMode) -> Unit = {},
    val onPreviousFrameClick: () -> Unit = {},
    val onNextFrameClick: () -> Unit = {},
    val onLikeClick: () -> Unit = {},
    val onUnderstoodClick: () -> Unit = {},
    val onNextCardClick: () -> Unit = {}
)

@Composable
internal fun ShortsScreen(
    state: ShortsUiState,
    onBack: () -> Unit,
    actions: ShortsActions,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = { FinTopBar(title = stringResource(R.string.shorts_title), onBack = onBack) }
    ) { padding ->
        when (state) {
            ShortsUiState.Loading -> Unit

            is ShortsUiState.Content -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ModeSwitch(mode = state.mode, onModeClick = actions.onModeClick)
                when (state.mode) {
                    ShortsMode.PLAYER -> ShortsPlayer(state = state, actions = actions)

                    ShortsMode.FEED -> ShortsFeed(
                        state = state,
                        onWatchClick = { actions.onModeClick(ShortsMode.PLAYER) },
                        onNextCardClick = actions.onNextCardClick
                    )
                }
            }
        }
    }
}

/** Переключатель «Смотреть / Лента». */
@Composable
private fun ModeSwitch(mode: ShortsMode, onModeClick: (ShortsMode) -> Unit) {
    Row(modifier = Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ShortsMode.entries.forEach { item ->
            FilterChip(
                selected = item == mode,
                onClick = { onModeClick(item) },
                label = { Text(text = stringResource(item.labelRes), style = MaterialTheme.typography.titleSmall) },
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier
                    .heightIn(min = MinTouchTarget)
                    .semantics { role = Role.Tab }
            )
        }
    }
}

private val ShortsMode.labelRes: Int
    get() = when (this) {
        ShortsMode.PLAYER -> R.string.shorts_mode_player
        ShortsMode.FEED -> R.string.shorts_mode_feed
    }

private val PreviewContent = ShortsUiState.Content(
    title = "Дзынь объясняет",
    frames = listOf(
        ShortsFrame("Шаг 1:", "первая шляпа. Солидно.", "Шляпа за 100. Одна. Пока всё под контролем."),
        ShortsFrame("POV:", "ты купил третью шляпу", "Три шляпы, одна голова. Математика не сходится.")
    ),
    feed = listOf(
        FeedCard("Доход", "Откуда берутся дзыньки — и почему не из воздуха."),
        FeedCard("Бюджет", "План, куда пойдут деньги, пока они не ушли сами.")
    ),
    look = PetLook(PetFur.MINT, PetHat.CAP)
)

@Preview
@Composable
private fun ShortsPlayerPreview() {
    FinEduPreview {
        ShortsScreen(state = PreviewContent.copy(frameIndex = 1, isLiked = true), onBack = {
        }, actions = ShortsActions())
    }
}

@Preview
@Composable
private fun ShortsFeedPreview() {
    FinEduPreview {
        ShortsScreen(
            state = PreviewContent.copy(
                mode = ShortsMode.FEED,
                reward = ShortsReward(Dzynki(10), "Шортс: Дзынь объясняет за 15 секунд")
            ),
            onBack = {},
            actions = ShortsActions()
        )
    }
}

@Preview
@Composable
private fun ShortsLoadingPreview() {
    FinEduPreview {
        ShortsScreen(state = ShortsUiState.Loading, onBack = {}, actions = ShortsActions())
    }
}

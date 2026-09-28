package ru.lct2026.finedu.feature.quests.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
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
import ru.lct2026.finedu.feature.quests.ui.component.ChoiceQuest
import ru.lct2026.finedu.feature.quests.ui.component.ScamQuest
import ru.lct2026.finedu.productcore.ui.components.FinTopBar
import ru.lct2026.finedu.productcore.ui.event.CloseScreenEvent
import ru.lct2026.finedu.productcore.ui.event.ObserveEvents
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview

@Composable
internal fun QuestRoute(
    questId: String,
    onBack: () -> Unit,
    viewModel: QuestViewModel = hiltViewModel<QuestViewModel, QuestViewModel.Factory>(
        key = questId,
        creationCallback = { factory -> factory.create(questId) }
    )
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    ObserveEvents(viewModel.events) { event ->
        when (event) {
            CloseScreenEvent -> onBack()
        }
    }
    QuestScreen(
        state = state,
        onBack = onBack,
        onOptionClick = viewModel::onOptionClick,
        onScamAnswer = viewModel::onScamAnswer,
        onScamNextClick = viewModel::onScamNextClick,
        onFinishClick = viewModel::onFinishClick
    )
}

@Composable
internal fun QuestScreen(
    state: QuestUiState,
    onBack: () -> Unit,
    onOptionClick: (String) -> Unit,
    onScamAnswer: (ScamAnswer) -> Unit,
    onScamNextClick: () -> Unit,
    onFinishClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            val (title, subtitle) = state.titles()
            FinTopBar(title = title, subtitle = subtitle, onBack = onBack, modifier = Modifier.padding(top = 8.dp))
        }
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
        when (state) {
            QuestUiState.Loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            is QuestUiState.Choice -> ChoiceQuest(
                state = state,
                onOptionClick = onOptionClick,
                onDoneClick = onFinishClick,
                modifier = contentModifier
            )

            is QuestUiState.Scam -> ScamQuest(
                state = state,
                onAnswer = onScamAnswer,
                onNextClick = onScamNextClick,
                onFinishClick = onFinishClick,
                modifier = contentModifier
            )
        }
    }
}

/** Заголовок и подзаголовок шапки для текущего шага. */
@Composable
private fun QuestUiState.titles(): Pair<String, String?> = when (this) {
    QuestUiState.Loading -> stringResource(R.string.quests_title) to null

    is QuestUiState.Choice -> when {
        result != null -> stringResource(R.string.quest_review_title) to quest.title
        isAd -> quest.title to stringResource(R.string.quest_tag_purchases)
        else -> quest.title to null
    }

    is QuestUiState.Scam -> {
        val total = quest.messages.size
        when (step) {
            is ScamStep.Message -> quest.title to stringResource(R.string.quest_scam_counter, step.index + 1, total)

            is ScamStep.Review ->
                stringResource(R.string.quest_review_title) to
                    stringResource(R.string.quest_scam_counter, step.index + 1, total)

            is ScamStep.Summary ->
                stringResource(R.string.quest_scam_summary_title) to
                    stringResource(R.string.quest_scam_counter, total, total)
        }
    }
}

@Preview
@Composable
private fun QuestScreenLoadingPreview() {
    FinEduPreview {
        QuestScreen(
            state = QuestUiState.Loading,
            onBack = {},
            onOptionClick = {},
            onScamAnswer = {},
            onScamNextClick = {},
            onFinishClick = {}
        )
    }
}

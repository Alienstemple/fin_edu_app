package ru.lct2026.finedu.feature.quests.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.PetStage
import ru.lct2026.finedu.productcore.domain.model.QuestLevel
import ru.lct2026.finedu.productcore.domain.model.QuestTheme
import ru.lct2026.finedu.productcore.navigation.api.FinEduRoute
import ru.lct2026.finedu.productcore.ui.R as CoreR
import ru.lct2026.finedu.productcore.ui.components.FinBottomBar
import ru.lct2026.finedu.productcore.ui.components.FinButton
import ru.lct2026.finedu.productcore.ui.components.FinButtonStyle
import ru.lct2026.finedu.productcore.ui.components.FinTab
import ru.lct2026.finedu.productcore.ui.components.FinTopBar
import ru.lct2026.finedu.productcore.ui.components.GlassStyle
import ru.lct2026.finedu.productcore.ui.components.MinTouchTarget
import ru.lct2026.finedu.productcore.ui.components.dzynkiText
import ru.lct2026.finedu.productcore.ui.components.glass
import ru.lct2026.finedu.productcore.ui.event.ObserveEvents
import ru.lct2026.finedu.productcore.ui.illustration.PetMood
import ru.lct2026.finedu.productcore.ui.illustration.PetView
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

@Composable
internal fun QuestsRoute(
    onNavigate: (FinEduRoute) -> Unit,
    onTabSelect: (FinTab) -> Unit,
    viewModel: QuestsViewModel = hiltViewModel()
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    ObserveEvents(viewModel.events) { event ->
        when (event) {
            is OpenRouteEvent -> onNavigate(event.route)
        }
    }
    QuestsScreen(
        state = state,
        onQuestClick = viewModel::onQuestClick,
        onChallengeJoinedChange = viewModel::onChallengeJoinedChange,
        onHelpClick = { onNavigate(FinEduRoute.Glossary) },
        onParentClick = { onNavigate(FinEduRoute.Parent) },
        onTabSelect = onTabSelect
    )
}

@Composable
internal fun QuestsScreen(
    state: QuestsUiState,
    onQuestClick: (String) -> Unit,
    onChallengeJoinedChange: (Boolean) -> Unit,
    onHelpClick: () -> Unit,
    onParentClick: () -> Unit,
    onTabSelect: (FinTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            FinTopBar(
                title = stringResource(R.string.quests_title),
                onHelp = onHelpClick,
                onParent = onParentClick,
                modifier = Modifier.padding(top = 8.dp)
            )
        },
        bottomBar = {
            FinBottomBar(
                selected = FinTab.QUESTS,
                onSelect = onTabSelect,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    ) { innerPadding ->
        when (state) {
            QuestsUiState.Loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            is QuestsUiState.Content -> QuestsContent(
                state = state,
                onQuestClick = onQuestClick,
                onChallengeJoinedChange = onChallengeJoinedChange,
                contentPadding = innerPadding
            )
        }
    }
}

@Composable
private fun QuestsContent(
    state: QuestsUiState.Content,
    onQuestClick: (String) -> Unit,
    onChallengeJoinedChange: (Boolean) -> Unit,
    contentPadding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { WeekCard(state) }
        state.groups.forEach { group ->
            item(key = "theme_${group.theme}") { ThemeHeader(group.theme) }
            items(group.quests, key = { it.id }) { quest ->
                QuestCard(quest = quest, onClick = { onQuestClick(quest.id) })
            }
        }
        item {
            ChallengeCard(
                look = state.petLook,
                stage = state.petStage,
                isJoined = state.isChallengeJoined,
                onJoinedChange = onChallengeJoinedChange
            )
        }
    }
}

@Composable
private fun WeekCard(state: QuestsUiState.Content) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glass(style = GlassStyle.Strong)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.quests_week_label),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (state.weekTitle != null) {
                    stringResource(R.string.quests_week_title, state.weekNumber, state.weekTitle)
                } else {
                    stringResource(R.string.quests_week_number, state.weekNumber)
                },
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() }
            )
            if (state.weekTagline != null) {
                Text(text = state.weekTagline, style = MaterialTheme.typography.bodyMedium)
            }
        }
        PetView(
            look = state.petLook,
            stage = state.petStage,
            mood = PetMood.HAPPY,
            modifier = Modifier.width(80.dp)
        )
    }
}

@Composable
private fun ThemeHeader(theme: QuestTheme) {
    val (nameRes, noteRes) = when (theme) {
        QuestTheme.BUDGET -> R.string.quests_theme_budget to R.string.quests_theme_budget_note
        QuestTheme.SAVINGS -> R.string.quests_theme_savings to R.string.quests_theme_savings_note
        QuestTheme.PURCHASES -> R.string.quests_theme_purchases to R.string.quests_theme_purchases_note
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = stringResource(nameRes),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            text = stringResource(noteRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QuestCard(quest: QuestCardItem, onClick: () -> Unit) {
    val levelText = when (quest.level) {
        QuestLevel.EASY -> stringResource(R.string.quests_level_easy)
        QuestLevel.MEDIUM -> stringResource(R.string.quests_level_medium)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = MinTouchTarget)
            .glass()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = quest.title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(R.string.quests_level, levelText),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (quest.isCompleted) {
                    stringResource(R.string.quests_reward_received, dzynkiText(quest.reward))
                } else {
                    stringResource(R.string.quests_reward, dzynkiText(quest.reward))
                },
                style = MaterialTheme.typography.bodyMedium,
                color = FinEduTheme.colors.goldLight
            )
        }
        QuestStatus(isCompleted = quest.isCompleted)
    }
}

/** Статус — иконка и подпись, не только цвет. */
@Composable
private fun QuestStatus(isCompleted: Boolean) {
    val color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else FinEduTheme.colors.gold
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(if (isCompleted) CoreR.drawable.ic_check else CoreR.drawable.ic_chevron_right),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = stringResource(if (isCompleted) R.string.quests_status_done else R.string.quests_status_available),
            style = MaterialTheme.typography.bodyMedium,
            color = color
        )
    }
}

@Composable
private fun ChallengeCard(look: PetLook, stage: PetStage, isJoined: Boolean, onJoinedChange: (Boolean) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .glass()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
            Text(
                text = stringResource(R.string.quests_challenge_label),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() }
            )
            Text(
                text = stringResource(R.string.quests_challenge_optional),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.quests_challenge_title),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(text = stringResource(R.string.quests_challenge_text), style = MaterialTheme.typography.bodyMedium)
            }
            PetView(
                look = look,
                stage = stage,
                mood = if (isJoined) PetMood.PROUD else PetMood.THINKING,
                modifier = Modifier.width(72.dp)
            )
        }
        if (isJoined) {
            Text(text = stringResource(R.string.quests_challenge_joined), style = MaterialTheme.typography.bodyMedium)
            FinButton(
                text = stringResource(R.string.quests_challenge_leave),
                onClick = { onJoinedChange(false) },
                style = FinButtonStyle.Secondary
            )
        } else {
            FinButton(text = stringResource(R.string.quests_challenge_join), onClick = { onJoinedChange(true) })
            Text(
                text = stringResource(R.string.quests_challenge_join_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private val previewState = QuestsUiState.Content(
    weekNumber = 2,
    weekTitle = "Хочу всё",
    weekTagline = "Всё и сразу. Что может пойти не так?",
    petLook = PetLook(PetFur.LILAC, PetHat.CAP),
    petStage = PetStage.SPRY,
    groups = listOf(
        QuestGroup(
            QuestTheme.BUDGET,
            listOf(
                QuestCardItem("three_bags", "Три мешочка — один план", QuestLevel.EASY, Dzynki(20), true),
                QuestCardItem("shorts", "Шортс: Дзынь объясняет за 15 секунд", QuestLevel.EASY, Dzynki(10), false)
            )
        ),
        QuestGroup(
            QuestTheme.SAVINGS,
            listOf(QuestCardItem("scam", "Это развод?", QuestLevel.MEDIUM, Dzynki(30), false))
        )
    ),
    isChallengeJoined = false
)

@Preview
@Composable
private fun QuestsScreenPreview() {
    FinEduPreview {
        QuestsScreen(
            state = previewState,
            onQuestClick = {},
            onChallengeJoinedChange = {},
            onHelpClick = {},
            onParentClick = {},
            onTabSelect = {}
        )
    }
}

@Preview
@Composable
private fun QuestsScreenChallengePreview() {
    FinEduPreview {
        ChallengeCard(
            look = PetLook(PetFur.MINT, PetHat.NONE),
            stage = PetStage.BABY,
            isJoined = true,
            onJoinedChange = {}
        )
    }
}

@Preview
@Composable
private fun QuestsScreenLoadingPreview() {
    FinEduPreview {
        QuestsScreen(
            state = QuestsUiState.Loading,
            onQuestClick = {},
            onChallengeJoinedChange = {},
            onHelpClick = {},
            onParentClick = {},
            onTabSelect = {}
        )
    }
}

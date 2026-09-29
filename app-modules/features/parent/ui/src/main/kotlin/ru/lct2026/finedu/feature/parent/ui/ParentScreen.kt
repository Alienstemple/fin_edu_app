package ru.lct2026.finedu.feature.parent.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.lct2026.finedu.feature.parent.domain.model.ParentGate
import ru.lct2026.finedu.feature.parent.ui.component.ArticleContent
import ru.lct2026.finedu.feature.parent.ui.component.ArticlesTab
import ru.lct2026.finedu.feature.parent.ui.component.ParentBarrier
import ru.lct2026.finedu.feature.parent.ui.component.ParentTabBar
import ru.lct2026.finedu.feature.parent.ui.component.PreviewArticles
import ru.lct2026.finedu.feature.parent.ui.component.PreviewProgress
import ru.lct2026.finedu.feature.parent.ui.component.PreviewSchemes
import ru.lct2026.finedu.feature.parent.ui.component.ProgressTab
import ru.lct2026.finedu.feature.parent.ui.component.ScamsTab
import ru.lct2026.finedu.feature.parent.ui.component.SettingsTab
import ru.lct2026.finedu.feature.parent.ui.component.labelRes
import ru.lct2026.finedu.productcore.domain.model.AgeMode
import ru.lct2026.finedu.productcore.domain.model.Article
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.ui.components.ConfirmDialog
import ru.lct2026.finedu.productcore.ui.components.FinTopBar
import ru.lct2026.finedu.productcore.ui.event.ObserveEvents
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview

@Composable
internal fun ParentRoute(
    onBack: () -> Unit,
    onOpenHome: () -> Unit,
    onOpenOnboarding: () -> Unit,
    viewModel: ParentViewModel = hiltViewModel()
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    ObserveEvents(viewModel.events) { event ->
        when (event) {
            ParentEvent.OpenHome -> onOpenHome()
            ParentEvent.OpenOnboarding -> onOpenOnboarding()
            else -> Unit
        }
    }
    ParentScreen(
        state = state,
        onBack = onBack,
        onDigitClick = viewModel::onDigitClick,
        onEraseClick = viewModel::onEraseClick,
        onDoneClick = viewModel::onDoneClick,
        onTabSelect = viewModel::onTabSelect,
        onArticleClick = viewModel::onArticleClick,
        onArticleClose = viewModel::onArticleClose,
        onSchemeToggle = viewModel::onSchemeToggle,
        onBonusMinusClick = viewModel::onBonusMinusClick,
        onBonusPlusClick = viewModel::onBonusPlusClick,
        onBonusReasonSelect = viewModel::onBonusReasonSelect,
        onGrantBonusClick = viewModel::onGrantBonusClick,
        onAgeModeSelect = viewModel::onAgeModeSelect,
        onCalmModeToggle = viewModel::onCalmModeToggle,
        onLargeFontToggle = viewModel::onLargeFontToggle,
        onVolumeChange = viewModel::onVolumeChange,
        onMusicToggle = viewModel::onMusicToggle,
        onResetClick = viewModel::onResetClick,
        onDeleteClick = viewModel::onDeleteClick,
        onDialogConfirm = viewModel::onDialogConfirm,
        onDialogDismiss = viewModel::onDialogDismiss
    )
}

@Composable
internal fun ParentScreen(
    state: ParentUiState,
    onBack: () -> Unit,
    onDigitClick: (Int) -> Unit,
    onEraseClick: () -> Unit,
    onDoneClick: () -> Unit,
    onTabSelect: (ParentTab) -> Unit,
    onArticleClick: (Article) -> Unit,
    onArticleClose: () -> Unit,
    onSchemeToggle: (String) -> Unit,
    onBonusMinusClick: () -> Unit,
    onBonusPlusClick: () -> Unit,
    onBonusReasonSelect: (BonusReason) -> Unit,
    onGrantBonusClick: (String) -> Unit,
    onAgeModeSelect: (AgeMode) -> Unit,
    onCalmModeToggle: () -> Unit,
    onLargeFontToggle: () -> Unit,
    onVolumeChange: (Int) -> Unit,
    onMusicToggle: () -> Unit,
    onResetClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDialogConfirm: () -> Unit,
    onDialogDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val article = state.openedArticle
    BackHandler(enabled = article != null, onBack = onArticleClose)
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            when {
                !state.isUnlocked -> Unit

                article != null -> FinTopBar(
                    title = stringResource(R.string.parent_article_back),
                    onBack = onArticleClose
                )

                else -> FinTopBar(
                    title = stringResource(state.tab.labelRes),
                    subtitle = stringResource(R.string.parent_subtitle),
                    onBack = onBack
                )
            }
        },
        bottomBar = {
            if (state.isUnlocked) {
                ParentTabBar(
                    selected = state.tab,
                    onSelect = onTabSelect,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    ) { innerPadding ->
        if (!state.isUnlocked) {
            ParentBarrier(
                gate = state.gate,
                input = state.input,
                isWrongAnswer = state.isWrongAnswer,
                petLook = state.petLook,
                onBack = onBack,
                onDigitClick = onDigitClick,
                onEraseClick = onEraseClick,
                onDoneClick = onDoneClick,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            key(state.tab, article?.id) {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    when (state.tab) {
                        ParentTab.PROGRESS -> state.progress?.let { ProgressTab(progress = it) }

                        ParentTab.ARTICLES -> if (article != null) {
                            ArticleContent(article = article, onClose = onArticleClose)
                        } else {
                            ArticlesTab(articles = state.articles, onArticleClick = onArticleClick)
                        }

                        ParentTab.SCAMS -> ScamsTab(
                            schemes = state.scamSchemes,
                            discussedIds = state.discussedSchemeIds,
                            talkQuestions = state.talkQuestions,
                            onSchemeToggle = onSchemeToggle
                        )

                        ParentTab.SETTINGS -> SettingsTab(
                            bonus = state.bonus,
                            settings = state.settings,
                            onBonusMinusClick = onBonusMinusClick,
                            onBonusPlusClick = onBonusPlusClick,
                            onBonusReasonSelect = onBonusReasonSelect,
                            onGrantBonusClick = onGrantBonusClick,
                            onAgeModeSelect = onAgeModeSelect,
                            onCalmModeToggle = onCalmModeToggle,
                            onLargeFontToggle = onLargeFontToggle,
                            onVolumeChange = onVolumeChange,
                            onMusicToggle = onMusicToggle,
                            onResetClick = onResetClick,
                            onDeleteClick = onDeleteClick
                        )
                    }
                }
            }
        }
    }
    state.dialog?.let { dialog ->
        ParentConfirmDialog(
            dialog = dialog,
            playerName = state.progress?.playerName.orEmpty(),
            onConfirm = onDialogConfirm,
            onDismiss = onDialogDismiss
        )
    }
}

@Composable
private fun ParentConfirmDialog(
    dialog: ParentDialog,
    playerName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    when (dialog) {
        ParentDialog.RESET -> ConfirmDialog(
            title = stringResource(R.string.parent_reset_title),
            text = stringResource(R.string.parent_reset_lead),
            items = listOf(
                stringResource(R.string.parent_reset_item_weeks),
                stringResource(R.string.parent_reset_item_bags),
                stringResource(R.string.parent_reset_item_room)
            ),
            note = stringResource(R.string.parent_reset_note),
            confirmText = stringResource(R.string.parent_reset_confirm),
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )

        ParentDialog.DELETE -> ConfirmDialog(
            title = stringResource(R.string.parent_delete_title),
            text = stringResource(R.string.parent_delete_lead, playerName),
            items = listOf(
                stringResource(R.string.parent_delete_item_room),
                stringResource(R.string.parent_delete_item_bags),
                stringResource(R.string.parent_delete_item_quests),
                stringResource(R.string.parent_delete_item_settings)
            ),
            note = stringResource(R.string.parent_delete_note),
            confirmText = stringResource(R.string.parent_delete_confirm),
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun PreviewParentScreen(state: ParentUiState) {
    FinEduPreview {
        ParentScreen(
            state = state,
            onBack = {},
            onDigitClick = {},
            onEraseClick = {},
            onDoneClick = {},
            onTabSelect = {},
            onArticleClick = {},
            onArticleClose = {},
            onSchemeToggle = {},
            onBonusMinusClick = {},
            onBonusPlusClick = {},
            onBonusReasonSelect = {},
            onGrantBonusClick = {},
            onAgeModeSelect = {},
            onCalmModeToggle = {},
            onLargeFontToggle = {},
            onVolumeChange = {},
            onMusicToggle = {},
            onResetClick = {},
            onDeleteClick = {},
            onDialogConfirm = {},
            onDialogDismiss = {}
        )
    }
}

private val PreviewState = ParentUiState(
    gate = ParentGate(7, 8),
    isUnlocked = true,
    petLook = PetLook(PetFur.LILAC, PetHat.NONE),
    progress = PreviewProgress,
    articles = PreviewArticles,
    scamSchemes = PreviewSchemes
)

@Preview
@Composable
private fun ParentBarrierScreenPreview() {
    PreviewParentScreen(PreviewState.copy(isUnlocked = false, input = "56"))
}

@Preview
@Composable
private fun ParentDashboardPreview() {
    PreviewParentScreen(PreviewState)
}

@Preview
@Composable
private fun ParentArticlesPreview() {
    PreviewParentScreen(PreviewState.copy(tab = ParentTab.ARTICLES))
}

@Preview
@Composable
private fun ParentArticlePreview() {
    PreviewParentScreen(PreviewState.copy(tab = ParentTab.ARTICLES, openedArticle = PreviewArticles.first()))
}

@Preview
@Composable
private fun ParentScamsPreview() {
    PreviewParentScreen(PreviewState.copy(tab = ParentTab.SCAMS, discussedSchemeIds = setOf("sms")))
}

@Preview
@Composable
private fun ParentSettingsPreview() {
    PreviewParentScreen(PreviewState.copy(tab = ParentTab.SETTINGS))
}

@Preview
@Composable
private fun ParentConfirmPreview() {
    PreviewParentScreen(PreviewState.copy(tab = ParentTab.SETTINGS, dialog = ParentDialog.RESET))
}

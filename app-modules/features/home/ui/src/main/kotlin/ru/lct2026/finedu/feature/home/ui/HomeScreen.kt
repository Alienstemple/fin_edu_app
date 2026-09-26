package ru.lct2026.finedu.feature.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.lct2026.finedu.productcore.navigation.api.FinEduRoute
import ru.lct2026.finedu.productcore.ui.components.StubAction
import ru.lct2026.finedu.productcore.ui.components.StubScreen
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview

@Composable
internal fun HomeRoute(onNavigate: (FinEduRoute) -> Unit, viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    HomeScreen(state = state, onNavigate = onNavigate)
}

@Composable
internal fun HomeScreen(state: HomeUiState, onNavigate: (FinEduRoute) -> Unit, modifier: Modifier = Modifier) {
    val description = when (state) {
        HomeUiState.Loading -> stringResource(R.string.home_loading)

        is HomeUiState.Content -> stringResource(
            R.string.home_summary,
            state.balance,
            state.savings,
            state.periodNumber
        )
    }
    StubScreen(
        title = stringResource(R.string.home_title),
        description = description,
        actions = listOf(
            StubAction(stringResource(R.string.home_action_budget)) { onNavigate(FinEduRoute.Budget) },
            StubAction(stringResource(R.string.home_action_shop)) { onNavigate(FinEduRoute.Shop) },
            StubAction(stringResource(R.string.home_action_savings)) { onNavigate(FinEduRoute.Savings) },
            StubAction(stringResource(R.string.home_action_quests)) { onNavigate(FinEduRoute.Quests) },
            StubAction(stringResource(R.string.home_action_finish_period)) { onNavigate(FinEduRoute.PeriodSummary) },
            StubAction(stringResource(R.string.home_action_parent)) { onNavigate(FinEduRoute.Parent) },
            StubAction(stringResource(R.string.home_action_help)) { onNavigate(FinEduRoute.Onboarding) }
        ),
        modifier = modifier
    )
}

@PreviewLightDark
@Composable
private fun HomeScreenPreview() {
    FinEduPreview {
        HomeScreen(
            state = HomeUiState.Content(balance = 100, savings = 20, periodNumber = 1),
            onNavigate = {}
        )
    }
}

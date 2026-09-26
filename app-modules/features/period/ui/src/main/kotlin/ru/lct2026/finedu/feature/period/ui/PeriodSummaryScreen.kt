package ru.lct2026.finedu.feature.period.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import ru.lct2026.finedu.feature.period.ui.R
import ru.lct2026.finedu.productcore.navigation.api.FinEduRoute
import ru.lct2026.finedu.productcore.ui.components.StubAction
import ru.lct2026.finedu.productcore.ui.components.StubScreen
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview

@Composable
internal fun PeriodSummaryScreen(onNavigate: (FinEduRoute) -> Unit, modifier: Modifier = Modifier) {
    StubScreen(
        title = stringResource(R.string.period_summary_title),
        description = stringResource(R.string.period_summary_description),
        actions = listOf(
            StubAction(stringResource(R.string.period_summary_action_next_period)) { onNavigate(FinEduRoute.Home) }
        ),
        modifier = modifier
    )
}

@PreviewLightDark
@Composable
private fun PeriodSummaryScreenPreview() {
    FinEduPreview {
        PeriodSummaryScreen(onNavigate = {})
    }
}

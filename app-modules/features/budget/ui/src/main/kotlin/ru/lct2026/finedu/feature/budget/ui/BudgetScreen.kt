package ru.lct2026.finedu.feature.budget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ru.lct2026.finedu.feature.budget.ui.R
import ru.lct2026.finedu.productcore.ui.components.StubAction
import ru.lct2026.finedu.productcore.ui.components.StubScreen
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview

@Composable
internal fun BudgetScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    StubScreen(
        title = stringResource(R.string.budget_title),
        description = stringResource(R.string.budget_description),
        actions = listOf(
            StubAction(stringResource(R.string.budget_action_back)) { onBack() }
        ),
        modifier = modifier
    )
}

@Preview
@Composable
private fun BudgetScreenPreview() {
    FinEduPreview {
        BudgetScreen(onBack = {})
    }
}

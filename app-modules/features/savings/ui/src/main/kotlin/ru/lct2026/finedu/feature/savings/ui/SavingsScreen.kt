package ru.lct2026.finedu.feature.savings.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import ru.lct2026.finedu.feature.savings.ui.R
import ru.lct2026.finedu.productcore.ui.components.StubAction
import ru.lct2026.finedu.productcore.ui.components.StubScreen
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview

@Composable
internal fun SavingsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    StubScreen(
        title = stringResource(R.string.savings_title),
        description = stringResource(R.string.savings_description),
        actions = listOf(
            StubAction(stringResource(R.string.savings_action_back)) { onBack() }
        ),
        modifier = modifier
    )
}

@PreviewLightDark
@Composable
private fun SavingsScreenPreview() {
    FinEduPreview {
        SavingsScreen(onBack = {})
    }
}

package ru.lct2026.finedu.feature.parent.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ru.lct2026.finedu.feature.parent.ui.R
import ru.lct2026.finedu.productcore.ui.components.StubAction
import ru.lct2026.finedu.productcore.ui.components.StubScreen
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview

@Composable
internal fun ParentScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    StubScreen(
        title = stringResource(R.string.parent_title),
        description = stringResource(R.string.parent_description),
        actions = listOf(
            StubAction(stringResource(R.string.parent_action_back)) { onBack() }
        ),
        modifier = modifier
    )
}

@Preview
@Composable
private fun ParentScreenPreview() {
    FinEduPreview {
        ParentScreen(onBack = {})
    }
}

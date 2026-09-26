package ru.lct2026.finedu.feature.shop.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import ru.lct2026.finedu.feature.shop.ui.R
import ru.lct2026.finedu.productcore.ui.components.StubAction
import ru.lct2026.finedu.productcore.ui.components.StubScreen
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview

@Composable
internal fun ShopScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    StubScreen(
        title = stringResource(R.string.shop_title),
        description = stringResource(R.string.shop_description),
        actions = listOf(
            StubAction(stringResource(R.string.shop_action_back)) { onBack() }
        ),
        modifier = modifier
    )
}

@PreviewLightDark
@Composable
private fun ShopScreenPreview() {
    FinEduPreview {
        ShopScreen(onBack = {})
    }
}

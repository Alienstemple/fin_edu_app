package ru.lct2026.finedu.feature.hero.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import ru.lct2026.finedu.feature.hero.ui.R
import ru.lct2026.finedu.productcore.navigation.api.FinEduRoute
import ru.lct2026.finedu.productcore.ui.components.StubAction
import ru.lct2026.finedu.productcore.ui.components.StubScreen
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview

@Composable
internal fun HeroScreen(onNavigate: (FinEduRoute) -> Unit, modifier: Modifier = Modifier) {
    StubScreen(
        title = stringResource(R.string.hero_title),
        description = stringResource(R.string.hero_description),
        actions = listOf(
            StubAction(stringResource(R.string.hero_action_done)) { onNavigate(FinEduRoute.Home) }
        ),
        modifier = modifier
    )
}

@PreviewLightDark
@Composable
private fun HeroScreenPreview() {
    FinEduPreview {
        HeroScreen(onNavigate = {})
    }
}

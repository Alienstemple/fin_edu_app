package ru.lct2026.finedu.feature.onboarding.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import ru.lct2026.finedu.feature.onboarding.ui.R
import ru.lct2026.finedu.productcore.navigation.api.FinEduRoute
import ru.lct2026.finedu.productcore.ui.components.StubAction
import ru.lct2026.finedu.productcore.ui.components.StubScreen
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview

@Composable
internal fun OnboardingScreen(onNavigate: (FinEduRoute) -> Unit, modifier: Modifier = Modifier) {
    StubScreen(
        title = stringResource(R.string.onboarding_title),
        description = stringResource(R.string.onboarding_description),
        actions = listOf(
            StubAction(stringResource(R.string.onboarding_action_create_hero)) { onNavigate(FinEduRoute.Hero) }
        ),
        modifier = modifier
    )
}

@PreviewLightDark
@Composable
private fun OnboardingScreenPreview() {
    FinEduPreview {
        OnboardingScreen(onNavigate = {})
    }
}

package ru.lct2026.finedu.feature.quests.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import ru.lct2026.finedu.feature.quests.ui.R
import ru.lct2026.finedu.productcore.navigation.api.FinEduRoute
import ru.lct2026.finedu.productcore.ui.components.StubAction
import ru.lct2026.finedu.productcore.ui.components.StubScreen
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview

// TODO: заменить на задания из контента (JSON в assets)
private const val DEMO_QUEST_ID = "demo-quest"

@Composable
internal fun QuestsScreen(onNavigate: (FinEduRoute) -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier) {
    StubScreen(
        title = stringResource(R.string.quests_title),
        description = stringResource(R.string.quests_description),
        actions = listOf(
            StubAction(stringResource(R.string.quests_action_open_demo_quest)) {
                onNavigate(FinEduRoute.Quest(questId = DEMO_QUEST_ID))
            },
            StubAction(stringResource(R.string.quests_action_back)) { onBack() }
        ),
        modifier = modifier
    )
}

@PreviewLightDark
@Composable
private fun QuestsScreenPreview() {
    FinEduPreview {
        QuestsScreen(onNavigate = {}, onBack = {})
    }
}

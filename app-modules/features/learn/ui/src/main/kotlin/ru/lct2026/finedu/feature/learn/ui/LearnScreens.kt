package ru.lct2026.finedu.feature.learn.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ru.lct2026.finedu.productcore.ui.components.StubAction
import ru.lct2026.finedu.productcore.ui.components.StubScreen

// TODO: заглушки — экраны сверстать по «Дзынь · 2026» (Glossary, Stories), см. docs/implementation-plan.md, этап 4.8.

@Composable
internal fun GlossaryScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    StubScreen(
        title = stringResource(R.string.glossary_title),
        description = "",
        actions = listOf(StubAction(stringResource(R.string.learn_action_back)) { onBack() }),
        modifier = modifier
    )
}

@Composable
internal fun ShortsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    StubScreen(
        title = stringResource(R.string.shorts_title),
        description = "",
        actions = listOf(StubAction(stringResource(R.string.learn_action_back)) { onBack() }),
        modifier = modifier
    )
}

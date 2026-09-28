package ru.lct2026.finedu.feature.learn.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.feature.learn.ui.R
import ru.lct2026.finedu.feature.learn.ui.ShortsActions
import ru.lct2026.finedu.feature.learn.ui.ShortsUiState
import ru.lct2026.finedu.productcore.ui.R as CoreR
import ru.lct2026.finedu.productcore.ui.components.FinButton
import ru.lct2026.finedu.productcore.ui.components.FinButtonStyle
import ru.lct2026.finedu.productcore.ui.components.GlassStyle
import ru.lct2026.finedu.productcore.ui.components.MinTouchTarget
import ru.lct2026.finedu.productcore.ui.components.glass
import ru.lct2026.finedu.productcore.ui.illustration.PetMood
import ru.lct2026.finedu.productcore.ui.illustration.PetView
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

/**
 * Плеер шортса: полоски по кадрам, кадр с Дзынем и субтитрами. Тап по левой/правой половине кадра — назад/вперёд;
 * для TalkBack те же действия продублированы кнопками.
 */
@Composable
internal fun ShortsPlayer(state: ShortsUiState.Content, actions: ShortsActions) {
    val frame = state.frames.getOrNull(state.frameIndex)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FrameProgress(current = state.frameIndex, total = state.frames.size)
        Text(
            text = stringResource(R.string.shorts_player_label, state.title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (frame != null) {
            Box {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glass(shape = MaterialTheme.shapes.extraLarge),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 20.dp)
                    ) {
                        Text(
                            text = frame.kicker,
                            style = MaterialTheme.typography.titleMedium,
                            color = FinEduTheme.colors.gold
                        )
                        Text(
                            text = frame.headline,
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier.semantics { heading() }
                        )
                    }
                    PetView(
                        look = state.look,
                        mood = FrameMoods[state.frameIndex % FrameMoods.size],
                        modifier = Modifier
                            .width(180.dp)
                            .padding(vertical = 8.dp)
                    )
                    Text(
                        text = frame.subtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .glass(shape = MaterialTheme.shapes.extraLarge, style = GlassStyle.Strong)
                            .padding(horizontal = 18.dp, vertical = 14.dp)
                    )
                }
                TapZones(
                    onPrevious = actions.onPreviousFrameClick,
                    onNext = actions.onNextFrameClick,
                    modifier = Modifier.matchParentSize()
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FinButton(
                text = stringResource(R.string.shorts_previous),
                onClick = actions.onPreviousFrameClick,
                style = FinButtonStyle.Secondary,
                enabled = state.frameIndex > 0,
                modifier = Modifier.weight(1f)
            )
            FinButton(
                text = stringResource(R.string.shorts_next),
                onClick = actions.onNextFrameClick,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ToggleChip(
                text = stringResource(R.string.shorts_like),
                selected = state.isLiked,
                onClick = actions.onLikeClick
            )
            ToggleChip(
                text = stringResource(R.string.shorts_understood),
                selected = state.isUnderstood,
                onClick = actions.onUnderstoodClick
            )
        }
    }
}

@Composable
private fun FrameProgress(current: Int, total: Int) {
    val description = stringResource(R.string.shorts_progress_a11y, current + 1, total)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(
                        color = if (index <= current) FinEduTheme.colors.gold else FinEduTheme.colors.glassBorder,
                        shape = CircleShape
                    )
            )
        }
    }
}

/** Невидимые половины кадра без семантики: для TalkBack есть кнопки «Назад» и «Дальше». */
@Composable
private fun TapZones(onPrevious: () -> Unit, onNext: () -> Unit, modifier: Modifier = Modifier) {
    val previous by rememberUpdatedState(onPrevious)
    val next by rememberUpdatedState(onNext)
    Row(modifier = modifier) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .pointerInput(Unit) { detectTapGestures { previous() } }
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .pointerInput(Unit) { detectTapGestures { next() } }
        )
    }
}

/** Переключатель без счётчика: отметка видна галочкой, не только цветом. */
@Composable
private fun ToggleChip(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = text, style = MaterialTheme.typography.titleSmall) },
        leadingIcon = if (selected) {
            {
                Icon(
                    painter = painterResource(CoreR.drawable.ic_check),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            null
        },
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.heightIn(min = MinTouchTarget)
    )
}

/** Эмоция Дзыня по кадру: гордится шляпой → шок от третьей → считает → доволен лайфхаком. */
private val FrameMoods = listOf(PetMood.PROUD, PetMood.SHOCK, PetMood.THINKING, PetMood.HAPPY)

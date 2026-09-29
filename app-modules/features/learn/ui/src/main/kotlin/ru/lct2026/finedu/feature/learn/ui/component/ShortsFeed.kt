package ru.lct2026.finedu.feature.learn.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.feature.learn.ui.R
import ru.lct2026.finedu.feature.learn.ui.ShortsReward
import ru.lct2026.finedu.feature.learn.ui.ShortsUiState
import ru.lct2026.finedu.productcore.ui.R as CoreR
import ru.lct2026.finedu.productcore.ui.components.FinButton
import ru.lct2026.finedu.productcore.ui.components.FinButtonStyle
import ru.lct2026.finedu.productcore.ui.components.GlassStyle
import ru.lct2026.finedu.productcore.ui.components.dzynkiText
import ru.lct2026.finedu.productcore.ui.components.glass
import ru.lct2026.finedu.productcore.ui.illustration.PetMood
import ru.lct2026.finedu.productcore.ui.illustration.PetView
import ru.lct2026.finedu.productcore.ui.sound.PlaySoundOnce
import ru.lct2026.finedu.productcore.ui.sound.SoundEffect
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

/** Лента «Дзынь объясняет за 15 секунд»: одна карточка за раз, «Следующая» и свайп влево. */
@Composable
internal fun ShortsFeed(state: ShortsUiState.Content, onWatchClick: () -> Unit, onNextCardClick: () -> Unit) {
    val card = state.feed.getOrNull(state.cardIndex)
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        state.reward?.let { RewardBanner(it) }
        Text(
            text = stringResource(R.string.feed_subtitle),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() }
        )
        if (card != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glass(shape = MaterialTheme.shapes.extraLarge)
                    .swipeLeft(onNextCardClick)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.feed_position, state.cardIndex + 1, state.feed.size),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                PetView(
                    look = state.look,
                    mood = CardMoods[state.cardIndex % CardMoods.size],
                    modifier = Modifier
                        .width(140.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Text(text = card.title, style = MaterialTheme.typography.headlineMedium)
                Text(text = card.hook, style = MaterialTheme.typography.bodyLarge)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FinButton(
                        text = stringResource(R.string.feed_watch),
                        onClick = onWatchClick,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = stringResource(R.string.feed_duration),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        FeedDots(current = state.cardIndex, total = state.feed.size)
        FinButton(
            text = stringResource(R.string.feed_next),
            onClick = onNextCardClick,
            style = FinButtonStyle.Secondary
        )
        Text(
            text = stringResource(R.string.feed_swipe_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun RewardBanner(reward: ShortsReward) {
    PlaySoundOnce(SoundEffect.INCOME, reward)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glass(style = GlassStyle.Strong)
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter = painterResource(CoreR.drawable.ic_coin),
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.shorts_reward, dzynkiText(reward.amount), reward.questTitle),
                style = MaterialTheme.typography.titleSmall,
                color = FinEduTheme.colors.gold
            )
            Text(text = stringResource(R.string.shorts_reward_hint), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Точки-индикаторы: номер карточки уже прочитан в «1 из 4», поэтому для TalkBack они скрыты. */
@Composable
private fun FeedDots(current: Int, total: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics {},
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
    ) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .size(width = if (index == current) 24.dp else 8.dp, height = 8.dp)
                    .background(
                        color = if (index == current) FinEduTheme.colors.gold else FinEduTheme.colors.glassBorder,
                        shape = CircleShape
                    )
            )
        }
    }
}

/** Свайп влево по карточке — следующая. Кнопка «Следующая» остаётся основным способом. */
@Composable
private fun Modifier.swipeLeft(onSwipe: () -> Unit): Modifier {
    val currentOnSwipe by rememberUpdatedState(onSwipe)
    val threshold = with(LocalDensity.current) { SwipeThreshold.toPx() }
    return pointerInput(threshold) {
        var total = 0f
        detectHorizontalDragGestures(
            onDragStart = { total = 0f },
            onDragEnd = { if (total < -threshold) currentOnSwipe() }
        ) { _, dragAmount -> total += dragAmount }
    }
}

private val SwipeThreshold = 64.dp

/** Эмоции Дзыня по карточкам: доход — радость, бюджет — гордость, реклама — шок, подушка — объятие. */
private val CardMoods = listOf(PetMood.HAPPY, PetMood.PROUD, PetMood.SHOCK, PetMood.HAPPY)

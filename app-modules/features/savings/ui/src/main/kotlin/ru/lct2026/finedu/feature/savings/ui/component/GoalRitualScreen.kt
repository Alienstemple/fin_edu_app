package ru.lct2026.finedu.feature.savings.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.feature.savings.ui.GoalProgress
import ru.lct2026.finedu.feature.savings.ui.GoalRitual
import ru.lct2026.finedu.feature.savings.ui.R
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.Goal
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.PetStage
import ru.lct2026.finedu.productcore.ui.components.FinButton
import ru.lct2026.finedu.productcore.ui.components.GlassStyle
import ru.lct2026.finedu.productcore.ui.components.SpeechBubble
import ru.lct2026.finedu.productcore.ui.components.glass
import ru.lct2026.finedu.productcore.ui.illustration.PetMood
import ru.lct2026.finedu.productcore.ui.illustration.PetView
import ru.lct2026.finedu.productcore.ui.illustration.RoomItems
import ru.lct2026.finedu.productcore.ui.illustration.RoomScene
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

/**
 * Ритуал достигнутой цели: уголок с предметом, «Куда поставим?» → «Теперь навсегда в уголке».
 * Предмет появляется в сцене после «Поставить» — [placedGoalIds] уже содержит его id.
 */
@Composable
internal fun GoalRitualScreen(
    ritual: GoalRitual,
    look: PetLook,
    stage: PetStage,
    placedGoalIds: Set<String>,
    stars: Int,
    nextGoal: GoalProgress?,
    onPlace: () -> Unit,
    onGoHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Сцена — фиксированной пропорции между карточками: во весь высокий экран она растягивается и перекрывает текст.
    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (ritual.isPlaced) {
            PlacedHeader(
                goal = ritual.goal,
                nextGoal = nextGoal
            )
        } else {
            ReachedHeader(goal = ritual.goal)
        }
        RoomScene(
            placedGoalIds = placedGoalIds,
            stars = stars,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(ROOM_ASPECT)
                .clip(MaterialTheme.shapes.large)
        ) { petModifier ->
            PetView(
                look = look,
                mood = if (ritual.isPlaced) PetMood.PROUD else PetMood.HAPPY,
                stage = stage,
                modifier = petModifier
            )
        }
        if (ritual.isPlaced) {
            RitualCard(
                line = stringResource(
                    if (ritual.goal.id == RoomItems.WINDOW) {
                        R.string.savings_ritual_line_window
                    } else {
                        R.string.savings_ritual_line_placed
                    }
                ),
                text = null,
                buttonText = stringResource(R.string.savings_ritual_go_home),
                onClick = onGoHome
            )
        } else {
            RitualCard(
                line = stringResource(R.string.savings_ritual_line),
                text = stringResource(R.string.savings_ritual_forever),
                buttonText = stringResource(R.string.savings_ritual_place),
                onClick = onPlace,
                title = stringResource(R.string.savings_ritual_where)
            )
        }
    }
}

/** Пропорция сцены уголка из макета: 360 × 340. */
private const val ROOM_ASPECT = 360f / 340f

@Composable
private fun ReachedHeader(goal: Goal) {
    HeaderCard {
        Text(
            text = stringResource(R.string.savings_ritual_reached),
            style = MaterialTheme.typography.titleMedium,
            color = FinEduTheme.colors.gold,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            text = stringResource(R.string.savings_ritual_goal, goal.title, goal.price.amount),
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@Composable
private fun PlacedHeader(goal: Goal, nextGoal: GoalProgress?) {
    HeaderCard {
        Text(
            text = stringResource(R.string.savings_ritual_placed),
            style = MaterialTheme.typography.titleMedium,
            color = FinEduTheme.colors.gold,
            modifier = Modifier.semantics { heading() }
        )
        Text(text = goal.title, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = if (nextGoal != null) {
                stringResource(
                    R.string.savings_ritual_next,
                    nextGoal.goal.title,
                    nextGoal.saved.amount,
                    nextGoal.goal.price.amount
                )
            } else {
                stringResource(R.string.savings_ritual_all_done)
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun HeaderCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glass(style = GlassStyle.Strong)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.Start
    ) {
        content()
    }
}

@Composable
private fun RitualCard(line: String, text: String?, buttonText: String, onClick: () -> Unit, title: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SpeechBubble(text = line)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glass(style = GlassStyle.Strong)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.semantics { heading() }
                )
            }
            if (text != null) {
                Text(text = text, style = MaterialTheme.typography.bodyMedium)
            }
            FinButton(text = buttonText, onClick = onClick)
        }
    }
}

private val PreviewWindow = Goal(RoomItems.WINDOW, "Окно с видом", Dzynki(200))

@Preview(heightDp = 800)
@Composable
private fun GoalRitualPreview() {
    FinEduPreview {
        GoalRitualScreen(
            ritual = GoalRitual(PreviewWindow, isPlaced = false),
            look = PetLook(PetFur.LILAC, PetHat.CAP),
            stage = PetStage.SPRY,
            placedGoalIds = setOf(RoomItems.PLAID, RoomItems.LAMP),
            stars = 2,
            nextGoal = null,
            onPlace = {},
            onGoHome = {}
        )
    }
}

@Preview(heightDp = 800)
@Composable
private fun GoalPlacedPreview() {
    FinEduPreview {
        GoalRitualScreen(
            ritual = GoalRitual(PreviewWindow, isPlaced = true),
            look = PetLook(PetFur.LILAC, PetHat.CAP),
            stage = PetStage.SPRY,
            placedGoalIds = setOf(RoomItems.PLAID, RoomItems.LAMP, RoomItems.WINDOW),
            stars = 2,
            nextGoal = GoalProgress(
                goal = Goal(RoomItems.SCOOTER, "Самокат", Dzynki(300)),
                saved = Dzynki(120),
                left = Dzynki(180),
                weeks = 6,
                weeklyAverage = 30
            ),
            onPlace = {},
            onGoHome = {}
        )
    }
}

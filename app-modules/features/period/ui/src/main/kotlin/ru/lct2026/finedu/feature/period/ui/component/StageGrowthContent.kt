package ru.lct2026.finedu.feature.period.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.feature.period.ui.R
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
import ru.lct2026.finedu.productcore.ui.illustration.RoomScene
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

/**
 * Рост стадии: питомец новой стадии в уголке, «было: …», шкала трёх стадий и условие следующей (ТЗ 2.5.11).
 */
@Composable
internal fun StageGrowthContent(
    look: PetLook,
    stageBefore: PetStage,
    stageAfter: PetStage,
    xpToNextStage: Int?,
    stars: Int,
    placedGoalIds: Set<String>,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GrowthHeader(stageAfter = stageAfter)
            Box(modifier = Modifier.fillMaxWidth()) {
                RoomScene(
                    placedGoalIds = placedGoalIds,
                    stars = stars,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .glass()
                ) { petModifier ->
                    PetView(look = look, mood = PetMood.PROUD, stage = stageAfter, modifier = petModifier)
                }
                SpeechBubble(
                    text = stringResource(R.string.period_growth_line),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                )
            }
            Text(
                text = stringResource(R.string.period_growth_was, stringResource(stageBefore.labelRes)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.period_growth_why),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            StageScale(current = stageAfter, xpToNextStage = xpToNextStage)
        }
        FinButton(
            text = stringResource(R.string.period_growth_next),
            onClick = onNextClick,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun GrowthHeader(stageAfter: PetStage) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(stageAfter.labelRes),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            text = stringResource(R.string.period_growth_new_stage, stageAfter.ordinal + 1, PetStage.entries.size),
            style = MaterialTheme.typography.titleMedium,
            color = FinEduTheme.colors.gold
        )
    }
}

/** Шкала «Малыш → Шустрик → Мастер мешочка»: пройденные — «✓», текущая — «★», впереди — «○». */
@Composable
internal fun StageScale(current: PetStage, xpToNextStage: Int?, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glass()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.period_growth_stages_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PetStage.entries.forEach { stage ->
                StageStep(stage = stage, current = current, modifier = Modifier.weight(1f))
            }
        }
        val next = current.next
        Text(
            text = if (next != null && xpToNextStage != null) {
                pluralStringResource(
                    R.plurals.period_xp_to_next_stage,
                    xpToNextStage,
                    xpToNextStage,
                    stringResource(next.labelRes)
                )
            } else {
                stringResource(R.string.period_growth_last_stage)
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun StageStep(stage: PetStage, current: PetStage, modifier: Modifier = Modifier) {
    val label = stringResource(stage.labelRes)
    val (mark, descriptionRes) = when {
        stage < current -> "✓" to R.string.period_growth_stage_done_a11y
        stage == current -> "★" to R.string.period_growth_stage_current_a11y
        else -> "○" to R.string.period_growth_stage_next_a11y
    }
    val description = stringResource(descriptionRes, label)
    val isCurrent = stage == current
    Column(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .then(
                    if (isCurrent) {
                        Modifier.background(FinEduTheme.colors.gold, CircleShape)
                    } else {
                        Modifier.glass(shape = CircleShape, style = GlassStyle.Strong)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = mark,
                style = MaterialTheme.typography.titleMedium,
                color = if (isCurrent) FinEduTheme.colors.onGold else MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = if (isCurrent) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview
@Composable
private fun StageGrowthContentPreview() {
    FinEduPreview {
        StageGrowthContent(
            look = PetLook(PetFur.MINT, PetHat.CAP),
            stageBefore = PetStage.BABY,
            stageAfter = PetStage.SPRY,
            xpToNextStage = 6,
            stars = 2,
            placedGoalIds = setOf("plaid"),
            onNextClick = {}
        )
    }
}

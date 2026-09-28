package ru.lct2026.finedu.feature.onboarding.ui.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.feature.onboarding.ui.R
import ru.lct2026.finedu.productcore.domain.model.PetStage
import ru.lct2026.finedu.productcore.ui.components.SpeechBubble
import ru.lct2026.finedu.productcore.ui.components.glass
import ru.lct2026.finedu.productcore.ui.illustration.PetMood
import ru.lct2026.finedu.productcore.ui.illustration.PetView
import ru.lct2026.finedu.productcore.ui.illustration.RoomScene
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

/** Сцена «коробка у батареи»: Дзынь шуршит в коробке, после «Взять на руки» выпрыгивает из неё. */
@Composable
internal fun SceneStep(isHeld: Boolean, modifier: Modifier = Modifier) {
    val jump by animateDpAsState(
        targetValue = if (isHeld) PetJumpHeight else 0.dp,
        animationSpec = if (FinEduTheme.reduceMotion) {
            snap()
        } else {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy
            )
        },
        label = "petJump"
    )
    val petName = stringResource(R.string.onboarding_pet_name)
    StepColumn(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.onboarding_scene_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() }
        )
        // Место под реплику держим всегда, чтобы сцена не прыгала.
        SpeechBubble(
            text = stringResource(R.string.onboarding_scene_warm),
            modifier = Modifier
                .padding(top = 8.dp)
                .then(if (isHeld) Modifier else Modifier.alpha(0f).clearAndSetSemantics {})
        )
        RoomScene(
            placedGoalIds = emptySet(),
            stars = 0,
            modifier = Modifier
                .fillMaxWidth()
                .height(SceneHeight)
                .glass()
        ) { petModifier ->
            PetView(
                look = OnboardingPetLook,
                modifier = petModifier.offset(y = -jump),
                mood = if (isHeld) PetMood.HAPPY else PetMood.NEUTRAL,
                stage = PetStage.BABY,
                contentDescription = petName
            )
        }
        if (isHeld) {
            Text(
                text = petName,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .glass(shape = MaterialTheme.shapes.small)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }
        Text(
            text = stringResource(if (isHeld) R.string.onboarding_scene_held else R.string.onboarding_scene_in_box),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
    }
}

private val SceneHeight: Dp = 320.dp
private val PetJumpHeight: Dp = 72.dp

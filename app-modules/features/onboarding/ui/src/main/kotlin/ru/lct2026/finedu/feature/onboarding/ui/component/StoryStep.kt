package ru.lct2026.finedu.feature.onboarding.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.feature.onboarding.ui.OnboardingStep
import ru.lct2026.finedu.feature.onboarding.ui.R
import ru.lct2026.finedu.productcore.domain.model.Bag
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetStage
import ru.lct2026.finedu.productcore.ui.components.BagIcon
import ru.lct2026.finedu.productcore.ui.components.MinTouchTarget
import ru.lct2026.finedu.productcore.ui.components.SpeechBubble
import ru.lct2026.finedu.productcore.ui.components.color
import ru.lct2026.finedu.productcore.ui.components.glass
import ru.lct2026.finedu.productcore.ui.components.labelRes
import ru.lct2026.finedu.productcore.ui.illustration.PetMood
import ru.lct2026.finedu.productcore.ui.illustration.PetView

/** Три сторис: «Я Дзынь», «Три мешочка», «Да, мы приложение про финграмотность». */
@Composable
internal fun StoryStep(index: Int, onSkipClick: () -> Unit, modifier: Modifier = Modifier) {
    StepColumn(modifier = modifier) {
        StoryHeader(index = index, onSkipClick = onSkipClick)
        when (index) {
            0 -> StoryAboutPet()
            1 -> StoryBags()
            else -> StoryFinance()
        }
    }
}

@Composable
private fun StoryHeader(index: Int, onSkipClick: () -> Unit) {
    val progress = stringResource(R.string.onboarding_story_progress, index + 1, OnboardingStep.STORY_COUNT)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = progress },
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(OnboardingStep.STORY_COUNT) { bar ->
                val alpha = if (bar <= index) 1f else INACTIVE_BAR_ALPHA
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha), CircleShape)
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            PetView(look = OnboardingPetLook, modifier = Modifier.width(36.dp), stage = PetStage.BABY)
            Text(
                text = stringResource(R.string.onboarding_pet_name),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f)
            )
            TextButton(onClick = onSkipClick, modifier = Modifier.heightIn(min = MinTouchTarget)) {
                Text(
                    text = stringResource(R.string.onboarding_story_skip),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StoryTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineLarge,
        modifier = Modifier
            .padding(top = 8.dp)
            .semantics { heading() }
    )
}

@Composable
private fun ColumnScope.StoryAboutPet() {
    StoryTitle(stringResource(R.string.onboarding_story1_title))
    TiltedTag(text = stringResource(R.string.onboarding_story1_tag))
    PetView(
        look = OnboardingPetLook,
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .width(200.dp),
        mood = PetMood.JOY,
        stage = PetStage.BABY
    )
    Text(text = stringResource(R.string.onboarding_story1_text), style = MaterialTheme.typography.bodyLarge)
}

@Composable
private fun StoryBags() {
    StoryTitle(stringResource(R.string.onboarding_story2_title))
    Text(
        text = stringResource(R.string.onboarding_story2_subtitle),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    BagRow(bag = Bag.NEEDS, line = stringResource(R.string.onboarding_story2_needs))
    BagRow(bag = Bag.WANTS, line = stringResource(R.string.onboarding_story2_wants))
    BagRow(bag = Bag.SAVINGS, line = stringResource(R.string.onboarding_story2_savings))
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        PetView(
            look = OnboardingPetLook.copy(hat = PetHat.HEADPHONES),
            modifier = Modifier.width(72.dp),
            mood = PetMood.JOY,
            stage = PetStage.BABY
        )
        SpeechBubble(text = stringResource(R.string.onboarding_story2_spoiler))
    }
}

@Composable
private fun BagRow(bag: Bag, line: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glass()
            .padding(16.dp)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        BagIcon(bag = bag)
        Column {
            Text(text = stringResource(bag.labelRes), style = MaterialTheme.typography.titleMedium, color = bag.color)
            Text(text = line, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun StoryFinance() {
    StoryTitle(stringResource(R.string.onboarding_story3_title))
    TiltedTag(text = stringResource(R.string.onboarding_story3_tag))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glass()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = stringResource(R.string.onboarding_story3_meme), style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PetView(
                look = OnboardingPetLook,
                modifier = Modifier.width(110.dp),
                mood = PetMood.THINKING,
                stage = PetStage.BABY
            )
            SpeechBubble(text = stringResource(R.string.onboarding_story3_meme_line))
        }
    }
    Text(text = stringResource(R.string.onboarding_story3_text), style = MaterialTheme.typography.bodyLarge)
}

private const val INACTIVE_BAR_ALPHA = 0.24f

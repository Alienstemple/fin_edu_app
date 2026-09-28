package ru.lct2026.finedu.feature.onboarding.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.feature.onboarding.ui.FirstChoice
import ru.lct2026.finedu.feature.onboarding.ui.R
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.PetStage
import ru.lct2026.finedu.productcore.ui.components.BagIcon
import ru.lct2026.finedu.productcore.ui.components.DzynkiAmount
import ru.lct2026.finedu.productcore.ui.components.SpeechBubble
import ru.lct2026.finedu.productcore.ui.components.color
import ru.lct2026.finedu.productcore.ui.components.glass
import ru.lct2026.finedu.productcore.ui.components.labelRes
import ru.lct2026.finedu.productcore.ui.illustration.PetMood
import ru.lct2026.finedu.productcore.ui.illustration.PetView

/** Сколько дзынек в учебном выборе: всё уходит на один вариант. */
internal val FirstChoiceDzynki = Dzynki(20)

/** Первое решение: «Последние 20 дзынек. Каша — Дзыню или печенье — себе?». Оба варианта нормальные. */
@Composable
internal fun ChoiceStep(onChoiceClick: (FirstChoice) -> Unit, modifier: Modifier = Modifier) {
    StepColumn(modifier = modifier) {
        Text(
            text = stringResource(R.string.onboarding_choice_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = stringResource(R.string.onboarding_choice_title),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.semantics { heading() }
        )
        Text(text = stringResource(R.string.onboarding_choice_question), style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PetView(
                look = OnboardingPetLook,
                modifier = Modifier.width(110.dp),
                mood = PetMood.THINKING,
                stage = PetStage.BABY
            )
            SpeechBubble(text = stringResource(R.string.onboarding_choice_pet_line))
        }
        FirstChoice.entries.forEach { choice ->
            ChoiceOption(choice = choice, onClick = { onChoiceClick(choice) })
        }
        Text(
            text = stringResource(R.string.onboarding_choice_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ChoiceOption(choice: FirstChoice, onClick: () -> Unit) {
    val bag = choice.bag
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = OptionMinHeight)
            .glass()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(16.dp)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        BagIcon(bag = bag)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(choice.titleRes), style = MaterialTheme.typography.titleMedium)
            Text(text = stringResource(bag.labelRes), style = MaterialTheme.typography.bodyMedium, color = bag.color)
        }
        DzynkiAmount(amount = FirstChoiceDzynki)
    }
}

private val OptionMinHeight = 72.dp

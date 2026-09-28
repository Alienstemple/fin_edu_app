package ru.lct2026.finedu.feature.onboarding.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.feature.onboarding.ui.FirstChoice
import ru.lct2026.finedu.feature.onboarding.ui.R
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.PetStage
import ru.lct2026.finedu.productcore.ui.components.DzynkiAmount
import ru.lct2026.finedu.productcore.ui.components.SpeechBubble
import ru.lct2026.finedu.productcore.ui.components.color
import ru.lct2026.finedu.productcore.ui.components.glass
import ru.lct2026.finedu.productcore.ui.components.iconRes
import ru.lct2026.finedu.productcore.ui.components.labelRes
import ru.lct2026.finedu.productcore.ui.illustration.PetView

/** Результат учебного выбора: реакция Дзыня, 20 → 0 дзынек, +10 к показателю и пояснение. */
@Composable
internal fun ResultStep(choice: FirstChoice, modifier: Modifier = Modifier) {
    StepColumn(modifier = modifier) {
        Row(
            modifier = Modifier.padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PetView(
                look = OnboardingPetLook,
                modifier = Modifier.width(150.dp),
                mood = choice.mood,
                stage = PetStage.BABY,
                contentDescription = stringResource(R.string.onboarding_pet_name)
            )
            SpeechBubble(text = stringResource(choice.lineRes))
        }
        TiltedTag(text = stringResource(choice.tagRes))
        Text(
            text = stringResource(choice.resultTitleRes),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.semantics { heading() }
        )
        ChangesCard(choice = choice)
        Text(text = stringResource(choice.whyRes), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ChangesCard(choice: FirstChoice) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glass()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val dzynkiDescription = stringResource(R.string.onboarding_result_dzynki_a11y)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clearAndSetSemantics { contentDescription = dzynkiDescription },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.onboarding_result_dzynki),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            DzynkiAmount(amount = FirstChoiceDzynki)
            Text(text = stringResource(R.string.onboarding_result_arrow), style = MaterialTheme.typography.titleMedium)
            DzynkiAmount(amount = Dzynki.ZERO)
        }
        val stat = choice.stat
        val statLabel = stringResource(stat.labelRes)
        val statDescription = stringResource(R.string.onboarding_result_stat_a11y, statLabel)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clearAndSetSemantics { contentDescription = statDescription },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(stat.iconRes),
                contentDescription = null,
                tint = stat.color,
                modifier = Modifier.size(24.dp)
            )
            Text(text = statLabel, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.onboarding_result_stat_plus),
                style = MaterialTheme.typography.titleMedium,
                color = stat.color
            )
        }
    }
}

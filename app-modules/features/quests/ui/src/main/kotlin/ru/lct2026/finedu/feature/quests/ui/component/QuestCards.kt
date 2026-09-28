package ru.lct2026.finedu.feature.quests.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.feature.quests.ui.R
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.ui.R as CoreR
import ru.lct2026.finedu.productcore.ui.components.GlassStyle
import ru.lct2026.finedu.productcore.ui.components.dzynkiText
import ru.lct2026.finedu.productcore.ui.components.glass

/** Заголовок блока внутри задания. */
@Composable
internal fun QuestHeading(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier.semantics { heading() }
    )
}

/** Текст в стеклянной карточке: ситуация, разбор приёма. */
@Composable
internal fun GlassText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier
            .fillMaxWidth()
            .glass()
            .padding(16.dp)
    )
}

/** Награда за задание: [text] — сколько и куда легло, или что награда уже получена. */
@Composable
internal fun RewardNote(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .glass(style = GlassStyle.Strong)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(CoreR.drawable.ic_coin),
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )
        Text(text = text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
    }
}

/** Подпись награды задания с выбором: «+20 дзынек в неразложенное…» или «награда уже получена». */
@Composable
internal fun choiceRewardText(reward: Dzynki): String = if (reward > Dzynki.ZERO) {
    stringResource(R.string.quest_reward_added, dzynkiText(reward))
} else {
    stringResource(R.string.quest_reward_already)
}

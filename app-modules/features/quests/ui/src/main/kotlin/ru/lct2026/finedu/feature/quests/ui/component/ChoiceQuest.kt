package ru.lct2026.finedu.feature.quests.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.feature.quests.ui.ChoiceResult
import ru.lct2026.finedu.feature.quests.ui.QuestUiState
import ru.lct2026.finedu.feature.quests.ui.R
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.PetStage
import ru.lct2026.finedu.productcore.domain.model.Quest
import ru.lct2026.finedu.productcore.domain.model.QuestLevel
import ru.lct2026.finedu.productcore.domain.model.QuestOption
import ru.lct2026.finedu.productcore.domain.model.QuestTheme
import ru.lct2026.finedu.productcore.ui.R as CoreR
import ru.lct2026.finedu.productcore.ui.components.FinButton
import ru.lct2026.finedu.productcore.ui.components.GlassStyle
import ru.lct2026.finedu.productcore.ui.components.MinTouchTarget
import ru.lct2026.finedu.productcore.ui.components.glass
import ru.lct2026.finedu.productcore.ui.illustration.PetMood
import ru.lct2026.finedu.productcore.ui.illustration.PetView
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

/** Задание с выбором: ситуация → равноценные варианты → разбор с наградой. */
@Composable
internal fun ChoiceQuest(
    state: QuestUiState.Choice,
    onOptionClick: (String) -> Unit,
    onDoneClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        val result = state.result
        if (result == null) {
            if (state.isAd) {
                AdMeme()
                PetLine(
                    look = state.petLook,
                    stage = state.petStage,
                    mood = PetMood.SHOCK,
                    text = stringResource(R.string.quest_ad_pet_line)
                )
            } else {
                Situation(state)
            }
            Options(options = state.quest.options, onOptionClick = onOptionClick)
        } else {
            Review(state = state, result = result, onDoneClick = onDoneClick)
        }
    }
}

/** «Никто: / Реклама:». Таймер — статичный текст: его показывают, чтобы разоблачить, а не чтобы торопить. */
@Composable
private fun AdMeme() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.quest_ad_nobody),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = stringResource(R.string.quest_ad_advert), style = MaterialTheme.typography.titleMedium)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glass(style = GlassStyle.Strong)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.quest_ad_banner),
                style = MaterialTheme.typography.headlineMedium,
                color = FinEduTheme.colors.gold,
                textAlign = TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(CoreR.drawable.ic_clock),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Text(text = stringResource(R.string.quest_ad_timer), style = MaterialTheme.typography.titleLarge)
            }
            Text(text = stringResource(R.string.quest_ad_item), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun Situation(state: QuestUiState.Choice) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glass()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = state.quest.situation,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        PetView(
            look = state.petLook,
            stage = state.petStage,
            mood = PetMood.THINKING,
            modifier = Modifier.width(80.dp)
        )
    }
}

@Composable
private fun Options(options: List<QuestOption>, onOptionClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        QuestHeading(text = stringResource(R.string.quest_choice_question))
        Text(
            text = stringResource(R.string.quest_choice_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        options.forEach { option ->
            OptionCard(text = option.text, onClick = { onOptionClick(option.id) })
        }
    }
}

/** Вариант ответа. Все варианты одного веса — ни один не подсвечен как «правильный». */
@Composable
private fun OptionCard(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = MinTouchTarget + 8.dp)
            .glass()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        Icon(
            painter = painterResource(CoreR.drawable.ic_chevron_right),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun Review(state: QuestUiState.Choice, result: ChoiceResult, onDoneClick: () -> Unit) {
    if (state.isAd) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AdNote(stringResource(R.string.quest_ad_note_only))
            AdNote(stringResource(R.string.quest_ad_note_timer))
        }
    }
    GlassText(text = state.quest.explanation)
    Text(
        text = stringResource(R.string.quest_choice_picked, result.option.text),
        style = MaterialTheme.typography.titleMedium
    )
    PetLine(look = state.petLook, stage = state.petStage, mood = PetMood.HAPPY, text = result.option.reaction)
    RewardNote(text = choiceRewardText(result.reward))
    FinButton(text = stringResource(R.string.quest_done), onClick = onDoneClick)
}

@Composable
private fun AdNote(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(CoreR.drawable.ic_alert),
            contentDescription = null,
            tint = FinEduTheme.colors.gold,
            modifier = Modifier.size(20.dp)
        )
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

private val previewAd = Quest.Choice(
    id = "ad",
    theme = QuestTheme.PURCHASES,
    level = QuestLevel.EASY,
    title = "«Никто: / Реклама:»",
    reward = Dzynki(20),
    situation = "ТОЛЬКО СЕГОДНЯ! КУПИ СЕЙЧАС!!! Шляпа за 100.",
    options = listOf(
        QuestOption("buy", "Купить", "Шляпа за 100, а в «Хочу» — 60."),
        QuestOption("later", "Отложить до завтра", "Завтра таймер… снова 10 минут. Смешно, да?"),
        QuestOption("goal", "Добавить в цель", "Шляпа ждёт в списке целей.")
    ),
    explanation = "«ТОЛЬКО СЕГОДНЯ!!!» — так пишут, когда хотят, чтобы ты не успел подумать."
)

private val previewLook = PetLook(PetFur.CORAL, PetHat.NONE)

@Preview
@Composable
private fun ChoiceQuestAdPreview() {
    FinEduPreview {
        ChoiceQuest(
            state = QuestUiState.Choice(previewAd, previewLook, PetStage.SPRY),
            onOptionClick = {},
            onDoneClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview
@Composable
private fun ChoiceQuestSituationPreview() {
    FinEduPreview {
        ChoiceQuest(
            state = QuestUiState.Choice(
                previewAd.copy(id = "hat_or_socks", situation = "Холодает. В «Нужном» 40, в «Хочу» 60."),
                previewLook,
                PetStage.SPRY
            ),
            onOptionClick = {},
            onDoneClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview
@Composable
private fun ChoiceQuestReviewPreview() {
    FinEduPreview {
        ChoiceQuest(
            state = QuestUiState.Choice(
                previewAd,
                previewLook,
                PetStage.SPRY,
                ChoiceResult(previewAd.options[1], Dzynki(20))
            ),
            onOptionClick = {},
            onDoneClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

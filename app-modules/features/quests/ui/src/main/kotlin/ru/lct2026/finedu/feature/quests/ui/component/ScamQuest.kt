package ru.lct2026.finedu.feature.quests.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.feature.quests.ui.QuestUiState
import ru.lct2026.finedu.feature.quests.ui.R
import ru.lct2026.finedu.feature.quests.ui.ScamAnswer
import ru.lct2026.finedu.feature.quests.ui.ScamStep
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.PetStage
import ru.lct2026.finedu.productcore.domain.model.Quest
import ru.lct2026.finedu.productcore.domain.model.QuestLevel
import ru.lct2026.finedu.productcore.domain.model.QuestTheme
import ru.lct2026.finedu.productcore.domain.model.ScamMessage
import ru.lct2026.finedu.productcore.domain.model.ScamSign
import ru.lct2026.finedu.productcore.ui.R as CoreR
import ru.lct2026.finedu.productcore.ui.components.FinButton
import ru.lct2026.finedu.productcore.ui.components.GlassStyle
import ru.lct2026.finedu.productcore.ui.components.dzynkiText
import ru.lct2026.finedu.productcore.ui.components.glass
import ru.lct2026.finedu.productcore.ui.illustration.PetMood
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

/** «Это мошенники?»: сообщение → разбор признаков → итог с главным правилом. Любой ответ — не ошибка. */
@Composable
internal fun ScamQuest(
    state: QuestUiState.Scam,
    onAnswer: (ScamAnswer) -> Unit,
    onNextClick: () -> Unit,
    onFinishClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        when (val step = state.step) {
            is ScamStep.Message -> MessageStep(state, state.quest.messages[step.index], onAnswer)

            is ScamStep.Review -> ReviewStep(
                state = state,
                message = state.quest.messages[step.index],
                answer = step.answer,
                isLast = step.index == state.quest.messages.lastIndex,
                onNextClick = onNextClick
            )

            is ScamStep.Summary -> SummaryStep(state, step.reward, onFinishClick)
        }
    }
}

@Composable
private fun MessageStep(state: QuestUiState.Scam, message: ScamMessage, onAnswer: (ScamAnswer) -> Unit) {
    Text(text = stringResource(R.string.quest_scam_question), style = MaterialTheme.typography.bodyLarge)
    MessageCard(message)
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        FinButton(
            text = stringResource(R.string.quest_scam_suspicious),
            onClick = { onAnswer(ScamAnswer.SUSPICIOUS) },
            modifier = Modifier.weight(1f)
        )
        FinButton(
            text = stringResource(R.string.quest_scam_normal),
            onClick = { onAnswer(ScamAnswer.NORMAL) },
            modifier = Modifier.weight(1f)
        )
    }
    PetLine(
        look = state.petLook,
        stage = state.petStage,
        mood = PetMood.NEUTRAL,
        text = stringResource(R.string.quest_scam_pet_line)
    )
}

@Composable
private fun MessageCard(message: ScamMessage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glass(style = GlassStyle.Strong)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row {
            Text(
                text = message.app,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(R.string.quest_scam_now),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(text = message.sender, style = MaterialTheme.typography.titleMedium)
        Text(text = message.text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ReviewStep(
    state: QuestUiState.Scam,
    message: ScamMessage,
    answer: ScamAnswer,
    isLast: Boolean,
    onNextClick: () -> Unit
) {
    MessageCard(message)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        message.signs.forEachIndexed { index, sign -> SignRow(number = index + 1, sign = sign) }
    }
    val answerText = when (answer) {
        ScamAnswer.SUSPICIOUS -> stringResource(R.string.quest_scam_picked_suspicious)
        ScamAnswer.NORMAL -> stringResource(R.string.quest_scam_picked_normal)
    }
    Text(text = stringResource(R.string.quest_scam_picked, answerText), style = MaterialTheme.typography.titleMedium)
    PetLine(look = state.petLook, stage = state.petStage, mood = PetMood.HAPPY, text = message.tip)
    FinButton(
        text = stringResource(if (isLast) R.string.quest_scam_to_summary else R.string.quest_scam_next),
        onClick = onNextClick
    )
}

@Composable
private fun SignRow(number: Int, sign: ScamSign) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glass()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = stringResource(R.string.quest_scam_sign, number, sign.name),
            style = MaterialTheme.typography.titleMedium,
            color = FinEduTheme.colors.goldLight
        )
        Text(text = sign.why, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SummaryStep(state: QuestUiState.Scam, reward: Dzynki, onFinishClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glass(style = GlassStyle.Strong)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(R.string.quest_scam_main_rule),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.quest_scam_main_rule_text),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() }
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        state.quest.rules.forEach { rule ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(CoreR.drawable.ic_check),
                    contentDescription = null,
                    tint = FinEduTheme.colors.gold,
                    modifier = Modifier.size(24.dp)
                )
                Text(text = rule, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
    PetLine(
        look = state.petLook,
        stage = state.petStage,
        mood = PetMood.PROUD,
        text = stringResource(R.string.quest_scam_pet_together)
    )
    RewardNote(
        text = if (reward > Dzynki.ZERO) {
            stringResource(R.string.quest_scam_reward, dzynkiText(reward), state.quest.title)
        } else {
            stringResource(R.string.quest_reward_already)
        }
    )
    FinButton(text = stringResource(R.string.quest_scam_finish), onClick = onFinishClick)
}

private val previewMessage = ScamMessage(
    app = "Сообщения",
    sender = "Неизвестный номер",
    text = "Ты выиграл 1000 дзынек! Скажи код из СМС — приз ждёт всего 10 минут.",
    signs = listOf(
        ScamSign("Незнакомый отправитель", "номер не из твоих контактов"),
        ScamSign("Код из СМС", "это ключ от аккаунта")
    ),
    tip = "Код никому не говорим. Закрыть — и показать взрослому."
)

private val previewState = QuestUiState.Scam(
    quest = Quest.Scam(
        id = "scam",
        theme = QuestTheme.SAVINGS,
        level = QuestLevel.MEDIUM,
        title = "Это мошенники?",
        reward = Dzynki(30),
        messages = listOf(previewMessage, previewMessage, previewMessage),
        rules = listOf("Коды из СМС — никому", "Странные ссылки — мимо")
    ),
    petLook = PetLook(PetFur.MINT, PetHat.HEADPHONES),
    petStage = PetStage.SPRY,
    step = ScamStep.Message(0)
)

@Preview
@Composable
private fun ScamQuestMessagePreview() {
    FinEduPreview {
        ScamQuest(previewState, {}, {}, {}, Modifier.padding(16.dp))
    }
}

@Preview
@Composable
private fun ScamQuestReviewPreview() {
    FinEduPreview {
        ScamQuest(previewState.copy(step = ScamStep.Review(2, ScamAnswer.NORMAL)), {}, {}, {}, Modifier.padding(16.dp))
    }
}

@Preview
@Composable
private fun ScamQuestSummaryPreview() {
    FinEduPreview {
        ScamQuest(previewState.copy(step = ScamStep.Summary(Dzynki(30))), {}, {}, {}, Modifier.padding(16.dp))
    }
}

package ru.lct2026.finedu.productcore.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import ru.lct2026.finedu.productcore.domain.model.Bag
import ru.lct2026.finedu.productcore.domain.model.Change
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.Feedback
import ru.lct2026.finedu.productcore.domain.model.FeedbackReason
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.PetStage
import ru.lct2026.finedu.productcore.domain.model.PetStat
import ru.lct2026.finedu.productcore.domain.model.StatChange
import ru.lct2026.finedu.productcore.ui.R
import ru.lct2026.finedu.productcore.ui.illustration.PetMood
import ru.lct2026.finedu.productcore.ui.illustration.PetView
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

/**
 * Обратная связь после действия (ТЗ 2.5.9) — одна шторка на все действия: реакция Дзыня → что изменилось
 * (было → стало) → почему → что дальше. [title] — что купили или «В копилку»; [why] по умолчанию — общий текст
 * для [Feedback.reason], экран может передать точнее (например, с новым сроком цели).
 * [onDismiss] — шторку смахнули или закрыли «назад»; [onPrimary] — нажали «Вернуться в уголок» (обычно переход на
 * главный), по умолчанию просто закрывает шторку.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackSheet(
    feedback: Feedback,
    look: PetLook,
    stage: PetStage,
    title: String,
    onDismiss: () -> Unit,
    onPrimary: () -> Unit = onDismiss,
    why: String = stringResource(feedback.reason.whyRes),
    secondaryText: String? = null,
    onSecondary: (() -> Unit)? = null
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        FeedbackContent(
            feedback = feedback,
            look = look,
            stage = stage,
            title = title,
            why = why,
            onPrimary = onPrimary,
            secondaryText = secondaryText,
            onSecondary = onSecondary,
            modifier = Modifier.navigationBarsPadding()
        )
    }
}

@Composable
private fun FeedbackContent(
    feedback: Feedback,
    look: PetLook,
    stage: PetStage,
    title: String,
    why: String,
    onPrimary: () -> Unit,
    secondaryText: String?,
    onSecondary: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PetView(look = look, mood = feedback.reason.mood, stage = stage, modifier = Modifier.width(96.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SpeechBubble(text = stringResource(feedback.reason.lineRes))
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = amountTag(feedback),
                    style = MaterialTheme.typography.titleMedium,
                    color = FinEduTheme.colors.gold
                )
            }
        }
        SectionTitle(R.string.feedback_changed)
        Column(modifier = Modifier.glass().padding(horizontal = 14.dp, vertical = 4.dp)) {
            ChangeRow(
                label = stringResource(R.string.feedback_balance),
                from = feedback.balance.from.amount,
                to = feedback.balance.to.amount,
                icon = {
                    Image(painterResource(R.drawable.ic_coin), contentDescription = null, Modifier.size(28.dp))
                }
            )
            ChangeRow(
                label = stringResource(feedback.bag.labelRes),
                from = feedback.bagChange.from.amount,
                to = feedback.bagChange.to.amount,
                icon = { BagIcon(bag = feedback.bag) }
            )
            val stat = feedback.statChange
            ChangeRow(
                label = stringResource(stat.stat.labelRes),
                from = stat.from,
                to = stat.to,
                showDelta = true,
                icon = {
                    Icon(
                        painter = painterResource(stat.stat.iconRes),
                        contentDescription = null,
                        tint = stat.stat.color,
                        modifier = Modifier.size(28.dp)
                    )
                }
            )
        }
        SectionTitle(R.string.feedback_why)
        Text(text = why, style = MaterialTheme.typography.bodyMedium)
        SectionTitle(R.string.feedback_next)
        FinButton(text = stringResource(R.string.feedback_back_home), onClick = onPrimary)
        if (secondaryText != null && onSecondary != null) {
            FinButton(text = secondaryText, onClick = onSecondary, style = FinButtonStyle.Secondary)
        }
    }
}

@Composable
private fun SectionTitle(@StringRes textRes: Int) {
    Text(
        text = stringResource(textRes),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.semantics { heading() }
    )
}

/** Строка «было → стало». Без изменений — «на месте», без стрелки: ничего не отнято. */
@Composable
private fun ChangeRow(label: String, from: Int, to: Int, icon: @Composable () -> Unit, showDelta: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = MinTouchTarget)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) { icon() }
        Text(text = label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        val value = if (from == to) {
            "${stringResource(R.string.feedback_unchanged)} · $to"
        } else {
            "$from → $to"
        }
        Text(text = value, style = MaterialTheme.typography.titleSmall)
        if (showDelta && from != to) {
            Text(
                text = if (to > from) "+${to - from}" else "−${from - to}",
                style = MaterialTheme.typography.titleSmall,
                color = FinEduTheme.colors.gold
            )
        }
    }
}

@Composable
private fun amountTag(feedback: Feedback): String {
    val moved = abs(feedback.bagChange.to.amount - feedback.bagChange.from.amount)
    return when (feedback.reason) {
        FeedbackReason.PAUSED -> stringResource(R.string.feedback_pause_tag)

        FeedbackReason.DEPOSITED -> stringResource(R.string.feedback_plus, dzynkiText(moved))

        FeedbackReason.BOUGHT_NEED,
        FeedbackReason.BOUGHT_WANT,
        FeedbackReason.WITHDREW,
        FeedbackReason.GOAL_PLACED -> stringResource(R.string.feedback_minus, dzynkiText(moved))
    }
}

private val FeedbackReason.mood: PetMood
    get() = when (this) {
        FeedbackReason.BOUGHT_NEED, FeedbackReason.DEPOSITED -> PetMood.HAPPY
        FeedbackReason.BOUGHT_WANT -> PetMood.JOY
        FeedbackReason.PAUSED -> PetMood.THINKING
        FeedbackReason.WITHDREW -> PetMood.NEUTRAL
        FeedbackReason.GOAL_PLACED -> PetMood.PROUD
    }

private val FeedbackReason.lineRes: Int
    get() = when (this) {
        FeedbackReason.BOUGHT_NEED -> R.string.feedback_line_need
        FeedbackReason.BOUGHT_WANT -> R.string.feedback_line_want
        FeedbackReason.PAUSED -> R.string.feedback_line_pause
        FeedbackReason.DEPOSITED -> R.string.feedback_line_deposit
        FeedbackReason.WITHDREW -> R.string.feedback_line_withdraw
        FeedbackReason.GOAL_PLACED -> R.string.feedback_line_goal
    }

private val FeedbackReason.whyRes: Int
    get() = when (this) {
        FeedbackReason.BOUGHT_NEED -> R.string.feedback_why_need
        FeedbackReason.BOUGHT_WANT -> R.string.feedback_why_want
        FeedbackReason.PAUSED -> R.string.feedback_why_pause
        FeedbackReason.DEPOSITED -> R.string.feedback_why_deposit
        FeedbackReason.WITHDREW -> R.string.feedback_why_withdraw
        FeedbackReason.GOAL_PLACED -> R.string.feedback_why_goal
    }

@Preview(heightDp = 760)
@Composable
private fun FeedbackContentPreview() {
    FinEduPreview {
        FeedbackContent(
            feedback = Feedback(
                reason = FeedbackReason.BOUGHT_NEED,
                balance = Change(Dzynki(130), Dzynki(90)),
                bag = Bag.NEEDS,
                bagChange = Change(Dzynki(70), Dzynki(30)),
                statChange = StatChange(PetStat.CHARGE, 70, 80)
            ),
            look = PetLook(PetFur.LILAC, PetHat.CAP),
            stage = PetStage.SPRY,
            title = "Тёплые носки",
            why = "Скучная покупка, зато тёплая. Нужное всегда подзаряжает Дзыня.",
            onPrimary = {},
            secondaryText = "Ещё в магазин",
            onSecondary = {}
        )
    }
}

@Preview(heightDp = 760)
@Composable
private fun FeedbackPausePreview() {
    FinEduPreview {
        FeedbackContent(
            feedback = Feedback(
                reason = FeedbackReason.PAUSED,
                balance = Change(Dzynki(130), Dzynki(130)),
                bag = Bag.WANTS,
                bagChange = Change(Dzynki(60), Dzynki(60)),
                statChange = StatChange(PetStat.CALM, 50, 60)
            ),
            look = PetLook(PetFur.MINT, PetHat.NONE),
            stage = PetStage.BABY,
            title = "Шляпа с пером",
            why = "Пауза — тоже решение. Если завтра всё ещё хочется, значит, правда хочется.",
            onPrimary = {},
            secondaryText = null,
            onSecondary = null
        )
    }
}

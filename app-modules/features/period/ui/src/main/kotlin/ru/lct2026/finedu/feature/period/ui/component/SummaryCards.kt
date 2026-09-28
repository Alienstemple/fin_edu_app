package ru.lct2026.finedu.feature.period.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.feature.period.ui.R
import ru.lct2026.finedu.productcore.domain.model.Bag
import ru.lct2026.finedu.productcore.domain.model.BagAmounts
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.PeriodResult
import ru.lct2026.finedu.productcore.domain.model.XpReason
import ru.lct2026.finedu.productcore.ui.components.BagIcon
import ru.lct2026.finedu.productcore.ui.components.GlassStyle
import ru.lct2026.finedu.productcore.ui.components.MinTouchTarget
import ru.lct2026.finedu.productcore.ui.components.dzynkiText
import ru.lct2026.finedu.productcore.ui.components.glass
import ru.lct2026.finedu.productcore.ui.components.labelRes
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

/** Карточка «Ожидание»: план недели по мешочкам или «плана не было». */
@Composable
internal fun ExpectationCard(plan: BagAmounts?, modifier: Modifier = Modifier) {
    MemeCard(title = stringResource(R.string.period_summary_expectation), modifier = modifier) {
        if (plan == null) {
            Text(text = stringResource(R.string.period_summary_no_plan), style = MaterialTheme.typography.bodyMedium)
        } else {
            Bag.entries.forEach { bag ->
                BagValueRow(
                    bag = bag,
                    value = plan[bag].amount.toString(),
                    description = stringResource(
                        R.string.period_summary_row_a11y,
                        stringResource(bag.labelRes),
                        dzynkiText(plan[bag])
                    )
                )
            }
        }
    }
}

/** Карточка «Реальность»: потрачено на Нужное и Хочу, сколько всего легло в копилку (выделено). */
@Composable
internal fun RealityCard(result: PeriodResult, modifier: Modifier = Modifier) {
    MemeCard(title = stringResource(R.string.period_summary_reality), modifier = modifier) {
        val needs = result.actual.needs
        val needsLabel = stringResource(Bag.NEEDS.labelRes)
        BagValueRow(
            bag = Bag.NEEDS,
            value = if (result.isNeedsClosed) {
                stringResource(R.string.period_summary_value_closed, needs.amount)
            } else {
                needs.amount.toString()
            },
            description = if (result.isNeedsClosed) {
                stringResource(R.string.period_summary_row_closed_a11y, needsLabel, dzynkiText(needs))
            } else {
                stringResource(R.string.period_summary_row_a11y, needsLabel, dzynkiText(needs))
            }
        )
        BagValueRow(
            bag = Bag.WANTS,
            value = result.actual.wants.amount.toString(),
            description = stringResource(
                R.string.period_summary_row_a11y,
                stringResource(Bag.WANTS.labelRes),
                dzynkiText(result.actual.wants)
            )
        )
        BagValueRow(
            bag = Bag.SAVINGS,
            value = stringResource(R.string.period_summary_value_saved, result.savedTotal.amount),
            description = stringResource(
                R.string.period_summary_row_saved_a11y,
                stringResource(Bag.SAVINGS.labelRes),
                dzynkiText(result.savedTotal)
            ),
            isHighlighted = true
        )
    }
}

@Composable
private fun MemeCard(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glass()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.semantics { heading() }
        )
        content()
    }
}

@Composable
private fun BagValueRow(bag: Bag, value: String, description: String, isHighlighted: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = MinTouchTarget)
            .then(
                if (isHighlighted) {
                    Modifier.glass(shape = MaterialTheme.shapes.medium, style = GlassStyle.Strong)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = if (isHighlighted) 8.dp else 0.dp)
            .clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BagIcon(bag = bag)
        Text(
            text = stringResource(bag.labelRes),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = if (isHighlighted) FinEduTheme.colors.gold else Color.Unspecified
        )
    }
}

/** Бирки итогов: «Нужное закрыто ✓», «В копилке +N». */
@Composable
internal fun SummaryTags(result: PeriodResult, modifier: Modifier = Modifier) {
    val hasSaved = result.savedTotal > Dzynki.ZERO
    if (result.isNeedsClosed || hasSaved) {
        FlowRow(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (result.isNeedsClosed) Tag(text = stringResource(R.string.period_summary_tag_needs_closed))
            if (hasSaved) Tag(text = stringResource(R.string.period_summary_tag_saved, result.savedTotal.amount))
        }
    }
}

@Composable
private fun Tag(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier
            .glass(shape = CircleShape, style = GlassStyle.Strong)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}

/** Карточка с заголовком и текстом: совет, звёздочка на пледе. */
@Composable
internal fun TextCard(
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    titleColor: Color = Color.Unspecified
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glass()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = titleColor,
            modifier = Modifier.semantics { heading() }
        )
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

/** «Опыт за неделю»: по +1 за каждую причину и общий опыт — чтобы рост был объясним. */
@Composable
internal fun XpCard(xpReasons: List<XpReason>, totalXp: Int, stageLabel: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glass()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.period_summary_xp_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() }
        )
        if (xpReasons.isEmpty()) {
            Text(text = stringResource(R.string.period_summary_xp_empty), style = MaterialTheme.typography.bodyMedium)
        }
        xpReasons.forEach { reason -> XpReasonRow(reason = reason) }
        Text(
            text = stringResource(R.string.period_summary_xp_total, totalXp, stageLabel),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun XpReasonRow(reason: XpReason) {
    val label = stringResource(reason.labelRes)
    val description = stringResource(R.string.period_summary_xp_reason_a11y, label)
    Row(
        modifier = Modifier.clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.period_summary_xp_plus),
            style = MaterialTheme.typography.titleMedium,
            color = FinEduTheme.colors.gold
        )
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

private val XpReason.labelRes: Int
    get() = when (this) {
        XpReason.PLANNED -> R.string.period_summary_xp_planned
        XpReason.NEEDS_CLOSED -> R.string.period_summary_xp_needs_closed
        XpReason.SAVED -> R.string.period_summary_xp_saved
        XpReason.QUEST_DONE -> R.string.period_summary_xp_quest_done
    }

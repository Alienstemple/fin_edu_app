package ru.lct2026.finedu.feature.parent.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.feature.parent.domain.model.SavingsChart
import ru.lct2026.finedu.feature.parent.domain.model.ThemeStatus
import ru.lct2026.finedu.feature.parent.ui.ProgressUiState
import ru.lct2026.finedu.feature.parent.ui.R
import ru.lct2026.finedu.feature.parent.ui.ThemeProgress
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.QuestTheme
import ru.lct2026.finedu.productcore.ui.R as CoreR
import ru.lct2026.finedu.productcore.ui.components.DzynkiAmount
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

/** Вкладка «Прогресс»: неделя, сюжет, звёздочки, копилка, график накоплений и темы. */
@Composable
internal fun ProgressTab(progress: ProgressUiState, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.parent_progress_lead, progress.playerName, progress.petName),
            style = MaterialTheme.typography.bodyLarge
        )
        ParentCard {
            SectionTitle(
                stringResource(
                    R.string.parent_progress_week,
                    progress.week,
                    maxOf(progress.week, progress.totalWeeks)
                )
            )
            progress.storyTitle?.let { SecondaryText(stringResource(R.string.parent_progress_story, it)) }
        }
        StarsCard(stars = progress.stars)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ParentCard(modifier = Modifier.weight(1f)) {
                SecondaryText(stringResource(R.string.parent_progress_savings))
                DzynkiAmount(amount = Dzynki(progress.savings), style = MaterialTheme.typography.headlineSmall)
            }
            ParentCard(modifier = Modifier.weight(1f)) {
                SecondaryText(stringResource(R.string.parent_progress_goal))
                val goal = if (progress.goalTitle != null && progress.goalPrice != null) {
                    stringResource(R.string.parent_progress_goal_value, progress.goalTitle, progress.goalPrice)
                } else {
                    stringResource(R.string.parent_progress_goal_none)
                }
                Text(text = goal, style = MaterialTheme.typography.titleMedium)
            }
        }
        ChartCard(chart = progress.chart)
        ThemesCard(themes = progress.themes)
        SecondaryText(stringResource(R.string.parent_progress_footer))
    }
}

@Composable
private fun StarsCard(stars: Int) {
    ParentCard {
        SectionTitle(stringResource(R.string.parent_progress_stars_title))
        val description = stringResource(R.string.parent_progress_stars_a11y, stars)
        if (stars == 0) {
            Text(text = stringResource(R.string.parent_progress_stars_none), style = MaterialTheme.typography.bodyLarge)
        } else {
            Row(
                modifier = Modifier.clearAndSetSemantics { contentDescription = description },
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(stars) {
                    Icon(
                        painter = painterResource(R.drawable.ic_parent_star),
                        contentDescription = null,
                        tint = FinEduTheme.colors.gold,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
        SecondaryText(stringResource(R.string.parent_progress_stars_hint))
    }
}

@Composable
private fun ChartCard(chart: SavingsChart?) {
    ParentCard {
        SectionTitle(stringResource(R.string.parent_progress_chart_title))
        if (chart == null) {
            SecondaryText(stringResource(R.string.parent_progress_chart_empty))
        } else {
            val description = stringResource(
                R.string.parent_progress_chart_a11y,
                chart.weekly.joinToString { it.amount.toString() },
                chart.forecast.joinToString { it.amount.toString() }
            )
            SavingsChartView(
                chart = chart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ChartHeight)
                    .clearAndSetSemantics { contentDescription = description }
            )
            SecondaryText(stringResource(R.string.parent_progress_chart_forecast, chart.averagePerWeek.amount))
        }
    }
}

/** Линия — копилка к концу закрытых недель (от нуля), пунктир — прогноз при текущем среднем. */
@Composable
private fun SavingsChartView(chart: SavingsChart, modifier: Modifier = Modifier) {
    val lineColor = FinEduTheme.colors.savings
    val forecastColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelStyle = MaterialTheme.typography.labelMedium.copy(color = forecastColor)
    val measurer = rememberTextMeasurer()
    Canvas(modifier = modifier) {
        val actual = listOf(0) + chart.weekly.map { it.amount }
        val forecast = chart.forecast.map { it.amount }
        val all = actual + forecast
        val maxValue = all.max().coerceAtLeast(1)
        val labelHeight = measurer.measure("0", labelStyle).size.height.toFloat()
        val plotHeight = size.height - labelHeight - LabelGap.toPx()
        val radius = PointRadius.toPx()
        val stepX = (size.width - 2 * radius) / (all.size - 1)
        fun point(index: Int, value: Int) =
            Offset(radius + index * stepX, radius + (plotHeight - 2 * radius) * (1f - value.toFloat() / maxValue))

        val line = Path()
        actual.forEachIndexed { index, value ->
            val p = point(index, value)
            if (index == 0) line.moveTo(p.x, p.y) else line.lineTo(p.x, p.y)
        }
        drawPath(line, lineColor, style = Stroke(width = LineWidth.toPx()))

        val dashed = Path()
        val start = point(actual.lastIndex, actual.last())
        dashed.moveTo(start.x, start.y)
        forecast.forEachIndexed { index, value ->
            val p = point(actual.size + index, value)
            dashed.lineTo(p.x, p.y)
        }
        drawPath(
            dashed,
            forecastColor,
            style = Stroke(
                width = LineWidth.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(DashLength.toPx(), DashLength.toPx()))
            )
        )
        actual.forEachIndexed { index, value -> drawCircle(lineColor, radius, point(index, value)) }

        for (week in 1 until all.size) {
            val layout = measurer.measure(week.toString(), labelStyle)
            val x = (point(week, 0).x - layout.size.width / 2f).coerceAtMost(size.width - layout.size.width)
            drawText(layout, topLeft = Offset(x, size.height - labelHeight))
        }
    }
}

@Composable
private fun ThemesCard(themes: List<ThemeProgress>) {
    ParentCard {
        SectionTitle(stringResource(R.string.parent_progress_themes))
        themes.forEach { ThemeRow(it) }
    }
}

@Composable
private fun ThemeRow(item: ThemeProgress) {
    val status = when (item.status) {
        ThemeStatus.DONE -> R.string.parent_progress_status_done
        ThemeStatus.IN_PROGRESS -> R.string.parent_progress_status_in_progress
        ThemeStatus.AHEAD -> R.string.parent_progress_status_ahead
    }
    val name = when (item.theme) {
        QuestTheme.BUDGET -> R.string.parent_progress_theme_budget
        QuestTheme.SAVINGS -> R.string.parent_progress_theme_savings
        QuestTheme.PURCHASES -> R.string.parent_progress_theme_purchases
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatusDot(item.status)
        Text(
            text = stringResource(name),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        SecondaryText(stringResource(status))
    }
}

@Composable
private fun StatusDot(status: ThemeStatus) {
    val colors = FinEduTheme.colors
    val border = MaterialTheme.colorScheme.outline
    Box(
        modifier = Modifier
            .size(32.dp)
            .border(width = 2.dp, color = border, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        when (status) {
            ThemeStatus.DONE -> Icon(
                painter = painterResource(CoreR.drawable.ic_check),
                contentDescription = null,
                tint = colors.savings,
                modifier = Modifier.size(20.dp)
            )

            ThemeStatus.IN_PROGRESS -> Icon(
                painter = painterResource(R.drawable.ic_parent_dots),
                contentDescription = null,
                tint = colors.gold,
                modifier = Modifier.size(20.dp)
            )

            ThemeStatus.AHEAD -> Unit
        }
    }
}

private val ChartHeight = 160.dp
private val LabelGap = 6.dp
private val PointRadius = 5.dp
private val LineWidth = 3.dp
private val DashLength = 6.dp

internal val PreviewProgress = ProgressUiState(
    playerName = "Капитан Носок",
    petName = "Дзынь",
    week = 3,
    totalWeeks = 5,
    storyTitle = "Плановый осмотр",
    stars = 2,
    savings = 120,
    goalTitle = "Самокат",
    goalPrice = 300,
    chart = SavingsChart(
        weekly = listOf(Dzynki(60), Dzynki(120)),
        forecast = listOf(Dzynki(180), Dzynki(240), Dzynki(300)),
        averagePerWeek = Dzynki(60)
    ),
    themes = listOf(
        ThemeProgress(QuestTheme.BUDGET, ThemeStatus.DONE),
        ThemeProgress(QuestTheme.SAVINGS, ThemeStatus.IN_PROGRESS),
        ThemeProgress(QuestTheme.PURCHASES, ThemeStatus.AHEAD)
    )
)

@Preview(heightDp = 1400)
@Composable
private fun ProgressTabPreview() {
    FinEduPreview {
        ProgressTab(progress = PreviewProgress)
    }
}

package ru.lct2026.finedu.feature.budget.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.productcore.domain.model.Bag
import ru.lct2026.finedu.productcore.domain.model.BagAmounts
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.ui.components.color
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview

/** Полоса раскладки: три мешочка подряд и незаполненный остаток из [total]. */
@Composable
internal fun DistributionBar(draft: BagAmounts, total: Int, description: String, modifier: Modifier = Modifier) {
    val colors = Bag.entries.map { it.color }
    val track = MaterialTheme.colorScheme.onSurface.copy(alpha = TRACK_ALPHA)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .semantics { contentDescription = description }
    ) {
        drawRect(color = track)
        if (total <= 0) return@Canvas
        var x = 0f
        Bag.entries.forEachIndexed { index, bag ->
            val width = size.width * draft[bag].amount / total
            drawRect(color = colors[index], topLeft = Offset(x, 0f), size = Size(width, size.height))
            x += width
        }
    }
}

/** План пунктиром, факт заливкой. Ширины — доли от [max]. */
@Composable
internal fun PlanFactBar(plan: Int, fact: Int, max: Int, color: Color, modifier: Modifier = Modifier) {
    val outline = MaterialTheme.colorScheme.onSurface.copy(alpha = OUTLINE_ALPHA)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp)
    ) {
        if (max <= 0) return@Canvas
        val radius = CornerRadius(size.height / 2)
        val strokeWidth = 2.dp.toPx()
        if (fact > 0) {
            drawRoundRect(
                color = color,
                size = Size(size.width * fact.coerceAtMost(max) / max, size.height),
                cornerRadius = radius
            )
        }
        if (plan > 0) {
            val dash = 6.dp.toPx()
            drawRoundRect(
                color = outline,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(size.width * plan / max - strokeWidth, size.height - strokeWidth),
                cornerRadius = radius,
                style = Stroke(width = strokeWidth, pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash)))
            )
        }
    }
}

private const val TRACK_ALPHA = 0.12f
private const val OUTLINE_ALPHA = 0.7f

@Preview
@Composable
private fun BudgetBarsPreview() {
    FinEduPreview {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            DistributionBar(
                draft = BagAmounts(Dzynki(150), Dzynki(90), Dzynki(20)),
                total = 300,
                description = ""
            )
            PlanFactBar(plan = 150, fact = 80, max = 150, color = Bag.NEEDS.color)
            PlanFactBar(plan = 30, fact = 30, max = 150, color = Bag.SAVINGS.color)
        }
    }
}

package ru.lct2026.finedu.productcore.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.productcore.domain.model.GameRules
import ru.lct2026.finedu.productcore.domain.model.PetStat
import ru.lct2026.finedu.productcore.ui.R
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

/**
 * Показатель питомца — стеклянная пробирка с жидкостью цвета мешочка (стиль из `app_resources`). Под ней иконка и
 * подпись; [showValue] = false — режим 7–8 лет: шкала без цифр.
 */
@Composable
fun TubeIndicator(stat: PetStat, value: Int, modifier: Modifier = Modifier, showValue: Boolean = true) {
    val label = stringResource(stat.labelRes)
    val description = if (value <= GameRules.TIRED_THRESHOLD) {
        stringResource(R.string.stat_tired_a11y, label)
    } else {
        stringResource(R.string.stat_value_a11y, label, value)
    }
    val liquid = stat.color
    val glass = FinEduTheme.colors.glassStrongBorder
    val glassFill = FinEduTheme.colors.glassTop
    val highlight = FinEduTheme.colors.glassHighlight
    Column(
        modifier = modifier
            .widthIn(min = 72.dp)
            .semantics(mergeDescendants = true) { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Canvas(modifier = Modifier.size(width = 28.dp, height = 88.dp)) {
            val radius = size.width / 2
            val tube = Path().apply {
                addRoundRect(RoundRect(0f, 0f, size.width, size.height, CornerRadius(radius, radius)))
            }
            drawPath(tube, glassFill)
            val fraction = value.coerceIn(0, GameRules.STAT_MAX) / GameRules.STAT_MAX.toFloat()
            val top = size.height * (1 - fraction)
            clipPath(tube) {
                drawRect(
                    brush = Brush.horizontalGradient(listOf(liquid, liquid.copy(alpha = LIQUID_EDGE_ALPHA), liquid)),
                    topLeft = Offset(0f, top),
                    size = Size(size.width, size.height - top)
                )
                // Поверхность жидкости — светлый эллипс.
                drawOval(
                    color = Color.White.copy(alpha = SURFACE_ALPHA),
                    topLeft = Offset(0f, top - SURFACE_HEIGHT_PX / 2),
                    size = Size(size.width, SURFACE_HEIGHT_PX)
                )
            }
            drawPath(tube, glass, style = Stroke(width = 1.5.dp.toPx()))
            // Блик на стекле слева.
            drawLine(
                color = highlight,
                start = Offset(size.width * HIGHLIGHT_X, radius),
                end = Offset(size.width * HIGHLIGHT_X, size.height - radius),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(
                painter = painterResource(stat.iconRes),
                contentDescription = null,
                tint = liquid,
                modifier = Modifier.size(16.dp)
            )
            if (showValue) Text(text = value.toString(), style = MaterialTheme.typography.titleSmall)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private const val LIQUID_EDGE_ALPHA = 0.7f
private const val SURFACE_ALPHA = 0.35f
private const val SURFACE_HEIGHT_PX = 8f
private const val HIGHLIGHT_X = 0.28f

@Preview
@Composable
private fun TubeIndicatorPreview() {
    FinEduPreview {
        Row(modifier = Modifier.padding(20.dp)) {
            TubeIndicator(stat = PetStat.CHARGE, value = 70)
            TubeIndicator(stat = PetStat.VIBE, value = 25)
            TubeIndicator(stat = PetStat.CALM, value = 50, showValue = false)
        }
    }
}

package ru.lct2026.finedu.productcore.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

/** Насыщенность «стекла»: обычная карточка или выделенная плашка. */
enum class GlassStyle { Regular, Strong }

/**
 * «Жидкое стекло» без blur (blur есть только с Android 12, а ТЗ требует Android 8+): диагональная
 * полупрозрачная заливка, волосяная граница и светлая кромка сверху.
 */
@Composable
fun Modifier.glass(shape: Shape = MaterialTheme.shapes.large, style: GlassStyle = GlassStyle.Regular): Modifier {
    val colors = FinEduTheme.colors
    val (top, bottom, border) = when (style) {
        GlassStyle.Regular -> Triple(colors.glassTop, colors.glassBottom, colors.glassBorder)
        GlassStyle.Strong -> Triple(colors.glassStrongTop, colors.glassStrongBottom, colors.glassStrongBorder)
    }
    val highlight = colors.glassHighlight
    return this
        .clip(shape)
        .background(Brush.linearGradient(listOf(top, bottom), start = Offset.Zero, end = Offset.Infinite), shape)
        .drawWithContent {
            drawContent()
            drawLine(
                color = highlight,
                start = Offset(0f, HIGHLIGHT_WIDTH_PX / 2),
                end = Offset(size.width, HIGHLIGHT_WIDTH_PX / 2),
                strokeWidth = HIGHLIGHT_WIDTH_PX
            )
        }
        .border(width = 1.dp, color = border, shape = shape)
}

private const val HIGHLIGHT_WIDTH_PX = 2f

@Preview
@Composable
private fun GlassPreview() {
    FinEduPreview {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Нужное",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .glass()
                    .padding(16.dp)
            )
            Text(
                text = "Демонстрационный режим",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .glass(style = GlassStyle.Strong)
                    .padding(16.dp)
            )
        }
    }
}

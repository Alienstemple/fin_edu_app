package ru.lct2026.finedu.productcore.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

/**
 * Общий фон всех экранов: три мягких цветовых пятна (фиолетовое сверху слева, небесное справа, розовое снизу)
 * на чернильном вертикальном градиенте. Экраны поверх него рисуются с прозрачным контейнером.
 */
@Composable
fun FinEduBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val colors = FinEduTheme.colors
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(Brush.verticalGradient(listOf(colors.backgroundTop, colors.backgroundBottom)))
                drawSpot(colors.backgroundSpotViolet, relativeCenter = VioletSpotCenter)
                drawSpot(colors.backgroundSpotSky, relativeCenter = SkySpotCenter)
                drawSpot(colors.backgroundSpotPink, relativeCenter = PinkSpotCenter)
            }
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
            content()
        }
    }
}

private fun DrawScope.drawSpot(color: Color, relativeCenter: Offset) {
    val center = Offset(size.width * relativeCenter.x, size.height * relativeCenter.y)
    val radius = size.width * SPOT_RADIUS_FRACTION
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color, color.copy(alpha = 0f)),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}

private const val SPOT_RADIUS_FRACTION = 0.8f

// Центры пятен в долях ширины и высоты экрана — как радиальные градиенты фона в макете.
private val VioletSpotCenter = Offset(x = 0.1f, y = -0.05f)
private val SkySpotCenter = Offset(x = 1.05f, y = 0.38f)
private val PinkSpotCenter = Offset(x = 0.2f, y = 1.08f)

@Preview
@Composable
private fun FinEduBackgroundPreview() {
    FinEduPreview {}
}

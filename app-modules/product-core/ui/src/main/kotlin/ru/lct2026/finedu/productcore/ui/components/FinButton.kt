package ru.lct2026.finedu.productcore.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

/** Минимальный размер зоны нажатия для детей (ТЗ: не меньше 48dp). */
val MinTouchTarget = 48.dp

/** Высота основной кнопки по макету. */
private val PrimaryButtonHeight = 56.dp

private const val DISABLED_ALPHA = 0.4f

enum class FinButtonStyle { Primary, Secondary }

@Composable
fun FinButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: FinButtonStyle = FinButtonStyle.Primary,
    enabled: Boolean = true
) {
    when (style) {
        FinButtonStyle.Primary -> {
            val colors = FinEduTheme.colors
            val shape = MaterialTheme.shapes.medium
            Button(
                onClick = onClick,
                enabled = enabled,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = modifier
                    .fillMaxWidth()
                    .heightIn(min = PrimaryButtonHeight)
                    .alpha(if (enabled) 1f else DISABLED_ALPHA)
                    .background(
                        brush = Brush.linearGradient(listOf(colors.buttonGradientStart, colors.buttonGradientEnd)),
                        shape = shape
                    )
            ) {
                Text(text = text, style = MaterialTheme.typography.labelLarge)
            }
        }

        FinButtonStyle.Secondary -> OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, FinEduTheme.colors.glassBorder),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = MinTouchTarget)
        ) {
            Text(text = text, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Preview
@Composable
private fun FinButtonPreview() {
    FinEduPreview {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FinButton(text = "Создать своего питомца", onClick = {})
            FinButton(text = "Подтвердить план", onClick = {}, enabled = false)
            FinButton(text = "Назад к подсказке", onClick = {}, style = FinButtonStyle.Secondary)
        }
    }
}

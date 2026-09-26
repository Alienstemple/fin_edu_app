package ru.lct2026.finedu.productcore.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview

/** Минимальный размер зоны нажатия для детей (ТЗ: не меньше 48dp). */
val MinTouchTarget = 48.dp

enum class FinButtonStyle { Primary, Secondary }

@Composable
fun FinButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: FinButtonStyle = FinButtonStyle.Primary,
    enabled: Boolean = true
) {
    val buttonModifier = modifier
        .fillMaxWidth()
        .heightIn(min = MinTouchTarget)
    when (style) {
        FinButtonStyle.Primary -> Button(onClick = onClick, modifier = buttonModifier, enabled = enabled) {
            Text(text = text)
        }

        FinButtonStyle.Secondary -> OutlinedButton(onClick = onClick, modifier = buttonModifier, enabled = enabled) {
            Text(text = text)
        }
    }
}

@PreviewLightDark
@Composable
private fun FinButtonPreview() {
    FinEduPreview {
        FinButton(text = "Магазин", onClick = {})
    }
}

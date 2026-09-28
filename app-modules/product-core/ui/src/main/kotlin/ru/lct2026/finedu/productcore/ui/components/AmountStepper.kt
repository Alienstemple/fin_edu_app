package ru.lct2026.finedu.productcore.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.ui.R
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview

/** Шаг степпера: суммы кратны 10 (ТЗ: простые вычисления). */
const val STEPPER_STEP = 10

/** «−10 · 120 · +10». Недоступная кнопка приглушена и не нажимается — уйти за границы нельзя. */
@Composable
fun AmountStepper(
    value: Dzynki,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    modifier: Modifier = Modifier,
    canMinus: Boolean = true,
    canPlus: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        StepButton(
            iconRes = R.drawable.ic_minus,
            description = stringResource(R.string.stepper_minus, STEPPER_STEP),
            enabled = canMinus,
            onClick = onMinus
        )
        Text(
            text = value.amount.toString(),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 52.dp)
        )
        StepButton(
            iconRes = R.drawable.ic_plus,
            description = stringResource(R.string.stepper_plus, STEPPER_STEP),
            enabled = canPlus,
            onClick = onPlus
        )
    }
}

@Composable
private fun StepButton(iconRes: Int, description: String, enabled: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(MinTouchTarget)
            .alpha(if (enabled) 1f else DISABLED_STEP_ALPHA)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .glass(shape = CircleShape, style = GlassStyle.Strong),
            contentAlignment = Alignment.Center
        ) {
            Icon(painter = painterResource(iconRes), contentDescription = description)
        }
    }
}

private const val DISABLED_STEP_ALPHA = 0.4f

@Preview
@Composable
private fun AmountStepperPreview() {
    FinEduPreview {
        AmountStepper(
            value = Dzynki(120),
            onMinus = {},
            onPlus = {},
            canPlus = false,
            modifier = Modifier.padding(20.dp)
        )
    }
}

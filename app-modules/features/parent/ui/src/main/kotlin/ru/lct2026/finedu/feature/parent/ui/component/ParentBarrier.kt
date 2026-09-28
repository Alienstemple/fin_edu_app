package ru.lct2026.finedu.feature.parent.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.feature.parent.domain.model.ParentGate
import ru.lct2026.finedu.feature.parent.ui.R
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.ui.components.FinTopBar
import ru.lct2026.finedu.productcore.ui.components.GlassStyle
import ru.lct2026.finedu.productcore.ui.components.SpeechBubble
import ru.lct2026.finedu.productcore.ui.components.glass
import ru.lct2026.finedu.productcore.ui.illustration.PetMood
import ru.lct2026.finedu.productcore.ui.illustration.PetView
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

/** Барьер «Только для взрослых»: пример и своя цифровая клавиатура 3 × 4. */
@Composable
internal fun ParentBarrier(
    gate: ParentGate,
    input: String,
    isWrongAnswer: Boolean,
    petLook: PetLook?,
    onBack: () -> Unit,
    onDigitClick: (Int) -> Unit,
    onEraseClick: () -> Unit,
    onDoneClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FinTopBar(title = stringResource(R.string.parent_barrier_back), onBack = onBack)
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.parent_barrier_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.semantics { heading() }
            )
            Text(text = stringResource(R.string.parent_barrier_lead), style = MaterialTheme.typography.bodyLarge)
            AnswerRow(gate = gate, input = input, isWrongAnswer = isWrongAnswer)
            Text(
                text = stringResource(
                    if (isWrongAnswer) R.string.parent_barrier_wrong else R.string.parent_barrier_hint
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isWrongAnswer) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            )
            PetLine(petLook = petLook, isWrongAnswer = isWrongAnswer)
            Keypad(onDigitClick = onDigitClick, onEraseClick = onEraseClick, onDoneClick = onDoneClick)
        }
    }
}

@Composable
private fun AnswerRow(gate: ParentGate, input: String, isWrongAnswer: Boolean) {
    val answerDescription = if (input.isEmpty()) {
        stringResource(R.string.parent_barrier_empty_a11y)
    } else {
        stringResource(R.string.parent_barrier_answer_a11y, input)
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.parent_barrier_example, gate.left, gate.right),
            style = MaterialTheme.typography.displaySmall
        )
        Box(
            modifier = Modifier
                .width(AnswerWidth)
                .heightIn(min = KeyHeight)
                .glass(
                    shape = MaterialTheme.shapes.medium,
                    style = if (isWrongAnswer) GlassStyle.Regular else GlassStyle.Strong
                )
                .semantics { contentDescription = answerDescription },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = input.ifEmpty { stringResource(R.string.parent_barrier_empty) },
                style = MaterialTheme.typography.displaySmall,
                color = if (input.isEmpty()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
private fun PetLine(petLook: PetLook?, isWrongAnswer: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (petLook != null) {
            PetView(
                look = petLook,
                mood = if (isWrongAnswer) PetMood.HAPPY else PetMood.THINKING,
                modifier = Modifier.width(PetWidth)
            )
        }
        SpeechBubble(
            text = stringResource(
                if (isWrongAnswer) R.string.parent_barrier_line_wrong else R.string.parent_barrier_line_idle
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun Keypad(onDigitClick: (Int) -> Unit, onEraseClick: () -> Unit, onDoneClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DigitRows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { digit ->
                    val description = stringResource(R.string.parent_barrier_digit_a11y, digit)
                    Key(description = description, onClick = { onDigitClick(digit) }) {
                        Text(text = digit.toString(), style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val eraseDescription = stringResource(R.string.parent_barrier_erase)
            Key(description = eraseDescription, onClick = onEraseClick, style = GlassStyle.Regular) {
                Icon(painter = painterResource(R.drawable.ic_parent_erase), contentDescription = null)
            }
            val zeroDescription = stringResource(R.string.parent_barrier_digit_a11y, 0)
            Key(description = zeroDescription, onClick = { onDigitClick(0) }) {
                Text(text = "0", style = MaterialTheme.typography.headlineMedium)
            }
            val done = stringResource(R.string.parent_barrier_done)
            Key(description = done, onClick = onDoneClick, isAccent = true) {
                Text(text = done, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun RowScope.Key(
    description: String,
    onClick: () -> Unit,
    style: GlassStyle = GlassStyle.Strong,
    isAccent: Boolean = false,
    content: @Composable () -> Unit
) {
    val shape = MaterialTheme.shapes.medium
    val colors = FinEduTheme.colors
    val background = if (isAccent) {
        Modifier
            .clip(shape)
            .background(Brush.linearGradient(listOf(colors.buttonGradientStart, colors.buttonGradientEnd)), shape)
    } else {
        Modifier.glass(shape = shape, style = style)
    }
    Box(
        modifier = Modifier
            .weight(1f)
            .heightIn(min = KeyHeight)
            .then(background)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

private val DigitRows = listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9))
private val KeyHeight = 64.dp
private val AnswerWidth = 96.dp
private val PetWidth = 96.dp

@Preview
@Composable
private fun ParentBarrierPreview() {
    FinEduPreview {
        ParentBarrier(
            gate = ParentGate(7, 8),
            input = "5",
            isWrongAnswer = false,
            petLook = PetLook(PetFur.LILAC, PetHat.NONE),
            onBack = {},
            onDigitClick = {},
            onEraseClick = {},
            onDoneClick = {}
        )
    }
}

@Preview
@Composable
private fun ParentBarrierWrongPreview() {
    FinEduPreview {
        ParentBarrier(
            gate = ParentGate(7, 8),
            input = "",
            isWrongAnswer = true,
            petLook = PetLook(PetFur.MINT, PetHat.CAP),
            onBack = {},
            onDigitClick = {},
            onEraseClick = {},
            onDoneClick = {}
        )
    }
}

package ru.lct2026.finedu.feature.savings.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.feature.savings.ui.R
import ru.lct2026.finedu.feature.savings.ui.SAVINGS_STEP
import ru.lct2026.finedu.feature.savings.ui.SavingsSheet
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.PetStage
import ru.lct2026.finedu.productcore.domain.model.WithdrawPreview
import ru.lct2026.finedu.productcore.ui.components.AmountStepper
import ru.lct2026.finedu.productcore.ui.components.FinButton
import ru.lct2026.finedu.productcore.ui.components.FinButtonStyle
import ru.lct2026.finedu.productcore.ui.components.MinTouchTarget
import ru.lct2026.finedu.productcore.ui.components.SpeechBubble
import ru.lct2026.finedu.productcore.ui.components.dzynkiText
import ru.lct2026.finedu.productcore.ui.components.glass
import ru.lct2026.finedu.productcore.ui.illustration.PetMood
import ru.lct2026.finedu.productcore.ui.illustration.PetView
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview

/** Шторка «Пополнить» или «Снять» со степпером. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AmountSheet(
    sheet: SavingsSheet,
    look: PetLook,
    stage: PetStage,
    wantsLeft: Dzynki,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        AmountSheetContent(
            sheet = sheet,
            look = look,
            stage = stage,
            wantsLeft = wantsLeft,
            onMinus = onMinus,
            onPlus = onPlus,
            onConfirm = onConfirm,
            onDismiss = onDismiss,
            modifier = Modifier.navigationBarsPadding()
        )
    }
}

@Composable
private fun AmountSheetContent(
    sheet: SavingsSheet,
    look: PetLook,
    stage: PetStage,
    wantsLeft: Dzynki,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (sheet) {
            is SavingsSheet.Deposit -> {
                SheetTitle(stringResource(R.string.savings_deposit_title))
                Text(
                    text = stringResource(R.string.savings_deposit_from_wants, dzynkiText(wantsLeft)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StepperRow(stringResource(R.string.savings_deposit_how_much), sheet, onMinus, onPlus)
                FinButton(
                    text = stringResource(R.string.savings_deposit_confirm, sheet.amount.amount),
                    onClick = onConfirm
                )
            }

            is SavingsSheet.Withdraw -> {
                SheetTitle(stringResource(R.string.savings_withdraw_title))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PetView(look = look, mood = PetMood.NEUTRAL, stage = stage, modifier = Modifier.width(88.dp))
                    SpeechBubble(text = stringResource(R.string.savings_withdraw_line))
                }
                StepperRow(stringResource(R.string.savings_withdraw_how_much), sheet, onMinus, onPlus)
                WithdrawPreviewCard(preview = sheet.preview, goalTitle = sheet.goalTitle)
                FinButton(
                    text = stringResource(R.string.savings_withdraw_confirm, sheet.amount.amount),
                    onClick = onConfirm
                )
                FinButton(
                    text = stringResource(R.string.savings_withdraw_keep),
                    onClick = onDismiss,
                    style = FinButtonStyle.Secondary
                )
            }
        }
    }
}

@Composable
private fun SheetTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
}

@Composable
private fun StepperRow(label: String, sheet: SavingsSheet, onMinus: () -> Unit, onPlus: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glass()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        AmountStepper(
            value = sheet.amount,
            onMinus = onMinus,
            onPlus = onPlus,
            canMinus = sheet.amount.amount > SAVINGS_STEP,
            canPlus = sheet.amount.amount + SAVINGS_STEP <= sheet.max.amount
        )
    }
}

/** Было → станет: сколько останется в копилке и как сдвинется срок цели. */
@Composable
private fun WithdrawPreviewCard(preview: WithdrawPreview, goalTitle: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glass()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        PreviewRow(
            label = stringResource(R.string.savings_in_savings),
            value = stringResource(
                R.string.savings_withdraw_change,
                preview.savingsBefore.amount,
                preview.savingsAfter.amount
            )
        )
        if (goalTitle != null) {
            PreviewRow(
                label = stringResource(R.string.savings_withdraw_term, goalTitle),
                value = stringResource(
                    R.string.savings_withdraw_term_change,
                    weeksText(preview.weeksBefore),
                    weeksText(preview.weeksAfter)
                )
            )
        }
    }
}

@Composable
private fun PreviewRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = MinTouchTarget)
            .padding(vertical = 6.dp)
            .semantics(mergeDescendants = true) {}
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun weeksText(weeks: Int?): String = when (weeks) {
    null -> stringResource(R.string.savings_withdraw_term_unknown)
    0 -> stringResource(R.string.savings_withdraw_term_reached)
    else -> pluralStringResource(R.plurals.savings_weeks, weeks, weeks)
}

@Preview(heightDp = 640)
@Composable
private fun DepositSheetPreview() {
    FinEduPreview {
        AmountSheetContent(
            sheet = SavingsSheet.Deposit(amount = Dzynki(20), max = Dzynki(60)),
            look = PetLook(PetFur.LILAC, PetHat.CAP),
            stage = PetStage.SPRY,
            wantsLeft = Dzynki(60),
            onMinus = {},
            onPlus = {},
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview(heightDp = 760)
@Composable
private fun WithdrawSheetPreview() {
    FinEduPreview {
        AmountSheetContent(
            sheet = SavingsSheet.Withdraw(
                amount = Dzynki(60),
                max = Dzynki(120),
                preview = WithdrawPreview(Dzynki(120), Dzynki(60), weeksBefore = 6, weeksAfter = 8),
                goalTitle = "Самокат"
            ),
            look = PetLook(PetFur.MINT, PetHat.NONE),
            stage = PetStage.BABY,
            wantsLeft = Dzynki(40),
            onMinus = {},
            onPlus = {},
            onConfirm = {},
            onDismiss = {}
        )
    }
}

package ru.lct2026.finedu.productcore.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ru.lct2026.finedu.productcore.ui.R
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview

/**
 * Подтверждение необратимого действия: сброс, удаление профиля. [items] — что именно пропадёт, [note] — мелкий
 * текст под списком. Кнопка «Отмена» всегда есть.
 */
@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    items: List<String> = emptyList(),
    note: String? = null
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .glass(style = GlassStyle.Strong)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.semantics { heading() }
            )
            Text(text = text, style = MaterialTheme.typography.bodyMedium)
            items.forEach { Text(text = "• $it", style = MaterialTheme.typography.bodyMedium) }
            if (note != null) {
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FinButton(text = stringResource(R.string.dialog_cancel), onClick = onDismiss)
            FinButton(text = confirmText, onClick = onConfirm, style = FinButtonStyle.Secondary)
        }
    }
}

@Preview
@Composable
private fun ConfirmDialogPreview() {
    FinEduPreview {
        ConfirmDialog(
            title = "Сбросить прогресс?",
            text = "Игра вернётся к первой неделе. Обнулятся:",
            items = listOf("недели, задания и звёздочки на пледе", "мешочки, копилка и цели"),
            note = "Дзынь, его имя и наряд останутся.",
            confirmText = "Сбросить",
            onConfirm = {},
            onDismiss = {}
        )
    }
}

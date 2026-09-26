package ru.lct2026.finedu.productcore.ui.preview

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

/**
 * Обёртка для всех `@PreviewLightDark`: тема + фон. Composition locals приложения добавлять сюда же.
 */
@Composable
fun FinEduPreview(content: @Composable () -> Unit) {
    FinEduTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            content()
        }
    }
}

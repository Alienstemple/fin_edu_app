package ru.lct2026.finedu.productcore.ui.preview

import androidx.compose.runtime.Composable
import ru.lct2026.finedu.productcore.ui.components.FinEduBackground
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

/**
 * Обёртка для всех `@Preview`: тема + общий фон экранов. Composition locals приложения добавлять сюда же.
 */
@Composable
fun FinEduPreview(content: @Composable () -> Unit) {
    FinEduTheme {
        FinEduBackground {
            content()
        }
    }
}

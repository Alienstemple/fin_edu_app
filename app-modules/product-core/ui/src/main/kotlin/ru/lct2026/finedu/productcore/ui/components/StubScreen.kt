package ru.lct2026.finedu.productcore.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview

/**
 * Временный экран каркаса: заголовок, описание и кнопки переходов.
 * Заменяется реальной вёрсткой по мере готовности фичи.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StubScreen(title: String, description: String, actions: List<StubAction>, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                title = { Text(text = title, modifier = Modifier.semantics { heading() }) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = description, style = MaterialTheme.typography.bodyLarge)
            actions.forEach { action ->
                FinButton(text = action.label, onClick = action.onClick, style = FinButtonStyle.Secondary)
            }
        }
    }
}

@Preview
@Composable
private fun StubScreenPreview() {
    FinEduPreview {
        StubScreen(
            title = "Главный",
            description = "Здесь будет питомец, баланс и текущая цель.",
            actions = listOf(StubAction("Магазин") {}, StubAction("Копилка") {})
        )
    }
}

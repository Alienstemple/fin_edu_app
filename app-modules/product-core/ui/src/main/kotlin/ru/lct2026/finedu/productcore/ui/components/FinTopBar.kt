package ru.lct2026.finedu.productcore.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.productcore.ui.R
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview

/**
 * Шапка экрана: «назад» (если [onBack] не `null`), заголовок, «?» → «Полезное» и замок → раздел для взрослого.
 * Кнопки, для которых не передан обработчик, не показываются. Отступ под статус-бар шапка делает сама.
 */
@Composable
fun FinTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    onHelp: (() -> Unit)? = null,
    onParent: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        onBack?.let { TopBarButton(R.drawable.ic_back, stringResource(R.string.top_bar_back), it) }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.semantics { heading() }
            )
        }
        onHelp?.let { TopBarButton(R.drawable.ic_help, stringResource(R.string.top_bar_help), it) }
        onParent?.let { TopBarButton(R.drawable.ic_lock, stringResource(R.string.top_bar_parent), it) }
    }
}

@Composable
private fun TopBarButton(iconRes: Int, description: String, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(MinTouchTarget)) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .glass(shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(painter = painterResource(iconRes), contentDescription = description)
        }
    }
}

@Preview
@Composable
private fun FinTopBarPreview() {
    FinEduPreview {
        Column {
            FinTopBar(title = "Хочу всё", subtitle = "Неделя 2 из 5", onHelp = {}, onParent = {})
            FinTopBar(title = "Магазин", onBack = {})
        }
    }
}

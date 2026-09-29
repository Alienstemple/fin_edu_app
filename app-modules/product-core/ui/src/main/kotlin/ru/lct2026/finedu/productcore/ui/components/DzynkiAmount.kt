package ru.lct2026.finedu.productcore.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.ui.R
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview

/** Сумма с монеткой. Для TalkBack читается словами: «120 дзынек». */
@Composable
fun DzynkiAmount(
    amount: Dzynki,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    coinSize: Dp = 20.dp
) {
    val description = dzynkiText(amount)
    Row(
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_coin),
            contentDescription = null,
            modifier = Modifier.size(coinSize)
        )
        Text(text = amount.amount.toString(), style = style)
    }
}

@Preview
@Composable
private fun DzynkiAmountPreview() {
    FinEduPreview {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DzynkiAmount(amount = Dzynki(120))
            DzynkiAmount(
                amount = Dzynki(300),
                style = MaterialTheme.typography.displaySmall,
                coinSize = 32.dp
            )
        }
    }
}

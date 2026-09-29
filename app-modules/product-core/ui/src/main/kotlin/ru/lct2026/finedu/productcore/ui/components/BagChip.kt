package ru.lct2026.finedu.productcore.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.productcore.domain.model.Bag
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview

/** Значок мешочка: иконка на круге цвета мешочка. */
@Composable
fun BagIcon(bag: Bag, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(36.dp)
            .background(bag.color.copy(alpha = BAG_ICON_BACKGROUND_ALPHA), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(bag.iconRes),
            contentDescription = null,
            tint = bag.color,
            modifier = Modifier.size(20.dp)
        )
    }
}

/** Строка мешочка: значок, подпись и сумма. [amount] `null` — только подпись. */
@Composable
fun BagChip(bag: Bag, amount: Dzynki?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .heightIn(min = MinTouchTarget)
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BagIcon(bag = bag)
        Text(
            text = stringResource(bag.labelRes),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        if (amount != null) DzynkiAmount(amount = amount)
    }
}

private const val BAG_ICON_BACKGROUND_ALPHA = 0.18f

@Preview
@Composable
private fun BagChipPreview() {
    FinEduPreview {
        Column(modifier = Modifier.padding(20.dp)) {
            BagChip(bag = Bag.NEEDS, amount = Dzynki(70))
            BagChip(bag = Bag.WANTS, amount = Dzynki(60))
            BagChip(bag = Bag.SAVINGS, amount = Dzynki(120))
        }
    }
}

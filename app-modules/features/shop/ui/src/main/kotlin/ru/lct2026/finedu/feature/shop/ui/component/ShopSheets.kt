package ru.lct2026.finedu.feature.shop.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.feature.shop.ui.R
import ru.lct2026.finedu.feature.shop.ui.ShopSheet
import ru.lct2026.finedu.productcore.domain.model.Bag
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.GameRules
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.PetStage
import ru.lct2026.finedu.productcore.domain.model.ShopItem
import ru.lct2026.finedu.productcore.ui.components.DzynkiAmount
import ru.lct2026.finedu.productcore.ui.components.FinButton
import ru.lct2026.finedu.productcore.ui.components.FinButtonStyle
import ru.lct2026.finedu.productcore.ui.components.MinTouchTarget
import ru.lct2026.finedu.productcore.ui.components.SpeechBubble
import ru.lct2026.finedu.productcore.ui.components.color
import ru.lct2026.finedu.productcore.ui.components.dzynkiText
import ru.lct2026.finedu.productcore.ui.components.glass
import ru.lct2026.finedu.productcore.ui.components.iconRes
import ru.lct2026.finedu.productcore.ui.components.labelRes
import ru.lct2026.finedu.productcore.ui.illustration.PetMood
import ru.lct2026.finedu.productcore.ui.illustration.PetView
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ShopBottomSheet(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

/** Шторка покупки: что, сколько, из какого мешочка, что станет с остатком и с Дзынем. */
@Composable
internal fun PurchaseContent(sheet: ShopSheet.Purchase, onBuy: () -> Unit, onWait: () -> Unit) {
    val item = sheet.item
    ItemHeader(item = item)
    Column(modifier = Modifier.glass().padding(horizontal = 14.dp, vertical = 4.dp)) {
        InfoRow(label = stringResource(item.bag.fromLabelRes)) {
            Text(
                text = stringResource(R.string.shop_change, sheet.remaining.amount, sheet.after.amount),
                style = MaterialTheme.typography.titleSmall
            )
        }
        val stat = item.bag.stat
        InfoRow(label = stringResource(R.string.shop_stat, stringResource(stat.labelRes))) {
            Text(
                text = stringResource(R.string.shop_stat_plus, GameRules.STAT_STEP),
                style = MaterialTheme.typography.titleSmall,
                color = FinEduTheme.colors.gold
            )
        }
    }
    Text(text = stringResource(item.bag.noteRes), style = MaterialTheme.typography.bodyMedium)
    FinButton(text = stringResource(R.string.shop_buy), onClick = onBuy)
    FinButton(text = stringResource(R.string.shop_wait), onClick = onWait, style = FinButtonStyle.Secondary)
}

/** Шторка «Не хватает N»: три равноценных выхода, без давления. */
@Composable
internal fun ShortageContent(
    sheet: ShopSheet.Shortage,
    look: PetLook,
    stage: PetStage,
    onSaveUp: () -> Unit,
    onDoQuest: () -> Unit,
    onCheaper: () -> Unit
) {
    val item = sheet.item
    ItemHeader(item = item)
    Text(
        text = stringResource(R.string.shop_shortage_title, dzynkiText(sheet.missing)),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.semantics { heading() }
    )
    Text(
        text = stringResource(
            R.string.shop_shortage_note,
            stringResource(item.bag.inLabelRes),
            sheet.remaining.amount
        ),
        style = MaterialTheme.typography.bodyMedium
    )
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PetView(look = look, mood = PetMood.THINKING, stage = stage, modifier = Modifier.width(72.dp))
        SpeechBubble(
            text = item.shortageLine ?: stringResource(R.string.shop_shortage_line),
            modifier = Modifier.weight(1f)
        )
    }
    FinButton(text = stringResource(R.string.shop_save_up), onClick = onSaveUp, style = FinButtonStyle.Secondary)
    FinButton(text = stringResource(R.string.shop_do_quest), onClick = onDoQuest, style = FinButtonStyle.Secondary)
    FinButton(text = stringResource(R.string.shop_cheaper), onClick = onCheaper, style = FinButtonStyle.Secondary)
}

/**
 * Картинка товара на кружке цвета мешочка. Рисунок выбирается по `id` из `shop.json`; для товара без рисунка
 * (например, добавленного в контент позже) — значок мешочка.
 */
@Composable
internal fun ItemPicture(item: ShopItem, size: Dp, modifier: Modifier = Modifier) {
    val bag = item.bag
    Box(
        modifier = modifier
            .size(size)
            .background(bag.color.copy(alpha = PICTURE_BACKGROUND_ALPHA), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val pictureRes = item.pictureRes
        if (pictureRes != null) {
            Image(
                painter = painterResource(pictureRes),
                contentDescription = null,
                modifier = Modifier.size(size * PICTURE_SCALE)
            )
        } else {
            Icon(
                painter = painterResource(bag.iconRes),
                contentDescription = null,
                tint = bag.color,
                modifier = Modifier.size(size / 2)
            )
        }
    }
}

@get:DrawableRes
private val ShopItem.pictureRes: Int?
    get() = when (id) {
        "kasha" -> R.drawable.ic_item_kasha
        "socks" -> R.drawable.ic_item_socks
        "soap" -> R.drawable.ic_item_soap
        "brush" -> R.drawable.ic_item_brush
        "scarf" -> R.drawable.ic_item_scarf
        "slippers" -> R.drawable.ic_item_slippers
        "hot_water_bottle" -> R.drawable.ic_item_hot_water_bottle
        "apples" -> R.drawable.ic_item_apples
        "ball" -> R.drawable.ic_item_ball
        "hat" -> R.drawable.ic_item_hat
        "garland" -> R.drawable.ic_item_garland
        "stickers" -> R.drawable.ic_item_stickers
        "kite" -> R.drawable.ic_item_kite
        "bow_tie" -> R.drawable.ic_item_bow_tie
        "headphones" -> R.drawable.ic_item_headphones
        "puzzle" -> R.drawable.ic_item_puzzle
        else -> null
    }

@Composable
private fun ItemHeader(item: ShopItem) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        ItemPicture(item = item, size = 64.dp)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() }
            )
            DzynkiAmount(amount = item.price)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = MinTouchTarget)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        value()
    }
}

internal val Bag.inLabelRes: Int
    get() = when (this) {
        Bag.NEEDS -> R.string.shop_in_needs
        Bag.WANTS, Bag.SAVINGS -> R.string.shop_in_wants
    }

private val Bag.fromLabelRes: Int
    get() = when (this) {
        Bag.NEEDS -> R.string.shop_from_needs
        Bag.WANTS, Bag.SAVINGS -> R.string.shop_from_wants
    }

private val Bag.noteRes: Int
    get() = when (this) {
        Bag.NEEDS -> R.string.shop_note_needs
        Bag.WANTS, Bag.SAVINGS -> R.string.shop_note_wants
    }

private const val PICTURE_BACKGROUND_ALPHA = 0.18f
private const val PICTURE_SCALE = 0.78f

private val PreviewBall = ShopItem("ball", "Мячик-попрыгун", Dzynki(30), Bag.WANTS, shortageLine = null)
private val PreviewHat = ShopItem("hat", "Шляпа с пером", Dzynki(100), Bag.WANTS, "Шляпа такая красивая…")

@Preview(heightDp = 600)
@Composable
private fun PurchaseContentPreview() {
    FinEduPreview {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            PurchaseContent(sheet = ShopSheet.Purchase(PreviewBall, Dzynki(60)), onBuy = {}, onWait = {})
        }
    }
}

@Preview(heightDp = 700)
@Composable
private fun ShortageContentPreview() {
    FinEduPreview {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ShortageContent(
                sheet = ShopSheet.Shortage(PreviewHat, missing = Dzynki(40), remaining = Dzynki(60)),
                look = PetLook(PetFur.MINT, PetHat.CAP),
                stage = PetStage.BABY,
                onSaveUp = {},
                onDoQuest = {},
                onCheaper = {}
            )
        }
    }
}

package ru.lct2026.finedu.feature.shop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.lct2026.finedu.feature.shop.ui.component.ItemPicture
import ru.lct2026.finedu.feature.shop.ui.component.PurchaseContent
import ru.lct2026.finedu.feature.shop.ui.component.ShopBottomSheet
import ru.lct2026.finedu.feature.shop.ui.component.ShortageContent
import ru.lct2026.finedu.feature.shop.ui.component.inLabelRes
import ru.lct2026.finedu.productcore.domain.model.Bag
import ru.lct2026.finedu.productcore.domain.model.BagAmounts
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.FeedbackReason
import ru.lct2026.finedu.productcore.domain.model.ShopItem
import ru.lct2026.finedu.productcore.ui.components.DzynkiAmount
import ru.lct2026.finedu.productcore.ui.components.FeedbackSheet
import ru.lct2026.finedu.productcore.ui.components.FinBottomBar
import ru.lct2026.finedu.productcore.ui.components.FinButton
import ru.lct2026.finedu.productcore.ui.components.FinButtonStyle
import ru.lct2026.finedu.productcore.ui.components.FinTab
import ru.lct2026.finedu.productcore.ui.components.FinTopBar
import ru.lct2026.finedu.productcore.ui.components.GlassStyle
import ru.lct2026.finedu.productcore.ui.components.MinTouchTarget
import ru.lct2026.finedu.productcore.ui.components.color
import ru.lct2026.finedu.productcore.ui.components.dzynkiText
import ru.lct2026.finedu.productcore.ui.components.glass
import ru.lct2026.finedu.productcore.ui.components.iconRes
import ru.lct2026.finedu.productcore.ui.components.labelRes
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview

@Composable
internal fun ShopRoute(navigation: ShopNavigation, viewModel: ShopViewModel = hiltViewModel()) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    ShopScreen(
        state = state,
        navigation = navigation,
        onTabClick = viewModel::onTabClick,
        onItemClick = viewModel::onItemClick,
        onClearFilterClick = viewModel::onClearFilterClick
    )
    SheetHost(
        state = state,
        onDismiss = viewModel::onSheetDismiss,
        onBuy = viewModel::onBuyClick,
        onWait = viewModel::onWaitClick,
        onCheaper = viewModel::onCheaperClick,
        onTab = { tab ->
            viewModel.onSheetDismiss()
            navigation.onTab(tab)
        }
    )
}

@Composable
internal fun ShopScreen(
    state: ShopUiState,
    navigation: ShopNavigation,
    onTabClick: (Bag) -> Unit,
    onItemClick: (ShopItem) -> Unit,
    onClearFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            FinTopBar(
                title = stringResource(R.string.shop_title),
                modifier = Modifier.statusBarsPadding(),
                onHelp = navigation.onHelp,
                onParent = navigation.onParent
            )
        },
        bottomBar = {
            FinBottomBar(
                selected = FinTab.SHOP,
                onSelect = navigation.onTab,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.isPlanPending) {
                item(span = { GridItemSpan(maxLineSpan) }) { PlanPendingBanner(onPlan = navigation.onPlan) }
            }
            item(span = { GridItemSpan(maxLineSpan) }) { BagTabs(selected = state.tab, onTabClick = onTabClick) }
            item(span = { GridItemSpan(maxLineSpan) }) {
                RemainingRow(state = state, onClearFilterClick = onClearFilterClick)
            }
            items(state.items, key = { it.id }) { item ->
                ItemCard(item = item, onClick = { onItemClick(item) })
            }
            if (state.items.isEmpty() && state.catalog.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(text = stringResource(R.string.shop_empty), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun SheetHost(
    state: ShopUiState,
    onDismiss: () -> Unit,
    onBuy: () -> Unit,
    onWait: () -> Unit,
    onCheaper: () -> Unit,
    onTab: (FinTab) -> Unit
) {
    when (val sheet = state.sheet) {
        null -> Unit

        is ShopSheet.Purchase -> ShopBottomSheet(onDismiss = onDismiss) {
            PurchaseContent(sheet = sheet, onBuy = onBuy, onWait = onWait)
        }

        is ShopSheet.Shortage -> ShopBottomSheet(onDismiss = onDismiss) {
            ShortageContent(
                sheet = sheet,
                look = state.look,
                stage = state.stage,
                onSaveUp = { onTab(FinTab.SAVINGS) },
                onDoQuest = { onTab(FinTab.QUESTS) },
                onCheaper = onCheaper
            )
        }

        is ShopSheet.Result -> {
            val isPause = when (sheet.feedback.reason) {
                FeedbackReason.PAUSED -> true

                FeedbackReason.BOUGHT_NEED,
                FeedbackReason.BOUGHT_WANT,
                FeedbackReason.DEPOSITED,
                FeedbackReason.WITHDREW,
                FeedbackReason.GOAL_PLACED -> false
            }
            FeedbackSheet(
                feedback = sheet.feedback,
                look = state.look,
                stage = state.stage,
                title = sheet.item.title,
                onDismiss = { onTab(FinTab.HOME) },
                secondaryText = stringResource(if (isPause) R.string.shop_see_quests else R.string.shop_more),
                onSecondary = if (isPause) ({ onTab(FinTab.QUESTS) }) else onDismiss
            )
        }
    }
}

@Composable
private fun PlanPendingBanner(onPlan: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glass(style = GlassStyle.Strong)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = stringResource(R.string.shop_plan_pending), style = MaterialTheme.typography.titleMedium)
        FinButton(text = stringResource(R.string.shop_plan_pending_action), onClick = onPlan)
    }
}

@Composable
private fun BagTabs(selected: Bag, onTabClick: (Bag) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(Bag.NEEDS, Bag.WANTS).forEach { bag ->
            val isSelected = bag == selected
            Row(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp)
                    .glass(style = if (isSelected) GlassStyle.Strong else GlassStyle.Regular)
                    .background(if (isSelected) bag.color.copy(alpha = SELECTED_TAB_ALPHA) else Color.Transparent)
                    .semantics { this.selected = isSelected }
                    .clickable(role = Role.Tab) { onTabClick(bag) }
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(painter = painterResource(bag.iconRes), contentDescription = null, tint = bag.color)
                Text(
                    text = stringResource(bag.labelRes),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                if (isSelected) {
                    Icon(
                        painter = painterResource(ru.lct2026.finedu.productcore.ui.R.drawable.ic_check),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RemainingRow(state: ShopUiState, onClearFilterClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = stringResource(state.tab.inLabelRes), style = MaterialTheme.typography.bodyMedium)
        DzynkiAmount(amount = state.remaining, modifier = Modifier.weight(1f))
        if (state.isCheaperOnly) {
            val description = stringResource(R.string.shop_filter_a11y)
            Text(
                text = stringResource(R.string.shop_filter, state.remaining.amount),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .heightIn(min = MinTouchTarget)
                    .glass(shape = MaterialTheme.shapes.extraLarge)
                    .clickable(role = Role.Button) { onClearFilterClick() }
                    .semantics { contentDescription = description }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun ItemCard(item: ShopItem, onClick: () -> Unit) {
    val description = stringResource(R.string.shop_item_a11y, item.title, dzynkiText(item.price))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glass()
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = description }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ItemPicture(bag = item.bag, size = 56.dp)
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            minLines = 2,
            maxLines = 2
        )
        DzynkiAmount(amount = item.price)
    }
}

private const val SELECTED_TAB_ALPHA = 0.25f

private val PreviewCatalog = listOf(
    ShopItem("kasha", "Каша", Dzynki(20), Bag.NEEDS, shortageLine = null),
    ShopItem("ball", "Мячик-попрыгун", Dzynki(30), Bag.WANTS, shortageLine = null),
    ShopItem("hat", "Шляпа с пером", Dzynki(100), Bag.WANTS, shortageLine = null),
    ShopItem("garland", "Гирлянда", Dzynki(60), Bag.WANTS, shortageLine = null),
    ShopItem("stickers", "Набор наклеек", Dzynki(40), Bag.WANTS, shortageLine = null)
)

private val PreviewNavigation = ShopNavigation(onTab = {}, onHelp = {}, onParent = {}, onPlan = {})

@Preview(heightDp = 800)
@Composable
private fun ShopScreenPreview() {
    FinEduPreview {
        ShopScreen(
            state = ShopUiState(catalog = PreviewCatalog, bags = BagAmounts(Dzynki(70), Dzynki(60))),
            navigation = PreviewNavigation,
            onTabClick = {},
            onItemClick = {},
            onClearFilterClick = {}
        )
    }
}

@Preview(heightDp = 800)
@Composable
private fun ShopCheaperPreview() {
    FinEduPreview {
        ShopScreen(
            state = ShopUiState(
                catalog = PreviewCatalog,
                bags = BagAmounts(Dzynki(70), Dzynki(60)),
                isCheaperOnly = true
            ),
            navigation = PreviewNavigation,
            onTabClick = {},
            onItemClick = {},
            onClearFilterClick = {}
        )
    }
}

@Preview(heightDp = 800)
@Composable
private fun ShopPlanPendingPreview() {
    FinEduPreview {
        ShopScreen(
            state = ShopUiState(catalog = PreviewCatalog, isPlanPending = true),
            navigation = PreviewNavigation,
            onTabClick = {},
            onItemClick = {},
            onClearFilterClick = {}
        )
    }
}

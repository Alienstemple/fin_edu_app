package ru.lct2026.finedu.feature.shop.ui

import ru.lct2026.finedu.productcore.ui.components.FinTab

/** Переходы из магазина: всё решает навигация фичи. */
internal class ShopNavigation(
    val onTab: (FinTab) -> Unit,
    val onHelp: () -> Unit,
    val onParent: () -> Unit,
    val onPlan: () -> Unit
)

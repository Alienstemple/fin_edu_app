package ru.lct2026.finedu.productcore.ui.components

import ru.lct2026.finedu.productcore.navigation.api.FinEduRoute

/** Маршрут раздела нижней навигации. */
val FinTab.route: FinEduRoute
    get() = when (this) {
        FinTab.HOME -> FinEduRoute.Home
        FinTab.QUESTS -> FinEduRoute.Quests
        FinTab.SHOP -> FinEduRoute.Shop
        FinTab.SAVINGS -> FinEduRoute.Savings
    }

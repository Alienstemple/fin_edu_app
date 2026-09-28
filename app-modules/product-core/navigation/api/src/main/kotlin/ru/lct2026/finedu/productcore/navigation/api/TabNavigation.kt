package ru.lct2026.finedu.productcore.navigation.api

import androidx.navigation.NavController

/** Переход в раздел нижней навигации: стек до главного, без дублей экрана. */
fun NavController.navigateToTab(route: FinEduRoute) {
    navigate(route) {
        popUpTo(FinEduRoute.Home)
        launchSingleTop = true
    }
}

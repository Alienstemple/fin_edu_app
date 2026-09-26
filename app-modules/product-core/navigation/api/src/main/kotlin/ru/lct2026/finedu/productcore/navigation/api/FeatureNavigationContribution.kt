package ru.lct2026.finedu.productcore.navigation.api

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder

/**
 * Вклад фичи в общий граф навигации.
 *
 * Каждый feature `impl` модуль регистрирует свои экраны через Hilt multibinding (`@Binds @IntoSet`),
 * `:app` собирает все вклады в один `NavHost`. Фичи не зависят друг от друга: переходы — только через [FinEduRoute].
 */
interface FeatureNavigationContribution {
    fun NavGraphBuilder.register(navController: NavController)
}

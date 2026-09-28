package ru.lct2026.finedu.feature.budget.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import javax.inject.Inject
import ru.lct2026.finedu.productcore.navigation.api.FeatureNavigationContribution
import ru.lct2026.finedu.productcore.navigation.api.FinEduRoute
import ru.lct2026.finedu.productcore.navigation.api.navigateToTab
import ru.lct2026.finedu.productcore.ui.components.FinTab
import ru.lct2026.finedu.productcore.ui.components.route

internal class BudgetNavigationContribution @Inject constructor() : FeatureNavigationContribution {

    override fun NavGraphBuilder.register(navController: NavController) {
        composable<FinEduRoute.Budget> {
            BudgetRoute(
                onBack = { navController.popBackStack() },
                onHelp = { navController.navigate(FinEduRoute.Glossary) },
                onParent = { navController.navigate(FinEduRoute.Parent) },
                onShop = { navController.navigateToTab(FinTab.SHOP.route) },
                onHome = { navController.navigateToTab(FinTab.HOME.route) }
            )
        }
    }
}

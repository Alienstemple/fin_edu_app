package ru.lct2026.finedu.feature.savings.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import javax.inject.Inject
import ru.lct2026.finedu.productcore.navigation.api.FeatureNavigationContribution
import ru.lct2026.finedu.productcore.navigation.api.FinEduRoute
import ru.lct2026.finedu.productcore.navigation.api.navigateToTab
import ru.lct2026.finedu.productcore.ui.components.route

internal class SavingsNavigationContribution @Inject constructor() : FeatureNavigationContribution {

    override fun NavGraphBuilder.register(navController: NavController) {
        composable<FinEduRoute.Savings> {
            SavingsRoute(
                onHelp = { navController.navigate(FinEduRoute.Glossary) },
                onParent = { navController.navigate(FinEduRoute.Parent) },
                onTabSelect = { tab -> navController.navigateToTab(tab.route) }
            )
        }
    }
}

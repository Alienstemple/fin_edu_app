package ru.lct2026.finedu.feature.quests.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import javax.inject.Inject
import ru.lct2026.finedu.productcore.navigation.api.FeatureNavigationContribution
import ru.lct2026.finedu.productcore.navigation.api.FinEduRoute
import ru.lct2026.finedu.productcore.navigation.api.navigateToTab
import ru.lct2026.finedu.productcore.ui.components.route

internal class QuestsNavigationContribution @Inject constructor() : FeatureNavigationContribution {

    override fun NavGraphBuilder.register(navController: NavController) {
        composable<FinEduRoute.Quests> {
            QuestsRoute(
                onNavigate = { route -> navController.navigate(route) },
                onTabSelect = { tab -> navController.navigateToTab(tab.route) }
            )
        }
        composable<FinEduRoute.Quest> { entry ->
            QuestRoute(
                questId = entry.toRoute<FinEduRoute.Quest>().questId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

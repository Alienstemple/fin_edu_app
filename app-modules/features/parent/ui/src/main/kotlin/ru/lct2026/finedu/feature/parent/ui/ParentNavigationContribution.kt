package ru.lct2026.finedu.feature.parent.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import javax.inject.Inject
import ru.lct2026.finedu.productcore.navigation.api.FeatureNavigationContribution
import ru.lct2026.finedu.productcore.navigation.api.FinEduRoute

internal class ParentNavigationContribution @Inject constructor() : FeatureNavigationContribution {

    override fun NavGraphBuilder.register(navController: NavController) {
        composable<FinEduRoute.Parent> {
            ParentRoute(
                onBack = { navController.popBackStack() },
                onOpenHome = { navController.navigate(FinEduRoute.Home) { popUpTo(0) } },
                onOpenOnboarding = { navController.navigate(FinEduRoute.Onboarding) { popUpTo(0) } }
            )
        }
    }
}

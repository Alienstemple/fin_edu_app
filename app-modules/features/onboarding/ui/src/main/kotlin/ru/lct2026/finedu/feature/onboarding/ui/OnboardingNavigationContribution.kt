package ru.lct2026.finedu.feature.onboarding.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import javax.inject.Inject
import ru.lct2026.finedu.productcore.navigation.api.FeatureNavigationContribution
import ru.lct2026.finedu.productcore.navigation.api.FinEduRoute

internal class OnboardingNavigationContribution @Inject constructor() : FeatureNavigationContribution {

    override fun NavGraphBuilder.register(navController: NavController) {
        composable<FinEduRoute.Onboarding> {
            OnboardingRoute(
                onOpenHero = { navController.navigate(FinEduRoute.Hero) },
                onClose = { navController.popBackStack() }
            )
        }
    }
}

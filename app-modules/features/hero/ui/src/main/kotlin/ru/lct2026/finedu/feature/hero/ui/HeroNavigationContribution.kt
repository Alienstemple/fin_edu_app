package ru.lct2026.finedu.feature.hero.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import javax.inject.Inject
import ru.lct2026.finedu.productcore.navigation.api.FeatureNavigationContribution
import ru.lct2026.finedu.productcore.navigation.api.FinEduRoute

internal class HeroNavigationContribution @Inject constructor() : FeatureNavigationContribution {

    override fun NavGraphBuilder.register(navController: NavController) {
        composable<FinEduRoute.Hero> {
            HeroScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(FinEduRoute.Onboarding) { inclusive = true }
                    }
                }
            )
        }
    }
}

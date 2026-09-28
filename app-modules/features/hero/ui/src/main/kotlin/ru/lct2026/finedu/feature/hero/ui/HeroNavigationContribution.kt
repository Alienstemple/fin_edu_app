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
            HeroRoute(
                onBack = { navController.popBackStack() },
                onOpenHome = {
                    // Стек очищается целиком: назад в онбординг и создание питомца не вернуться.
                    navController.navigate(FinEduRoute.Home) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }
    }
}

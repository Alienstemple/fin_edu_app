package ru.lct2026.finedu.feature.learn.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import javax.inject.Inject
import ru.lct2026.finedu.productcore.navigation.api.FeatureNavigationContribution
import ru.lct2026.finedu.productcore.navigation.api.FinEduRoute

internal class LearnNavigationContribution @Inject constructor() : FeatureNavigationContribution {

    override fun NavGraphBuilder.register(navController: NavController) {
        composable<FinEduRoute.Glossary> {
            GlossaryScreen(onBack = { navController.popBackStack() })
        }
        composable<FinEduRoute.Shorts> {
            ShortsScreen(onBack = { navController.popBackStack() })
        }
    }
}

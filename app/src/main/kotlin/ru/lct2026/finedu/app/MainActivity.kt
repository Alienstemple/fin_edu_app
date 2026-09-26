package ru.lct2026.finedu.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import ru.lct2026.finedu.app.navigation.FinEduNavHost
import ru.lct2026.finedu.productcore.navigation.api.FeatureNavigationContribution
import ru.lct2026.finedu.productcore.navigation.api.FinEduRoute
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var navigationContributions: Set<@JvmSuppressWildcards FeatureNavigationContribution>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinEduTheme {
                FinEduNavHost(
                    contributions = navigationContributions,
                    // TODO: есть сохранённый профиль → Home, нет → Onboarding (decomposition.md, раздел 1)
                    startDestination = FinEduRoute.Onboarding
                )
            }
        }
    }
}

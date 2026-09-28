package ru.lct2026.finedu.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import ru.lct2026.finedu.app.navigation.FinEduNavHost
import ru.lct2026.finedu.productcore.navigation.api.FeatureNavigationContribution
import ru.lct2026.finedu.productcore.navigation.api.FinEduRoute
import ru.lct2026.finedu.productcore.ui.components.FinEduBackground
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var navigationContributions: Set<@JvmSuppressWildcards FeatureNavigationContribution>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Тема только тёмная: светлые иконки статус-бара и навигации независимо от системной темы.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            FinEduTheme {
                FinEduBackground {
                    FinEduNavHost(
                        contributions = navigationContributions,
                        // TODO: есть сохранённый профиль → Home, нет → Onboarding (decomposition.md, раздел 1)
                        startDestination = FinEduRoute.Onboarding
                    )
                }
            }
        }
    }
}

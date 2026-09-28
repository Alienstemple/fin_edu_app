package ru.lct2026.finedu.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import ru.lct2026.finedu.app.navigation.FinEduNavHost
import ru.lct2026.finedu.productcore.domain.repository.GameRepository
import ru.lct2026.finedu.productcore.navigation.api.FeatureNavigationContribution
import ru.lct2026.finedu.productcore.navigation.api.FinEduRoute
import ru.lct2026.finedu.productcore.ui.components.FinEduBackground
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var navigationContributions: Set<@JvmSuppressWildcards FeatureNavigationContribution>

    @Inject
    lateinit var gameRepository: GameRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Тема только тёмная: светлые иконки статус-бара и навигации независимо от системной темы.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            // Пока профиль читается с диска (доли секунды), виден только фон.
            val startDestination by produceState<FinEduRoute?>(initialValue = null) {
                value = if (gameRepository.state.first() == null) FinEduRoute.Onboarding else FinEduRoute.Home
            }
            // Настройки взрослого («Крупный шрифт», «Спокойный режим») применяются сразу после изменения.
            val game by gameRepository.state.collectAsState(initial = null)
            FinEduTheme(
                largeFont = game?.settings?.largeFont == true,
                reduceMotion = game?.settings?.calmMode == true
            ) {
                FinEduBackground {
                    startDestination?.let { start ->
                        FinEduNavHost(contributions = navigationContributions, startDestination = start)
                    }
                }
            }
        }
    }
}

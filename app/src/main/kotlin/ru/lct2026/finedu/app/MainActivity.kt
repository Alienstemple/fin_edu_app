package ru.lct2026.finedu.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import ru.lct2026.finedu.app.navigation.FinEduNavHost
import ru.lct2026.finedu.app.sound.GameAudio
import ru.lct2026.finedu.app.sound.SoundSettingsViewModel
import ru.lct2026.finedu.productcore.domain.model.Settings
import ru.lct2026.finedu.productcore.domain.repository.GameRepository
import ru.lct2026.finedu.productcore.navigation.api.FeatureNavigationContribution
import ru.lct2026.finedu.productcore.navigation.api.FinEduRoute
import ru.lct2026.finedu.productcore.ui.components.FinEduBackground
import ru.lct2026.finedu.productcore.ui.sound.LocalSoundControls
import ru.lct2026.finedu.productcore.ui.sound.LocalSoundPlayer
import ru.lct2026.finedu.productcore.ui.sound.SoundControls
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var navigationContributions: Set<@JvmSuppressWildcards FeatureNavigationContribution>

    @Inject
    lateinit var gameRepository: GameRepository

    @Inject
    internal lateinit var gameAudio: GameAudio

    private val soundSettings: SoundSettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Тема только тёмная: светлые иконки статус-бара и навигации независимо от системной темы.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        // В фоне всё звуковое сопровождение на паузе, при возвращении продолжается с того же места.
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) = gameAudio.onForeground()

            override fun onStop(owner: LifecycleOwner) = gameAudio.onBackground()
        })
        setContent {
            // Пока профиль читается с диска (доли секунды), виден только фон.
            val startDestination by produceState<FinEduRoute?>(initialValue = null) {
                value = if (gameRepository.state.first() == null) FinEduRoute.Onboarding else FinEduRoute.Home
            }
            // Настройки взрослого («Крупный шрифт», «Спокойный режим») применяются сразу после изменения.
            val game by gameRepository.state.collectAsState(initial = null)
            // До создания профиля (знакомство) — настройки по умолчанию: звук на максимуме, мелодия включена.
            val settings = game?.settings ?: Settings()
            LaunchedEffect(settings.volume, settings.musicEnabled) {
                gameAudio.setSettings(settings.volume, settings.musicEnabled)
            }
            val soundControls = game?.let {
                SoundControls(
                    volume = settings.volume,
                    musicEnabled = settings.musicEnabled,
                    onVolumeChange = soundSettings::onVolumeChange,
                    onMusicToggle = soundSettings::onMusicToggle
                )
            }
            FinEduTheme(
                largeFont = game?.settings?.largeFont == true,
                reduceMotion = game?.settings?.calmMode == true
            ) {
                CompositionLocalProvider(
                    LocalSoundPlayer provides gameAudio,
                    LocalSoundControls provides soundControls
                ) {
                    FinEduBackground {
                        startDestination?.let { start ->
                            FinEduNavHost(
                                contributions = navigationContributions,
                                startDestination = start,
                                onMusicChange = gameAudio::setTrack
                            )
                        }
                    }
                }
            }
        }
    }
}

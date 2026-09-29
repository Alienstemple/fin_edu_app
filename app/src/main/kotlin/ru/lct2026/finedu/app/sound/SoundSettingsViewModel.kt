package ru.lct2026.finedu.app.sound

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.lct2026.finedu.productcore.domain.model.Settings
import ru.lct2026.finedu.productcore.domain.repository.GameRepository
import ru.lct2026.finedu.productcore.ui.viewmodel.StatelessViewModel

/** Шторка «Звук» из шапки: меняет те же настройки, что и раздел для взрослого. Без профиля — ничего. */
@HiltViewModel
internal class SoundSettingsViewModel @Inject constructor(private val gameRepository: GameRepository) :
    StatelessViewModel() {

    fun onVolumeChange(volume: Int) {
        updateSettings { copy(volume = volume.coerceIn(0, Settings.MAX_VOLUME)) }
    }

    fun onMusicToggle() {
        updateSettings { copy(musicEnabled = !musicEnabled) }
    }

    private fun updateSettings(transform: Settings.() -> Settings) {
        viewModelScope.launch {
            val game = gameRepository.state.first() ?: return@launch
            gameRepository.save(game.copy(settings = game.settings.transform()))
        }
    }
}

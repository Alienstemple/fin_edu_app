package ru.lct2026.finedu.app.sound

import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import ru.lct2026.finedu.productcore.domain.model.GameEngine
import ru.lct2026.finedu.productcore.domain.model.GameState
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.Profile
import ru.lct2026.finedu.productcore.domain.model.Settings
import ru.lct2026.finedu.productcore.domain.repository.GameRepository

class SoundSettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeGameRepository()
    private val viewModel = SoundSettingsViewModel(repository)

    @Test
    fun `шторка меняет громкость и выключает мелодию в сохранении`() {
        repository.state.value = game()

        viewModel.onVolumeChange(3)
        viewModel.onMusicToggle()

        val settings = requireNotNull(repository.state.value).settings
        assertEquals(3, settings.volume)
        assertFalse(settings.musicEnabled)
    }

    @Test
    fun `громкость не выходит за пределы шкалы`() {
        repository.state.value = game()

        viewModel.onVolumeChange(Settings.MAX_VOLUME + 1)

        assertEquals(Settings.MAX_VOLUME, requireNotNull(repository.state.value).settings.volume)
    }

    @Test
    fun `без профиля ничего не сохраняется`() {
        viewModel.onVolumeChange(3)

        assertNull(repository.state.value)
    }

    private fun game(): GameState = GameEngine.newGame(
        Profile("Капитан Носок", "Дзынь", PetLook(PetFur.MINT, PetHat.CAP), isDemo = true),
        nowMillis = 0
    )

    private class FakeGameRepository : GameRepository {
        override val state = MutableStateFlow<GameState?>(null)

        override suspend fun save(state: GameState) {
            this.state.value = state
        }

        override suspend fun clear() {
            state.value = null
        }
    }
}

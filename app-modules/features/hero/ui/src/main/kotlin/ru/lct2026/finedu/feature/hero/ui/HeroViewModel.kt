package ru.lct2026.finedu.feature.hero.ui

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import ru.lct2026.finedu.productcore.domain.model.GameEngine
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.Profile
import ru.lct2026.finedu.productcore.domain.repository.GameRepository
import ru.lct2026.finedu.productcore.ui.viewmodel.StatefulViewModel

@HiltViewModel
internal class HeroViewModel @Inject constructor(private val gameRepository: GameRepository) :
    StatefulViewModel<HeroUiState>(HeroUiState()) {

    fun onFurClick(fur: PetFur) {
        updateState { copy(fur = fur) }
    }

    fun onHatClick(hat: PetHat) {
        updateState { copy(hat = hat) }
    }

    fun onPetNameChanged(name: String) {
        updateState { copy(petName = name.take(HeroUiState.PET_NAME_MAX_LENGTH)) }
    }

    fun onPlayerNameChanged(name: String) {
        updateState { copy(playerName = name.take(HeroUiState.PLAYER_NAME_MAX_LENGTH)) }
    }

    /** «Готово». Пустые имена заменяются значениями по умолчанию: [defaultPetName] и [defaultPlayerName]. */
    fun onDoneClick(defaultPetName: String, defaultPlayerName: String) {
        startGame(defaultPetName, defaultPlayerName, isDemo = false)
    }

    /** «Демо-режим для проверки»: то же создание профиля, но с флагом демо. */
    fun onDemoClick(defaultPetName: String, defaultPlayerName: String) {
        startGame(defaultPetName, defaultPlayerName, isDemo = true)
    }

    private fun startGame(defaultPetName: String, defaultPlayerName: String, isDemo: Boolean) {
        val current = currentState
        if (current.isSaving) return
        updateState { copy(isSaving = true) }
        val profile = Profile(
            playerName = current.playerName.trim().ifEmpty { defaultPlayerName },
            petName = current.petName.orEmpty().trim().ifEmpty { defaultPetName },
            look = current.look,
            isDemo = isDemo
        )
        viewModelScope.launch {
            gameRepository.save(GameEngine.newGame(profile, System.currentTimeMillis()))
            offerEvent(HeroEvent.OpenHome)
        }
    }
}

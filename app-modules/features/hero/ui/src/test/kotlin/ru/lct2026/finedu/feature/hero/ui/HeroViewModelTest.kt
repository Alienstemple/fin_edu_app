package ru.lct2026.finedu.feature.hero.ui

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.lct2026.finedu.productcore.domain.model.GameRules
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook

class HeroViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeGameRepository()
    private val viewModel = HeroViewModel(repository)

    @Test
    fun `Готово создаёт новую игру с выбранной внешностью и именами`() = runTest {
        viewModel.onFurClick(PetFur.CORAL)
        viewModel.onHatClick(PetHat.HEADPHONES)
        viewModel.onPetNameChanged("  Бублик ")
        viewModel.onPlayerNameChanged("Капитан")

        viewModel.events.flow.test {
            viewModel.onDoneClick(DEFAULT_PET, DEFAULT_PLAYER)

            assertEquals(HeroEvent.OpenHome, awaitItem())
        }
        val game = repository.saved.single()
        assertEquals("Бублик", game.profile.petName)
        assertEquals("Капитан", game.profile.playerName)
        assertEquals(PetLook(PetFur.CORAL, PetHat.HEADPHONES), game.profile.look)
        assertFalse(game.profile.isDemo)
        assertEquals(GameRules.WEEKLY_INCOME, game.unallocated)
        assertEquals(1, game.period.number)
    }

    @Test
    fun `нетронутое имя питомца — Дзынь`() {
        viewModel.onDoneClick(DEFAULT_PET, DEFAULT_PLAYER)

        assertEquals(DEFAULT_PET, repository.saved.single().profile.petName)
    }

    @Test
    fun `пустое имя питомца заменяется на Дзынь`() {
        viewModel.onPetNameChanged("   ")

        viewModel.onDoneClick(DEFAULT_PET, DEFAULT_PLAYER)

        assertEquals(DEFAULT_PET, repository.saved.single().profile.petName)
    }

    @Test
    fun `пустое игровое имя заменяется на значение по умолчанию`() {
        viewModel.onPlayerNameChanged(" ")

        viewModel.onDoneClick(DEFAULT_PET, DEFAULT_PLAYER)

        assertEquals(DEFAULT_PLAYER, repository.saved.single().profile.playerName)
    }

    @Test
    fun `демо-режим создаёт профиль с флагом демо`() {
        viewModel.onDemoClick(DEFAULT_PET, DEFAULT_PLAYER)

        assertTrue(repository.saved.single().profile.isDemo)
    }

    @Test
    fun `повторное нажатие не создаёт второй профиль`() {
        viewModel.onDoneClick(DEFAULT_PET, DEFAULT_PLAYER)
        viewModel.onDemoClick(DEFAULT_PET, DEFAULT_PLAYER)

        assertEquals(1, repository.saved.size)
        assertTrue(viewModel.currentState.isSaving)
    }

    @Test
    fun `имена обрезаются до максимальной длины`() {
        viewModel.onPetNameChanged("а".repeat(HeroUiState.PET_NAME_MAX_LENGTH + 5))
        viewModel.onPlayerNameChanged("б".repeat(HeroUiState.PLAYER_NAME_MAX_LENGTH + 5))

        assertEquals(HeroUiState.PET_NAME_MAX_LENGTH, viewModel.currentState.petName?.length)
        assertEquals(HeroUiState.PLAYER_NAME_MAX_LENGTH, viewModel.currentState.playerName.length)
    }

    private companion object {
        const val DEFAULT_PET = "Дзынь"
        const val DEFAULT_PLAYER = "Капитан Носок"
    }
}

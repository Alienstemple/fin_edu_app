package ru.lct2026.finedu.feature.onboarding.ui

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import ru.lct2026.finedu.productcore.domain.model.GameState
import ru.lct2026.finedu.productcore.domain.repository.GameRepository

/** Профиль в памяти вместо DataStore. */
class FakeGameRepository(initial: GameState? = null) : GameRepository {

    private val stateFlow = MutableStateFlow(initial)

    val saved = mutableListOf<GameState>()

    override val state: Flow<GameState?> = stateFlow

    override suspend fun save(state: GameState) {
        saved += state
        stateFlow.value = state
    }

    override suspend fun clear() {
        stateFlow.value = null
    }
}

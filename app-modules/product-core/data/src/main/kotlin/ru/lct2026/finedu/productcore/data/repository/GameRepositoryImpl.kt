package ru.lct2026.finedu.productcore.data.repository

import androidx.datastore.core.DataStore
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.lct2026.finedu.productcore.data.local.StoredGameDto
import ru.lct2026.finedu.productcore.data.mapper.toDomain
import ru.lct2026.finedu.productcore.data.mapper.toDto
import ru.lct2026.finedu.productcore.domain.model.GameState
import ru.lct2026.finedu.productcore.domain.repository.GameRepository

internal class GameRepositoryImpl @Inject constructor(private val dataStore: DataStore<StoredGameDto>) :
    GameRepository {

    override val state: Flow<GameState?> = dataStore.data.map { it.state?.toDomain() }

    override suspend fun save(state: GameState) {
        dataStore.updateData { StoredGameDto(state.toDto()) }
    }

    override suspend fun clear() {
        dataStore.updateData { StoredGameDto() }
    }
}

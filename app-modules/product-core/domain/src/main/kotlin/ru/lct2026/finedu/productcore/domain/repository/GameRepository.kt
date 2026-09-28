package ru.lct2026.finedu.productcore.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.lct2026.finedu.productcore.domain.model.GameState

/** Локальное состояние профиля. */
interface GameRepository {
    /** Текущее состояние; `null`, если профиль ещё не создан. */
    val state: Flow<GameState?>

    suspend fun save(state: GameState)

    /** Удаляет профиль: следующий запуск начнётся с онбординга. */
    suspend fun clear()
}

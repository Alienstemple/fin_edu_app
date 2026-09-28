package ru.lct2026.finedu.productcore.data.repository

import androidx.datastore.core.DataStoreFactory
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import ru.lct2026.finedu.productcore.data.TestGame
import ru.lct2026.finedu.productcore.data.local.GameStateSerializer

class GameRepositoryImplTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val file: File by lazy { File(folder.root, "game.json") }

    @Test
    fun `без профиля состояние пустое`() = runTest {
        withRepository { assertNull(it.state.first()) }
    }

    @Test
    fun `сохранённое состояние переживает перезапуск`() = runTest {
        withRepository { it.save(TestGame.played) }

        withRepository { assertEquals(TestGame.played, it.state.first()) }
    }

    @Test
    fun `удаление профиля стирает состояние`() = runTest {
        withRepository {
            it.save(TestGame.played)
            it.clear()
            assertNull(it.state.first())
        }
    }

    /** Отдельный DataStore на время [block], как один запуск приложения. */
    private suspend fun CoroutineScope.withRepository(block: suspend (GameRepositoryImpl) -> Unit) {
        val job = Job()
        val dataStore = DataStoreFactory.create(
            serializer = GameStateSerializer(TestGame.json),
            scope = CoroutineScope(coroutineContext + job),
            produceFile = { file }
        )
        block(GameRepositoryImpl(dataStore))
        job.cancelAndJoin()
    }
}

package ru.lct2026.finedu.productcore.data.local

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import ru.lct2026.finedu.productcore.data.TestGame

class GameStateSerializerTest {

    private val serializer = GameStateSerializer(TestGame.json)

    @Test(expected = CorruptionException::class)
    fun `повреждённый файл — ошибка повреждения, а не падение`() = runTest {
        serializer.readFrom("{не json".byteInputStream())
    }

    @Test(expected = CorruptionException::class)
    fun `неизвестное значение enum — тоже повреждение`() = runTest {
        serializer.readFrom(
            """{"state":{"profile":{"playerName":"","petName":"","fur":"GOLD","hat":"NONE",""".byteInputStream()
        )
    }

    @Test
    fun `по умолчанию профиля нет`() {
        assertEquals(StoredGameDto(), serializer.defaultValue)
    }
}

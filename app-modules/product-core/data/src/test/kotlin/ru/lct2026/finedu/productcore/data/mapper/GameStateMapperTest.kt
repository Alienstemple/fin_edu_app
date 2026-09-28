package ru.lct2026.finedu.productcore.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.lct2026.finedu.productcore.data.TestGame
import ru.lct2026.finedu.productcore.data.local.GameStateDto
import ru.lct2026.finedu.productcore.domain.model.PetStat

class GameStateMapperTest {

    @Test
    fun `состояние переживает преобразование в JSON и обратно без потерь`() {
        val encoded = TestGame.json.encodeToString(GameStateDto.serializer(), TestGame.played.toDto())

        val decoded = TestGame.json.decodeFromString(GameStateDto.serializer(), encoded).toDomain()

        assertEquals(TestGame.played, decoded)
    }

    @Test
    fun `имена enum читаются без учёта регистра`() {
        assertEquals(PetStat.CALM, "calm".toEnum<PetStat>())
        assertEquals(PetStat.CALM, "CALM".toEnum<PetStat>())
    }
}

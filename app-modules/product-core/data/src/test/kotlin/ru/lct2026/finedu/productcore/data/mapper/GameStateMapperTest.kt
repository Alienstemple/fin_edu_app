package ru.lct2026.finedu.productcore.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.lct2026.finedu.productcore.data.TestGame
import ru.lct2026.finedu.productcore.data.local.GameStateDto
import ru.lct2026.finedu.productcore.data.local.SettingsDto
import ru.lct2026.finedu.productcore.domain.model.PetStat
import ru.lct2026.finedu.productcore.domain.model.Settings

class GameStateMapperTest {

    @Test
    fun `состояние переживает преобразование в JSON и обратно без потерь`() {
        val encoded = TestGame.json.encodeToString(GameStateDto.serializer(), TestGame.played.toDto())

        val decoded = TestGame.json.decodeFromString(GameStateDto.serializer(), encoded).toDomain()

        assertEquals(TestGame.played, decoded)
    }

    @Test
    fun `громкость и фоновая мелодия сохраняются`() {
        val game = TestGame.played.copy(settings = TestGame.played.settings.copy(volume = 3, musicEnabled = false))
        val encoded = TestGame.json.encodeToString(GameStateDto.serializer(), game.toDto())

        val settings = TestGame.json.decodeFromString(GameStateDto.serializer(), encoded).toDomain().settings

        assertEquals(3, settings.volume)
        assertFalse(settings.musicEnabled)
    }

    @Test
    fun `в старом сохранении без звука — звук на максимуме и мелодия включена`() {
        val settings = TestGame.json.decodeFromString(SettingsDto.serializer(), """{"ageMode":"YOUNGER"}""")

        val domain = TestGame.played.toDto().copy(settings = settings).toDomain().settings

        assertEquals(Settings.MAX_VOLUME, domain.volume)
        assertTrue(domain.musicEnabled)
    }

    @Test
    fun `громкость вне диапазона ограничивается`() {
        val dto = TestGame.played.toDto().let { it.copy(settings = it.settings.copy(volume = 42)) }

        assertEquals(Settings.MAX_VOLUME, dto.toDomain().settings.volume)
    }

    @Test
    fun `имена enum читаются без учёта регистра`() {
        assertEquals(PetStat.CALM, "calm".toEnum<PetStat>())
        assertEquals(PetStat.CALM, "CALM".toEnum<PetStat>())
    }
}

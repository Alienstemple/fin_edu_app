package ru.lct2026.finedu.app.sound

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.lct2026.finedu.productcore.domain.model.Settings
import ru.lct2026.finedu.productcore.ui.sound.MusicTrack

class SoundMixTest {

    private val playing = SoundMix(track = MusicTrack.CALM, isForeground = true)

    @Test
    fun `по умолчанию звук на максимуме и мелодия раздела играет`() {
        assertEquals(1f, playing.effectGain)
        assertEquals(MusicTrack.CALM, playing.activeTrack)
    }

    @Test
    fun `мелодия тише коротких звуков`() {
        assertTrue(playing.musicGain < playing.effectGain)
    }

    @Test
    fun `громкость растёт ступенями до максимума`() {
        val gains = (0..Settings.MAX_VOLUME).map { playing.copy(volume = it).effectGain }

        assertEquals(0f, gains.first())
        assertEquals(gains.sorted(), gains)
        assertEquals(gains.size, gains.distinct().size)
    }

    @Test
    fun `без звука — ни коротких звуков, ни мелодии`() {
        val silent = playing.copy(volume = 0)

        assertEquals(0f, silent.effectGain)
        assertNull(silent.activeTrack)
    }

    @Test
    fun `выключенная мелодия молчит, а короткие звуки остаются`() {
        val noMusic = playing.copy(musicEnabled = false)

        assertNull(noMusic.activeTrack)
        assertEquals(1f, noMusic.effectGain)
    }

    @Test
    fun `в фоне всё на паузе`() {
        val background = playing.copy(isForeground = false)

        assertNull(background.activeTrack)
        assertEquals(0f, background.effectGain)
    }

    @Test
    fun `на звонок мелодия встаёт на паузу, на уведомление — приглушается`() {
        assertNull(playing.copy(focus = MusicFocus.PAUSED).activeTrack)

        val ducked = playing.copy(focus = MusicFocus.DUCKED)
        assertEquals(MusicTrack.CALM, ducked.activeTrack)
        assertTrue(ducked.musicGain < playing.musicGain)
    }

    @Test
    fun `экран без мелодии — тишина`() {
        assertNull(playing.copy(track = null).activeTrack)
    }
}

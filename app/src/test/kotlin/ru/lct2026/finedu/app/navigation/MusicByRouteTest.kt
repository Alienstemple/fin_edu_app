package ru.lct2026.finedu.app.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.lct2026.finedu.productcore.navigation.api.FinEduRoute
import ru.lct2026.finedu.productcore.ui.sound.MusicTrack

class MusicByRouteTest {

    @Test
    fun `у каждого раздела своя мелодия`() {
        assertEquals(MusicTrack.CALM, musicTrackFor(FinEduRoute.Home.serializer().descriptor.serialName))
        assertEquals(MusicTrack.THOUGHTFUL, musicTrackFor(FinEduRoute.Budget.serializer().descriptor.serialName))
        assertEquals(MusicTrack.DISCO, musicTrackFor(FinEduRoute.Shop.serializer().descriptor.serialName))
        assertEquals(MusicTrack.BUILDER, musicTrackFor(FinEduRoute.Savings.serializer().descriptor.serialName))
    }

    @Test
    fun `маршрут с аргументом узнаётся по имени экрана`() {
        val quest = FinEduRoute.Quest.serializer().descriptor.serialName

        assertEquals(MusicTrack.THOUGHTFUL, musicTrackFor("$quest/{questId}"))
    }

    @Test
    fun `неизвестный маршрут и его отсутствие — без мелодии`() {
        assertNull(musicTrackFor("unknown"))
        assertNull(musicTrackFor(null))
    }
}

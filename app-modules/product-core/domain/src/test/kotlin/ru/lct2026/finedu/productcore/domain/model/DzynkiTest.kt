package ru.lct2026.finedu.productcore.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DzynkiTest {

    @Test
    fun `plus складывает суммы`() {
        assertEquals(Dzynki(15), Dzynki(10) + Dzynki(5))
    }

    @Test
    fun `minusOrNull вычитает, если дзынек хватает`() {
        assertEquals(Dzynki(3), Dzynki(10).minusOrNull(Dzynki(7)))
    }

    @Test
    fun `minusOrNull возвращает ноль при списании всей суммы`() {
        assertEquals(Dzynki.ZERO, Dzynki(10).minusOrNull(Dzynki(10)))
    }

    @Test
    fun `minusOrNull возвращает null, если дзынек не хватает`() {
        assertNull(Dzynki(5).minusOrNull(Dzynki(6)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `отрицательная сумма запрещена`() {
        Dzynki(-1)
    }
}

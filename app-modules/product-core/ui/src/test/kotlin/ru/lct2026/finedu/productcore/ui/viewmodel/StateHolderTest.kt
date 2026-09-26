package ru.lct2026.finedu.productcore.ui.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Test

class StateHolderTest {

    @Test
    fun `начальное состояние доступно сразу`() {
        val holder = StateHolder.create(initialState = 1)

        assertEquals(1, holder.currentState)
        assertEquals(1, holder.stateFlow.value)
    }

    @Test
    fun `updateState вычисляет новое состояние из текущего`() {
        val holder = StateHolder.create(initialState = 1)

        holder.updateState { this + 2 }

        assertEquals(3, holder.stateFlow.value)
    }

    @Test
    fun `setState заменяет состояние целиком`() {
        val holder = StateHolder.create(initialState = 1)

        holder.setState(10)

        assertEquals(10, holder.currentState)
    }
}

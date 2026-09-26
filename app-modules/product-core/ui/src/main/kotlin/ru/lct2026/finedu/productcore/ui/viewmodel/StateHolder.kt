package ru.lct2026.finedu.productcore.ui.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Хранитель состояния экрана. Единственный источник правды для UI: экран подписывается на [stateFlow],
 * ViewModel меняет состояние только через [updateState] / [setState].
 */
interface StateHolder<T> {

    val stateFlow: StateFlow<T>

    val currentState: T

    /** Атомарно меняет состояние на основе текущего. Предпочтительный способ. */
    fun updateState(block: T.() -> T)

    /** Заменяет состояние целиком, не глядя на текущее. */
    fun setState(state: T)

    private class Impl<T>(initialState: T) : StateHolder<T> {

        private val internalStateFlow = MutableStateFlow(initialState)

        override val stateFlow: StateFlow<T> = internalStateFlow.asStateFlow()

        override val currentState: T
            get() = internalStateFlow.value

        override fun updateState(block: T.() -> T) {
            internalStateFlow.update(block)
        }

        override fun setState(state: T) {
            internalStateFlow.value = state
        }
    }

    companion object {
        fun <T> create(initialState: T): StateHolder<T> = Impl(initialState)
    }
}

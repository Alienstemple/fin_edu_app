package ru.lct2026.finedu.productcore.ui.viewmodel

/**
 * Базовая ViewModel экрана: состояние [T] (через [StateHolder]) + одноразовые события (через [StatelessViewModel]).
 *
 * Действия пользователя — публичные методы `onXxxClick()`, экран передаёт их ссылками (`viewModel::onRetryClick`).
 */
abstract class StatefulViewModel<T>(
    initialState: T,
    private val internalStateHolder: StateHolder<T> = StateHolder.create(initialState)
) : StatelessViewModel(),
    StateHolder<T> by internalStateHolder

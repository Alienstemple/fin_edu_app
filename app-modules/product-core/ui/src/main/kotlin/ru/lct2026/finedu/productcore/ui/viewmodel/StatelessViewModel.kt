package ru.lct2026.finedu.productcore.ui.viewmodel

import androidx.lifecycle.ViewModel
import ru.lct2026.finedu.productcore.ui.event.Event
import ru.lct2026.finedu.productcore.ui.event.EventQueue

/**
 * Базовая ViewModel без состояния: только одноразовые события (навигация, закрытие экрана, сообщения).
 * Экран читает их через [ru.lct2026.finedu.productcore.ui.event.ObserveEvents].
 */
abstract class StatelessViewModel : ViewModel() {

    val events: EventQueue<Event> = EventQueue()

    protected fun offerEvent(event: Event) {
        events.offerEvent(event)
    }
}

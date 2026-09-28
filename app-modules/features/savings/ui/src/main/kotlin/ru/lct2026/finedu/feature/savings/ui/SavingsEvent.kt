package ru.lct2026.finedu.feature.savings.ui

import ru.lct2026.finedu.productcore.ui.event.Event

internal sealed interface SavingsEvent : Event {

    /** Предмет поставлен — идём смотреть уголок на главном. */
    data object OpenHome : SavingsEvent
}

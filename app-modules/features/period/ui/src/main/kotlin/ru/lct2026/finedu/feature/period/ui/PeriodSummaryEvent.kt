package ru.lct2026.finedu.feature.period.ui

import ru.lct2026.finedu.productcore.ui.event.Event

internal sealed interface PeriodSummaryEvent : Event {

    /** Итоги просмотрены: начинается новая неделя на главном. */
    data object OpenHome : PeriodSummaryEvent
}

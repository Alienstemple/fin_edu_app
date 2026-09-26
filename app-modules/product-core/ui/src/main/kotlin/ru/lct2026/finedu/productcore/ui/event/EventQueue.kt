package ru.lct2026.finedu.productcore.ui.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update

/**
 * Очередь одноразовых событий. События копятся, пока экран не подписан (например, во время поворота
 * или ухода в фон), и доставляются ровно один раз.
 *
 * Использовать только внутри ViewModel.
 */
class EventQueue<T : Event> {

    private val eventsFlow = MutableStateFlow<List<T>>(emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val flow: Flow<T>
        get() = eventsFlow.flatMapConcat { consumeAll() }

    fun offerEvent(event: T) {
        eventsFlow.update { it + event }
    }

    private fun consumeAll(): Flow<T> = eventsFlow.getAndUpdate { emptyList() }.asFlow()
}

/**
 * Подписка экрана на события ViewModel, пока lifecycle не ниже [minActiveState].
 */
@Composable
fun ObserveEvents(
    events: EventQueue<Event>,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    onEvent: CoroutineScope.(Event) -> Unit = {}
) {
    val currentOnEvent by rememberUpdatedState(onEvent)
    LaunchedEffect(events, lifecycleOwner) {
        events.flow
            .flowWithLifecycle(lifecycleOwner.lifecycle, minActiveState)
            .collect { event -> currentOnEvent(event) }
    }
}

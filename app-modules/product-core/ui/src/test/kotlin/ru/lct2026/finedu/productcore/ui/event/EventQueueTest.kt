package ru.lct2026.finedu.productcore.ui.event

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class EventQueueTest {

    private data class TestEvent(val id: Int) : Event

    @Test
    fun `события, отправленные до подписки, доставляются по порядку`() = runTest {
        val queue = EventQueue<TestEvent>()
        queue.offerEvent(TestEvent(1))
        queue.offerEvent(TestEvent(2))

        queue.flow.test {
            assertEquals(TestEvent(1), awaitItem())
            assertEquals(TestEvent(2), awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `событие доставляется только один раз`() = runTest {
        val queue = EventQueue<TestEvent>()
        queue.offerEvent(TestEvent(1))

        queue.flow.test {
            assertEquals(TestEvent(1), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        queue.flow.test {
            expectNoEvents()
        }
    }

    @Test
    fun `событие после подписки доставляется подписчику`() = runTest {
        val queue = EventQueue<TestEvent>()

        queue.flow.test {
            queue.offerEvent(TestEvent(3))

            assertEquals(TestEvent(3), awaitItem())
        }
    }
}

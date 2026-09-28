package ru.lct2026.finedu.feature.learn.ui

import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class GlossaryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `показывает термины из контента`() {
        val viewModel = GlossaryViewModel(FakeContentRepository(LearnFixtures.content))

        assertEquals(GlossaryUiState.Content(LearnFixtures.content.glossary), viewModel.currentState)
    }
}

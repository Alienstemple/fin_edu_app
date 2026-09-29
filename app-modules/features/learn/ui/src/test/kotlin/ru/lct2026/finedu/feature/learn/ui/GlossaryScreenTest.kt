package ru.lct2026.finedu.feature.learn.ui

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.lct2026.finedu.productcore.navigation.api.FinEduRoute
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

@RunWith(RobolectricTestRunner::class)
class GlossaryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `ссылка на шортсы открывает шортсы`() {
        // Arrange
        val routes = mutableListOf<FinEduRoute>()
        composeTestRule.setContent {
            FinEduTheme {
                GlossaryScreen(
                    state = GlossaryUiState.Content(LearnFixtures.content.glossary),
                    onBack = {},
                    onNavigate = { routes += it }
                )
            }
        }

        // Act
        composeTestRule.onNodeWithText("Дзынь объясняет за 15 секунд").performScrollTo().performClick()

        // Assert
        assertEquals(listOf<FinEduRoute>(FinEduRoute.Shorts), routes)
    }
}

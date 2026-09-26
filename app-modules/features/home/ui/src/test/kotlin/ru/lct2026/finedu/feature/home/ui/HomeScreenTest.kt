package ru.lct2026.finedu.feature.home.ui

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
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `нажатие на Магазин открывает магазин`() {
        // Arrange
        val routes = mutableListOf<FinEduRoute>()
        composeTestRule.setContent {
            FinEduTheme {
                HomeScreen(
                    state = HomeUiState.Content(balance = 100, savings = 0, periodNumber = 1),
                    onNavigate = { routes += it }
                )
            }
        }

        // Act
        composeTestRule.onNodeWithText("Магазин").performScrollTo().performClick()

        // Assert
        assertEquals(listOf<FinEduRoute>(FinEduRoute.Shop), routes)
    }
}

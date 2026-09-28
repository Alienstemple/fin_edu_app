package ru.lct2026.finedu.feature.parent.ui

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.lct2026.finedu.feature.parent.domain.model.ParentGate
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

@RunWith(RobolectricTestRunner::class)
class ParentScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val digits = mutableListOf<Int>()
    private val tabs = mutableListOf<ParentTab>()
    private var doneClicks = 0

    private fun setScreen(state: ParentUiState) {
        composeTestRule.setContent {
            FinEduTheme {
                ParentScreen(
                    state = state,
                    onBack = {},
                    onDigitClick = { digits += it },
                    onEraseClick = {},
                    onDoneClick = { doneClicks++ },
                    onTabSelect = { tabs += it },
                    onArticleClick = {},
                    onArticleClose = {},
                    onSchemeToggle = {},
                    onBonusMinusClick = {},
                    onBonusPlusClick = {},
                    onBonusReasonSelect = {},
                    onGrantBonusClick = {},
                    onAgeModeSelect = {},
                    onCalmModeToggle = {},
                    onLargeFontToggle = {},
                    onResetClick = {},
                    onDeleteClick = {},
                    onDialogConfirm = {},
                    onDialogDismiss = {}
                )
            }
        }
    }

    @Test
    fun `барьер показывает пример и передаёт нажатия клавиатуры`() {
        // Arrange
        setScreen(ParentUiState(gate = ParentGate(7, 8), isWrongAnswer = true))

        // Act
        composeTestRule.onNodeWithContentDescription("Цифра 5").performScrollTo().performClick()
        composeTestRule.onNodeWithContentDescription("Готово").performScrollTo().performClick()

        // Assert
        composeTestRule.onNodeWithText("7 × 8 =").assertExists()
        composeTestRule.onNodeWithText("Не то. Ещё раз?").assertExists()
        assertEquals(listOf(5), digits)
        assertEquals(1, doneClicks)
    }

    @Test
    fun `после барьера нижняя панель переключает вкладки`() {
        // Arrange
        setScreen(ParentUiState(gate = ParentGate(7, 8), isUnlocked = true, tab = ParentTab.SETTINGS))

        // Act
        composeTestRule.onNodeWithText("Мошенники").performClick()

        // Assert
        assertEquals(listOf(ParentTab.SCAMS), tabs)
    }
}

package ru.lct2026.finedu.app.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.lct2026.finedu.app.MainActivity

@RunWith(AndroidJUnit4::class)
class DemoScenarioTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    /** TC-01. Приложение А, шаги 1–2: онбординг → создание героя → главный */
    @Test
    fun onboardingLeadsToHome() {
        composeRule.onNodeWithText("Создать героя").performClick()
        composeRule.onNodeWithText("Готово").performClick()

        composeRule.onNodeWithText("Главный").assertIsDisplayed()
    }
}

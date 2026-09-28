package ru.lct2026.finedu.feature.home.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.Pet
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.PetStat
import ru.lct2026.finedu.productcore.navigation.api.FinEduRoute
import ru.lct2026.finedu.productcore.ui.illustration.PetMood
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

@RunWith(RobolectricTestRunner::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val routes = mutableListOf<FinEduRoute>()

    @Test
    fun `в начале недели кнопка «Разложить» ведёт в план недели`() {
        setContent(state.copy(unallocated = Dzynki(300), line = HomeLine.UNALLOCATED))

        composeTestRule.onNodeWithText("Разложить 300 дзынек").performScrollTo().performClick()

        assertEquals(listOf<FinEduRoute>(FinEduRoute.Budget), routes)
    }

    @Test
    fun `полоска задания открывает задание`() {
        setContent(state)

        composeTestRule.onNodeWithText("Задание: Шляпа или носки?").performScrollTo().performClick()

        assertEquals(listOf<FinEduRoute>(FinEduRoute.Quest("hat_or_socks")), routes)
    }

    @Test
    fun `«Завершить неделю» ведёт к итогам`() {
        setContent(state)

        composeTestRule.onNodeWithText("Завершить неделю").performScrollTo().performClick()

        assertEquals(listOf<FinEduRoute>(FinEduRoute.PeriodSummary), routes)
    }

    @Test
    fun `низкое состояние показывает реплику и подсказку`() {
        setContent(state.copy(mood = PetMood.COLD, line = HomeLine.LOW, notice = HomeNotice.Low(PetStat.CHARGE)))

        composeTestRule.onNodeWithText("Мне сейчас зябко").assertIsDisplayed()
        composeTestRule.onNodeWithText("В «Нужном» пусто. Каша или носки согреют").assertIsDisplayed()
    }

    private fun setContent(state: HomeUiState) {
        composeTestRule.setContent {
            FinEduTheme {
                HomeScreen(
                    state = state,
                    onNavigate = { routes += it },
                    onSelectTab = {},
                    onPetClick = {},
                    onPetLongPress = {}
                )
            }
        }
    }

    private companion object {
        val state = HomeUiState.Content(
            weekNumber = 2,
            weekTitle = "Хочу всё",
            isDemo = false,
            isYounger = false,
            look = PetLook(PetFur.LILAC, PetHat.NONE),
            pet = Pet(),
            mood = PetMood.NEUTRAL,
            line = HomeLine.NORMAL,
            notice = null,
            isDim = false,
            placedGoalIds = emptySet(),
            stars = 0,
            balance = Dzynki(130),
            needsLeft = Dzynki(70),
            wantsLeft = Dzynki(60),
            savings = Dzynki(40),
            unallocated = Dzynki.ZERO,
            goal = HomeGoal(title = "Лампа", saved = Dzynki(40), price = Dzynki(100), weeksLeft = 6),
            quest = HomeQuest(id = "hat_or_socks", title = "Шляпа или носки?")
        )
    }
}

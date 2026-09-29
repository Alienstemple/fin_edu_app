package ru.lct2026.finedu.feature.home.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
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
    private val clickedBubbles = mutableListOf<HomeBubble>()
    private var dismissCount = 0

    @Test
    fun `тап по пузырьку баланса просит его раскрыть`() {
        setContent(state)

        composeTestRule.onNode(hasContentDescription("Баланс: 130 дзынек") and hasClickAction()).performClick()

        assertEquals(listOf<HomeBubble>(HomeBubble.Balance), clickedBubbles)
    }

    @Test
    fun `переход из раскрытого пузырька сворачивает его`() {
        setContent(state.copy(openBubble = HomeBubble.FinishWeek))

        composeTestRule.onNodeWithText("Завершить неделю").performClick()

        assertEquals(1, dismissCount)
        assertEquals(listOf<FinEduRoute>(FinEduRoute.PeriodSummary), routes)
    }

    @Test
    fun `в начале недели «Разложить» в раскрытом балансе ведёт в план недели`() {
        setContent(
            state.copy(unallocated = Dzynki(300), line = HomeLine.UNALLOCATED, openBubble = HomeBubble.Balance)
        )

        // На маленьком экране карточка баланса прокручивается.
        composeTestRule.onNodeWithText("Разложить 300 дзынек").performScrollTo().performClick()

        assertEquals(listOf<FinEduRoute>(FinEduRoute.Budget), routes)
    }

    @Test
    fun `раскрытое задание открывается кнопкой «Начать»`() {
        setContent(state.copy(openBubble = HomeBubble.Quest))

        composeTestRule.onNodeWithText("Шляпа или носки?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Начать").performClick()

        assertEquals(listOf<FinEduRoute>(FinEduRoute.Quest("hat_or_socks")), routes)
    }

    @Test
    fun `«Завершить неделю» в раскрытом пузырьке ведёт к итогам`() {
        setContent(state.copy(openBubble = HomeBubble.FinishWeek))

        composeTestRule.onNodeWithText("Завершить неделю").performClick()

        assertEquals(listOf<FinEduRoute>(FinEduRoute.PeriodSummary), routes)
    }

    @Test
    fun `у младших нет пузырька задания`() {
        setContent(state.copy(isYounger = true))

        composeTestRule.onAllNodes(hasContentDescription("Задание", substring = true)).assertCountEquals(0)
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
                    onPetLongPress = {},
                    onBubbleClick = { clickedBubbles += it },
                    onBubbleDismiss = { dismissCount++ }
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

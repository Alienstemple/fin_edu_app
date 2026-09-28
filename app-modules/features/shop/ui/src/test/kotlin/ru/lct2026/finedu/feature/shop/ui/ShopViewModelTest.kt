package ru.lct2026.finedu.feature.shop.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.lct2026.finedu.productcore.domain.model.Bag
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.FeedbackReason
import ru.lct2026.finedu.productcore.domain.model.GameState

class ShopViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val gameRepository = FakeGameRepository(TestGame.planned)

    private fun viewModel(game: GameState = TestGame.planned): ShopViewModel {
        gameRepository.current.value = game
        return ShopViewModel(gameRepository, FakeContentRepository(TestGame.content()))
    }

    @Test
    fun `витрина показывает товары вкладки Хочу и остаток мешочка`() {
        val viewModel = viewModel()

        assertEquals(listOf(TestGame.ball, TestGame.hat), viewModel.currentState.items)
        assertEquals(Dzynki(60), viewModel.currentState.remaining)
        assertFalse(viewModel.currentState.isPlanPending)
    }

    @Test
    fun `до раскладки плана магазин предлагает сначала разложить дзыньки`() {
        val viewModel = viewModel(TestGame.start)

        assertTrue(viewModel.currentState.isPlanPending)
    }

    @Test
    fun `вкладка Нужное показывает свои товары и сбрасывает фильтр`() {
        val viewModel = viewModel()
        viewModel.onCheaperClick()

        viewModel.onTabClick(Bag.NEEDS)

        assertEquals(listOf(TestGame.kasha), viewModel.currentState.items)
        assertEquals(Dzynki(150), viewModel.currentState.remaining)
        assertFalse(viewModel.currentState.isCheaperOnly)
    }

    @Test
    fun `если хватает, тап открывает шторку покупки без списания`() {
        val viewModel = viewModel()

        viewModel.onItemClick(TestGame.ball)

        assertEquals(ShopSheet.Purchase(TestGame.ball, Dzynki(60)), viewModel.currentState.sheet)
        assertEquals(TestGame.planned, gameRepository.current.value)
    }

    @Test
    fun `если не хватает, тап показывает сколько не хватает`() {
        val viewModel = viewModel()

        viewModel.onItemClick(TestGame.hat)

        assertEquals(ShopSheet.Shortage(TestGame.hat, Dzynki(40), Dzynki(60)), viewModel.currentState.sheet)
    }

    @Test
    fun `Купить списывает из мешочка и показывает обратную связь`() {
        val viewModel = viewModel()
        viewModel.onItemClick(TestGame.ball)

        viewModel.onBuyClick()

        assertEquals(Dzynki(30), checkNotNull(gameRepository.current.value).wantsLeft)
        assertEquals(Dzynki(30), viewModel.currentState.remaining)
        val reason = when (val sheet = viewModel.currentState.sheet) {
            is ShopSheet.Result -> sheet.feedback.reason
            is ShopSheet.Purchase, is ShopSheet.Shortage, null -> null
        }
        assertEquals(FeedbackReason.BOUGHT_WANT, reason)
    }

    @Test
    fun `Подожду ничего не списывает и добавляет спокойствия`() {
        val viewModel = viewModel()
        viewModel.onItemClick(TestGame.ball)

        viewModel.onWaitClick()

        val saved = checkNotNull(gameRepository.current.value)
        assertEquals(Dzynki(60), saved.wantsLeft)
        assertEquals(TestGame.planned.pet.calm + 10, saved.pet.calm)
    }

    @Test
    fun `Выбрать дешевле оставляет товары не дороже остатка, сброс возвращает все`() {
        val viewModel = viewModel()
        viewModel.onItemClick(TestGame.hat)

        viewModel.onCheaperClick()

        assertEquals(listOf(TestGame.ball), viewModel.currentState.items)
        assertNull(viewModel.currentState.sheet)

        viewModel.onClearFilterClick()

        assertEquals(listOf(TestGame.ball, TestGame.hat), viewModel.currentState.items)
    }

    @Test
    fun `Купить без открытой шторки покупки ничего не делает`() {
        val viewModel = viewModel()

        viewModel.onBuyClick()
        viewModel.onWaitClick()

        assertEquals(TestGame.planned, gameRepository.current.value)
        assertNull(viewModel.currentState.sheet)
    }
}

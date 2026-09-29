package ru.lct2026.finedu.feature.shop.ui

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.lct2026.finedu.productcore.domain.model.Bag
import ru.lct2026.finedu.productcore.domain.model.BagAmounts
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.GameEngine
import ru.lct2026.finedu.productcore.domain.model.GameResult
import ru.lct2026.finedu.productcore.domain.model.GameState
import ru.lct2026.finedu.productcore.domain.model.ShopItem
import ru.lct2026.finedu.productcore.domain.repository.ContentRepository
import ru.lct2026.finedu.productcore.domain.repository.GameRepository
import ru.lct2026.finedu.productcore.ui.viewmodel.StatefulViewModel

@HiltViewModel
internal class ShopViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val contentRepository: ContentRepository
) : StatefulViewModel<ShopUiState>(ShopUiState()) {

    init {
        observe()
    }

    private fun observe() {
        viewModelScope.launch {
            val catalog = contentRepository.content().shopItems
            gameRepository.state.filterNotNull().collect { game ->
                updateState {
                    copy(
                        catalog = catalog,
                        bags = BagAmounts(needs = game.needsLeft, wants = game.wantsLeft),
                        isPlanPending = game.unallocated > Dzynki.ZERO &&
                            game.needsLeft == Dzynki.ZERO &&
                            game.wantsLeft == Dzynki.ZERO,
                        look = game.profile.look,
                        stage = game.pet.stage
                    )
                }
            }
        }
    }

    fun onTabClick(bag: Bag) {
        updateState { copy(tab = bag, isCheaperOnly = false, sheet = null) }
    }

    /** Тап по товару: хватает — шторка покупки, не хватает — шторка «Не хватает N». Покупка — только по «Купить». */
    fun onItemClick(item: ShopItem) {
        withGame { game ->
            val remaining = game.amountIn(item.bag)
            val sheet = when (val result = GameEngine.buy(game, item)) {
                is GameResult.Success -> ShopSheet.Purchase(item, remaining)
                is GameResult.NotEnoughMoney -> ShopSheet.Shortage(item, result.missing, remaining)
            }
            updateState { copy(sheet = sheet) }
        }
    }

    fun onBuyClick() {
        val item = purchaseItem() ?: return
        withGame { game ->
            when (val result = GameEngine.buy(game, item)) {
                is GameResult.Success -> {
                    gameRepository.save(result.state)
                    updateState { copy(sheet = ShopSheet.Result(item, result.feedback)) }
                }

                is GameResult.NotEnoughMoney -> updateState {
                    copy(sheet = ShopSheet.Shortage(item, result.missing, game.amountIn(item.bag)))
                }
            }
        }
    }

    fun onWaitClick() {
        val item = purchaseItem() ?: return
        withGame { game ->
            val result = GameEngine.pause(game, item)
            gameRepository.save(result.state)
            updateState { copy(sheet = ShopSheet.Result(item, result.feedback)) }
        }
    }

    fun onCheaperClick() {
        updateState { copy(isCheaperOnly = true, sheet = null) }
    }

    fun onClearFilterClick() {
        updateState { copy(isCheaperOnly = false) }
    }

    fun onSheetDismiss() {
        updateState { copy(sheet = null) }
    }

    private fun purchaseItem(): ShopItem? = when (val sheet = currentState.sheet) {
        is ShopSheet.Purchase -> sheet.item
        is ShopSheet.Shortage, is ShopSheet.Result, null -> null
    }

    private fun withGame(block: suspend (GameState) -> Unit) {
        viewModelScope.launch {
            val game = gameRepository.state.first() ?: return@launch
            block(game)
        }
    }
}

package ru.lct2026.finedu.feature.shop.ui

import androidx.compose.runtime.Immutable
import ru.lct2026.finedu.productcore.domain.model.Bag
import ru.lct2026.finedu.productcore.domain.model.BagAmounts
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.Feedback
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.PetStage
import ru.lct2026.finedu.productcore.domain.model.ShopItem

@Immutable
internal data class ShopUiState(
    val tab: Bag = Bag.WANTS,
    val catalog: List<ShopItem> = emptyList(),
    /** Остатки «Нужного» и «Хочу». */
    val bags: BagAmounts = BagAmounts(),
    /** Дзыньки пришли, но ещё не разложены по мешочкам. */
    val isPlanPending: Boolean = false,
    /** «Выбрать дешевле»: только товары не дороже остатка. */
    val isCheaperOnly: Boolean = false,
    val sheet: ShopSheet? = null,
    val look: PetLook = PetLook(PetFur.LILAC, PetHat.NONE),
    val stage: PetStage = PetStage.BABY
) {
    val remaining: Dzynki get() = bags[tab]

    val items: List<ShopItem>
        get() = catalog.filter { it.bag == tab && (!isCheaperOnly || it.price <= remaining) }
}

@Immutable
internal sealed interface ShopSheet {

    /** Хватает: «Купить» / «Подожду». */
    data class Purchase(val item: ShopItem, val remaining: Dzynki) : ShopSheet {
        val after: Dzynki get() = remaining.minusOrNull(item.price) ?: Dzynki.ZERO
    }

    /** Не хватает [missing] дзынек: подкопить, задание или дешевле. */
    data class Shortage(val item: ShopItem, val missing: Dzynki, val remaining: Dzynki) : ShopSheet

    /** Обратная связь после «Купить» или «Подожду». */
    data class Result(val item: ShopItem, val feedback: Feedback) : ShopSheet
}

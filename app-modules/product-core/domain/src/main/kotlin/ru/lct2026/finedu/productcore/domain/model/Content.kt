package ru.lct2026.finedu.productcore.domain.model

/** Игровой контент из JSON (ТЗ 2.5.14): новое задание или товар — новая запись без правки кода. */
data class GameContent(
    val shopItems: List<ShopItem>,
    val goals: List<Goal>,
    val quests: List<Quest>,
    val weeks: List<WeekStory>,
    val glossary: List<GlossaryTerm>
)

data class ShopItem(
    val id: String,
    val title: String,
    val price: Dzynki,
    val bag: Bag,
    /** На сколько покупка поднимает показатель мешочка. */
    val statBoost: Int
) {
    init {
        require(bag != Bag.SAVINGS) { "Товар покупается из «Нужного» или «Хочу»: $id" }
    }
}

/** Цель копилки. */
data class Goal(val id: String, val title: String, val price: Dzynki)

enum class QuestTheme { BUDGET_PLANNING, PAYMENTS, SAVINGS }

/** Задание — игровая ситуация с выбором и последствиями (ТЗ 2.5.8). */
sealed interface Quest {
    val id: String
    val theme: QuestTheme

    /** Неделя сюжета, с которой задание доступно. */
    val week: Int
    val title: String
    val situation: String

    /** Награда за первое прохождение, при любом выборе. */
    val reward: Dzynki

    /** Выбрать один из вариантов. */
    data class Choice(
        override val id: String,
        override val theme: QuestTheme,
        override val week: Int,
        override val title: String,
        override val situation: String,
        override val reward: Dzynki,
        val options: List<QuestOption>
    ) : Quest

    /** Собрать корзину, уложиться в [budget] и не забыть нужное. */
    data class Basket(
        override val id: String,
        override val theme: QuestTheme,
        override val week: Int,
        override val title: String,
        override val situation: String,
        override val reward: Dzynki,
        val budget: Dzynki,
        val items: List<BasketItem>
    ) : Quest
}

/**
 * Вариант ответа. [cost] списывается из мешочка [bag]: из «Копилки» — снятием, из остальных — с кошелька.
 * [explanation] показывается после выбора: почему так вышло.
 */
data class QuestOption(
    val id: String,
    val text: String,
    val explanation: String,
    val cost: Dzynki = Dzynki.ZERO,
    val bag: Bag? = null,
    val statChanges: Map<PetStat, Int> = emptyMap()
)

data class BasketItem(val id: String, val title: String, val price: Dzynki, val isNeeded: Boolean)

data class BasketCheck(val total: Dzynki, val fitsBudget: Boolean, val hasAllNeeded: Boolean) {
    val isSuccess: Boolean get() = fitsBudget && hasAllNeeded
}

/** Сюжетный поворот недели. */
data class WeekStory(val number: Int, val title: String, val text: String)

data class GlossaryTerm(val term: String, val definition: String)

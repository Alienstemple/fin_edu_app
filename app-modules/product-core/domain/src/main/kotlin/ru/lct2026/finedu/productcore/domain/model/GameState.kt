package ru.lct2026.finedu.productcore.domain.model

/**
 * Всё состояние профиля. Хранится локально одним документом.
 *
 * Деньги лежат по мешочкам-конвертам: [unallocated] ещё не разложены, [needsLeft] и [wantsLeft] — остатки
 * «Нужного» и «Хочу», [savings] — копилка.
 */
data class GameState(
    val profile: Profile,
    val unallocated: Dzynki,
    val needsLeft: Dzynki,
    val wantsLeft: Dzynki,
    val savings: Dzynki,
    /** Цели, поставленные в уголок. Не отнимаются. */
    val placedGoalIds: Set<String>,
    /** Звёздочки на пледе за недели в рамках плана. Не отнимаются. */
    val stars: Int,
    val period: Period,
    val pet: Pet,
    /** Задания, пройденные хотя бы раз: награда за них уже выдана. */
    val completedQuestIds: Set<String>,
    val history: List<PeriodResult>,
    /** Все поступления с источником (ТЗ 2.5.4). */
    val ledger: List<LedgerEntry>,
    val settings: Settings,
    val lastVisitMillis: Long
) {
    /** «Баланс» на главном: что можно потратить на этой неделе. */
    val balance: Dzynki get() = needsLeft + wantsLeft

    /** Сколько лежит в мешочке. */
    fun amountIn(bag: Bag): Dzynki = when (bag) {
        Bag.NEEDS -> needsLeft
        Bag.WANTS -> wantsLeft
        Bag.SAVINGS -> savings
    }
}

data class Profile(val playerName: String, val petName: String, val look: PetLook, val isDemo: Boolean)

enum class AgeMode {
    /** 7–8 лет: крупные числа, шкалы без цифр. */
    YOUNGER,

    /** 9–11 лет: всё с цифрами. */
    OLDER
}

/** Настройки из раздела для взрослого. */
data class Settings(val ageMode: AgeMode = AgeMode.OLDER, val calmMode: Boolean = false, val largeFont: Boolean = false)

/** Текущая игровая неделя. */
data class Period(
    val number: Int,
    val income: Dzynki,
    /** Всё, что разложено за неделю; `null`, пока план не зафиксирован. */
    val plan: BagAmounts? = null,
    /** Факт: потрачено на «Нужное» и «Хочу», положено в копилку. */
    val actual: BagAmounts = BagAmounts(),
    val withdrawn: Dzynki = Dzynki.ZERO,
    val completedQuestIds: Set<String> = emptySet(),
    /** Участвует ли в необязательном челлендже недели. */
    val isChallengeJoined: Boolean = false
)

enum class IncomeSource {
    /** Регулярный доход в начале недели. */
    POCKET_MONEY,

    /** Награда за прохождение задания. */
    QUEST_REWARD,

    /** Бонус от взрослого. */
    PARENT_BONUS
}

data class LedgerEntry(
    val periodNumber: Int,
    val source: IncomeSource,
    val amount: Dzynki,
    /** Причина бонуса от взрослого или id задания. */
    val note: String? = null
)

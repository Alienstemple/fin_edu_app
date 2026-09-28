package ru.lct2026.finedu.productcore.domain.model

/** Всё состояние профиля. Хранится локально одним документом. */
data class GameState(
    val profile: Profile,
    /** Кошелёк: дзыньки, которые можно потратить или отложить. */
    val balance: Dzynki,
    /** Копилка. */
    val savings: Dzynki,
    val selectedGoalId: String?,
    val period: Period,
    val pet: Pet,
    /** Задания, пройденные хотя бы раз: награда за них уже выдана. */
    val completedQuestIds: Set<String>,
    val history: List<PeriodResult>,
    /** Все начисления с источником (ТЗ 2.5.4). */
    val ledger: List<LedgerEntry>
)

data class Profile(val playerName: String, val petName: String, val look: PetLook, val isDemo: Boolean)

/** Текущая игровая неделя. */
data class Period(
    val number: Int,
    val income: Dzynki,
    /** План по мешочкам; `null`, пока не подтверждён. */
    val plan: BagAmounts? = null,
    /** Факт: потрачено на «Нужное» и «Хочу», положено в копилку. */
    val actual: BagAmounts = BagAmounts(),
    val withdrawn: Dzynki = Dzynki.ZERO,
    val completedQuestIds: Set<String> = emptySet()
)

enum class IncomeSource {
    /** Карманные деньги в начале недели. */
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

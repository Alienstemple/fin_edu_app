package ru.lct2026.finedu.productcore.domain.model

/** Три мешочка бюджета (ТЗ 2.5.5). Каждый влияет на свой показатель питомца. */
enum class Bag(val stat: PetStat) {
    /** «Нужное» — обязательные расходы. */
    NEEDS(PetStat.CHARGE),

    /** «Хочу» — необязательные расходы. */
    WANTS(PetStat.VIBE),

    /** «Копилка» — накопления на цель. */
    SAVINGS(PetStat.CALM)
}

/** Суммы по трём мешочкам: план недели или факт. */
data class BagAmounts(
    val needs: Dzynki = Dzynki.ZERO,
    val wants: Dzynki = Dzynki.ZERO,
    val savings: Dzynki = Dzynki.ZERO
) {
    val total: Dzynki get() = needs + wants + savings

    operator fun get(bag: Bag): Dzynki = when (bag) {
        Bag.NEEDS -> needs
        Bag.WANTS -> wants
        Bag.SAVINGS -> savings
    }

    operator fun plus(other: BagAmounts): BagAmounts =
        BagAmounts(needs + other.needs, wants + other.wants, savings + other.savings)

    fun add(bag: Bag, amount: Dzynki): BagAmounts = when (bag) {
        Bag.NEEDS -> copy(needs = needs + amount)
        Bag.WANTS -> copy(wants = wants + amount)
        Bag.SAVINGS -> copy(savings = savings + amount)
    }
}

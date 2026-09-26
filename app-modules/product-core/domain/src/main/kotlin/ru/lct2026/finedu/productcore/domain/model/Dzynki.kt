package ru.lct2026.finedu.productcore.domain.model

/**
 * Игровая валюта — «дзыньки». Сумма не бывает отрицательной: уйти в минус нельзя (ТЗ 2.5.6).
 */
@JvmInline
value class Dzynki(val amount: Int) : Comparable<Dzynki> {

    init {
        require(amount >= 0) { "Сумма не может быть отрицательной: $amount" }
    }

    operator fun plus(other: Dzynki): Dzynki = Dzynki(amount + other.amount)

    /** Вычитает сумму или возвращает `null`, если дзынек не хватает. */
    fun minusOrNull(other: Dzynki): Dzynki? = if (other > this) null else Dzynki(amount - other.amount)

    override fun compareTo(other: Dzynki): Int = amount.compareTo(other.amount)

    companion object {
        val ZERO = Dzynki(0)
    }
}

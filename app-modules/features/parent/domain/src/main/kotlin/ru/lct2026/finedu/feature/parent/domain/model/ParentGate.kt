package ru.lct2026.finedu.feature.parent.domain.model

import kotlin.random.Random

/** Барьер «Только для взрослых»: пример на умножение однозначных чисел. */
data class ParentGate(val left: Int, val right: Int) {

    val answer: Int get() = left * right

    fun isCorrect(input: String): Boolean = input.toIntOrNull() == answer

    companion object {
        /** Множители от 3 до 9: такую таблицу умножения ребёнок 7–8 лет ещё не знает наизусть. */
        private const val MIN_FACTOR = 3
        private const val MAX_FACTOR = 9

        /** Самый длинный ответ — 81: больше двух цифр не набираем. */
        const val MAX_INPUT_LENGTH = 2

        fun random(random: Random): ParentGate = ParentGate(
            left = random.nextInt(MIN_FACTOR, MAX_FACTOR + 1),
            right = random.nextInt(MIN_FACTOR, MAX_FACTOR + 1)
        )
    }
}

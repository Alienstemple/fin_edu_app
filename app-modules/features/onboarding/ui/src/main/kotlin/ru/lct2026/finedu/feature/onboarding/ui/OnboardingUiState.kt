package ru.lct2026.finedu.feature.onboarding.ui

import androidx.compose.runtime.Immutable
import ru.lct2026.finedu.productcore.ui.event.Event

/**
 * Знакомство с Дзынем. [hasProfile] — онбординг открыт повторно из словарика: финальная кнопка возвращает назад.
 */
@Immutable
internal data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.Scene(),
    val hasProfile: Boolean = false
)

/** Шаги онбординга по макету `Welcome`: сцена → три сторис → первое решение → результат. */
@Immutable
internal sealed interface OnboardingStep {

    /** Коробка у батареи. [isHeld] — Дзыня взяли на руки, он выпрыгнул. */
    data class Scene(val isHeld: Boolean = false) : OnboardingStep

    /** Сторис [index] из [STORY_COUNT]. */
    data class Story(val index: Int) : OnboardingStep

    /** Первое решение: последние 20 дзынек. */
    data object Choice : OnboardingStep

    /** Результат учебного выбора. В игровое состояние ничего не пишется. */
    data class Result(val choice: FirstChoice) : OnboardingStep

    companion object {
        const val STORY_COUNT = 3
    }
}

/** Варианты первого решения. */
internal enum class FirstChoice {
    /** Каша для Дзыня — «Нужное», +10 к Заряду. */
    PORRIDGE,

    /** Печенье себе — «Хочу», +10 к Вайбу. */
    COOKIE
}

internal sealed interface OnboardingEvent : Event {

    /** Профиля ещё нет — дальше создание питомца. */
    data object OpenHero : OnboardingEvent
}

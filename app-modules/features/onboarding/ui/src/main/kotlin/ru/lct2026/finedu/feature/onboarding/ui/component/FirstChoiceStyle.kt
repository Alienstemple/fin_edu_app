package ru.lct2026.finedu.feature.onboarding.ui.component

import androidx.annotation.StringRes
import ru.lct2026.finedu.feature.onboarding.ui.FirstChoice
import ru.lct2026.finedu.feature.onboarding.ui.R
import ru.lct2026.finedu.productcore.domain.model.Bag
import ru.lct2026.finedu.productcore.domain.model.PetStat
import ru.lct2026.finedu.productcore.ui.illustration.PetMood

// Мешочек, показатель, реакция и тексты учебного выбора — вместе, чтобы варианты не разъехались.

internal val FirstChoice.bag: Bag
    get() = when (this) {
        FirstChoice.PORRIDGE -> Bag.NEEDS
        FirstChoice.COOKIE -> Bag.WANTS
    }

internal val FirstChoice.stat: PetStat
    get() = when (this) {
        FirstChoice.PORRIDGE -> PetStat.CHARGE
        FirstChoice.COOKIE -> PetStat.VIBE
    }

internal val FirstChoice.mood: PetMood
    get() = when (this) {
        FirstChoice.PORRIDGE -> PetMood.HAPPY
        FirstChoice.COOKIE -> PetMood.JOY
    }

@get:StringRes
internal val FirstChoice.titleRes: Int
    get() = when (this) {
        FirstChoice.PORRIDGE -> R.string.onboarding_choice_porridge
        FirstChoice.COOKIE -> R.string.onboarding_choice_cookie
    }

@get:StringRes
internal val FirstChoice.lineRes: Int
    get() = when (this) {
        FirstChoice.PORRIDGE -> R.string.onboarding_result_porridge_line
        FirstChoice.COOKIE -> R.string.onboarding_result_cookie_line
    }

@get:StringRes
internal val FirstChoice.tagRes: Int
    get() = when (this) {
        FirstChoice.PORRIDGE -> R.string.onboarding_result_porridge_tag
        FirstChoice.COOKIE -> R.string.onboarding_result_cookie_tag
    }

@get:StringRes
internal val FirstChoice.resultTitleRes: Int
    get() = when (this) {
        FirstChoice.PORRIDGE -> R.string.onboarding_result_porridge_title
        FirstChoice.COOKIE -> R.string.onboarding_result_cookie_title
    }

@get:StringRes
internal val FirstChoice.whyRes: Int
    get() = when (this) {
        FirstChoice.PORRIDGE -> R.string.onboarding_result_porridge_why
        FirstChoice.COOKIE -> R.string.onboarding_result_cookie_why
    }

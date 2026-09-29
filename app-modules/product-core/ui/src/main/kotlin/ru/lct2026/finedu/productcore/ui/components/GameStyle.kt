package ru.lct2026.finedu.productcore.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import ru.lct2026.finedu.productcore.domain.model.Bag
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.PetStat
import ru.lct2026.finedu.productcore.ui.R
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

// Цвет, иконка и подпись мешочков и показателей — всегда вместе: смысл не только в цвете (ТЗ 3.6).

val Bag.color: Color
    @Composable
    @ReadOnlyComposable
    get() = when (this) {
        Bag.NEEDS -> FinEduTheme.colors.needs
        Bag.WANTS -> FinEduTheme.colors.wants
        Bag.SAVINGS -> FinEduTheme.colors.savings
    }

@get:DrawableRes
val Bag.iconRes: Int
    get() = when (this) {
        Bag.NEEDS -> R.drawable.ic_bag_needs
        Bag.WANTS -> R.drawable.ic_bag_wants
        Bag.SAVINGS -> R.drawable.ic_bag_savings
    }

@get:StringRes
val Bag.labelRes: Int
    get() = when (this) {
        Bag.NEEDS -> R.string.bag_needs
        Bag.WANTS -> R.string.bag_wants
        Bag.SAVINGS -> R.string.bag_savings
    }

/** Показатель окрашен цветом своего мешочка: Заряд ← Нужное, Вайб ← Хочу, Спокойствие ← Копилка. */
val PetStat.color: Color
    @Composable
    @ReadOnlyComposable
    get() = when (this) {
        PetStat.CHARGE -> Bag.NEEDS.color
        PetStat.VIBE -> Bag.WANTS.color
        PetStat.CALM -> Bag.SAVINGS.color
    }

@get:DrawableRes
val PetStat.iconRes: Int
    get() = when (this) {
        PetStat.CHARGE -> R.drawable.ic_stat_charge
        PetStat.VIBE -> R.drawable.ic_stat_vibe
        PetStat.CALM -> R.drawable.ic_stat_calm
    }

@get:StringRes
val PetStat.labelRes: Int
    get() = when (this) {
        PetStat.CHARGE -> R.string.stat_charge
        PetStat.VIBE -> R.string.stat_vibe
        PetStat.CALM -> R.string.stat_calm
    }

/** «30 дзынек», «1 дзынька», «2 дзыньки». */
@Composable
fun dzynkiText(amount: Int): String = pluralStringResource(R.plurals.dzynki_amount, amount, amount)

@Composable
fun dzynkiText(amount: Dzynki): String = dzynkiText(amount.amount)

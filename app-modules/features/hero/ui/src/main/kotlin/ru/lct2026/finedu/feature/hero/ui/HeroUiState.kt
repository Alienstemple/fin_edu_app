package ru.lct2026.finedu.feature.hero.ui

import androidx.compose.runtime.Immutable
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.ui.event.Event

/**
 * Создание питомца. [petName] `null` — поле ещё не трогали, в нём имя по умолчанию («Дзынь» из ресурсов).
 * [isSaving] — профиль сохраняется, кнопки не реагируют на повторное нажатие.
 */
@Immutable
internal data class HeroUiState(
    val fur: PetFur = PetFur.LILAC,
    val hat: PetHat = PetHat.NONE,
    val petName: String? = null,
    val playerName: String = "",
    val isSaving: Boolean = false
) {
    val look: PetLook get() = PetLook(fur, hat)

    companion object {
        const val PET_NAME_MAX_LENGTH = 16
        const val PLAYER_NAME_MAX_LENGTH = 20
    }
}

internal sealed interface HeroEvent : Event {

    /** Профиль создан — на главный, без возврата в онбординг. */
    data object OpenHome : HeroEvent
}

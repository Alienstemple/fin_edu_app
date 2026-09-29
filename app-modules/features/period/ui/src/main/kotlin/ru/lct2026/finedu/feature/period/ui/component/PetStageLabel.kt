package ru.lct2026.finedu.feature.period.ui.component

import androidx.annotation.StringRes
import ru.lct2026.finedu.feature.period.ui.R
import ru.lct2026.finedu.productcore.domain.model.PetStage

@get:StringRes
internal val PetStage.labelRes: Int
    get() = when (this) {
        PetStage.BABY -> R.string.period_stage_baby
        PetStage.SPRY -> R.string.period_stage_spry
        PetStage.MASTER -> R.string.period_stage_master
    }

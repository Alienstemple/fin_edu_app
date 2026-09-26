package ru.lct2026.finedu.feature.home.domain.model

import ru.lct2026.finedu.productcore.domain.model.Dzynki

/** Сводка для главного экрана: сколько дзынек на руках и в копилке, какая идёт неделя. */
data class HomeSummary(val balance: Dzynki, val savings: Dzynki, val periodNumber: Int)

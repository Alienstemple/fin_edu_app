package ru.lct2026.finedu.feature.learn.ui

import androidx.compose.runtime.Immutable
import ru.lct2026.finedu.productcore.domain.model.GlossaryTerm

@Immutable
internal sealed interface GlossaryUiState {

    data object Loading : GlossaryUiState

    data class Content(val terms: List<GlossaryTerm>) : GlossaryUiState
}

package ru.lct2026.finedu.feature.home.ui

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface HomeUiState {

    data object Loading : HomeUiState

    data class Content(val balance: Int, val savings: Int, val periodNumber: Int) : HomeUiState
}

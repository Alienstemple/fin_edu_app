package ru.lct2026.finedu.feature.home.ui

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import ru.lct2026.finedu.feature.home.domain.model.HomeSummary
import ru.lct2026.finedu.feature.home.domain.repository.HomeRepository
import ru.lct2026.finedu.productcore.ui.viewmodel.StatefulViewModel

@HiltViewModel
internal class HomeViewModel @Inject constructor(private val homeRepository: HomeRepository) :
    StatefulViewModel<HomeUiState>(HomeUiState.Loading) {

    init {
        observeSummary()
    }

    private fun observeSummary() {
        viewModelScope.launch {
            homeRepository.observeSummary().collect { summary ->
                setState(summary.toUiState())
            }
        }
    }

    private fun HomeSummary.toUiState() = HomeUiState.Content(
        balance = balance.amount,
        savings = savings.amount,
        periodNumber = periodNumber
    )
}

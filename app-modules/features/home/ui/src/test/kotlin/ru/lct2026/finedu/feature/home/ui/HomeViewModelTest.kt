package ru.lct2026.finedu.feature.home.ui

import app.cash.turbine.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import ru.lct2026.finedu.feature.home.domain.model.HomeSummary
import ru.lct2026.finedu.feature.home.domain.repository.HomeRepository
import ru.lct2026.finedu.productcore.domain.model.Dzynki

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val summaries = MutableSharedFlow<HomeSummary>()
    private val repository = object : HomeRepository {
        override fun observeSummary(): Flow<HomeSummary> = summaries
    }

    @Test
    fun `пока сводки нет, показывается загрузка, затем данные сводки`() = runTest {
        val viewModel = HomeViewModel(repository)

        viewModel.stateFlow.test {
            assertEquals(HomeUiState.Loading, awaitItem())

            summaries.emit(HomeSummary(balance = Dzynki(40), savings = Dzynki(15), periodNumber = 2))

            assertEquals(HomeUiState.Content(balance = 40, savings = 15, periodNumber = 2), awaitItem())
        }
    }
}

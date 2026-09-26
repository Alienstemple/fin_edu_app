package ru.lct2026.finedu.feature.home.data.repository

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import ru.lct2026.finedu.feature.home.domain.model.HomeSummary
import ru.lct2026.finedu.feature.home.domain.repository.HomeRepository
import ru.lct2026.finedu.productcore.domain.model.Dzynki

// TODO: читать баланс, копилку и номер периода из хранилища профиля (Room), когда оно появится.
internal class HomeRepositoryImpl @Inject constructor() : HomeRepository {

    override fun observeSummary(): Flow<HomeSummary> = flowOf(
        HomeSummary(balance = Dzynki(STARTING_BALANCE), savings = Dzynki.ZERO, periodNumber = 1)
    )

    private companion object {
        const val STARTING_BALANCE = 100
    }
}

package ru.lct2026.finedu.feature.home.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.lct2026.finedu.feature.home.domain.model.HomeSummary

interface HomeRepository {
    fun observeSummary(): Flow<HomeSummary>
}

package ru.lct2026.finedu.feature.home.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.lct2026.finedu.feature.home.data.repository.HomeRepositoryImpl
import ru.lct2026.finedu.feature.home.domain.repository.HomeRepository

@Module
@InstallIn(SingletonComponent::class)
internal interface HomeDataModule {

    @Binds
    fun bindHomeRepository(impl: HomeRepositoryImpl): HomeRepository
}

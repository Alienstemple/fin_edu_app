package ru.lct2026.finedu.feature.learn.ui.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import ru.lct2026.finedu.feature.learn.ui.LearnNavigationContribution
import ru.lct2026.finedu.productcore.navigation.api.FeatureNavigationContribution

@Module
@InstallIn(SingletonComponent::class)
internal interface LearnNavigationModule {

    @Binds
    @IntoSet
    fun bindNavigationContribution(impl: LearnNavigationContribution): FeatureNavigationContribution
}

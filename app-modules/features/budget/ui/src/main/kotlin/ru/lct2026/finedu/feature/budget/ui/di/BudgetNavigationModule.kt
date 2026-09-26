package ru.lct2026.finedu.feature.budget.ui.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import ru.lct2026.finedu.feature.budget.ui.BudgetNavigationContribution
import ru.lct2026.finedu.productcore.navigation.api.FeatureNavigationContribution

@Module
@InstallIn(SingletonComponent::class)
internal interface BudgetNavigationModule {

    @Binds
    @IntoSet
    fun bindNavigationContribution(impl: BudgetNavigationContribution): FeatureNavigationContribution
}

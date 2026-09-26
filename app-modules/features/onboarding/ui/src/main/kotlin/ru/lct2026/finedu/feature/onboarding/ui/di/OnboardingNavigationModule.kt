package ru.lct2026.finedu.feature.onboarding.ui.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import ru.lct2026.finedu.feature.onboarding.ui.OnboardingNavigationContribution
import ru.lct2026.finedu.productcore.navigation.api.FeatureNavigationContribution

@Module
@InstallIn(SingletonComponent::class)
internal interface OnboardingNavigationModule {

    @Binds
    @IntoSet
    fun bindNavigationContribution(impl: OnboardingNavigationContribution): FeatureNavigationContribution
}

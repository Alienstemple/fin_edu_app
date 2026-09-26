plugins {
    alias(libs.plugins.convention.android.feature.ui)
}

android {
    namespace = "ru.lct2026.finedu.feature.budget.ui"
}

dependencies {
    implementation(projects.appModules.features.budget.domain)
}

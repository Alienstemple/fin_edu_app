plugins {
    alias(libs.plugins.convention.android.feature.data)
}

android {
    namespace = "ru.lct2026.finedu.feature.budget.data"
}

dependencies {
    implementation(projects.appModules.features.budget.domain)
}

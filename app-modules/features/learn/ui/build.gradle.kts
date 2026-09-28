plugins {
    alias(libs.plugins.convention.android.feature.ui)
}

android {
    namespace = "ru.lct2026.finedu.feature.learn.ui"
}

dependencies {
    implementation(projects.appModules.features.learn.domain)
}

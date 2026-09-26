plugins {
    alias(libs.plugins.convention.android.feature.ui)
}

android {
    namespace = "ru.lct2026.finedu.feature.parent.ui"
}

dependencies {
    implementation(projects.appModules.features.parent.domain)
}

plugins {
    alias(libs.plugins.convention.android.feature.ui)
}

android {
    namespace = "ru.lct2026.finedu.feature.quests.ui"
}

dependencies {
    implementation(projects.appModules.features.quests.domain)
}

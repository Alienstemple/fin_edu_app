plugins {
    alias(libs.plugins.convention.android.feature.api)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "ru.lct2026.finedu.productcore.navigation.api"
}

dependencies {
    api(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
}

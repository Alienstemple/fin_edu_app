plugins {
    alias(libs.plugins.convention.android.feature.data)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "ru.lct2026.finedu.productcore.data"
}

dependencies {
    implementation(libs.androidx.datastore)
    implementation(libs.kotlinx.serialization.json)
}

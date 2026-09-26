plugins {
    alias(libs.plugins.convention.kotlin.feature.domain)
}

dependencies {
    // Flow в публичных сигнатурах репозиториев
    api(libs.kotlinx.coroutines.core)
}

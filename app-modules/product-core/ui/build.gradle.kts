plugins {
    alias(libs.plugins.convention.android.library.compose)
}

android {
    namespace = "ru.lct2026.finedu.productcore.ui"
}

dependencies {
    // Общие компоненты рисуют доменные сущности: мешочки, показатели, питомца, обратную связь.
    api(projects.appModules.productCore.domain)
    implementation(projects.appModules.productCore.navigation.api)
    // ViewModel — суперкласс StatelessViewModel, торчит в публичном API модуля
    api(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
}

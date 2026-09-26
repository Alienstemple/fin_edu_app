plugins {
    alias(libs.plugins.convention.android.library.compose)
}

android {
    namespace = "ru.lct2026.finedu.productcore.ui"
}

dependencies {
    // ViewModel — суперкласс StatelessViewModel, торчит в публичном API модуля
    api(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
}

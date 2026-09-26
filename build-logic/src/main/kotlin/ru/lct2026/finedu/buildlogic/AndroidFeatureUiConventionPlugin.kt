package ru.lct2026.finedu.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Модуль `ui` фичи: Compose-экраны, ViewModel, регистрация в навигации.
 * Видит `domain` своей фичи (подключается явно в build.gradle.kts), но никогда — `data`.
 */
class AndroidFeatureUiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply(libs.pluginId("android-library"))
        pluginManager.apply("convention.hilt")

        extensions.configure<LibraryExtension> {
            configureKotlinAndroid(this)
            configureCompose(this)
        }

        dependencies.apply {
            add("implementation", project(":app-modules:product-core:ui"))
            add("implementation", project(":app-modules:product-core:navigation:api"))
            add("implementation", project(":app-modules:product-core:domain"))
            add("implementation", libs.lib("androidx-lifecycle-runtime-compose"))
            add("implementation", libs.lib("androidx-lifecycle-viewmodel-compose"))
            add("implementation", libs.lib("androidx-hilt-lifecycle-viewmodel-compose"))
            add("testImplementation", libs.lib("test-robolectric"))
        }
        addUnitTestDependencies()
    }
}
